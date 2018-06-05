#!/bin/bash -e
# =====================================================================
# Cleanup script to delete and rename existing events in the OpenNMS.
# The issue with the cleaned up events is in NMS-9207.
#
# =====================================================================

# Initialize with useful default values
PSQL_BIN=/usr/bin/psql
PSQL_SCRIPT=./NMS-9303-cleanup-deprecated-events.sql

PSQL_HOST=localhost
PSQL_PORT=5432

OPENNMS_DBNAME=opennms
OPENNMS_DBUSER=opennms

ANSWER=NO

# Error codes
E_PSQL_NOT_FOUND=1
E_PSQL_SCRIPT_NOT_FOUND=2
E_ABORT=3
E_ILLEGAL_ARGS=126

# Check if the psql client binary and script file exist, if not exit with error
if [ ! -f ${PSQL_BIN} ]; then
    echo "The PostgreSQL client is required to execute the maintenance script and could not be found on your disk in ${PSQL_BIN}."
    echo "Please check if the PostgreSQL client is installed and where they are located."
    echo "Change the PSQL_BIN variable according to the location of the psql command on your disk and run the script again."
    exit ${E_PSQL_NOT_FOUND}
fi

if [ ! -f ${PSQL_SCRIPT} ]; then
    echo "The SQL script to execute the maintenance task could not be found on your disk in ${PSQL_SCRIPT}."
    echo "Please verify if you run the maintenance script from the same directory as the SQL script file."
    exit ${E_PSQL_SCRIPT_NOT_FOUND}
fi

usage() {
    echo ""
    echo "Script to run a SQL maintenance task to cleanup deprecated and renamed OpenNMS unique event identifiers (uei)."
    echo "With OpenNMS Horizon 20 deprecated events are removed from the system."
    echo "This script will rename the existing event UEIs in your database or delete the events which are not used with Horizon 20 and later."
    echo ""
    echo "  -h: Host name or IP of your PostgreSQL database, default is \"localhost\""
    echo "  -p: PostgreSQL port, default is \"5432\""
    echo "  -d: Database name used by OpenNMS, default is \"opennms\""
    echo "  -u: Database user name for OpenNMS database, default is \"opennms\""
    echo ""
}

# Evaluate arguments for build script.
while getopts h:p:d:u:w flag; do
    case ${flag} in
        h)
            PSQL_HOST="${OPTARG}"
            ;;
        p)
            PSQL_PORT="${OPTARG}"
            ;;
        d)
            OPENNMS_DBNAME="${OPTARG}"
            ;;
        u)
            OPENNMS_DBUSER="${OPTARG}"
            ;;
        *)
            usage
            exit ${E_ILLEGAL_ARGS}
            ;;
    esac
done

echo "The following command will be executed:"
echo ""
echo "  ${PSQL_BIN} -h ${PSQL_HOST} -p ${PSQL_PORT} -d ${OPENNMS_DBNAME} -a -f ${PSQL_SCRIPT}"
echo ""
echo "You will be asked for the OpenNMS database password for the user ${OPENNMS_DBUSER}."
echo ""

read -p "Is this OK? Answer with YES or [NO]. " ANSWER

if [ "YES" = "${ANSWER}" ]; then
    echo ""
    echo "--- Maintenance script start ---"
    echo ""
    ${PSQL_BIN} -h ${PSQL_HOST} -p ${PSQL_PORT} -U ${OPENNMS_DBUSER} -d ${OPENNMS_DBNAME} -W -a -f ${PSQL_SCRIPT}
    echo ""
    echo "--- Maintenance script end   ---"
else
    echo ""
    echo "Maintenance script cancelled by the user."
    echo ""
    exit ${E_ABORT}
fi
