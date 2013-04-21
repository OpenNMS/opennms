#!/bin/bash

. ./provision.sh

export BASE_URL=http://localhost:8980/opennms/rest
export PROV_GROUP=testGroup



shuSetUp() 
{
    # verify that the server is up before we run tests
    curl --user ${REST_USER}:${REST_PASSWD} -sSf -X GET ${BASE_URL}/requisitions -o /dev/null
    shuAssert "Expected server to respond" $?
}

shuTearDown() 
{
    rm -rf /tmp/provisionTest.data.*
}

shuAssertStringEquals()
{
    test "x${1}" = "x${2}"
    shuAssert "Expected '${1}' but was '${2}'" $? 
}

shuAssertRequisitionExists()
{
    local result=/tmp/provisionTest.data.assertReqExists.$$
    local foreignSource=$1

    curl --user ${REST_USER}:${REST_PASSWD} -sSf -X GET ${BASE_URL}/requisitions > ${result}
    shuAssert "Failed to get current requisitions" $?

    perl xpath.pl ${result} "//model-import[@foreign-source='$foreignSource']" > /dev/null
    shuAssert "Requisition not created" $?

}

shuAssertNodeInRequisition()
{
    local result=/tmp/provisionTest.data.assertNodeInRequisition.$$
    local foreignSource=$1
    local foreignId=$2

    curl --user ${REST_USER}:${REST_PASSWD} -sSf -X GET ${BASE_URL}/requisitions > ${result}
    shuAssert "Failed to get current requisitions" $?

    perl xpath.pl ${result} "//model-import[@foreign-source='$foreignSource']" > /dev/null
    shuAssert "Requisition ${foreignSource} doesn't exist" $?

    perl xpath.pl ${result} "//model-import[@foreign-source='$foreignSource']/node[@foreign-id='$foreignId']" > /dev/null
    shuAssert "Node ${foreignId} not in requisistion ${foreignSource}" $?
}

shuAssertRequisitionNodeCount()
{
    local result=/tmp/provisionTest.data.assertRequisitionNodeCount.$$
    local foreignSource=$1
    local expectedCount=$2

    curl --user ${REST_USER}:${REST_PASSWD} -sSf -X GET ${BASE_URL}/requisitions/${foreignSource}/nodes > ${result}
    shuAssert "Failed to get current requisitions" $?

    perl xpath.pl ${result} "//nodes[@count='${expectedCount}']" > /dev/null
    shuAssert "Unexpected count of nodes in requisition ${foreignSource}" $?

}

shuDenyNodeInRequisition()
{
    local result=/tmp/provisionTest.data.assertNodeNotInRequisition.$$
    local foreignSource=$1
    local foreignId=$2

    curl --user ${REST_USER}:${REST_PASSWD} -sSf -X GET ${BASE_URL}/requisitions > ${result}
    shuAssert "Failed to get current requisitions" $?

    perl xpath.pl ${result} "//model-import[@foreign-source='$foreignSource']" > /dev/null
    shuAssert "Requisition ${foreignSource} doesn't exist" $?

    perl xpath.pl ${result} "//model-import[@foreign-source='$foreignSource']/node[@foreign-id='$foreignId']" > /dev/null
    shuDeny "Node ${foreignId} should not be in requisistion ${foreignSource}" $?
}

shuAssertRequisitionEmpty()
{
    local result=/tmp/provisionTest.data.assertRequisitionEmpty.$$
    local foreignSource=$1

    curl --user ${REST_USER}:${REST_PASSWD} -sSf -X GET ${BASE_URL}/requisitions > ${result}
    shuAssert "Failed to get current requisitions" $?

    perl xpath.pl ${result} "//model-import[@foreign-source='$foreignSource']" > /dev/null
    shuAssert "Requisition ${foreignSource} doesn't exist" $?

    perl xpath.pl ${result} "//model-import[@foreign-source='$foreignSource']/node" > /dev/null
    shuDeny "Requisition ${foreignSource} should be empty but isn't" $?
}

isNodeInDB()
{
    local foreignSource=$1
    local foreignId=$2
    local result=/tmp/provisionTest.data.isNodeInDB.$$

    curl --user ${REST_USER}:${REST_PASSWD} -sSf -X GET "${BASE_URL}/nodes?foreignSource=${foreignSource}&foreignId=${foreignId}" > ${result}
    shuAssert "Failure querying database for ${foreignSource}:${foreignId}" $?

    perl xpath.pl ${result} "/nodes/node[@foreignSource='${foreignSource}' and @foreignId='${foreignId}']" >/dev/null
    local RET=$?

    rm $result

    return $RET
}

