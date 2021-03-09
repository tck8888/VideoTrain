
cmake_minimum_required(VERSION 3.4.1)

add_subdirectory(librtmp)

add_library(
        myrtmp
        SHARED
        myrtmp-lib.cpp)

find_library(
        log-lib
        log)

target_link_libraries(
        myrtmp
        ${log-lib}
        rtmp)