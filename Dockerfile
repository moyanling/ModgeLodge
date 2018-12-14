#FROM show0k/alpine-minimal-notebook
#
#USER root
######## Java
#
#ENV JAVA_VERSION=8 \
#    JAVA_UPDATE=192 \
#    JAVA_BUILD=12 \
#    JAVA_PATH=750e1c8617c5452694857ad95c3ee230 \
#    JAVA_HOME="/usr/lib/jvm/default-jvm"
#
#RUN apk add --no-cache --virtual=build-dependencies wget ca-certificates unzip && \
#    cd "/tmp" && \
#    wget --header "Cookie: oraclelicense=accept-securebackup-cookie;" \
#        "http://download.oracle.com/otn-pub/java/jdk/${JAVA_VERSION}u${JAVA_UPDATE}-b${JAVA_BUILD}/${JAVA_PATH}/jdk-${JAVA_VERSION}u${JAVA_UPDATE}-linux-x64.tar.gz" && \
#    tar -xzf "jdk-${JAVA_VERSION}u${JAVA_UPDATE}-linux-x64.tar.gz" && \
#    mkdir -p "/usr/lib/jvm" && \
#    mv "/tmp/jdk1.${JAVA_VERSION}.0_${JAVA_UPDATE}" "/usr/lib/jvm/java-${JAVA_VERSION}-oracle" && \
#    ln -s "java-${JAVA_VERSION}-oracle" "$JAVA_HOME" && \
#    ln -s "$JAVA_HOME/bin/"* "/usr/bin/" && \
#    rm -rf "$JAVA_HOME/"*src.zip && \
#    rm -rf "$JAVA_HOME/lib/missioncontrol" \
#           "$JAVA_HOME/lib/visualvm" \
#           "$JAVA_HOME/lib/"*javafx* \
#           "$JAVA_HOME/jre/lib/plugin.jar" \
#           "$JAVA_HOME/jre/lib/ext/jfxrt.jar" \
#           "$JAVA_HOME/jre/bin/javaws" \
#           "$JAVA_HOME/jre/lib/javaws.jar" \
#           "$JAVA_HOME/jre/lib/desktop" \
#           "$JAVA_HOME/jre/plugin" \
#           "$JAVA_HOME/jre/lib/"deploy* \
#           "$JAVA_HOME/jre/lib/"*javafx* \
#           "$JAVA_HOME/jre/lib/"*jfx* \
#           "$JAVA_HOME/jre/lib/amd64/libdecora_sse.so" \
#           "$JAVA_HOME/jre/lib/amd64/"libprism_*.so \
#           "$JAVA_HOME/jre/lib/amd64/libfxplugins.so" \
#           "$JAVA_HOME/jre/lib/amd64/libglass.so" \
#           "$JAVA_HOME/jre/lib/amd64/libgstreamer-lite.so" \
#           "$JAVA_HOME/jre/lib/amd64/"libjavafx*.so \
#           "$JAVA_HOME/jre/lib/amd64/"libjfx*.so && \
#    rm -rf "$JAVA_HOME/jre/bin/jjs" \
#           "$JAVA_HOME/jre/bin/keytool" \
#           "$JAVA_HOME/jre/bin/orbd" \
#           "$JAVA_HOME/jre/bin/pack200" \
#           "$JAVA_HOME/jre/bin/policytool" \
#           "$JAVA_HOME/jre/bin/rmid" \
#           "$JAVA_HOME/jre/bin/rmiregistry" \
#           "$JAVA_HOME/jre/bin/servertool" \
#           "$JAVA_HOME/jre/bin/tnameserv" \
#           "$JAVA_HOME/jre/bin/unpack200" \
#           "$JAVA_HOME/jre/lib/ext/nashorn.jar" \
#           "$JAVA_HOME/jre/lib/jfr.jar" \
#           "$JAVA_HOME/jre/lib/jfr" \
#           "$JAVA_HOME/jre/lib/oblique-fonts" && \
#    wget --header "Cookie: oraclelicense=accept-securebackup-cookie;" \
#        "http://download.oracle.com/otn-pub/java/jce/${JAVA_VERSION}/jce_policy-${JAVA_VERSION}.zip" && \
#    unzip -jo -d "${JAVA_HOME}/jre/lib/security" "jce_policy-${JAVA_VERSION}.zip" && \
#    rm "${JAVA_HOME}/jre/lib/security/README.txt" && \
#    apk del build-dependencies && \
#    rm "/tmp/"* && \
#    \
#    echo 'public class Main { public static void main(String[] args) { System.out.println("Java code is running fine!"); } }' > Main.java && \
#    javac Main.java && \
#    java Main && \
#    rm -r "/tmp/"*
#
################# Scala
#ENV SCALA_VERSION=2.12.7 \
#    SCALA_HOME=/usr/share/scala
#
## NOTE: bash is used by scala/scalac scripts, and it cannot be easily replaced with ash.
#RUN apk add --no-cache --virtual=.build-dependencies wget ca-certificates && \
#    apk add --no-cache bash curl jq && \
#    cd "/tmp" && \
#    wget --no-verbose "https://downloads.typesafe.com/scala/${SCALA_VERSION}/scala-${SCALA_VERSION}.tgz" && \
#    tar xzf "scala-${SCALA_VERSION}.tgz" && \
#    mkdir "${SCALA_HOME}" && \
#    rm "/tmp/scala-${SCALA_VERSION}/bin/"*.bat && \
#    mv "/tmp/scala-${SCALA_VERSION}/bin" "/tmp/scala-${SCALA_VERSION}/lib" "${SCALA_HOME}" && \
#    ln -s "${SCALA_HOME}/bin/"* "/usr/bin/" && \
#    apk del .build-dependencies && \
#    rm -rf "/tmp/"*
#
#RUN export PATH="/usr/local/sbt/bin:$PATH" &&  apk update && apk add ca-certificates wget tar && mkdir -p "/usr/local/sbt" && wget -qO - --no-check-certificate "https://cocl.us/sbt-0.13.16.tgz" | tar xz -C /usr/local/sbt --strip-components=1 && sbt sbtVersion
#
############### Scala Kernel
#ENV ALMOND_VERSION=0.1.12
#RUN curl -L -o coursier https://git.io/coursier && chmod +x coursier
#RUN ./coursier bootstrap \
#        -i user -I user:sh.almond:scala-kernel-api_$SCALA_VERSION:$ALMOND_VERSION \
#        sh.almond:scala-kernel_$SCALA_VERSION:$ALMOND_VERSION \
#        -o almond
#RUN ./almond --install
#
#
###################
#ENTRYPOINT ["start-notebook.sh", "--notebook-dir=/opt/ModgeLodge"]
#CMD []
#
# ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ Not Working ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
# I am fxxking done with it (╯‵□′)╯︵┻━┻. I am gonna just get a FAT Ubuntu and install everything easily. I love it FAT. Any question?!

