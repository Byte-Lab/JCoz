# CMakeLists.txt
#
# File containing directives for building JCoz on multiple
# platforms. Currently supports Linux, Windows, and OSX


# Minimum Cmake version
cmake_minimum_required (VERSION 2.8)

# Project title
project(JCoz)

set(CMAKE_VERBOSE_MAKEFILE ON)

# Set flags
IF(APPLE OR UNIX OR LINUX)
	SET(CMD_DELIM "-")
ELSE() # Windows uses / for command line options instead of -
	SET(CMD_DELIM "/")
ENDIF(APPLE OR UNIX OR LINUX)

SET(GLOBAL_WARNING_FLAGS "${CMD_DELIM}Wall")
SET(GLOBAL_COPTS "${CMD_DELIM}fdiagnostics-show-option ${CMD_DELIM}fno-exceptions ${CMD_DELIM}fno-omit-frame-pointer ${CMD_DELIM}fno-strict-aliasing ${CMD_DELIM}funsigned-char ${CMD_DELIM}fno-asynchronous-unwind-tables ${CMD_DELIM}m$(BITS) ${CMD_DELIM}msse2 ${CMD_DELIM}g ${CMD_DELIM}D__STDC_FORMAT_MACROS")
SET(JCOZ_OPT "3" CACHE string "Optimization level of JCoz agent (1, 2, or 3)")

# JDK root dir
SET(JCOZ_JAVA_HOME "" CACHE PATH "Path to Java JDK root directory")

IF(NOT EXISTS ${JCOZ_JAVA_HOME})
    message(FATAL_ERROR "You must specify a path to a JDK directory for JCOZ_JAVA_HOME")
ENDIF(NOT EXISTS ${JCOZ_JAVA_HOME})

SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${GLOBAL_WARNING_FLAGS} ${GLOBAL_OPTS}")

# Library output name
SET(JCOZ_LIB_NAME "jcoz" CACHE string "Name of jcoz shared library")

IF(APPLE)
    SET(CLANG_FLAGS "-std=c++11 -stdlib=libc++ -lpthread -ldl")
    SET(CLANG_LDFLAGS "-Wl,-fatal_warnings -Wl,-std=c++11 -Wl,-stdlib=libc++")
	SET(ACTIVE_LIBRARIES "${ACTIVE_LIBRARIES} ${JCOZ_JAVA_HOME}/lib/*.dylib")
	include_directories("${JCOZ_JAVA_HOME}/Headers" "${JCOZ_JAVA_HOME}/Headers/darwin")
    SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O${JCOZ_OPT} ${CLANG_FLAGS} ${WARNING_FLAGS}")
ELSEIF(UNIX OR LINUX)
    SET(GCC_FLAGS "-std=c++11 -lpthread -fPIC -ldl")
    SET(GCC_LDFLAGS "-Wl,--fatal_warnings")
	include_directories("${JCOZ_JAVA_HOME}/include" "${JCOZ_JAVA_HOME}/include/linux")
	SET(ACTIVE_LIBRARIES "${ACTIVE_LIBRARIES} ${JCOZ_JAVA_HOME}/lib/*.so")
    SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O${JCOZ_OPT} ${GCC_FLAGS} ${WARNING_FLAGS}")
ELSE() # Otherwise we're in windows land
    SET(GCC_LDFLAGS "/Wl,--fatal_warnings")
	include_directories("${JCOZ_JAVA_HOME}/include" "${JCOZ_JAVA_HOME}/include/win32" "${JCOZ_JAVA_HOME}/include/win32/bridge")
	SET(ACTIVE_LIBRARIES "${ACTIVE_LIBRARIES} ${JCOZ_JAVA_HOME}/lib/*.lib")
	IF(${JCOZ_OPT} GREATER 2)
		SET(JCOZ_OPT 2)
	ENDIF(${JCOZ_OPT} GREATER 2)
    SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} /O${JCOZ_OPT} ${GCC_FLAGS} ${WARNING_FLAGS}")
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

# CMake requires no leading or trailing spaces around linking libraries
STRING(STRIP ${ACTIVE_LIBRARIES} ACTIVE_LIBRARIES)

target_link_libraries(${JCOZ_LIB_NAME} ${ACTIVE_LIBRARIES})
# target_include_directories(${JCOZ_LIB_NAME} PUBLIC "${JCOZ_JAVA_HOME}/include" PUBLIC "${JCOZ_JAVA_HOME}/include/win32" PUBLIC "${JCOZ_JAVA_HOME}/include/win32/bridge")
