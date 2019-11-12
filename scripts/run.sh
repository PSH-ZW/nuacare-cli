#!/bin/bash
BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
classpath=''
for i in `find ${BASEDIR}/../lib/*`
do
    classpath="$classpath:$i"
done
export CLASSPATH="${classpath}:${BASEDIR}/../target/classes"
java  com.nuchange.nuacare.common.Uploader
exit 0