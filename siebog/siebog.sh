#!/bin/bash
export JBOSS_HOME=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/wildfly-9.x
java -jar siebog.war $@
