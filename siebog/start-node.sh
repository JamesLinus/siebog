#!/bin/sh

if [ "x$JBOSS_HOME" = "x" ]; then
  echo "ERROR: Environment variable JBOSS_HOME not set."
  exit -1
fi

if [ "x$JAVA_HOME" = "x" ]; then
  echo "WARNING: Environment variable JAVA_HOME not set, unknown errors may occur."
  JAVA_EXE=java;
else
  JAVA_EXE=$JAVA_HOME/bin/java;
fi

JBOSS_MODULES_BASE=$JBOSS_HOME/modules/system/layers/base;
JBOSS_BIN=$JBOSS_HOME/bin;
CWD=$(pwd);

$JAVA_EXE -server -Dxjaf.base.dir=$CWD -cp .:\
$JBOSS_MODULES_BASE/org/infinispan/main/infinispan-core-6.0.2.Final.jar:\
$JBOSS_MODULES_BASE/org/infinispan/commons/infinispan-commons-6.0.2.Final.jar:\
$JBOSS_MODULES_BASE/javax/ws/rs/api/main/jaxrs-api-3.0.8.Final.jar:\
$JBOSS_BIN/client/jboss-client.jar:\
$JBOSS_BIN/client/jboss-cli-client.jar:\
$JBOSS_MODULES_BASE/org/jboss/ejb-client/main/jboss-ejb-client-2.0.1.Final.jar:\
$JBOSS_MODULES_BASE/org/jboss/ejb3/main/jboss-ejb3-ext-api-2.1.0.jar:\
$JBOSS_MODULES_BASE/org/jboss/msc/main/jboss-msc-1.2.2.Final.jar:\
$JBOSS_MODULES_BASE/org/jboss/as/cli/main/wildfly-cli-1.0.0.Alpha3.jar:\
$JBOSS_MODULES_BASE/org/jboss/as/controller-client/main/wildfly-controller-client-1.0.0.Alpha3.jar:\
$JBOSS_MODULES_BASE/org/jboss/as/naming/main/wildfly-naming-9.0.0.Alpha1-SNAPSHOT.jar:\
$JBOSS_MODULES_BASE/org/jboss/as/process-controller/main/wildfly-process-controller-1.0.0.Alpha3.jar:\
$JBOSS_MODULES_BASE/org/jboss/logmanager/main/jboss-logmanager-1.5.2.Final.jar:\
$CWD/siebog.war \
siebog.server.StartNode $@
