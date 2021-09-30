-- Delete all depecrated events in the database
DELETE
FROM
    events
WHERE
    eventuei = 'uei.opennms.org/internal/control/status' OR
    eventuei = 'uei.opennms.org/internal/control/start' OR
    eventuei = 'uei.opennms.org/internal/control/pause' OR
    eventuei = 'uei.opennms.org/internal/control/resume' OR
    eventuei = 'uei.opennms.org/internal/control/stop' OR
    eventuei = 'uei.opennms.org/internal/control/startPending' OR
    eventuei = 'uei.opennms.org/internal/control/starting' OR
    eventuei = 'uei.opennms.org/internal/control/pausePending' OR
    eventuei = 'uei.opennms.org/internal/control/paused' OR
    eventuei = 'uei.opennms.org/internal/control/resumePending' OR
    eventuei = 'uei.opennms.org/internal/control/running' OR
    eventuei = 'uei.opennms.org/internal/control/stopPending' OR
    eventuei = 'uei.opennms.org/internal/control/stopped' OR
    eventuei = 'uei.opennms.org/internal/control/error' OR
    eventuei = 'uei.opennms.org/internal/capsd/updateServer' OR
    eventuei = 'uei.opennms.org/internal/capsd/updateService' OR
    eventuei = 'uei.opennms.org/internal/capsd/addInterface' OR
    eventuei = 'uei.opennms.org/internal/capsd/changeService' OR
    eventuei = 'uei.opennms.org/nodes/restartPollingInterface' OR
    eventuei = 'uei.opennms.org/nodes/interfaceIndexChanged' OR
    eventuei = 'uei.opennms.org/internal/capsd/interfaceSupportsSNMP' OR
    eventuei = 'uei.opennms.org/internal/capsd/duplicateIPAddress' OR
    eventuei = 'uei.opennms.org/nodes/interfaceIPHostNameChanged' OR
    eventuei = 'uei.opennms.org/nodes/nodeLabelSourceChanged' OR
    eventuei = 'uei.opennms.org/nodes/thresholdingFailed' OR
    eventuei = 'uei.opennms.org/nodes/thresholdingSucceeded' OR
    eventuei = 'uei.opennms.org/internal/capsd/snmpConflictsWithDb' OR
    eventuei = 'uei.opennms.org/internal/capsd/suspectScanCompleted' OR
    eventuei = 'uei.opennms.org/internal/unknownServiceStatus' OR
    eventuei = 'uei.opennms.org/vulnscand/specificVulnerabilityScan' OR
    eventuei = 'uei.opennms.org/remote/configurationChangeDetected' OR
    eventuei = 'uei.opennms.org/internal/linkd/dataLinkFailed' OR
    eventuei = 'uei.opennms.org/internal/linkd/dataLinkRestored' OR
    eventuei = 'uei.opennms.org/internal/linkd/dataLinkUnmanaged' OR
    eventuei = 'uei.opennms.org/ackd/acknowledgment';
