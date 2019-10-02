# This file is part of JCoz.
#
# JCoz is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# JCoz is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with JCoz.  If not, see <https://www.gnu.org/licenses/>.

CC=g++
BITS?=64

SHELL:=/bin/bash
UNAME:=$(shell uname | tr '[A-Z]' '[a-z]')
PWD:=$(shell pwd)

TARGET=liblagent.so

PLATFORM_COPTS:=-mfpmath=sse \
	-std=gnu++0x
PLATFORM_WARNINGS:=-Wframe-larger-than=16384 \
	-Wno-unused-but-set-variable \
	-Wunused-but-set-parameter \
	-Wvla \
	-Wno-conversion-null \
	-Wno-builtin-macro-redefined

LIBS=-ldl -lpthread

SRC_DIR:=$(PWD)/src/native
BUILD_DIR?=$(shell mkdir build-$(BITS) 2> /dev/null; echo $(PWD)/build-$(BITS))

OPT?=-O3

GLOBAL_WARNINGS=-Wformat-security \
	-Wformat \
	-Wno-char-subscripts \
	-Wno-sign-compare \
	-Wno-strict-overflow \
	-Wnon-virtual-dtor \
	-Woverloaded-virtual \
	-Wwrite-strings
GLOBAL_COPTS=-fdiagnostics-show-option \
	-fexceptions \
	-fno-asynchronous-unwind-tables \
	-fno-omit-frame-pointer \
	-fno-strict-aliasing \
	-fPIC \
	-funsigned-char \
	-Fvisibility=hidden \
	-m$(BITS) \
	-msse2 \
	-g \
	-D__STDC_FORMAT_MACROS

COPTS:=$(PLATFORM_COPTS) \
	$(GLOBAL_COPTS) \
	$(PLATFORM_WARNINGS) \
	$(GLOBAL_WARNINGS) \
	$(OPT)

JAVA_HOME:=$(shell \
	[[ -n "$${JAVA_HOME}" ]] || \
	  JAVA_HOME=$$(dirname $$(readlink -f $$(which java)))/..; \
	[[ "$${JAVA_HOME}" =~ /jre ]] && JAVA_HOME=$${JAVA_HOME}/..; \
	[[ -n "$${JAVA_HOME}" ]] || (echo "Cannot find JAVA_HOME" && exit); \
	echo $${JAVA_HOME})

INCLUDES=-I$(JAVA_HOME)/include \
	-I$(JAVA_HOME)/include/$(UNAME) \
	-I/usr/include

SOURCES=$(wildcard $(SRC_DIR)/*.cc)
_OBJECTS=$(SOURCES:.cc=.pic.o)
OBJECTS = $(patsubst $(SRC_DIR)/%,$(BUILD_DIR)/%,$(_OBJECTS))

$(BUILD_DIR)/%.pic.o: $(SRC_DIR)/%.cc
	$(CC) $(INCLUDES) $(COPTS) -c $< -o $@

all: native java tests

native: $(OBJECTS)
	$(CC) $(COPTS) -shared \
	  -o $(BUILD_DIR)/$(TARGET) \
	  -Bsymbolic $(OBJECTS) $(LIBS)

java:
	mvn -f src/java/pom.xml install

tests:
	javac src/java/src/test/java/test/*.java -cp src/java/target/jcoz-client*dependencies.jar

clean:
	rm -rf $(BUILD_DIR)/*
	rm -rf src/java/src/test/java/test/*.class
	rm -rf src/java/target

run:
	cd src/java/src/test/java/; \
	java \
	  -agentpath:$(BUILD_DIR)/liblagent.so \
	  -cp $$(readlink -f ../../../target/jcoz-client*dependencies.jar):. \
	  test.TestThreadSerial &

kill:
	pkill -9 java
