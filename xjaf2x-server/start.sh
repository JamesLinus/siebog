#!/bin/sh

if [ "x$JBOSS_HOME" = "x" ]; then
  echo "ERROR: Environment variable JBOSS_HOME not set."
  exit -1
fi

JBOSS_MODULES=$JBOSS_HOME/modules;
JBOSS_BIN=$JBOSS_HOME/bin;

java -cp .:$JBOSS_MODULES/infinispan/main/infinispan-core-5.2.6.Final-redhat-1.jar:$JBOSS_MODULES/jboss/as/cli/main/jboss-as-cli-7.2.0.Final-redhat-8.jar:$JBOSS_MODULES/jboss/as/controller-client/main/jboss-as-controller-client-7.2.0.Final-redhat-8.jar:$JBOSS_MODULES/jboss/as/naming/main/jboss-as-naming-7.2.0.Final-redhat-8.jar:$JBOSS_MODULES/jboss/ejb-client/main/jboss-ejb-client-1.0.21.Final-redhat-1.jar:$JBOSS_MODULES/jboss/ejb3/main/jboss-ejb3-ext-api-2.0.0-redhat-2.jar:$JBOSS_MODULES/jboss/as/jaxrs/main/jboss-as-jaxrs-7.2.0.Final-redhat-8.jar:$JBOSS_MODULES/jboss/msc/main/jboss-msc-1.0.4.GA-redhat-1.jar:$JBOSS_MODULES/jboss/as/process-controller/main/jboss-as-process-controller-7.2.0.Final-redhat-8.jar:$JBOSS_BIN/client/jboss-client.jar:$JBOSS_BIN/client/jboss-cli-client.jar:xjaf2x.jar xjaf2x.StartNode $@
