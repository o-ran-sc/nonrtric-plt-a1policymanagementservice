#
# ============LICENSE_START=======================================================
#  Copyright (C) 2019-2023 Nordix Foundation.
#  Copyright (C) 2024-2025: OpenInfra Foundation Europe. All rights reserved.
# ================================================================================
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# SPDX-License-Identifier: Apache-2.0
# ============LICENSE_END=========================================================
#
FROM amazoncorretto:17-alpine as jre-build

RUN apk add binutils
RUN $JAVA_HOME/bin/jlink \
--verbose \
--add-modules ALL-MODULE-PATH \
--strip-debug \
--no-man-pages \
--no-header-files \
--compress=2 \
--output /customjre

# Use debian base image (same as openjdk uses)
FROM amazoncorretto:17-alpine

ENV JAVA_HOME=/jre
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Copy JRE from the base image
COPY --from=jre-build /customjre $JAVA_HOME

ARG JAR

EXPOSE 8081 8433


WORKDIR /opt/app/policy-agent
RUN mkdir -p /var/log/policy-agent
RUN mkdir -p /opt/app/policy-agent/etc/cert/
RUN mkdir -p /var/policy-management-service
EXPOSE 8081 8433

ADD /config/application.yaml /opt/app/policy-agent/config/application.yaml
ADD /config/application_configuration.json /opt/app/policy-agent/data/application_configuration.json_example
ADD /config/keystore.jks /opt/app/policy-agent/etc/cert/keystore.jks
ADD /config/truststore.jks /opt/app/policy-agent/etc/cert/truststore.jks

ARG user=nonrtric
ARG userid=120957
ARG group=nonrtric
ARG groupid=120957

RUN addgroup --gid $groupid $group && \
    adduser -u $userid -G $group -D -g "" $user
RUN chown -R $user:$group /opt/app/policy-agent
RUN chown -R $user:$group /var/log/policy-agent
RUN chown -R $user:$group /var/policy-management-service

USER ${user}

ADD target/${JAR} /opt/app/policy-agent/policy-agent.jar
CMD ["/jre/bin/java", "-jar", "/opt/app/policy-agent/policy-agent.jar"]
