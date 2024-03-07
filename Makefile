# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.    

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

tests: java
	javac src/java/src/test/java/test/*.java -cp src/java/target/client*dependencies.jar

clean:
	rm -rf $(BUILD_DIR)/*
	rm -rf src/java/src/test/java/test/*.class
	rm -rf src/java/target

run-workload:
	cd src/java/src/test/java/; \
	java \
	  -agentpath:$(BUILD_DIR)/liblagent.so \
	  -cp $$(readlink -f ../../../target/client*dependencies.jar):. \
	  test.TestThreadSerial --fast

run-rmi-host:
	java \
	  -cp $$(readlink -f ./src/java/target/client-*-jar-with-dependencies.jar):$(JAVA_HOME)/lib/tools.jar \
	  jcoz.service.JCozService

run-profiler:
	cd src/java/src/test/java/; \
	java \
	  -agentpath:$(BUILD_DIR)/liblagent.so \
	  -cp $$(readlink -f ../../../target/client*dependencies.jar):. \
	  test.JCozServiceTest
