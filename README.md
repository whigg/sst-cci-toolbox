# ESA SST-CCI Toolbox

## Overview

To be completed.

## Setting up the developer environment on CEMS

1. Append the following snippet to your '.bash_profile'

>
  export JAVA_HOME='/usr/java/jdk1.7.0_51'
  export PATH=$HOME/bin:$JAVA_HOME/bin:$PATH
  # To Access HTTP or HTTPS you need to go through the RAL site web
  # proxies. How this is configured varies with the application you
  # are using to access the web but commonly you need to set
  export http_proxy=wwwcache.rl.ac.uk:8080
  export https_proxy=wwwcache.rl.ac.uk:8080

2. Include the following snippet in your '.m2/settings.xml' file and adapt the username

>
<settings>
  <localRepository>/data/mboettcher/mms/m2/repository/</localRepository>
  <servers>
    <server>
      <id>bc-mvn-repo-public</id>
      <username>maven</username>
      <!-- the BC maven private key is only needed for deployment -->
      <privateKey>/home/<ADAPT USERNAME>/.m2/id_rsa</privateKey>
      <passphrase><ADAPT PASSPHRASE></passphrase>
      -->
      <filePermissions>664</filePermissions>
      <directoryPermissions>775</directoryPermissions>
    </server>
    <server>
      <id>bc-mvn-repo-closed</id>
      <username>maven-cs</username>
      <!-- the BC maven private key -->
      <privateKey>/home/<ADAPT USERNAME>/.m2/id_rsa</privateKey>
      <passphrase><ADAPT PASSPHRASE></passphrase>
      -->
      <filePermissions>664</filePermissions>
      <directoryPermissions>775</directoryPermissions>
    </server>
  </servers>
  <proxies>
    <proxy>
      <active>true</active>
      <protocol>http</protocol>
      <host>wwwcache.rl.ac.uk</host>
      <port>8080</port>
      <!--
      <username>proxyuser</username>
      <password>somepassword</password>
      <nonProxyHosts>www.google.com|*.somewhere.com</nonProxyHosts>
      -->
    </proxy>
    <proxy>
      <active>true</active>
      <protocol>https</protocol>
      <host>wwwcache.rl.ac.uk</host>
      <port>8080</port>
      <!--
      <username>proxyuser</username>
      <password>somepassword</password>
      <nonProxyHosts>www.google.com|*.somewhere.com</nonProxyHosts>
      -->
    </proxy>
  </proxies>
  <profiles>
    <profile>
      <id>compiler</id>
        <properties>
          <java.home>/usr/java/jdk1.7.0_51</java.home>
        </properties>
    </profile>
  </profiles>
  <activeProfiles>
    <activeProfile>compiler</activeProfile>
  </activeProfiles>
</settings>


## How to install the SST-CCI software?

1. Login to mms1 virtual machine
2. Change your working directory
>
   cd /group_workspaces/cems2/esacci_sst/mms/github/sst-cci-toolbox
 
3. Build the software. Type 
>
   git pull
   mvn clean package install
   
4. Make assemblies
>
   mvn -f mms/pom.xml assembly:assembly
   mvn -f tools/pom.xml assembly:assembly
   
5. Move the assembly to the installation location. Type
>
   mv mms/target/sst-cci-mms-${project.version}-bin/sst-cci-mms-${project.version} /group_workspaces/cems2/esacci_sst/mms/software
   
6. Change file modes. Type
>
   chmod -R ug+X /group_workspaces/cems2/esacci_sst/mms/software/sst-cci-mms-${project.version}


## How to update the SST-CCI software?

1. Login to mms1 virtual machine
2. Execute the 'mymmsinstall' script in
>
  /group_workspaces/cems2/esacci_sst/mms/software/sst-cci-mms-${project.version}/bin
  

## How to produce a set of MMD files?

1. Login to lotus frontend
2. Change your working directory
>
   cd /group_workspaces/cems2/esacci_sst/mms/inst
3. Create a new directory for your usecase and change to the new directory
>
   mkdir mms11a
   cd mms11a
4. Source the 'mymms' settings
>
    . ../../software/sst-cci-mms-2.0-SNAPSHOT/bin/mymms
5. Prepare or clean the directory structure 
>
   pmclean mms11a
6. Start the Python script for your usecase
>
    pmstartup mms11a.py # start jobs
    bjobs -w            # inspect lotus job list
    watch cat mms11a.status # watch pmonitor status
7. For inspecting job message files inspect the '*.out' files in the 'log' directory
8. For inspecting job log files inspect the '*.err' files in the 'log' directory
9. For inspecting output of shell scripts inspect the '*.out' files in the 'trace' directory

## Contact information

* Ralf Quast (ralf.quast@brockmann-consult.de)
