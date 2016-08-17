FROM  172.16.6.127:5000/java

ENV WORK_HOME /u01/im/
ENV EMPLOYEE_DATA_DIR /u01/employee_data/
ENV DEPT_DATA_DIR /u01/dept_data/
ENV JPDA_ADDRESS=8000
ENV JPDA_TRANSPORT=dt_socket

RUN mkdir -p "$WORK_HOME"
RUN mkdir -p $EMPLOYEE_DATA_DIR
RUN mkdir -p $DEPT_DATA_DIR

RUN echo "Asia/Shanghai" > /etc/timezone

COPY apache-tomcat-8.0.26.tar.gz $WORK_HOME
WORKDIR $WORK_HOME
RUN tar xf apache-tomcat-8.0.26.tar.gz
ENV CATALINA_HOME /u01/im/apache-tomcat-8.0.26
RUN rm -rf $CATALINA_HOME/webapps/*
COPY micservice.war $CATALINA_HOME/webapps/
ENV PATH $CATALINA_HOME/bin:$PATH

EXPOSE 8080 8000
CMD ["catalina.sh","jpda","run"]