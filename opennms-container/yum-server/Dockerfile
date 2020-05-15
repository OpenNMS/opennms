FROM debian:stable-slim

RUN apt-get update && \
    apt-get -y install createrepo lighttpd && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

COPY resources/httpd.conf /etc/
COPY resources/start-yum-server.sh /usr/local/bin/

EXPOSE 19990
VOLUME ["/repo"]

CMD ["/usr/local/bin/start-yum-server.sh"]
