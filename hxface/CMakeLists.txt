
cmake_minimum_required(VERSION 3.4.1)
project(FaceTracking-lib)

#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")
#set(CMAKE_C_FLAGS "${CMAKE_CXX_FLAGS} -fopenmp")
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fopenmp")

include_directories(src/main/cpp/include
                    src/main/cpp/)

file(GLOB MTCNN_SRC src/main/cpp/*.h
                    src/main/cpp/*.cpp)
#set(distribution_DIR ${CMAKE_SOURCE_DIR}/../../../../src/main/jniLibs)
#
##### libyuv  start###
#add_library( # Sets the name of the library.
#        yuv
#
#        # Sets the library as ashared library.
#        SHARED
#
#        # Provides a relative pathto your source file(s).
#        IMPORTED )
#
#set_target_properties(
#        yuv
#        PROPERTIES IMPORTED_LOCATION
#        ../../../../src/main/jniLibs/${ANDROID_ABI}/libyuv.so
#)
#
#
#
find_library( # Sets the name of the path variable.
              log-lib
              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

add_library(libncnn STATIC IMPORTED )

set_target_properties(libncnn PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libncnn.a)


add_library(Tracking-lib SHARED ${MTCNN_SRC})

add_library(lib_opencv STATIC IMPORTED ) #表示创建一个导入库，静态方式 引入libopencv_java3.so文件
set_target_properties(lib_opencv
                       PROPERTIES
                       IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libopencv_java3.so
                       )
#set(OpenCV_DIR "/Users/develop/OpenCV-android-sdk/sdk/native/jni")
#find_package(OpenCV REQUIRED)

target_link_libraries(Tracking-lib libncnn lib_opencv ${log-lib} z jnigraphics)


