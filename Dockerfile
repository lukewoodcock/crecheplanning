FROM java:8-jre

COPY ./target/universal/crocos-1.0.zip .

COPY ./conf/application.conf .

RUN unzip crocos-1.0.zip && \
ls crocos-1.0/bin

WORKDIR crocos-1.0

CMD ["bin/crocos", "-Dconfig.file=../application.conf"]