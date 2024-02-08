# docker build -t david62243/jmeter-test:v1.0 .
# docker run -it --rm --env-file veltest.env david62243/jmeter-test:v1.0
# docker run -it --rm --env-file veltest.env -v template.jmx:/home/tester/  david62243/jmeter-test:v1.0  
FROM alpine

RUN apk update \
    && apk add openjdk17 python3 py3-pip bash curl

ENV USER=tester

RUN adduser --disabled-password \
    --gecos "" \
    --home "/home/$USER" \
    "$USER"

ADD apache-jmeter-5.6.2.tgz /home/$USER

COPY --chown=${USER}:${USER} *.py /home/$USER
COPY --chown=${USER}:${USER} *.json /home/$USER
COPY --chown=${USER}:${USER} run.sh /home/$USER
COPY --chown=${USER}:${USER} template.jmx /home/$USER


USER ${USER}

WORKDIR /home/$USER

RUN pip3 install requests psycopg2-binary prometheus-client  \
   && ln -s apache-jmeter-5.6.2 apache-jmeter
