#!/bin/bash

REST_USER=admin
REST_PASSWD=admin

doCurl()
{
   #echo curl "$@" 1>&2
   curl "$@"
}

createForeignSource()
{
    local baseUrl=$1
    local foreignSource=$2

    local req=/tmp/provision.request.$$

    cat <<EOF > $req
<foreign-source xmlns="http://xmlns.opennms.org/xsd/config/foreign-source" name="$foreignSource">
    <scan-interval>52w</scan-interval>
    <detectors>
        <detector name="SNMP" class="org.opennms.netmgt.provision.detector.snmp.SnmpDetector" />
    </detectors>
</foreign-source>
EOF

    doCurl --user ${REST_USER}:${REST_PASSWD} -sSf -d @${req} -X POST -H 'Content-type: application/xml' ${baseUrl}/foreignSources -o /dev/null

    local RET=$?

    rm -f $req

    return $RET

}

createEmptyRequisition()
{
    local baseUrl=$1
    local foreignSource=$2

    local req=/tmp/provision.request.$$


    cat <<EOF > $req
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<model-import foreign-source="${foreignSource}" />
EOF

    doCurl --user ${REST_USER}:${REST_PASSWD} -sSf -d @${req} -X POST -H 'Content-type: application/xml' ${baseUrl}/requisitions -o /dev/null

    local RET=$?

    rm -f $req

    return $RET

}


createRequisitionWithOneNode()
{
    local baseUrl=$1
    local foreignSource=$2
    local foreignId=$3
    local nodeLabel=$4
    local ip=$5

    local req=/tmp/provision.request.$$

    cat <<EOF > $req
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<model-import foreign-source="${foreignSource}">
    <node node-label="${nodeLabel}" foreign-id="${foreignId}">
        <interface status="1" snmp-primary="P" ip-addr="${ip}" descr="vmnet8">
            <monitored-service service-name="ICMP"/>
        </interface>
    </node>
</model-import>
EOF

    doCurl --user ${REST_USER}:${REST_PASSWD} -sSf -d @${req} -X POST -H 'Content-type: application/xml' ${baseUrl}/requisitions -o /dev/null

    local RET=$?

    rm -f $req

    return $RET
}

addNodeToRequisition()
{
    local baseUrl=$1
    local foreignSource=$2
    local baseUrl=$1
    local foreignSource=$2
    local foreignId=$3
    local nodeLabel=$4
    local ip=$5

    local req=/tmp/provision.request.$$

    cat <<EOF > $req
    <node node-label="${nodeLabel}" foreign-id="${foreignId}">
        <interface status="1" snmp-primary="P" ip-addr="${ip}" descr="vmnet8">
            <monitored-service service-name="ICMP"/>
        </interface>
    </node>
EOF

    doCurl --user ${REST_USER}:${REST_PASSWD} -sSf -d @${req} -X POST -H 'Content-type: application/xml' "${baseUrl}/requisitions/${foreignSource}/nodes" -o /dev/null

    local RET=$?

    rm -f $req

    return $RET
}

deleteNodeFromRequisition()
{
    local baseUrl=$1
    local foreignSource=$2
    local foreignId=$3

    doCurl --user ${REST_USER}:${REST_PASSWD} -sSf -X DELETE "${baseUrl}/requisitions/${foreignSource}/nodes/${foreignId}" -o /dev/null
    
}

synchRequisition()
{
    local baseUrl=$1
    local foreignSource=$2

    doCurl --user ${REST_USER}:${REST_PASSWD} -sSf -X PUT "${baseUrl}/requisitions/${foreignSource}/import?rescanExisting=false" -o /dev/null

}

getRequisition()
{
    local baseUrl=$1
    local foreignSource=$2

    doCurl --user ${REST_USER}:${REST_PASSWD} -sSf -X GET "${baseUrl}/requisitions/${foreignSource}"
}

getSnmpConfig()
{
    local baseUrl=$1
    local ip=$2

    doCurl --user ${REST_USER}:${REST_PASSWD} -sSf -X GET "${baseUrl}/snmpConfig/${ip}"
}

setSnmpConfig()
{
    local baseUrl=$1
    local ip=$2
    shift
    shift

    # This remaings args are expected to be of the parm=value
    # valid parms are community, timeout, version, port, retries
    local form_parms=""
    for parm in "$@"; do
	form_parms="${form_parms} -d ${parm}"
    done

    doCurl --user ${REST_USER}:${REST_PASSWD} -sSf -X PUT $form_parms "${baseUrl}/snmpConfig/${ip}"
    
}
