/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.events.api.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.opennms.core.utils.ImmutableCollections;
import org.opennms.core.utils.MutableCollections;
import org.opennms.core.utils.StringUtils;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * An immutable implementation of '{@link IEvent}'.
 */
public final class ImmutableEvent implements IEvent {
    private final String uuid;
    private final Integer dbId;
    private final String distPoller;
    private final Date creationTime;
    private final String masterStation;
    private final IMask mask;
    private final String uei;
    private final String source;
    private final Long nodeid;
    private final Date time;
    private final String host;
    private final InetAddress interfaceAddress;
    private final String interfaceString;
    private final String snmpHost;
    private final String service;
    private final ISnmp snmp;
    private final List<IParm> parms;
    private final String descr;
    private final ILogMsg logMsg;
    private final String severity;
    private final String pathOutage;
    private final ICorrelation correlation;
    private final String operInstruct;
    private final List<IAutoAction> autoActionList;
    private final List<IOperAction> operActionList;
    private final IAutoAcknowledge autoAcknowledge;
    private final List<String> logGroupList;
    private final ITticket tTicket;
    private final List<IForward> forwardList;
    private final List<IScript> scriptList;
    private final Integer ifIndex;
    private final String ifAlias;
    private final String mouseOverText;
    private final IAlarmData alarmData;

