# CMakeLists.txt
#
# File containing directives for building JCoz on multiple
# platforms. Currently supports Linux, Windows, and OSX


# Minimum Cmake version
cmake_minimum_required (VERSION 2.8)

# Project title
project(JCoz)


# Set flags
SET(GLOBAL_WARNING_FLAGS "-Wformat -Wformat-security -Wno-char-subscripts  -Wno-sign-compare -Wno-strict-overflow -Wwrite-strings -Wnon-virtual-dtor -Woverloaded-virtual")
SET(GLOBAL_COPTS "-fdiagnostics-show-option -fno-exceptions -fno-omit-frame-pointer -fno-strict-aliasing -funsigned-char -fno-asynchronous-unwind-tables -m$(BITS) -msse2 -g -D__STDC_FORMAT_MACROS")
SET(JCOZ_OPT "-O3" CACHE string "Optimization level of JCoz agent")

# JDK root dir
SET(JCOZ_JAVA_HOME "" CACHE path "Path to Java JDK root directory")

IF(NOT EXISTS ${JCOZ_JAVA_HOME})
    message(FATAL_ERROR "You must specify a path to a JDK directory for JCOZ_JAVA_HOME")
ENDIF(NOT EXISTS ${JCOZ_JAVA_HOME})

SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${GLOBAL_WARNING_FLAGS} ${GLOBAL_OPTS} ${JCOZ_OPT}")

# Library output name
SET(JCOZ_LIB_NAME "jcoz" CACHE string "Name of jcoz shared library")

IF(APPLE)

    SET(CLANG_FLAGS "-std=c++11 -stdlib=libc++ -lpthread -ldl")
    SET(WARNING_FLAGS "-Wno-c++98-compat-pedantic -Wno-padded -Wno-missing-prototypes")
    SET(CLANG_LDFLAGS "-Wl,-fatal_warnings -Wl,-std=c++11 -Wl,-stdlib=libc++")
    SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -I${JCOZ_JAVA_HOME}/Headers/ -I${JCOZ_JAVA_HOME}/Headers/darwin ${CLANG_FLAGS} ${WARNING_FLAGS}")

ELSEIF(UNIX OR LINUX)
    SET(GCC_FLAGS "-std=c++11 -lpthread -fPIC -ldl")
    SET(WARNING_FLAGS "-Wframe-larger-than=16384 -Wno-unused-but-set-variable -Wunused-but-set-parameter -Wvla -Wno-conversion-null -Wno-builtin-macro-redefined")
    SET(GCC_LDFLAGS "-Wl,--fatal_warnings")
    SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -I${JCOZ_JAVA_HOME}/include -I${JCOZ_JAVA_HOME}/include/linux ${GCC_FLAGS} ${WARNING_FLAGS}")
ENDIF(APPLE)

SET(JCOZ_CORE_SRC
    ${PROJECT_SOURCE_DIR}/display.h
    ${PROJECT_SOURCE_DIR}/globals.h
    ${PROJECT_SOURCE_DIR}/profiler.h
    ${PROJECT_SOURCE_DIR}/stacktraces.h
    ${PROJECT_SOURCE_DIR}/entry.cc
    ${PROJECT_SOURCE_DIR}/display.cc
    ${PROJECT_SOURCE_DIR}/profiler.cc
)

add_library(${JCOZ_LIB_NAME} SHARED ${JCOZ_CORE_SRC})

