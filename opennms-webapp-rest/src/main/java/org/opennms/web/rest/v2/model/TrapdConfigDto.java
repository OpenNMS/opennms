package org.opennms.web.rest.v2.model;

import org.opennms.netmgt.config.trapd.TrapdConfiguration;

public class TrapdConfigDto {
    private String snmpTrapAddress;
    private Integer snmpTrapPort;
    private Boolean newSuspectOnTrap;
    private Boolean includeRawMessage;
    private Integer threads;
    private Integer queueSize;
    private Integer batchSize;
    private Integer batchInterval;
    private Boolean useAddressFromVarbind;

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

    public TrapdConfigDto toDto(final TrapdConfiguration config) {
        TrapdConfigDto dto = new TrapdConfigDto();
        dto.setSnmpTrapAddress(config.getSnmpTrapAddress());
        dto.setSnmpTrapPort(config.getSnmpTrapPort());
        dto.setNewSuspectOnTrap(config.getNewSuspectOnTrap());
        dto.setIncludeRawMessage(config.isIncludeRawMessage());
        dto.setThreads(config.getThreads());
        dto.setQueueSize(config.getQueueSize());
        dto.setBatchSize(config.getBatchSize());
        dto.setBatchInterval(config.getBatchInterval());
        dto.setUseAddressFromVarbind(config.shouldUseAddressFromVarbind());
        return dto;
    }
}
