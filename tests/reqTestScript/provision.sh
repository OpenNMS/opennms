#!/bin/bash

createEmptyRequisition()
{
    local baseUrl=$1
    local foreignSource=$2

    local req=/tmp/provision.request.$$


    cat <<EOF > $req
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<model-import foreign-source="${foreignSource}" />
EOF

    curl --user admin:admin -sSf -d @${req} -X POST -H 'Content-type: application/xml' ${baseUrl}/requisitions -o /dev/null

    local RET=$?

    rm -f $req

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
            <monitored-service service-name="SNMP"/>
            <monitored-service service-name="ICMP"/>
        </interface>
    </node>
</model-import>
EOF

    curl --user admin:admin -sSf -d @${req} -X POST -H 'Content-type: application/xml' ${baseUrl}/requisitions -o /dev/null

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
            <monitored-service service-name="SNMP"/>
            <monitored-service service-name="ICMP"/>
        </interface>
    </node>
EOF

    curl --user admin:admin -sSf -d @${req} -X POST -H 'Content-type: application/xml' "${baseUrl}/requisitions/${foreignSource}/nodes" -o /dev/null

    local RET=$?

    rm -f $req

    return $RET
}

deleteNodeFromRequisition()
{
    local baseUrl=$1
    local foreignSource=$2
    local foreignId=$3

    curl --user admin:admin -sSf -X DELETE "${baseUrl}/requisitions/${foreignSource}/nodes/${foreignId}" -o /dev/null
    
}

synchRequisition()
{
    local baseUrl=$1
    local foreignSource=$2

    curl --user admin:admin -sSf -X PUT "${baseUrl}/requisitions/${foreignSource}/import" -o /dev/null

}
