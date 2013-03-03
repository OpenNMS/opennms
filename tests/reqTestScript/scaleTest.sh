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

addSampleNodesToRequistion()
{
    local foreignSource=$1
    local foreignIdPrefix=$2
    local -i count=$3
    local -i octet1=${4:-10}
    local -i octet2=${5:-1}
    local -i octet3=${6:-1}
    local -i octet4=${7:-1}

    local -i index=0
    local -i start=$(date "+%s")

    while [ $index -lt $count ] && [ $octet1 -lt 254 ]; do
	while [ $index -lt $count ] && [ $octet2 -lt 254 ]; do
	    while [ $index -lt $count ] && [ $octet3 -lt 254 ]; do
		while [ $index -lt $count ] && [ $octet4 -lt 254 ]; do
		    index=index+1
		    addNodeToRequisition ${BASE_URL} ${foreignSource} "${foreignIdPrefix}-${index}" "${foreignIdPrefix}-${index}" "${octet1}.${octet2}.${octet3}.${octet4}"
		    shuAssert "${index}: Unexpected failure adding node${index} to requisition" $?
		    octet4=octet4+1
		done
		octet4=1
		octet3=octet3+1
	    done
	    octet3=1
	    octet2=octet2+1
	done
	octet2=1
	octet1=octet1+1
    done

    synchRequisition ${BASE_URL} ${foreignSource} > /dev/null
    shuAssert "1: Failure synching requistion" $?

    local -i end=$(date "+%s")
    local -i elapsed=end-start
  
    echo
    echo "Added Batch ${foreignIdPrefix} - Elapsed Time = $elapsed s"
    

    
}

TestRequisition26000Nodes()
{
    local req=/tmp/provisionTest.data.req.$$
    local result=/tmp/provionTest.data.result.$$
    local foreignSource=$PROV_GROUP
    local foreignId=21

    createForeignSource ${BASE_URL} ${foreignSource}
    shuAssert "0: Unexpected failure creating foreignSource" $?

    # Create requisition
    createEmptyRequisition ${BASE_URL} ${foreignSource}
    shuAssert "1: Unexpected failure creating requistion" $?

    local -i batchNumber=1
    echo

    for batch in A B C D E F G H I J K L M N O P Q R S T U V W X Y Z; do
	addSampleNodesToRequistion ${foreignSource} ${batch} 1000 10 ${batchNumber}
	batchNumber=batchNumber+1
    done

#    for batch in A B C; do
#	addSampleNodesToRequistion ${foreignSource} ${batch} 1000 10 ${batchNumber}
#	batchNumber=batchNumber+1
#    done

    local -i start=$(date "+%s")
    getRequisition ${BASE_URL} ${foreignSource} > /dev/null
    local -i end=$(date "+%s")
    local -i elapsed=end-start

    echo
    echo
    echo "time to fetch req=$elapsed"
    
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

