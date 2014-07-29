#!/bin/bash

# fetch changes from github
currentdir=`pwd`
cd ${mms.github} && git pull
cd ${currentdir}

# build new software version
mvn -f ${mms.github}/pom.xml clean package assembly:assembly

# remove old software version
rm -rf ${mms.home}

# move assembly to target directory
mv ${mms.github}/target/sst-cci-mms-${project.version}-bin/sst-cci-mms-${project.version} $(dirname ${mms.home})

# change file permissions manually because maven assembly does not do this
chmod -R ug+X $(dirname ${mms.home})
