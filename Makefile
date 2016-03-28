CFLAGS=-rdynamic -Wall -fPIC -shared -g
INCLUDE=-I/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.71-2.b15.el7_2.x86_64/include -I/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.71-2.b15.el7_2.x86_64/include/linux
CXX=gcc
JCFLAGS=-g
JFLAGS=-Xdebug

all: jcoz

jcoz: jcoz.c
	$(CXX) -o jcoz.so $(CFLAGS) $(INCLUDE) jcoz.c

test: jcoz Test.java 
	javac $(JCFLAGS) Test.java
	java $(JFLAGS) -agentpath:./jcoz.so Test

clean:
	rm -rf *.class *.log *.so *.o
