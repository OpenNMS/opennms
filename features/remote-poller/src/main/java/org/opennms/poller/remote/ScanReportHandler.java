package org.opennms.poller.remote;

import org.opennms.netmgt.model.ScanReport;

public interface ScanReportHandler {
    public void scanComplete(ScanReport report);
}
