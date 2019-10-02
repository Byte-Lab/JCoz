# JCoz (https://github.com/Decave/JCoz)
# Copyright (C) 2019 Mark Street (mkst@protonmail.com)
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

FROM maven:3.6.2-jdk-8 as build

RUN apt-get update && \
  apt-get install -y \
    g++ \
    libspdlog-dev \
    make \
    openjfx && \
  mkdir -p /usr/java/ && \
  ln -nfs /usr/share/java/openjfx /usr/java/latest

COPY . /jcoz

WORKDIR /jcoz

RUN make all

FROM openjdk:8-slim

COPY --from=build /jcoz/build*/liblagent.so /jcoz/
COPY --from=build /jcoz/src/java/target/*.jar /jcoz/
COPY --from=build /jcoz/src/java/src/test/java/test /jcoz/test/

WORKDIR /jcoz

CMD ["/bin/bash"]
