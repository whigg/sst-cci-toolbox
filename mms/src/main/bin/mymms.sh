#!/bin/bash
# MMS environment setup
# usage:  . mymms

umask 007

export PGPORT=5423
export PGHOST=mms1.cems.rl.ac.uk
export PGDATA=/data/mboettcher/mms/db/mmdb

export PATH=/group_workspaces/cems/esacci_sst/mms/software/jdk1.7.0_51/bin:${PATH}
export PATH=/group_workspaces/cems/esacci_sst/mms/software/bin:${PATH}
export LD_LIBRARY_PATH=/group_workspaces/cems/esacci_sst/mms/software/lib:/group_workspaces/cems/esacci_sst/software/common/lib:${LD_LIBRARY_PATH}

export MMS_HOME=/group_workspaces/cems/esacci_sst/mms/software/sst-cci-mms-${pom.version}
export MMS_ARCHIVE=/group_workspaces/cems/esacci_sst/mms/archive

export MMS_INST=/group_workspaces/cems/esacci_sst/mms/inst
export MMS_CONFIG=${MMS_INST}/mms-config.properties
export MMS_TASKS=${MMS_INST}/tasks
export MMS_LOG=${MMS_INST}/log
export TMPDIR=${MMS_INST}/tmp

export PYTHONPATH=${MMS_INST}:${MMS_HOME}/python:${PYTHONPATH}
export PATH=${MMS_INST}:${MMS_HOME}/bin:${PATH}

echo "using MMS instance $MMS_INST"
echo "using MMS software $MMS_HOME"