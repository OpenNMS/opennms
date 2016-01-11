package org.opennms.poller.remote;

import org.opennms.netmgt.poller.remote.support.ScanReport;

public interface ScanReportHandler {
    public void scanComplete(ScanReport report);
}
