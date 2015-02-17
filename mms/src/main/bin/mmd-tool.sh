#! /bin/sh

MMS_OPTIONS=""
if [ ! -z ${MMS_DEBUG} ]; then
    MMS_OPTIONS="-Xdebug -Xrunjdwp:transport=dt_socket,address=8001,server=y,suspend=y"
fi

${mms.jdk.home}/bin/java \
    -Dmms.home="${mms.home}" \
    -Xms16G -Xmx16G ${MMS_OPTIONS} \
    -javaagent:"${mms.home}/lib/openjpa-all-${openjpaversion}.jar" \
    -classpath "${mms.home}/lib/*" \
    org.esa.cci.sst.tools.mmdgeneration.MmdTool "$@"
