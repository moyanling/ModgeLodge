FROM show0k/alpine-minimal-notebook

################ ENV ##################
USER root
ENV LANG=C.UTF-8 \
    JAVA_ALPINE_VERSION=8.181.13-r0 \
    SCALA_VERSION=2.12.7 \
    SCALA_HOME=/usr/share/scala \
    ALMOND_VERSION=0.1.12

################ JRE ##################
RUN set -x && \
    apk add --no-cache openjdk8-jre="$JAVA_ALPINE_VERSION"

############### Scala #################
RUN apk add --no-cache --virtual=.build-dependencies wget ca-certificates && \
    apk add --no-cache bash curl jq && \
    cd "/tmp" && \
    wget --no-verbose "https://downloads.typesafe.com/scala/${SCALA_VERSION}/scala-${SCALA_VERSION}.tgz" && \
    tar xzf "scala-${SCALA_VERSION}.tgz" && \
    mkdir "${SCALA_HOME}" && \
    rm "/tmp/scala-${SCALA_VERSION}/bin/"*.bat && \
    mv "/tmp/scala-${SCALA_VERSION}/bin" "/tmp/scala-${SCALA_VERSION}/lib" "${SCALA_HOME}" && \
    ln -s "${SCALA_HOME}/bin/"* "/usr/bin/" && \
    apk del .build-dependencies && \
    rm -rf "/tmp/"* && \
    export PATH="/usr/local/sbt/bin:$PATH" && \
    apk update && \
    apk add ca-certificates wget tar && \
    mkdir -p "/usr/local/sbt" && \
    wget -qO - --no-check-certificate "https://cocl.us/sbt-0.13.16.tgz" | tar xz -C /usr/local/sbt --strip-components=1

############### Kernel ################
RUN curl -L -o coursier https://git.io/coursier && \
    chmod +x coursier && \
    ./coursier bootstrap \
        -i user -I user:sh.almond:scala-kernel-api_$SCALA_VERSION:$ALMOND_VERSION \
        sh.almond:scala-kernel_$SCALA_VERSION:$ALMOND_VERSION \
        -o almond && \
    ./almond --install --jupyter-path=/opt/conda/share/jupyter/kernels --display-name="Scala $SCALA_VERSION" && \
    rm coursier && \
    rm almond

USER jovyan
RUN mkdir "/home/jovyan/.ivy2"
ENTRYPOINT ["start-notebook.sh"]
CMD []
