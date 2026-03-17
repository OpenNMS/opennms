package org.opennms.web.rest.v2.model;

import java.util.List;

import org.opennms.netmgt.config.trapd.Snmpv3User;

public class TrapdConfigPayload {
    private String snmpTrapAddress;
    private Integer snmpTrapPort;
    private Boolean newSuspectOnTrap;
    private Boolean includeRawMessage;
    private Integer threads;
    private Integer queueSize;
    private Integer batchSize;
    private Integer batchInterval;
    private Boolean useAddressFromVarbind;
    private List<Snmpv3User> snmpv3Users;

    public String getSnmpTrapAddress() {
        return snmpTrapAddress;
    }

    public void setSnmpTrapAddress(final String snmpTrapAddress) {
        this.snmpTrapAddress = snmpTrapAddress;
    }

    public Integer getSnmpTrapPort() {
        return snmpTrapPort;
    }

    public void setSnmpTrapPort(final Integer snmpTrapPort) {
        this.snmpTrapPort = snmpTrapPort;
    }

    public Boolean getNewSuspectOnTrap() {
        return newSuspectOnTrap;
    }

    public void setNewSuspectOnTrap(final Boolean newSuspectOnTrap) {
        this.newSuspectOnTrap = newSuspectOnTrap;
    }

    public Boolean getIncludeRawMessage() {
        return includeRawMessage;
    }

    public void setIncludeRawMessage(final Boolean includeRawMessage) {
        this.includeRawMessage = includeRawMessage;
    }

    public Integer getThreads() {
        return threads;
    }

    public void setThreads(final Integer threads) {
        this.threads = threads;
    }

    public Integer getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(final Integer queueSize) {
        this.queueSize = queueSize;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(final Integer batchSize) {
        this.batchSize = batchSize;
    }

    public Integer getBatchInterval() {
        return batchInterval;
    }

    public void setBatchInterval(final Integer batchInterval) {
        this.batchInterval = batchInterval;
    }

    public Boolean getUseAddressFromVarbind() {
        return useAddressFromVarbind;
    }

    public void setUseAddressFromVarbind(final Boolean useAddressFromVarbind) {
        this.useAddressFromVarbind = useAddressFromVarbind;
    }

    public List<Snmpv3User> getSnmpv3Users() {
        return snmpv3Users;
    }

    public void setSnmpv3Users(final List<Snmpv3User> snmpv3Users) {
        this.snmpv3Users = snmpv3Users;
    }
}
