# !/bin/bash

echo "Running TestThreadSerial with xprof"

JCOZ_HOME="${HOME}/JCoz"
JCOZ_TEST_CLASSES="${JCOZ_HOME}/jcoz-client/target/test-classes"

java -cp ${JCOZ_TEST_CLASSES} -Xprof test.TestThreadSerial
