FROM openjdk:17-alpine3.14
RUN apk add make
COPY . /usr/src/heartbeat
WORKDIR /usr/src/heartbeat
RUN make build-monitor
RUN make build-app
CMD ./run.sh