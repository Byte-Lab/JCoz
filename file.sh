#!/usr/bin/bash

for i in `seq 1 400`; do
     java -agentpath:./build-64/liblagent.so=pkg=test_progress-point=test/Test:21_warmup=1000_slow-exp test.Test
done
