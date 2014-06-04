#!/bin/sh

# THIS CRON WRAPPER SCRIPT IS NOT YET WORKING. PLEASE DO NOT ASK FOR SUPPORT
# FOR THIS SCRIPT UNLESS IT'S TO TELL US THAT YOU GOT IT WORKING AND WOULD LIKE
# TO HAVE YOUR FIXES INCLUDED UPSTREAM :)

OPENNMS_HOME=/opt/opennms

LDAP_HOST=adc.example.com
BIND_DN="CN=OpenNMSbind,OU=Application Users,OU=Example.com Users,DC=example,DC=com"
BIND_PASSWORD="YourLDAPBindUserPasswordHere"
BASE_DN="OU=Employee Users,OU=Example.com Users,DC=example,DC=com"
BASE_FILTER="memberOf=CN=OpenNMS DESIGNATOR_GOES_HERE,OU=Security Groups,OU=Example.com Security Groups,DC=example,DC=com";
GROUP_DESIGNATORS="Users Admins Read Provision"

STAGE_USERS_FILE=${OPENNMS_HOME}/etc/users.xml.stage
FINAL_USERS_FILE=${OPENNMS_HOME}/etc/users.xml
LDAP_SB_PATH=${OPENNMS_HOME}/contrib/ldap_user_saugblaser/ldap_user_saugblaser.pl

echo > ${STAGE_USERS_FILE} <<EOH
<?xml version="1.0" encoding="UTF-8"?>
<userinfo xmlns="http://xmlns.opennms.org/xsd/users">
    <header>
        <rev>.9</rev>
        <created>Thursday, November 3, 2011 9:28:08 PM GMT</created>
        <mstation>master.nmanage.com</mstation>
    </header>
    <users>
        <user>
            <user-id>admin</user-id>
            <full-name>Administrator</full-name>
            <user-comments>Default administrator, do not delete</user-comments>
            <password>01234567890ABCDEFEDCBA9876543210</password>
        </user>
EOH

for groupDesignator in $GROUP_DESIGNATORS ; do
	echo Doing group designator $groupDesignator
	actualFilter=`echo ${BASE_FILTER} | sed s/DESIGNATOR_GOES_HERE/${groupDesignator}/g`
	${LDAP_SB_PATH} ${LDAP_HOST} \'${BIND_DN}\' \'${BIND_PASSWORD}\' \'${BASE_DN}\' "${actualFilter}" # >> ${STAGE_USERS_FILE}
done

echo >> ${STAGE_USERS_FILE} <<EOF
    </users>
</userinfo>
EOF
