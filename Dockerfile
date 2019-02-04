FROM jupyter/base-notebook
################ ENV ##################
USER root
ENV SCALA_VERSION=2.13.0 \
    ALMOND_VERSION=0.1.12

################ JDK ##################
RUN apt-get update && \
    apt-get install -y software-properties-common && \
    echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
    add-apt-repository -y ppa:webupd8team/java && \
    apt-get install -y oracle-java8-installer && \
    rm -rf /var/lib/apt/lists/* && \
    rm -rf /var/cache/oracle-jdk8-installer

############ Scala Kernel #############
RUN apt-get update && \
    apt-get install -y curl && \
    curl -L -o coursier https://git.io/coursier && \
    chmod +x coursier && \
    ./coursier bootstrap \
        -i user -I user:sh.almond:scala-kernel-api_$SCALA_VERSION:$ALMOND_VERSION \
        sh.almond:scala-kernel_$SCALA_VERSION:$ALMOND_VERSION \
        -o almond && \
    ./almond --install --jupyter-path=/opt/conda/share/jupyter/kernels --display-name="Scala $SCALA_VERSION" && \
    rm almond

USER jovyan
RUN mkdir "/home/jovyan/.ivy2"
ENTRYPOINT ["start-notebook.sh", "--notebook-dir=work/"]
CMD []
