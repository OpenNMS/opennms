-- Delete all depecrated events in the database
DELETE
FROM
    events
WHERE
    eventuei = 'uei.opennms.org/internal/capsd/updateServer' OR
    eventuei = 'uei.opennms.org/internal/capsd/updateService' OR
    eventuei = 'uei.opennms.org/internal/capsd/changeService' OR
    eventuei = 'uei.opennms.org/nodes/interfaceIndexChanged' OR
    eventuei = 'uei.opennms.org/internal/capsd/interfaceSupportsSNMP' OR
    eventuei = 'uei.opennms.org/internal/capsd/duplicateIPAddress' OR
    eventuei = 'uei.opennms.org/nodes/interfaceIPHostNameChanged' OR
    eventuei = 'uei.opennms.org/nodes/thresholdingFailed' OR
    eventuei = 'uei.opennms.org/nodes/thresholdingSucceeded' OR
    eventuei = 'uei.opennms.org/internal/capsd/snmpConflictsWithDb' OR
    eventuei = 'uei.opennms.org/internal/capsd/rescanCompleted' OR
    eventuei = 'uei.opennms.org/internal/capsd/suspectScanCompleted' OR
    eventuei = 'uei.opennms.org/internal/unknownServiceStatus' OR
    eventuei = 'uei.opennms.org/vulnscand/specificVulnerabilityScan' OR
    eventuei = 'uei.opennms.org/internal/linkd/dataLinkFailed' OR
    eventuei = 'uei.opennms.org/internal/linkd/dataLinkRestored' OR
    eventuei = 'uei.opennms.org/internal/linkd/dataLinkUnmanaged' OR
    eventuei = 'uei.opennms.org/ackd/acknowledgment';

-- Rename unique event identifier
UPDATE
    events
SET
    eventuei = 'uei.opennms.org/internal/discovery/pause'
WHERE
    eventuei = 'uei.opennms.org/internal/capsd/discPause';

UPDATE
    events
SET
    eventuei = 'uei.opennms.org/internal/discovery/resume'
WHERE
    eventuei = 'uei.opennms.org/internal/capsd/discResume';

UPDATE
    events
SET
    eventuei = 'uei.opennms.org/internal/provisiond/addNode'
WHERE
    eventuei = 'uei.opennms.org/internal/capsd/addNode';

UPDATE
    events
SET
    eventuei = 'uei.opennms.org/internal/provisiond/deleteInterface'
WHERE
    eventuei = 'uei.opennms.org/internal/capsd/deleteInterface';

UPDATE
    events
SET
    eventuei = 'uei.opennms.org/internal/provisiond/forceRescan'
WHERE
    eventuei = 'uei.opennms.org/internal/capsd/forceRescan';