shuAssertNodeInDB()
{
    local foreignSource=$1
    local foreignId=$2

    isNodeInDB $foreignSource $foreignId
    shuAssert "Expected to find node ${foreignSource}:${foreignId} in database" $?
    
}

shuDenyNodeInDB()
{
    local foreignSource=$1
    local foreignId=$2

    isNodeInDB $foreignSource $foreignId
    shuDeny "Expected NOT to find node ${foreignSource}:${foreignId} in database" $?
}

TestCreateEmptyRequisition()
{
    local req=/tmp/provisionTest.data.req.$$
    local result=/tmp/provionTest.data.result.$$
    local foreignSource=$PROV_GROUP

    createEmptyRequisition ${BASE_URL} ${foreignSource}
    shuAssert "Unexpected failure creating requistion" $?

    shuAssertRequisitionEmpty $foreignSource

}

TestCreateRequisitionWithOneNode()
{
    local req=/tmp/provisionTest.data.req.$$
    local result=/tmp/provionTest.data.result.$$
    local foreignSource=$PROV_GROUP
    local foreignId=19
    local nodeLabel=uzbekistan
    local ip=192.168.39.1

    createRequisitionWithOneNode ${BASE_URL} ${foreignSource} ${foreignId} ${nodeLabel} ${ip}
    shuAssert "Unexpected failure creating requistion" $?

    shuAssertRequisitionExists $foreignSource

    shuAssertNodeInRequisition $foreignSource $foreignId

}

TestAddNodeToRequisition()
{
    local req=/tmp/provisionTest.data.req.$$
    local result=/tmp/provionTest.data.result.$$
    local foreignSource=$PROV_GROUP
    local foreignId=21

    # Create requisition
    createEmptyRequisition ${BASE_URL} ${foreignSource}
    shuAssert "0: Unexpected failure creating requistion" $?

    shuAssertRequisitionExists ${foreignSource}

    # add a node
    addNodeToRequisition ${BASE_URL} ${foreignSource} 1 node1 1.1.1.1
    shuAssert "1: Unexpected failure adding node1 to requisition" $?

    shuAssertNodeInRequisition ${foreignSource} 1
    shuDenyNodeInRequisition ${foreignSource} 2

    # and node2
    addNodeToRequisition ${BASE_URL} ${foreignSource} 2 node2 2.2.2.2
    shuAssert "2: Unexpected failure adding node1 to requisition" $?

    shuAssertNodeInRequisition ${foreignSource} 1
    shuAssertNodeInRequisition ${foreignSource} 2

    # remove node 1
    deleteNodeFromRequisition ${BASE_URL} ${foreignSource} 1
    shuAssert "3: Unexpected failure deleting node from requisition" $?

    shuDenyNodeInRequisition ${foreignSource} 1
    shuAssertNodeInRequisition ${foreignSource} 2

    # remove node 2
    deleteNodeFromRequisition ${BASE_URL} ${foreignSource} 2
    shuAssert "4: Unexpected failure deleting node from requisition" $?

    shuDenyNodeInRequisition ${foreignSource} 1
    shuDenyNodeInRequisition ${foreignSource} 2

    shuAssertRequisitionEmpty ${foreignSource}
    
}

TestAddNodeWithDuplicatedForeignIdToRequisition()
{
    local req=/tmp/provisionTest.data.req.$$
    local result=/tmp/provionTest.data.result.$$
    local foreignSource=$PROV_GROUP
    local foreignId=19
    local nodeLabel=uzbekistan
    local ip=192.168.39.1

    createRequisitionWithOneNode ${BASE_URL} ${foreignSource} ${foreignId} ${nodeLabel} ${ip}
    shuAssert "Unexpected failure creating requistion" $?

    shuAssertRequisitionExists $foreignSource

    shuAssertNodeInRequisition $foreignSource $foreignId

    shuAssertRequisitionNodeCount $foreignSource 1

    # add a node
    addNodeToRequisition ${BASE_URL} ${foreignSource} ${foreignId} node1 1.1.1.1
    shuAssert "Unexpected failure adding node1 to requisition" $?

    shuAssertRequisitionNodeCount $foreignSource 1
}

