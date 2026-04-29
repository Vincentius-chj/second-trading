# 基础镜像
FROM maven:3.8.1-jdk-8-slim as builder
# 解决容器时期与真实时间相差8个小时的问题
RUN ln -snf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' > /etc/timezone
# 复制代码到容器内
WORKDIR /app
COPY pom.xml .
COPY src ./src
# 打包构建
RUN mvn clean package -DskipTests
# 容器启动时运行jar包
CMD ["java", "-jar", "/app/target/second-hand-trading-0.0.1-SNAPSHOT.jar", "--spring.profiles.active=bprod"]