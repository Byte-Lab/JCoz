# !/bin/bash


echo "Running TestThreadSerial with hprof"

JCOZ_HOME="${HOME}/JCoz"
JCOZ_TEST_CLASSES="${JCOZ_HOME}/jcoz-client/target/test-classes"

java -cp ${JCOZ_TEST_CLASSES} -agentlib:hprof=cpu=samples,depth=3 test.TestThreadSerial