#RUN wget https://repo.anaconda.com/archive/Anaconda3-5.3.1-Linux-x86_64.sh
#RUN yes yes | bash Anaconda3-5.3.1-Linux-x86_64.sh

#USER root
FROM jupyter/base-notebook

USER root
RUN apt-get update && \
    apt-get install -y software-properties-common && \
#    add-apt-repository ppa:jonathonf/python-3.6 && \
    add-apt-repository ppa:webupd8team/java && \
    apt-get update


#RUN apt-get install wget
RUN apt-get install -y openjdk-8-jre
RUN wget www.scala-lang.org/files/archive/scala-2.12.7.deb && \
    dpkg -i scala-2.12.7.deb

ENV SCALA_VERSION=2.12.7 ALMOND_VERSION=0.2.0
RUN wget https://git.io/coursier && chmod +x coursier
RUN ./coursier bootstrap \
          -r jitpack \
          -i user -I user:sh.almond:scala-kernel-api_$SCALA_VERSION:$ALMOND_VERSION \
          sh.almond:scala-kernel_$SCALA_VERSION:$ALMOND_VERSION \
          --sources --default=true \
          -o almond
RUN ./almond --install --jupyter-path=/opt/conda/share/jupyter/kernels

#ENTRYPOINT jupyter notebook --allow-root --notebook-dir=/opt/ModgeLodge
#RUN apt-get install -y build-essential python3.6 python3-pip && \
#    python3 -m pip install pip --upgrade &&\
#    pip3 install jupyter

#RUN python3 --version
#RUN jupyter -version