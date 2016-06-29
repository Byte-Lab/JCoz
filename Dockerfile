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

###############################################################################
# Dockerfile for creating a docker image and testing that the compiled
# JCoz shared library works on different platforms. To test on a specific
# platform, change the FROM instruction argument to whatever base image
# you'd like, and update the argument passed to entry.sh.
# 
# To build a docker image, perform the following steps:
# 1. Run `docker build .` from the root directory of the project containing
#		the dockerfile.
# 2. Run `docker run <sha_ID_of_new_image>` to run the tests.
#
# TODO(david): Figure out how to get profile out of running container.
#			   Need to figure out how to mount a volume on the container.
# TODO(david): Have a more abstracted, well designed system for this
#


FROM centos
MAINTAINER David Vernet <dcvernet@gmail.com>

WORKDIR /jcoz/test


# Add compiled shared library, test files, and dependencies
ENV agentPath="liblagent.so"
ENV testPath="test/Test.java"
ENV testExec="file.sh"
ADD lib/liblagent.so ${agentPath}
ADD test/Test.java ${testPath}
ADD file.sh file.sh
ADD docker/debian_bootstrap.sh debian_bootstrap.sh
ADD docker/redhat_bootstrap.sh redhat_bootstrap.sh
ADD docker/entry.sh entry.sh

# Run test
ENTRYPOINT ["/bin/bash", "entry.sh", "redhat_bootstrap.sh"]
