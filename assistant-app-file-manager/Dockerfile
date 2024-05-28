# build
FROM  openjdk:19-alpine as builder
ADD /target /src
WORKDIR /src
RUN ls
RUN unzip /src/*.jar -d temp &&  \
	jdeps  \
	--print-module-deps \
	--ignore-missing-deps \
	--recursive \
	-q \
	--multi-release 19 \
	--class-path="./temp/BOOT-INF/lib/*" \
	--module-path="./temp/BOOT-INF/lib/*" \
	/src/*.jar > /modules.txt

# jre
FROM openjdk:19-alpine as packager
COPY --from=builder /modules.txt /modules.txt
# for strip-debug to work
RUN apk update && apk add --no-cache binutils 
RUN jlink \
	--add-modules "$(cat /modules.txt)" \ 
	--strip-debug \
	--no-man-pages \
	--no-header-files \
	--compress=2 \
	--output /jre

# copy to clean image
FROM alpine:3.18
COPY --from=packager /jre $JAVA_HOME
COPY --from=builder /src/*.jar app.jar
ENV PATH="${JAVA_HOME}/bin:${PATH}"
ENTRYPOINT ["java","-jar","/app.jar"]