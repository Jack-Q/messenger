#!/usr/bin/env bash

# This is used to generate JNI native headers to be used in 
# latter NDK complination.

# Before generate JNI header, the ".class" should be generated.
# However, if the source is compiled by deprecated "JACK" toolchain,
# no *.class file will be generated.
# A temporary approach is disable jack toolchian or use platform JDK
# to generate *.class file. 

# Generates the necessary JNI to use native audio libraries using JavaCPP.
# Call this from the project root.

# For example, this can be achieved by following command 
# ./gradlew assembleDebug

java -jar tools/javacpp-1.3.2.jar -cp build/intermediates/classes/debug/ -d src/main/jni/ -nocompile cn.jackq.messenger.audio.OpusCodec

# NDK building is integrated into build process