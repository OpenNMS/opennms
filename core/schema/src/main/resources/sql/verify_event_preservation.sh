#!/bin/bash

# Verification script to check that all XML events are preserved in SQL files
# This ensures the eventconf_events.sql file accurately reflects the XML source files
#
# Usage: Run from opennms root directory
#   ./core/schema/src/main/resources/sql/verify_event_preservation.sh

SQL_FILE="core/schema/src/main/resources/sql/eventconf_events.sql"
SOURCES_SQL="core/schema/src/main/resources/sql/eventconf_sources.sql"
XML_DIR="opennms-base-assembly/src/main/filtered/etc/examples/events-defaults"

echo "================================================================================"
echo "Event Preservation Verification Script"
echo "================================================================================"
echo "Verifies that all XML events are present in SQL initialization files"
echo ""

# Check if we're in the right directory
if [ ! -f "$SQL_FILE" ]; then
    echo "Error: $SQL_FILE not found"
    echo "Please run this script from the opennms root directory"
    exit 1
fi

total_xml_events=0
total_found=0
total_missing=0

check_file() {
    local source_id=$1
    local filename=$2

    xml_file="$XML_DIR/${filename}.xml"

    if [ ! -f "$xml_file" ]; then
        echo "⚠️  XML file not found: ${filename}.xml"
        return
    fi

    echo "📄 ${filename}.xml (source_id=$source_id)"
    echo "--------------------------------------------------------------------------------"

    # Extract UEIs from XML file
    ueis=$(sed -n 's/.*<uei>\([^<]*\)<\/uei>.*/\1/p' "$xml_file")

    if [ -z "$ueis" ]; then
        echo "  ⚠️  No UEIs found in XML file"
        return
    fi

    file_total=0
    file_found=0
    file_missing=0
    missing_list=""

    while IFS= read -r uei; do
        if [ -z "$uei" ]; then
            continue
        fi

        ((file_total++))
        ((total_xml_events++))

        # Check if UEI exists in SQL with this source_id
        # Pattern: VALUES (id, source_id, 'uei', ...
        if grep -q "VALUES ([0-9]*, $source_id, '$uei'" "$SQL_FILE" 2>/dev/null; then
            ((file_found++))
            ((total_found++))
        else
            ((file_missing++))
            ((total_missing++))
            missing_list="${missing_list}     ❌ $uei\n"
        fi
    done <<< "$ueis"

    if [ $file_missing -eq 0 ]; then
        echo "  ✅ All $file_found events preserved"
    else
        echo "  ⚠️  Found: $file_found, Missing: $file_missing"
        echo -e "$missing_list"
    fi
    echo ""
}

# Check each event configuration file
check_file 1 "opennms.snmp.trap.translator.events"
check_file 2 "opennms.ackd.events"
check_file 3 "opennms.alarm.events"
check_file 4 "opennms.bmp.events"
check_file 5 "opennms.bsm.events"
check_file 6 "opennms.capsd.events"
check_file 7 "opennms.collectd.events"
check_file 8 "opennms.config.events"
check_file 9 "opennms.correlation.events"
check_file 10 "opennms.default.threshold.events"
check_file 11 "opennms.discovery.events"
check_file 12 "opennms.internal.events"
check_file 13 "opennms.linkd.events"
check_file 14 "opennms.mib.events"
check_file 15 "opennms.pollerd.events"
check_file 16 "opennms.provisioning.events"
check_file 17 "opennms.minion.events"
check_file 18 "opennms.perspective.poller.events"
check_file 19 "opennms.reportd.events"
check_file 20 "opennms.syslogd.events"
check_file 21 "opennms.ticketd.events"
check_file 22 "opennms.tl1d.events"
check_file 23 "opennms.catch-all.events"

# Summary
echo "================================================================================"
echo "SUMMARY"
echo "================================================================================"
echo "Total events in XML files: $total_xml_events"
echo "Events preserved in SQL: $total_found ✅"
echo "Events missing from SQL: $total_missing ❌"
echo ""

if [ $total_missing -eq 0 ]; then
    echo "🎉 SUCCESS: All XML events are properly preserved in SQL files!"
    echo ""
    echo "This verifies that the SQL initialization files accurately reflect"
    echo "the XML source files and maintain proper event ordering."
    exit 0
else
    echo "❌ FAILURE: $total_missing events are missing from SQL files."
    echo ""
    echo "The SQL files need to be regenerated to include missing events."
    echo "See EventConfPersistenceService.saveEventsToDatabase() for the generation process."
    exit 1
fi