TestDeleteNodeFromRequisition()
{
    local req=/tmp/provisionTest.data.req.$$
    local result=/tmp/provionTest.data.result.$$
    local foreignSource=$PROV_GROUP
    local foreignId=21

    createRequisitionWithOneNode ${BASE_URL} ${foreignSource} ${foreignId} testNode 1.1.1.1
    shuAssert "Unexpected failure creating requistion" $?

    shuAssertNodeInRequisition ${foreignSource} ${foreignId}

    deleteNodeFromRequisition ${BASE_URL} ${foreignSource} ${foreignId}
    shuAssert "Unexpected failure deleting node from requisition" $?

    shuDenyNodeInRequisition ${foreignSource} ${foreignId}

    shuAssertRequisitionEmpty ${foreignSource}
}

TestSyncRequisition()
{
    local req=/tmp/provisionTest.data.req.$$
    local result=/tmp/provionTest.data.result.$$
    local foreignSource=$PROV_GROUP
    local foreignId=55
    local nodeLabel=uzbekistan
    local ip=192.168.39.1
    
    createEmptyRequisition ${BASE_URL} ${foreignSource}
    shuAssert "Unexpected failure createing requisition ${foreignSource}" $?

    # make sure all nodes that already exist for the requisition are deleted
    synchRequisition ${BASE_URL} ${foreignSource}

    sleep 5

    shuDenyNodeInDB ${foreignSource} ${foreignId}

    addNodeToRequisition ${BASE_URL} ${foreignSource} ${foreignId} ${nodeLabel} ${ip}

    synchRequisition ${BASE_URL} ${foreignSource}

    sleep 5

    shuAssertNodeInDB ${foreignSource} ${foreignId}

    deleteNodeFromRequisition ${BASE_URL} ${foreignSource} ${foreignId}

    synchRequisition ${BASE_URL} ${foreignSource}

    sleep 5

    shuDenyNodeInDB ${foreignSource} ${foreignId}

}

shuAssertCommunityEquals()
{
    local config=$1
    local expectedCommunity=$2

    local community=$(perl xpath.pl ${config} "/snmp-info/community" | sed 's: *<community>\(.*\)</community>:\1:')
    shuAssert "Unable to find community string in configuration" $?
    
    shuAssertStringEquals "$community" "$expectedCommunity"
}

shuAssertTimeoutEquals()
{
    local config=$1
    local expectedTimeout=$2

    local timeout=$(perl xpath.pl ${config} "/snmp-info/timeout" | sed 's: *<timeout>\(.*\)</timeout>:\1:')
    shuAssert "Unable to find port in configuration" $?

    shuAssertStringEquals "$timeout" "$expectedTimeout"
}    

TestGetSnmpConfig()
{
    local config=/tmp/provisionTest.data.req.$$
    local ip=192.168.39.1
    local expectedCommunity=public
    local expectedTimeout=10000

    getSnmpConfig ${BASE_URL} ${ip}  > ${config}
    shuAssert "Unexpected failure getting snmp config data for ${ip}" $?

    shuAssertCommunityEquals ${config} ${expectedCommunity}
    shuAssertTimeoutEquals ${config} ${expectedTimeout}

}


TestSetSnmpConfig()
{
    local config=/tmp/provisionTest.data.req.$$
    local ip=192.168.39.1
    local expectedCommunity=newcommunity
    local expectedTimeout=1999

    setSnmpConfig ${BASE_URL} ${ip} community=${expectedCommunity} timeout=${expectedTimeout}
    shuAssert "Unexpected failure seting snmp config data for ${ip}" $?

    getSnmpConfig ${BASE_URL} ${ip}  > ${config}
    shuAssert "Unexpected failure getting snmp config data for ${ip}" $?

    shuAssertCommunityEquals ${config} ${expectedCommunity}
    shuAssertTimeoutEquals ${config} ${expectedTimeout}

    setSnmpConfig ${BASE_URL} ${ip} community=public timeout=1800
    shuAssert "Unexpected failure resetting snmp config data for ${ip}: $(cat ${config})" $?

    getSnmpConfig ${BASE_URL} ${ip}  > ${config}
    shuAssert "Unexpected failure getting snmp config data for ${ip}: $(cat ${config})" $?

    shuAssertCommunityEquals ${config} public
    shuAssertTimeoutEquals ${config} 1800

}

PassedInTestSuite()
{
    for t in "$@"
    do
	shuRegTest "$t"
    done
}

. ./shunit

if [ $# -ne 0 ]
then
    shuStart "PassedInTestSuite $@"
else
    shuStart
fi

