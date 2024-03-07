# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the "License"); you may not use
# this file except in compliance with the License.  You may obtain a copy of
# the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.    


FROM maven:3.6.2-jdk-8 as build

RUN apt-get update && \
  apt-get install -y \
    g++ \
    libspdlog-dev \
    make

COPY . /jcoz

WORKDIR /jcoz

RUN make -j`nproc` all

FROM openjdk:8-slim

COPY --from=build /jcoz/build*/liblagent.so /jcoz/
COPY --from=build /jcoz/src/java/target/*.jar /jcoz/
COPY --from=build /jcoz/src/java/src/test/java/test /jcoz/test/

WORKDIR /jcoz

CMD ["/bin/bash"]
