#APP_OPTIM := debug
# "armeabi" is opted out because of a complination error
# of clang in NDK with that set of backend
# besides, when compiling with Gradle packed NDK make, 
# this file is ignored, and the related configurations are
# placed in module level build.gradle file
APP_ABI := x86 armeabi-v7a arm64-v8a x86_64
APP_STL := gnustl_static