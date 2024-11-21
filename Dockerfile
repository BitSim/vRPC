FROM alpine3-jdk21
COPY . /usr/src/myapp
WORKDIR /usr/src/myapp
RUN chmod +x gradlew && ./gradlew build
# 命令行
CMD ["/bin/sh"]
