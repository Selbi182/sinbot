FROM adoptopenjdk/openjdk11:alpine
WORKDIR /
ADD SinBot.jar SinBot.jar
ADD counters counters
RUN chmod -R 777 counters
CMD java -jar SinBot.jar