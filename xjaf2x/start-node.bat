@echo off

if "x%JBOSS_HOME%" == "x" (
  echo "ERROR: Environment variable JBOSS_HOME not set."
  goto END
)

if "x%JAVA_HOME%" == "x" (
  set JAVA_EXE=java
) else (
  set "JAVA_EXE=%JAVA_HOME%\bin\java"
)

set "JBOSS_MODULES_BASE=%JBOSS_HOME%\modules\system\layers\base"
set "JBOSS_BIN=%JBOSS_HOME%\bin"
set "CWD=%CD%"

"%JAVA_EXE%" -Dxjaf.base.dir="%CWD%" -cp .;"%JBOSS_MODULES_BASE%\org\infinispan\main\infinispan-core-6.0.2.Final.jar";"%JBOSS_MODULES_BASE%\org\infinispan\commons\infinispan-commons-6.0.2.Final.jar";"%JBOSS_MODULES_BASE%\javax\ws\rs\api\main\jaxrs-api-3.0.8.Final.jar";"%JBOSS_BIN%\client\jboss-client.jar";"%JBOSS_BIN%\client\jboss-cli-client.jar";"%JBOSS_MODULES_BASE%\org\jboss\ejb-client\main\jboss-ejb-client-2.0.1.Final.jar";"%JBOSS_MODULES_BASE%\org\jboss\ejb3\main\jboss-ejb3-ext-api-2.1.0.jar";"%JBOSS_MODULES_BASE%\org\jboss\msc\main\jboss-msc-1.2.2.Final.jar";"%JBOSS_MODULES_BASE%\org\jboss\as\cli\main\wildfly-cli-1.0.0.Alpha3.jar";"%JBOSS_MODULES_BASE%\org\jboss\as\controller-client\main\wildfly-controller-client-1.0.0.Alpha3.jar";"%JBOSS_MODULES_BASE%\org\jboss\as\naming\main\wildfly-naming-9.0.0.Alpha1-SNAPSHOT.jar";"%JBOSS_MODULES_BASE%\org\jboss\as\process-controller\main\wildfly-process-controller-1.0.0.Alpha3.jar";"%JBOSS_MODULES_BASE%\org\jboss\logmanager\main\jboss-logmanager-1.5.2.Final.jar";"%CWD%\xjaf.jar" xjaf.StartNode %*

:END
if "x%NOPAUSE%" == "x" pause
