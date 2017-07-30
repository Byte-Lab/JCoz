#!python

##
# SConstruct - The main SCons build file for JCoz. To build JCoz, call `scons` with any of the following arguments:
#
# agent  - Build the native JCoz agent that performs the causal profiling on a running JVM process.
# client - Build the Java client that is used to communicate over RMI with the MBean service.
# mbean  - Build the MBean service that communicates with clients, and coordinates profiling of local JVM processes.
#
# Just calling `scons` with no arguments will cause scons to build all 3 targets mentioned above.
#
# The build process is pretty fast, but if it ever starts to get slow just use the -j <# threads> option with SCons
# to spawn multiple threads and build in parallel.
#
import os 
import sys
import struct

jcoz_home = os.getcwd()
src_dir = jcoz_home + '/src'
native_dir = src_dir + '/native'
debug = ARGUMENTS.get('debug', 0)
uname = os.uname()[0].lower()
bitness = struct.calcsize("P") * 8


# Should we build all of the targets? We should if the user
# doesn't specify any command line targets. Otherwise we'll
# only build the ones specified by the user.
build_all = not COMMAND_LINE_TARGETS


###################################################
# Set up global compiler flags                    #
###################################################

## CFLAGS
# Set up environment flags
global_copts = [ '-fdiagnostics-show-option', '-fno-exceptions',
	         '-fno-omit-frame-pointer' , '-fno-strict-aliasing',
                 '-funsigned-char', '-fno-asynchronous-unwind-tables',
                 '-fexceptions -m%d' % bitness, '-msse2', '-g',
                 '-D__STDC_FORMAT_MACROS' ]
# What warnings should be disabled when compiling in all environments.
global_warnings = [ '-Wformat-security', '-Wno-char-subscripts',
                    '-Wno-sign-compare', '-Wno-strict-overflow',
                    '-Wwrite-strings', '-Wnon-virtual-dtor',
                    '-Woverloaded-virtual' ]
# Optimization level
if int(debug):
    opt = [ '-O0', '-g' ]
else:
    opt = [ '-O3' ]
global_cflags = global_copts + global_warnings + opt


## Includes

# Where Java is installed
java_home = ARGUMENTS.get('java_home', "")
if java_home == "":
    print "java_home not found as argument. Checking for JAVA_HOME environment variable."
    java_home = os.environ.get('JAVA_HOME')
    print java_home
    if java_home is None or java_home == "":
        print "JAVA_HOME environment variable not found. You must specify where Java is installed for JVMTI and JavaFX"
        sys.exit(-1)
    else:
        print "Found JAVA_HOME: " + java_home + " environment variable."

# Pointer to directories that might have header files we need.
java_includes = [ java_home + '/include', java_home + '/include/' + uname ]
global_includes = [ native_dir, native_dir + '/include' ] + java_includes


## External libraries
# Note that we take a dependency on pthread which currently precludes us from using
# windows. Until we figure out how to send signals in windows, we can't support it
# (at least not for building the native library).
libs = [ '-ldl', '-lpthread' ]

###################################################
# Store environments for building cross platform. #
###################################################

# Linux
linux_copts = [ '-mfpmath=sse', '-std=gnu++0x' ]
linux_warnings = [ '-Wframe-larger-than=16384', '-Wno-unused-but-set-variable',
                   '-Wunused-but-set-parameter', '-Wvla -Wno-conversion-null',
                   '-Wno-builtin-macro-redefined' ]
linux_cflags = linux_copts + linux_warnings + global_cflags
linux = Environment(CC = 'g++',
                    CCFLAGS = linux_cflags,
                    CPPPATH = global_includes,
                    LIBS = libs)


jcoz_agent = "liblagent"

builds_dir = jcoz_home + '/builds'

if not os.path.isdir(builds_dir):
    os.makedirs(builds_dir)

VariantDir(builds_dir, src_dir)

SharedLibrary(jcoz_agent, native_dir)
