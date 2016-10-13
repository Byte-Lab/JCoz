# !/bin/bash


echo "Running TestThreadSerial on node $HOSTNAME\n"
echo ${HOME}

JCOZ_HOME="${HOME}/JCoz"
CLASSES_DIR="${JCOZ_HOME}/jcoz-client/target/classes"
CLASS_BASE_DIR="${CLASSES_DIR}/com/vernetperronllc/jcoz"
JCOZ_CLASSES="${JCOZ_HOME}/jcoz-client/target/jcoz-client-0.0.1-SNAPSHOT.jar:${CLASSES_DIR}:${CLASS_BASE_DIR}:${CLASS_BASE_DIR}/agent:${CLASS_BASE_DIR}/client:${CLASS_BASE_DIR}/service:${CLASS_BASE_DIR}/client/ui:${CLASS_BASE_DIR}/client/cli"
JCOZ_TEST_CLASSES="${JCOZ_HOME}/jcoz-client/target/test-classes"
TOOLS_JAR="/usr/java/jdk1.8.0_40/lib/tools.jar"
JCOZ_JARS="${JCOZ_CLASSES}:${TOOLS_JAR}"
JCOZ_FLAG="-agentpath:${JCOZ_HOME}/build-64/liblagent.so"
RMI_FLAGS="-Djava.rmi.useLocalHostname=false -Djava.rmi.server.hostname=${HOSTNAME}"
alias jtest=""

java -cp ${JCOZ_JARS} ${RMI_FLAGS} com.vernetperronllc.jcoz.service.JCozService > jcoz_service.log &
java -cp ${JCOZ_TEST_CLASSES}:${JCOZ_JARS} ${JCOZ_FLAG} test.TestThreadSerial > jcoz_test.log
