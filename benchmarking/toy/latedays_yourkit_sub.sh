# !/bin/bash

echo "Running TestThreadSerial for YourKit"

JCOZ_HOME="${HOME}/JCoz"
JCOZ_TEST_CLASSES="${JCOZ_HOME}/jcoz-client/target/test-classes"

java -cp ${JCOZ_TEST_CLASSES} test.TestThreadSerial
