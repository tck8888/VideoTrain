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

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_tck_av_video_rtmp_ScreenLiveController_sendData(JNIEnv *env, jobject thiz, jbyteArray data,
                                                         jint len, jlong tms) {
    // TODO: implement sendData()
}