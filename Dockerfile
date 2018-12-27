FROM show0k/alpine-minimal-notebook

################ ENV ##################
USER root
ENV LANG=C.UTF-8 \
    JAVA_ALPINE_VERSION=8.181.13-r0 \
    SCALA_VERSION=2.12.7 \
    ALMOND_VERSION=0.1.12

################ JRE ##################
RUN set -x && \
    apk add --no-cache openjdk8-jre="$JAVA_ALPINE_VERSION"

############ Scala Kernel #############
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