    private ImmutableEvent(Builder builder) {
        uuid = builder.uuid;
        dbId = builder.dbId;
        distPoller = builder.distPoller;
        creationTime = builder.creationTime;
        masterStation = builder.masterStation;
        mask = ImmutableMask.immutableCopy(builder.mask);
        uei = builder.uei;
        source = builder.source;
        nodeid = builder.nodeid;
        time = builder.time;
        host = builder.host;
        interfaceAddress = builder.interfaceAddress;
        interfaceString = builder.interfaceString;
        snmpHost = builder.snmpHost;
        service = builder.service;
        snmp = ImmutableSnmp.immutableCopy(builder.snmp);
        parms = ImmutableCollections.with(ImmutableParm::immutableCopy).newList(builder.parms);
        descr = builder.descr;
        logMsg = ImmutableLogMsg.immutableCopy(builder.logMsg);
        severity = builder.severity;
        pathOutage = builder.pathOutage;
        correlation = ImmutableCorrelation.immutableCopy(builder.correlation);
        operInstruct = builder.operInstruct;
        autoActionList = ImmutableCollections.with(ImmutableAutoAction::immutableCopy).newList(builder.autoActionList);
        operActionList = ImmutableCollections.with(ImmutableOperAction::immutableCopy).newList(builder.operActionList);
        autoAcknowledge = ImmutableAutoAcknowledge.immutableCopy(builder.autoAcknowledge);
        logGroupList = ImmutableCollections.newListOfImmutableType(builder.logGroupList);
        tTicket = ImmutableTticket.immutableCopy(builder.tTicket);
        forwardList = ImmutableCollections.with(ImmutableForward::immutableCopy).newList(builder.forwardList);
        scriptList = ImmutableCollections.with(ImmutableScript::immutableCopy).newList(builder.scriptList);
        ifIndex = builder.ifIndex;
        ifAlias = builder.ifAlias;
        mouseOverText = builder.mouseOverText;
        alarmData = ImmutableAlarmData.immutableCopy(builder.alarmData);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilderFrom(IEvent event) {
        return new Builder(event);
    }

    public static IEvent immutableCopy(IEvent event) {
        if (event == null || event instanceof ImmutableEvent) {
            return event;
        }
        return newBuilderFrom(event).build();
    }

    public static final class Builder {
        private String uuid;
        private Integer dbId;
        private String distPoller;
        private Date creationTime;
        private String masterStation;
        private IMask mask;
        private String uei;
        private String source;
        private Long nodeid;
        private Date time;
        private String host;
        private InetAddress interfaceAddress;
        private String interfaceString;
        private String snmpHost;
        private String service;
        private ISnmp snmp;
        private List<IParm> parms;
        private String descr;
        private ILogMsg logMsg;
        private String severity;
        private String pathOutage;
        private ICorrelation correlation;
        private String operInstruct;
        private List<IAutoAction> autoActionList;
        private List<IOperAction> operActionList;
        private IAutoAcknowledge autoAcknowledge;
        private List<String> logGroupList;
        private ITticket tTicket;
        private List<IForward> forwardList;
        private List<IScript> scriptList;
        private Integer ifIndex;
        private String ifAlias;
        private String mouseOverText;
        private IAlarmData alarmData;

        private Builder() {
        }

        private Builder(IEvent event) {
            uuid = event.getUuid();
            dbId = event.getDbid();
            distPoller = event.getDistPoller();
            creationTime = new Date(event.getCreationTime().getTime());
            masterStation = event.getMasterStation();
            mask = event.getMask();
            uei = event.getUei();
            source = event.getSource();
            nodeid = event.getNodeid();
            time = new Date(event.getTime().getTime());
            host = event.getHost();
            interfaceAddress = event.getInterfaceAddress();
            interfaceString = event.getInterface();
            snmpHost = event.getSnmphost();
            service = event.getService();
            snmp = event.getSnmp();
            parms = MutableCollections.copyListFromNullable(event.getParmCollection());
            descr = event.getDescr();
            logMsg = event.getLogmsg();
            severity = event.getSeverity();
            pathOutage = event.getPathoutage();
            correlation = event.getCorrelation();
            operInstruct = event.getOperinstruct();
            autoActionList = MutableCollections.copyListFromNullable(event.getAutoactionCollection());
            operActionList = MutableCollections.copyListFromNullable(event.getOperactionCollection());
            autoAcknowledge = event.getAutoacknowledge();
            logGroupList = MutableCollections.copyListFromNullable(event.getLoggroupCollection());
            tTicket = event.getTticket();
            forwardList = MutableCollections.copyListFromNullable(event.getForwardCollection());
            scriptList = MutableCollections.copyListFromNullable(event.getScriptCollection());
            ifIndex = event.getIfIndex();
            ifAlias = event.getIfAlias();
            mouseOverText = event.getMouseovertext();
            alarmData = event.getAlarmData();
        }

        public Builder setUuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder setDbId(Integer dbId) {
            this.dbId = dbId;
            return this;
        }

        public Builder setDistPoller(String distPoller) {
            this.distPoller = distPoller;
            return this;
        }

        public Builder setCreationTime(Date creationTime) {
            this.creationTime = creationTime;
            return this;
        }

        public Builder setMasterStation(String masterStation) {
            this.masterStation = masterStation;
            return this;
        }

        public Builder setMask(IMask mask) {
            this.mask = mask;
            return this;
        }

        public Builder setUei(String uei) {
            this.uei = uei;
            return this;
        }

        public Builder setSource(String source) {
            this.source = source;
            return this;
        }

        public Builder setNodeid(Long nodeid) {
            this.nodeid = nodeid;
            return this;
        }

        public Builder setTime(Date time) {
            this.time = time;
            return this;
        }

        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public Builder setInterfaceAddress(InetAddress interfaceAddress) {
            this.interfaceAddress = interfaceAddress;
            return this;
        }

        public Builder setInterface(String interfaceString) {
            this.interfaceString = interfaceString;
            return this;
        }

        public Builder setSnmpHost(String snmpHost) {
            this.snmpHost = snmpHost;
            return this;
        }

        public Builder setService(String service) {
            this.service = service;
            return this;
        }

        public Builder setSnmp(ISnmp snmp) {
            this.snmp = snmp;
            return this;
        }

        public Builder setParms(List<IParm> parms) {
            this.parms = parms;
            return this;
        }

        public Builder setDescr(String descr) {
            this.descr = descr;
            return this;
        }

        public Builder setLogMsg(ILogMsg logMsg) {
            this.logMsg = logMsg;
            return this;
        }

        public Builder setSeverity(String severity) {
            this.severity = severity;
            return this;
        }

        public Builder setPathOutage(String pathOutage) {
            this.pathOutage = pathOutage;
            return this;
        }

        public Builder setCorrelation(ICorrelation correlation) {
            this.correlation = correlation;
            return this;
        }

        public Builder setOperInstruct(String operInstruct) {
            this.operInstruct = operInstruct;
            return this;
        }

        public Builder setAutoActionList(List<IAutoAction> autoActionList) {
            this.autoActionList = autoActionList;
            return this;
        }

        public Builder setOperActionList(List<IOperAction> operActionList) {
            this.operActionList = operActionList;
            return this;
        }

        public Builder setAutoAcknowledge(IAutoAcknowledge autoAcknowledge) {
            this.autoAcknowledge = autoAcknowledge;
            return this;
        }

        public Builder setLogGroupList(List<String> logGroupList) {
            this.logGroupList = logGroupList;
            return this;
        }

        public Builder settTicket(ITticket tTicket) {
            this.tTicket = tTicket;
            return this;
        }

        public Builder setForwardList(List<IForward> forwardList) {
            this.forwardList = forwardList;
            return this;
        }

        public Builder setScriptList(List<IScript> scriptList) {
            this.scriptList = scriptList;
            return this;
        }

        public Builder setIfIndex(Integer ifIndex) {
            this.ifIndex = ifIndex;
            return this;
        }

        public Builder setIfAlias(String ifAlias) {
            this.ifAlias = ifAlias;
            return this;
        }

        public Builder setMouseOverText(String mouseOverText) {
            this.mouseOverText = mouseOverText;
            return this;
        }

        public Builder setAlarmData(IAlarmData alarmData) {
            this.alarmData = alarmData;
            return this;
        }

        public ImmutableEvent build() {
            return new ImmutableEvent(this);
        }
    }

    @Override
    public IAlarmData getAlarmData() {
        return alarmData;
    }

    @Override
    public IAutoAcknowledge getAutoacknowledge() {
        return autoAcknowledge;
    }

    @Override
    public IAutoAction getAutoaction(int index) {
        // check bounds for index
        if (index < 0 || index >= autoActionList.size()) {
            throw new IndexOutOfBoundsException("getAutoaction: Index value '" + index + "' not in range [0.." +
                    (autoActionList.size() - 1) + "]");
        }

        return autoActionList.get(index);
    }

    @Override
    public IAutoAction[] getAutoaction() {
        return autoActionList.toArray(new IAutoAction[0]);
    }

    @Override
    public List<IAutoAction> getAutoactionCollection() {
        return autoActionList;
    }

    @Override
    public int getAutoactionCount() {
        return autoActionList.size();
    }

    @Override
    public ICorrelation getCorrelation() {
        return correlation;
    }

    @Override
    public Date getCreationTime() {
        return creationTime == null ? null : new Date(creationTime.getTime());
    }

    @Override
    public Integer getDbid() {
        return dbId == null ? 0 : dbId;
    }

    @Override
    public String getDescr() {
        return descr;
    }

    @Override
    public String getDistPoller() {
        return distPoller;
    }

    @Override
    public IForward getForward(int index) {
        if (index < 0 || index >= forwardList.size()) {
            throw new IndexOutOfBoundsException("getForward: Index value '" + index + "' not in range [0.." +
                    (forwardList.size() - 1) + "]");
        }

        return forwardList.get(index);
    }

    @Override
    public IForward[] getForward() {
        return forwardList.toArray(new IForward[0]);
    }

    @Override
    public List<IForward> getForwardCollection() {
        return forwardList;
    }

    @Override
    public int getForwardCount() {
        return forwardList.size();
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public String getIfAlias() {
        return ifAlias;
    }

    @Override
    public Integer getIfIndex() {
        return ifIndex == null ? 0 : ifIndex;
    }

    @Override
    public String getInterface() {
        return interfaceString;
    }

    @Override
    public InetAddress getInterfaceAddress() {
        return interfaceAddress;
    }

    @Override
    public String getLoggroup(int index) {
        if (index < 0 || index >= logGroupList.size()) {
            throw new IndexOutOfBoundsException("getLoggroup: Index value '" + index + "' not in range [0.." +
                    (logGroupList.size() - 1) + "]");
        }

        return logGroupList.get(index);
    }

    @Override
    public String[] getLoggroup() {
        return logGroupList.toArray(new String[0]);
    }

    @Override
    public List<String> getLoggroupCollection() {
        return logGroupList;
    }

    @Override
    public int getLoggroupCount() {
        return logGroupList.size();
    }

    @Override
    public ILogMsg getLogmsg() {
        return logMsg;
    }

    @Override
    public IMask getMask() {
        return mask;
    }

    @Override
    public String getMasterStation() {
        return masterStation;
    }

    @Override
    public String getMouseovertext() {
        return mouseOverText;
    }

    @Override
    public Long getNodeid() {
        return nodeid == null ? 0 : nodeid;
    }

    @Override
    public IOperAction getOperaction(int index) {
        if (index < 0 || index >= operActionList.size()) {
            throw new IndexOutOfBoundsException("getOperaction: Index value '" + index + "' not in range [0.." +
                    (operActionList.size() - 1) + "]");
        }

        return operActionList.get(index);
    }

    @Override
    public IOperAction[] getOperaction() {
        return operActionList.toArray(new IOperAction[0]);
    }

    @Override
    public List<IOperAction> getOperactionCollection() {
        return operActionList;
    }

    @Override
    public int getOperactionCount() {
        return operActionList.size();
    }

    @Override
    public String getOperinstruct() {
        return operInstruct;
    }

    @Override
    public List<IParm> getParmCollection() {
        return parms == null ? Collections.emptyList() : parms;
    }

    @Override
    public IParm getParm(String key) {
        if (parms == null) {
            return null;
        }
        if (key == null) {
            throw new IllegalArgumentException("Parameter key cannot be null!");
        }

        return parms.stream().filter(p -> Objects.equals(key, p.getParmName()))
                .findFirst().orElse(null);
    }

    @Override
    public IParm getParmTrim(String key) {
        if (parms == null) {
            return null;
        }

        return parms.stream().filter(p -> StringUtils.equalsTrimmed(key, p.getParmName()))
                .findFirst().orElse(null);
    }

    @Override
    public String getPathoutage() {
        return pathOutage;
    }

    @Override
    public IScript getScript(int index) {
        if (index < 0 || index >= scriptList.size()) {
            throw new IndexOutOfBoundsException("getScript: Index value '" + index + "' not in range [0.." +
                    (scriptList.size() - 1) + "]");
        }

        return scriptList.get(index);
    }

    @Override
    public IScript[] getScript() {
        return scriptList.toArray(new IScript[0]);
    }

    @Override
    public List<IScript> getScriptCollection() {
        return scriptList;
    }

    @Override
    public int getScriptCount() {
        return scriptList.size();
    }

    @Override
    public String getService() {
        return service;
    }

    @Override
    public String getSeverity() {
        return severity;
    }

    @Override
    public ISnmp getSnmp() {
        return snmp;
    }

    @Override
    public String getSnmphost() {
        return snmpHost;
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public Date getTime() {
        return time == null ? null : new Date(time.getTime());
    }

    @Override
    public ITticket getTticket() {
        return tTicket;
    }

    @Override
    public String getUei() {
        return uei;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public boolean hasDbid() {
        return dbId != null;
    }

    @Override
    public boolean hasIfIndex() {
        return ifIndex != null;
    }

    @Override
    public boolean hasNodeid() {
        return nodeid != null;
    }

    @Override
    public Enumeration<IAutoAction> enumerateAutoaction() {
        return Collections.enumeration(autoActionList);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImmutableEvent that = (ImmutableEvent) o;
        return Objects.equals(uuid, that.uuid) &&
                Objects.equals(dbId, that.dbId) &&
                Objects.equals(distPoller, that.distPoller) &&
                Objects.equals(creationTime, that.creationTime) &&
                Objects.equals(masterStation, that.masterStation) &&
                Objects.equals(mask, that.mask) &&
                Objects.equals(uei, that.uei) &&
                Objects.equals(source, that.source) &&
                Objects.equals(nodeid, that.nodeid) &&
                Objects.equals(time, that.time) &&
                Objects.equals(host, that.host) &&
                Objects.equals(snmpHost, that.snmpHost) &&
                Objects.equals(service, that.service) &&
                Objects.equals(snmp, that.snmp) &&
                Objects.equals(parms, that.parms) &&
                Objects.equals(descr, that.descr) &&
                Objects.equals(logMsg, that.logMsg) &&
                Objects.equals(severity, that.severity) &&
                Objects.equals(pathOutage, that.pathOutage) &&
                Objects.equals(correlation, that.correlation) &&
                Objects.equals(operInstruct, that.operInstruct) &&
                Objects.equals(autoActionList, that.autoActionList) &&
                Objects.equals(operActionList, that.operActionList) &&
                Objects.equals(autoAcknowledge, that.autoAcknowledge) &&
                Objects.equals(logGroupList, that.logGroupList) &&
                Objects.equals(tTicket, that.tTicket) &&
                Objects.equals(forwardList, that.forwardList) &&
                Objects.equals(scriptList, that.scriptList) &&
                Objects.equals(ifIndex, that.ifIndex) &&
                Objects.equals(ifAlias, that.ifAlias) &&
                Objects.equals(mouseOverText, that.mouseOverText) &&
                Objects.equals(alarmData, that.alarmData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, dbId, distPoller, creationTime, masterStation, mask, uei, source, nodeid, time, host,
                snmpHost, service, snmp, parms, descr, logMsg, severity, pathOutage, correlation, operInstruct,
                autoActionList, operActionList, autoAcknowledge, logGroupList, tTicket, forwardList, scriptList,
                ifIndex, ifAlias, mouseOverText, alarmData);
    }

    @Override
    public String toString() {
        return "ImmutableEvent{" +
                "uuid='" + uuid + '\'' +
                ", dbId=" + dbId +
                ", distPoller='" + distPoller + '\'' +
                ", creationTime=" + creationTime +
                ", masterStation='" + masterStation + '\'' +
                ", mask=" + mask +
                ", uei='" + uei + '\'' +
                ", source='" + source + '\'' +
                ", nodeid=" + nodeid +
                ", time=" + time +
                ", host='" + host + '\'' +
                ", interfaceAddress=" + interfaceAddress +
                ", interfaceString='" + interfaceString + '\'' +
                ", snmpHost='" + snmpHost + '\'' +
                ", service='" + service + '\'' +
                ", snmp=" + snmp +
                ", parms=" + parms +
                ", descr='" + descr + '\'' +
                ", logMsg=" + logMsg +
                ", severity='" + severity + '\'' +
                ", pathOutage='" + pathOutage + '\'' +
                ", correlation=" + correlation +
                ", operInstruct='" + operInstruct + '\'' +
                ", autoActionList=" + autoActionList +
                ", operActionList=" + operActionList +
                ", autoAcknowledge=" + autoAcknowledge +
                ", logGroupList=" + logGroupList +
                ", tTicket=" + tTicket +
                ", forwardList=" + forwardList +
                ", scriptList=" + scriptList +
                ", ifIndex=" + ifIndex +
                ", ifAlias='" + ifAlias + '\'' +
                ", mouseOverText='" + mouseOverText + '\'' +
                ", alarmData=" + alarmData +
                '}';
    }

    @Override
    public String toStringSimple() {
        ToStringBuilder builder =  new ToStringBuilder(this, ToStringStyle.JSON_STYLE);
        builder.append("uei", uei);
        builder.append("time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSSZ").format(time));
        if (dbId   != null) builder.append("dbid", dbId);
        if (source != null) builder.append("source", source);
        if (nodeid != null) builder.append("nodeid", nodeid);
        if (parms  != null) builder.append("parms",  parms);
        return builder.toString();
    }
}
