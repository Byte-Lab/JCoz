#!/usr/bin/bash

##
# NOTICE
#
# Copyright (c) 2016 David C Vernet and Matthew J Perron. All rights reserved.
#
# Unless otherwise noted, all of the material in this file is Copyright (c) 2016
# by David C Vernet and Matthew J Perron. All rights reserved. No part of this file
# may be reproduced, published, distributed, displayed, performed, copied,
# stored, modified, transmitted or otherwise used or viewed by anyone other
# than the authors (David C Vernet and Matthew J Perron),
# for either public or private use.
#
# No part of this file may be modified, changed, exploited, or in any way
# used for derivative works or offered for sale without the express
# written permission of the authors.
#

for i in `seq 1 400`; do
     java -agentpath:./build-64/liblagent.so=pkg=test_progress-point=test/Test:21_warmup=1000_slow-exp test.Test
done
