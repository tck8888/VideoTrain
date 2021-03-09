#include <jni.h>
#include <string>
#include <android/log.h>

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,"tck6666",__VA_ARGS__)
extern "C" {
#include  "librtmp/rtmp.h"
}


typedef struct {
    RTMP *rtmp;
    int16_t sps_len;
    int8_t *sps;
    int16_t pps_len;
    int8_t *pps;
} Live;
Live *live = NULL;

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_tck_av_video_rtmp_ScreenLiveController_connect(JNIEnv *env, jobject thiz, jstring url_) {

    const char *url = env->GetStringUTFChars(url_, 0);

    int ret = 0;

    do {
        live = (Live *) malloc(sizeof(Live));
        memset(live, 0, sizeof(Live));
        live->rtmp = RTMP_Alloc();
        RTMP_Init(live->rtmp);
        live->rtmp->Link.timeout = 20;

        LOGI("connect %s", url);
        if (!(ret = RTMP_SetupURL(live->rtmp, (char *) url))) break;


        RTMP_EnableWrite(live->rtmp);

        LOGI("RTMP_Connect");
        if (!(ret = RTMP_Connect(live->rtmp, nullptr))) break;

        LOGI("RTMP_ConnectStream ");
        if (!(ret = RTMP_ConnectStream(live->rtmp, 0))) break;

        LOGI("connect success");
    } while (0);

    if (!ret && live) {
        free(live);
        live = nullptr;
    }

    env->ReleaseStringUTFChars(url_, url);
    return ret;
}

int sendPacket(RTMPPacket *packet) {
    int ret = RTMP_SendPacket(live->rtmp, packet, 1);
    RTMPPacket_Free(packet);
    free(packet);
    return ret;
}

void prepareVideo(int8_t *data, int len, Live *live) {
    for (int i = 0; i < len; i++) {
        if (i + 4 < len) {
            if (data[i] == 0x00 && data[i + 1] == 0x00 && data[i + 2] == 0x00 &&
                data[i + 3] == 0x01) {
                if (data[i + 4] == 0x68) {
                    live->sps_len = i - 4;
                    live->sps = static_cast<int8_t *>(malloc(live->sps_len));
                    //  sps解析出来了
                    memcpy(live->sps, data + 4, live->sps_len);
                    //解析pps
                    live->pps_len = len - (4 + live->sps_len) - 4;
                    live->pps = static_cast<int8_t *>(malloc(live->pps_len));
                    memcpy(live->pps, data + 4 + live->sps_len + 4, live->pps_len);
                    LOGI("sps:%d pps:%d", live->sps_len, live->pps_len);
                    break;
                }
            }
        }
    }
}
RTMPPacket *createVideoPackage(Live *live) {
    int body_size = 16 + live->sps_len + live->pps_len;
    RTMPPacket *packet = (RTMPPacket *) malloc(sizeof(RTMPPacket));
    RTMPPacket_Alloc(packet, body_size);
    int i = 0;
    packet->m_body[i++] = 0x17;
    //AVC sequence header 设置为0x00
    packet->m_body[i++] = 0x00;
    //CompositionTime
    packet->m_body[i++] = 0x00;
    packet->m_body[i++] = 0x00;
    packet->m_body[i++] = 0x00;
    //AVC sequence header
    packet->m_body[i++] = 0x01;
//    原始 操作

    packet->m_body[i++] = live->sps[1]; //profile 如baseline、main、 high
    packet->m_body[i++] = live->sps[2]; //profile_compatibility 兼容性
    packet->m_body[i++] = live->sps[3]; //profile level
    packet->m_body[i++] = 0xFF;//已经给你规定好了
    packet->m_body[i++] = 0xE1; //reserved（111） + lengthSizeMinusOne（5位 sps 个数） 总是0xe1
    //高八位
    packet->m_body[i++] = (live->sps_len >> 8) & 0xFF;
//    低八位
    packet->m_body[i++] = live->sps_len & 0xff;
//    拷贝sps的内容
    memcpy(&packet->m_body[i], live->sps, live->sps_len);
    i +=live->sps_len;
    //    pps
    packet->m_body[i++] = 0x01; //pps number
    //rtmp 协议
    //pps length
    packet->m_body[i++] = (live->pps_len >> 8) & 0xff;
    packet->m_body[i++] = live->pps_len & 0xff;
    //    拷贝pps内容
    memcpy(&packet->m_body[i], live->pps, live->pps_len);
    //packaet
//视频类型
    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
//
    packet->m_nBodySize = body_size;
    //    视频 04
    packet->m_nChannel = 0x04;
    packet->m_nTimeStamp = 0;
    packet->m_hasAbsTimestamp = 0;
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    packet->m_nInfoField2 = live->rtmp->m_stream_id;
    return packet;
}

RTMPPacket *createVideoPackage(int8_t *buf, int len, const long tms, Live *live) {
    buf += 4;
//长度
    RTMPPacket *packet = (RTMPPacket *) malloc(sizeof(RTMPPacket));
    int body_size = len + 9;
//初始化RTMP内部的body数组
    RTMPPacket_Alloc(packet, body_size);



    if (buf[0] == 0x65) {
        packet->m_body[0] = 0x17;
        LOGI("发送关键帧 data");
    } else{
        packet->m_body[0] = 0x27;
        LOGI("发送非关键帧 data");
    }
//    固定的大小
    packet->m_body[1] = 0x01;
    packet->m_body[2] = 0x00;
    packet->m_body[3] = 0x00;
    packet->m_body[4] = 0x00;

    //长度
    packet->m_body[5] = (len >> 24) & 0xff;
    packet->m_body[6] = (len >> 16) & 0xff;
    packet->m_body[7] = (len >> 8) & 0xff;
    packet->m_body[8] = (len) & 0xff;

    //数据
    memcpy(&packet->m_body[9], buf, len);
    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nBodySize = body_size;
    packet->m_nChannel = 0x04;
    packet->m_nTimeStamp = tms;
    packet->m_hasAbsTimestamp = 0;
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    packet->m_nInfoField2 = live->rtmp->m_stream_id;
    return packet;
}


int sendVideo(int8_t *buf, int len, long tms) {
    int ret = 0;
    if (buf[4] == 0x67) {
        //        缓存sps 和pps 到全局遍历 不需要推流
        if (live && (!live->pps || !live->sps)) {
            //缓存 没有推流
            prepareVideo(buf, len, live);
        }
        return ret;
    }
    //    I帧
    if (buf[4] == 0x65) {
        //         推两个
//sps 和 ppps 的paclet  发送sps pps
        RTMPPacket *packet = createVideoPackage(live);
        sendPacket(packet);
    }
    RTMPPacket *packet2 = createVideoPackage(buf, len, tms, live);
    ret = sendPacket(packet2);
    return ret;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_tck_av_video_rtmp_ScreenLiveController_sendData(JNIEnv *env, jobject thiz,
                                                         jbyteArray data_,
                                                         jint len, jlong tms) {
    int ret = 0;
    jbyte *data = env->GetByteArrayElements(data_, 0);
    ret = sendVideo(data, len, tms);

    env->ReleaseByteArrayElements(data_, data, 0);
    return ret;
}