/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsEventParameter;
import org.opennms.netmgt.model.OnmsMonitoringSystem;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.TroubleTicketState;

/**
 * Wraps the OnmsAlarm into a more generic Alarm instance
 * 
 * FIXME: Improve this alarm to support TIP and 3GPP collaboration.
 * FIXME: Most of these fields are not implemented waiting on above fix to be completed.
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="northbound-alarm")
@ValidateUsing("northbound-alarm.xsd")
@XmlAccessorType(XmlAccessType.NONE)
public class NorthboundAlarm implements Preservable, Serializable {

    private static final long serialVersionUID = -9207587487002564273L;

    /** The Constant SYNC_LOST_ALARM. */
    public static final NorthboundAlarm SYNC_LOST_ALARM = new NorthboundAlarm(-1, "uei.opennms.org/alarmd/northbounderSyncLost");

    /**
     * The Enumeration AlarmType.
     */
    public enum AlarmType {

        /** The problem. */
        PROBLEM,

        /** The resolution. */
        RESOLUTION,

        /** The notification. */
        NOTIFICATION;

        /**
         * To alarm type.
         *
         * @param alarmType the alarm type
         * @return the alarm type
         */
        static AlarmType toAlarmType(final int alarmType) {
            if (alarmType == OnmsAlarm.PROBLEM_TYPE) {
                return PROBLEM;
            } else if (alarmType == OnmsAlarm.RESOLUTION_TYPE) {
                return RESOLUTION;
            } else {
                return NOTIFICATION;
            }
        }
    }

    /**
     * The Enumeration x733ProbableCause.
     */
    public enum x733ProbableCause {

        /** The other. */
        other (1, "other"),

        /** The adapter error. */
        adapterError (2, "adapterError"),

        /** The application subsystem failure. */
        applicationSubsystemFailure (3, "applicationSubsystemFailure"),

        /** The bandwidth reduced. */
        bandwidthReduced (4, "bandwidthReduced"),

        /** The call establishment error. */
        callEstablishmentError (5, "callEstablishmentError"),

        /** The communications protocol error. */
        communicationsProtocolError (6, "communicationsProtocolError"),

        /** The communications subsystem failure. */
        communicationsSubsystemFailure (7, "communicationsSubsystemFailure"),

        /** The configuration or customization error. */
        configurationOrCustomizationError (8, "configurationOrCustomizationError"),

        /** The congestion. */
        congestion (9, "congestion"),

        /** The corrupt data. */
        corruptData (10, "corruptData"),

        /** The cpu cycles limit exceeded. */
        cpuCyclesLimitExceeded (11, "cpuCyclesLimitExceeded"),

        /** The data set or modem error. */
        dataSetOrModemError (12, "dataSetOrModemError"),

        /** The degraded signal. */
        degradedSignal (13, "degradedSignal"),

        /** The DTE/DCE interface error. */
        dteDceInterfaceError (14, "dteDceInterfaceError"),

        /** The enclosure door open. */
        enclosureDoorOpen (15, "enclosureDoorOpen"),

        /** The equipment malfunction. */
        equipmentMalfunction (16, "equipmentMalfunction"),

        /** The excessive vibration. */
        excessiveVibration (17, "excessiveVibration"),

        /** The file error. */
        fileError (18, "fileError"),

        /** The fire detected. */
        fireDetected (19, "fireDetected"),

        /** The flood detected. */
        floodDetected (20, "floodDetected"),

        /** The framing error. */
        framingError (21, "framingError"),

        /** The heating vent cooling system problem. */
        heatingVentCoolingSystemProblem (22, "heatingVentCoolingSystemProblem"),

        /** The humidity unacceptable. */
        humidityUnacceptable (23, "humidityUnacceptable"),

        /** The input output device error. */
        inputOutputDeviceError (24, "inputOutputDeviceError"),

        /** The input device error. */
        inputDeviceError (25, "inputDeviceError"),

        /** The LAN error. */
        lanError (26, "lanError"),

        /** The leak detected. */
        leakDetected (27, "leakDetected"),

        /** The local node transmission error. */
        localNodeTransmissionError (28, "localNodeTransmissionError"),

        /** The loss of frame. */
        lossOfFrame (29, "lossOfFrame"),

        /** The loss of signal. */
        lossOfSignal (30, "lossOfSignal"),

        /** The material supply exhausted. */
        materialSupplyExhausted (31, "materialSupplyExhausted"),

        /** The multiplexer problem. */
        multiplexerProblem (32, "multiplexerProblem"),

        /** The out of memory. */
        outOfMemory (33, "multiplexerProblem"),

        /** The output device error. */
        outputDeviceError (34, "outputDeviceError"),

        /** The performance degraded. */
        performanceDegraded (35, "performanceDegraded"),

        /** The power problem. */
        powerProblem (36, "powerProblem"),

        /** The pressure unacceptable. */
        pressureUnacceptable (37, "pressureUnacceptable"),

        /** The processor problem. */
        processorProblem (38, "processorProblem"),

        /** The pump failure. */
        pumpFailure (39, "pumpFailure"),

        /** The queue size exceeded. */
        queueSizeExceeded (40, "queueSizeExceeded"),

        /** The receive failure. */
        receiveFailure (41, "receiveFailure"),

        /** The receiver failure. */
        receiverFailure (42, "receiverFailure"),

        /** The remote node transmission error. */
        remoteNodeTransmissionError (43, "remoteNodeTransmissionError"),

        /** The resource at or nearing capacity. */
        resourceAtOrNearingCapacity (44, "resourceAtOrNearingCapacity"),

        /** The response time excessive. */
        responseTimeExecessive (45, "responseTimeExcessive"),

        /** The retransmission rate excessive. */
        retransmissionRateExcessive (46, "retransmissionRateExcessive"),

        /** The software error. */
        softwareError (47, "softwareError"),

        /** The software program abnormally terminated. */
        softwareProgramAbnormallyTerminated (48, "softwareProgramAbnormallyTerminated"),

        /** The software program error. */
        softwareProgramError (49, "softwareProgramError"),

        /** The storage capacity problem. */
        storageCapacityProblem (50, "storageCapacityProblem"),

        /** The temperature unacceptable. */
        temperatureUnacceptable (51, "temperatureUnacceptable"),

        /** The threshold crossed. */
        thresholdCrossed (52, "thresholdCrossed"),

        /** The timing problem. */
        timingProblem (53, "timingProblem"),

        /** The toxic leak detected. */
        toxicLeakDetected (54, "toxicLeakDetected"),

        /** The transmit failure. */
        transmitFailure (55, "transmitFailure"),

        /** The transmitter failure. */
        transmitterFailure (56, "transmitterFailure"),

        /** The underlying resource unavailable. */
        underlyingResourceUnavailable (57, "underlyingResourceUnavailable"),

        /** The version mismatch. */
        versionMismatch (58, "versionMismatch"),

        /** The authentication failure. */
        authenticationFailure (59, "authenticationFailure"),

        /** The breach of confidentiality. */
        breachOfConfidentiality (60, "breachOfConfidentiality"),

        /** The cable tamper. */
        cableTamper (61, "cableTamper"),

        /** The delayed information. */
        delayedInformation (62, "delayedInformation"),

        /** The denial of service. */
        denialOfService (63, "denialOfService"),

        /** The duplicate information. */
        duplicateInformation (64, "duplicateInformation"),

        /** The information missing. */
        informationMissing (65, "informationMissing"),

        /** The information modification detected. */
        informationModificationDetected (66, "informationModificationDetected"),

        /** The information out of sequence. */
        informationOutOfSequence (67, "informationOutOfSequence"),

        /** The intrusion detection. */
        intrusionDetection (68, "intrusionDetection"),

        /** The key expired. */
        keyExpired (69, "keyExpired"),

        /** The non repudiation failure. */
        nonRepudiationFailure (70, "nonRepudiationFailure"),

        /** The out of hours activity. */
        outOfHoursActivity (71, "outOfHoursActivity"),

        /** The out of service. */
        outOfService (72, "outOfService"),

        /** The procedural error. */
        proceduralError (73, "proceduralError"),

        /** The unauthorized access attempt. */
        unauthorizedAccessAttempt (74, "unauthorizedAccessAttempt"),

        /** The unexpected information. */
        unexpectedInformation (75, "unexpectedInformation");

        /** The Constant m_idMap. */
        private static final Map<Integer, x733ProbableCause> m_idMap;

        /** The ID. */
        private int m_id;

        /** The label. */
        private String m_label;

        /**
         * Instantiates a new x733 probable cause.
         *
         * @param id the ID
         * @param label the label
         */
        private x733ProbableCause(final int id, final String label) {
            m_id = id;
            m_label = label;
        }

        static {
            m_idMap = new HashMap<Integer, x733ProbableCause>(values().length);
            for (final x733ProbableCause cause : values()) {
                m_idMap.put(cause.getId(), cause);
            }
        }

        /**
         * Gets the ID.
         *
         * @return the ID
         */
        public int getId() {
            return m_id;
        }

        /**
         * Gets the label.
         *
         * @return the label
         */
        public String getLabel() {
            return m_label;
        }

        /**
         * This get returns the x733ProbableCause matching the requested label.  If
         * a null string is passed, x733ProbablCause.other is returned.
         *
         * @param label the label
         * @return the x733 probable cause
         */
        public static x733ProbableCause get(final String label) {
            x733ProbableCause cause = other;

            if (label == null) {
                return cause;
            }

            for (final Integer key : m_idMap.keySet()) {
                if (m_idMap.get(key).getLabel().equalsIgnoreCase(label)) {
                    cause = m_idMap.get(key);
                }
            }
            return cause;
        }

        /**
         * Return an x733ProbableCause by ID.
         *
         * @param id the id
         * @return the x733 probable cause
         */
        public static x733ProbableCause get(int id) {
            if (m_idMap.containsKey(id)) {
                return m_idMap.get(id);
            } else {
                throw new IllegalArgumentException("Unknown x733 Probable Cause ID requested: "+id);
            }
        }

    }

    /**
     * The Enumeration x733AlarmType.
     */
    public enum x733AlarmType {

        /** The other. */
        other (1, "other"),

        /** The communications alarm. */
        communicationsAlarm (2, "communicationsAlarm"),

        /** The quality of service alarm. */
        qualityOfServiceAlarm (3, "qualityOfServiceAlarm"),

        /** The processing error alarm. */
        processingErrorAlarm (4, "processingErrorAlarm"),

        /** The equipment alarm. */
        equipmentAlarm (5, "equipmentAlarm"),

        /** The environmental alarm. */
        environmentalAlarm (6, "environmentalAlarm"),

        /** The integrity violation. */
        integrityViolation (7, "integrityViolation"),

        /** The operational violation. */
        operationalViolation (8, "operationalViolation"),

        /** The physical violation. */
        physicalViolation (9, "physicalViolation"),

        /** The security service or mechanism violation. */
        securityServiceOrMechanismViolation (10, "securityServiceOrMechanismViolation"),

        /** The time domain violation. */
        timeDomainViolation (11, "timeDomainViolation");

        /** The ID map. */
        private static Map<Integer, x733AlarmType> m_idMap;

        /** The ID. */
        private int m_id;

        /** The label. */
        private String m_label;

        /**
         * Instantiates a new x733 alarm type.
         *
         * @param id the ID
         * @param label the label
         */
        private x733AlarmType(final int id, final String label) {
            m_id = id;
            m_label = label;
        }

        static {
            m_idMap = new HashMap<Integer, x733AlarmType>(values().length);
            for (final x733AlarmType type : values()) {
                m_idMap.put(type.getId(), type);
            }
        }

        /**
         * Gets the ID.
         *
         * @return the ID
         */
        public int getId() {
            return m_id;
        }

        /**
         * This get returns the x733ProbableCause matching the requested label.
         * <p>If a null string is passed, x733ProbablCause.other is returned.</p>
         *
         * @param label the label
         * @return the x733 alarm type
         */
        public static x733AlarmType get(final String label) {
            x733AlarmType cause = other;

            if (label == null) {
                return cause;
            }

            for (final Integer key : m_idMap.keySet()) {
                if (m_idMap.get(key).getLabel().equalsIgnoreCase(label)) {
                    cause = m_idMap.get(key);
                }
            }
            return cause;
        }

        /**
         * Gets the label.
         *
         * @return the label
         */
        private String getLabel() {
            return m_label;
        }

        /**
         * Return an x733ProbableCause by ID.
         *
         * @param id the id
         * @return the x733 alarm type
         */
        public static x733AlarmType get(int id) {
            if (m_idMap.containsKey(id)) {
                return m_idMap.get(id);
            } else {
                throw new IllegalArgumentException("Unknown x733 Alarm Type ID requested: "+id);
            }
        }

    }

    /** The ID. */
    @XmlAttribute(name="id")
    private Integer m_id;

    /** The UEI. */
    @XmlElement(name="uei")
    private String m_uei;

    /** The Node id. */
    @XmlElement(name="node-id")
    private Integer m_nodeId;

    /** The Node label. */
    @XmlElement(name="node-label")
    private String m_nodeLabel;

    /** The Node System Object ID. */
    @XmlElement(name="node-sysobjectid")
    private String m_nodeSysObjectId;

    /** The Node Foreign source. */
    @XmlElement(name="node-foreignsource")
    private String m_foreignSource;

    /** The Node Foreign ID. */
    @XmlElement(name="node-foreignid")
    private String m_foreignId;

    /** The acknowledge time. */
    @XmlElement(name="ack-time")
    private Date m_ackTime;

    /** The acknowledge user. */
    @XmlElement(name="ack-user")
    private String m_ackUser;

    /** The alarm type. */
    @XmlElement(name="alarm-type")
    private AlarmType m_alarmType;

    /** The App DN. */
    @XmlElement(name="app-dn")
    private String m_appDn;

    /** The clear key. */
    @XmlElement(name="clear-key")
    private String m_clearKey;

    /** The count. */
    @XmlElement(name="count")
    private Integer m_count;

    /** The description. */
    @XmlElement(name="description")
    private String m_desc;

    /** The distributed poller object. */
    private OnmsMonitoringSystem m_poller;

    /** The first occurrence date. */
    @XmlElement(name="first-occurrence")
    private Date m_firstOccurrence;

    /** The IP address. */
    @XmlElement(name="ip-address")
    private String m_ipAddr;

    /** The last occurrence date. */
    @XmlElement(name="last-occurrence")
    private Date m_lastOccurrence;

    /** The LOG message. */
    @XmlElement(name="log-messsage")
    private String m_logMsg;

    /** The object instance. */
    @XmlElement(name="object-instance")
    private String m_objectInstance;

    /** The object type. */
    @XmlElement(name="object-type")
    private String m_objectType;

    /** The operator instructions. */
    @XmlElement(name="operator-instructions")
    private String m_operInst;

    /** The OSS key. */
    @XmlElement(name="oss-key")
    private String m_ossKey;

    /** The OSS state. */
    @XmlElement(name="oss-state")
    private String m_ossState;

    /** The alarm key. */
    @XmlElement(name="alarm-key")
    private String m_alarmKey;

    /** The service. */
    @XmlElement(name="service")
    private String m_service;

    /** The severity. */
    @XmlElement(name="severity")
    private OnmsSeverity m_severity;

    /** The suppressed date. */
    @XmlElement(name="suppressed")
    private Date m_suppressed;

    /** The suppressed until date. */
    @XmlElement(name="suppressed-until")
    private Date m_suppressedUntil;

    /** The suppressed by. */
    @XmlElement(name="suppressed-by")
    private String m_suppressedBy;

    /** The ticket ID. */
    @XmlElement(name="ticket-id")
    private String m_ticketId;

    /** The ticket state. */
    @XmlElement(name="ticket-state")
    private TroubleTicketState m_ticketState;

    /** The x733 type. */
    @XmlElement(name="x733-type")
    private String m_x733Type;

    /** The x733 cause. */
    @XmlElement(name="x733-cause")
    private int m_x733Cause;

    /** The event parameters map. */
    private Map<String,String> m_eventParametersMap = new HashMap<String,String>();

    /** The event parameters collection. */
    @XmlElementWrapper(name="parameters")
    @XmlElement(name="parameter")
    private List<OnmsEventParameter> m_eventParametersCollection = new ArrayList<>();

    /** The preserved flag. */
    @XmlElement(name="preserved", defaultValue="false")
    private volatile boolean m_preserved = false;

    /**
     * Instantiates a new northbound alarm.
     */
    public NorthboundAlarm() {
        // No-arg constructore required by JAXB
    }

    /**
     * Instantiates a new northbound alarm.
     *
     * @param id the ID
     * @param uei the UEI
     */
    private NorthboundAlarm(int id, String uei) {
        // I only set these for the 'special event'
        m_id = id;
        m_uei = uei;

        m_nodeId = null;
        m_ackTime = null;
        m_ackUser = null;
        m_alarmType = null;
        m_appDn = null;
        m_clearKey = null;
        m_count = null;
        m_desc = null;
        m_poller = null;
        m_firstOccurrence = null;
        m_ipAddr = null;
        m_lastOccurrence = null;
        m_logMsg = null;
        m_objectInstance = null;
        m_objectType = null;
        m_operInst = null;
        m_ossKey = null;
        m_ossState = null;
        m_alarmKey = null;
        m_service = null;
        m_severity = null;
        m_suppressed = null;
        m_suppressedUntil = null;
        m_suppressedBy = null;
        m_ticketId = null;
        m_ticketState = null;
        m_x733Type = null;
        m_x733Cause = -1;
    }

    /**
     * Instantiates a new northbound alarm.
     *
     * @param alarm the alarm
     */
    public NorthboundAlarm(OnmsAlarm alarm) {
        m_nodeId = alarm.getNodeId();
        m_ackTime = alarm.getAlarmAckTime();
        m_ackUser = alarm.getAlarmAckUser();
        m_alarmType = alarm.getAlarmType() == null ? null : AlarmType.toAlarmType(alarm.getAlarmType());
        m_appDn = alarm.getApplicationDN();
        m_clearKey = alarm.getClearKey();
        m_count = alarm.getCounter();
        m_desc = alarm.getDescription();
        m_poller = alarm.getDistPoller();
        m_firstOccurrence = alarm.getFirstEventTime();
        m_id = alarm.getId();
        m_ipAddr = alarm.getIpAddr() != null ? InetAddressUtils.toIpAddrString(alarm.getIpAddr()) : null;
        m_lastOccurrence = alarm.getLastEventTime();
        m_logMsg = alarm.getLogMsg();
        m_objectInstance = alarm.getManagedObjectInstance();
        m_objectType = alarm.getManagedObjectType();
        m_operInst = alarm.getOperInstruct();
        m_ossKey = alarm.getOssPrimaryKey();
        m_ossState = alarm.getQosAlarmState();
        m_alarmKey = alarm.getReductionKey();
        m_service = alarm.getServiceType() == null ? null : alarm.getServiceType().getName();
        m_severity = alarm.getSeverity();
        m_suppressed = alarm.getSuppressedTime();
        m_suppressedUntil = alarm.getSuppressedUntil();
        m_suppressedBy = alarm.getSuppressedUser();
        m_ticketId = alarm.getTTicketId();
        m_ticketState = alarm.getTTicketState();
        m_uei = alarm.getUei();
        m_x733Type = alarm.getX733AlarmType();
        m_x733Cause = alarm.getX733ProbableCause();

        if (alarm.getNode() != null) {
            m_foreignSource = alarm.getNode().getForeignSource();
            m_foreignId = alarm.getNode().getForeignId();
            m_nodeLabel = alarm.getNode().getLabel();
            m_nodeSysObjectId = alarm.getNode().getSysObjectId();
        }

        if (alarm.getEventParameters() != null) {
            for (OnmsEventParameter parm : alarm.getEventParameters()) {
                m_eventParametersCollection.add(parm);
                m_eventParametersMap.put(parm.getName(), parm.getValue());
            }
        }
    }

    /**
     * Gets the ID.
     *
     * @return the ID
     */
    public Integer getId() {
        return m_id;
    }

    /**
     * Gets the UEI.
     *
     * @return the UEI
     */
    public String getUei() {
        return m_uei;
    }

    /**
     * Gets the acknowledge time.
     *
     * @return the acknowledge time
     */
    public Date getAckTime() {
        return m_ackTime;
    }

    /**
     * Gets the acknowledge user.
     *
     * @return the acknowledge user
     */
    public String getAckUser() {
        return m_ackUser;
    }

    /**
     * Gets the alarm type.
     *
     * @return the alarm type
     */
    public AlarmType getAlarmType() {
        return m_alarmType;
    }

    /**
     * Gets the App DN.
     *
     * @return the App DN
     */
    public String getAppDn() {
        return m_appDn;
    }

    /**
     * Gets the clear key.
     *
     * @return the clear key
     */
    public String getClearKey() {
        return m_clearKey;
    }

    /**
     * Gets the count.
     *
     * @return the count
     */
    public Integer getCount() {
        return m_count;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDesc() {
        return m_desc;
    }

    /**
     * Gets the poller.
     *
     * @return the poller
     */
    public OnmsMonitoringSystem getPoller() {
        return m_poller;
    }

    /**
     * Gets the first occurrence.
     *
     * @return the first occurrence
     */
    public Date getFirstOccurrence() {
        return m_firstOccurrence;
    }

    /**
     * Gets the IP address.
     *
     * @return the IP address
     */
    public String getIpAddr() {
        return m_ipAddr;
    }

    /**
     * Gets the last occurrence.
     *
     * @return the last occurrence
     */
    public Date getLastOccurrence() {
        return m_lastOccurrence;
    }

    /**
     * Gets the log message.
     *
     * @return the log message
     */
    public String getLogMsg() {
        return m_logMsg;
    }

    /**
     * Gets the object instance.
     *
     * @return the object instance
     */
    public String getObjectInstance() {
        return m_objectInstance;
    }

    /**
     * Gets the object type.
     *
     * @return the object type
     */
    public String getObjectType() {
        return m_objectType;
    }

    /**
     * Gets the operator instructions.
     *
     * @return the operator instructions
     */
    public String getOperInst() {
        return m_operInst;
    }

    /**
     * Gets the OSS key.
     *
     * @return the OSS key
     */
    public String getOssKey() {
        return m_ossKey;
    }

    /**
     * Gets the OSS state.
     *
     * @return the OSS state
     */
    public String getOssState() {
        return m_ossState;
    }

    /**
     * Gets the alarm key.
     *
     * @return the alarm key
     */
    public String getAlarmKey() {
        return m_alarmKey;
    }

    /**
     * Gets the service.
     *
     * @return the service
     */
    public String getService() {
        return m_service;
    }

    /**
     * Gets the severity.
     *
     * @return the severity
     */
    public OnmsSeverity getSeverity() {
        return m_severity;
    }

    /**
     * Gets the suppressed.
     *
     * @return the suppressed
     */
    public Date getSuppressed() {
        return m_suppressed;
    }

    /**
     * Gets the suppressed until.
     *
     * @return the suppressed until
     */
    public Date getSuppressedUntil() {
        return m_suppressedUntil;
    }

    /**
     * Gets the suppressed by.
     *
     * @return the suppressed by
     */
    public String getSuppressedBy() {
        return m_suppressedBy;
    }

    /**
     * Gets the ticket id.
     *
     * @return the ticket id
     */
    public String getTicketId() {
        return m_ticketId;
    }

    /**
     * Gets the ticket state.
     *
     * @return the ticket state
     */
    public TroubleTicketState getTicketState() {
        return m_ticketState;
    }

    /**
     * Gets the x733 type.
     *
     * @return the x733 type
     */
    public String getX733Type() {
        return m_x733Type;
    }

    /**
     * Gets the x733 cause.
     *
     * @return the x733 cause
     */
    public int getX733Cause() {
        return m_x733Cause;
    }

    /**
     * Gets the parameters.
     *
     * @return the parameters
     */
    public Map<String,String> getParameters() {
        return m_eventParametersMap;
    }

    /**
     * Gets the event parameters collection.
     *
     * @return the event parameters collection
     */
    public List<OnmsEventParameter> getEventParametersCollection() {
        return m_eventParametersCollection;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.alarmd.api.Preservable#isPreserved()
     */
    @Override
    public boolean isPreserved() {
        return m_preserved;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.alarmd.api.Preservable#setPreserved(boolean)
     */
    @Override
    public void setPreserved(boolean preserved) {
        m_preserved = preserved;
    }

    /**
     * Gets the node id.
     *
     * @return the node id
     */
    public Integer getNodeId() {
        return m_nodeId;
    }

    /**
     * Gets the node label.
     *
     * @return the node label
     */
    public String getNodeLabel() {
        return m_nodeLabel;
    }

    /**
     * Gets the node system object id.
     *
     * @return the node system object id
     */
    public String getNodeSysObjectId() {
        return m_nodeSysObjectId;
    }

    /**
     * Gets the node foreign source.
     *
     * @return the node foreign source
     */
    public String getForeignSource() {
        return m_foreignSource;
    }

    /**
     * Gets the node foreign id.
     *
     * @return the node foreign id
     */
    public String getForeignId() {
        return m_foreignId;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    public void setId(Integer id) {
        m_id = id;
    }

    /**
     * Sets the UEI.
     *
     * @param uei the new UEI
     */
    public void setUei(String uei) {
        m_uei = uei;
    }

    /**
     * Sets the node id.
     *
     * @param nodeId the new node id
     */
    public void setNodeId(Integer nodeId) {
        m_nodeId = nodeId;
    }

    /**
     * Sets the node label.
     *
     * @param nodeLabel the new node label
     */
    public void setNodeLabel(String nodeLabel) {
        m_nodeLabel = nodeLabel;
    }

    /**
     * Sets the node sys object id.
     *
     * @param nodeSysObjectId the new node sys object id
     */
    public void setNodeSysObjectId(String nodeSysObjectId) {
        m_nodeSysObjectId = nodeSysObjectId;
    }

    /**
     * Sets the foreign source.
     *
     * @param foreignSource the new foreign source
     */
    public void setForeignSource(String foreignSource) {
        m_foreignSource = foreignSource;
    }

    /**
     * Sets the foreign id.
     *
     * @param foreignId the new foreign id
     */
    public void setForeignId(String foreignId) {
        m_foreignId = foreignId;
    }

    /**
     * Sets the acknowledge time.
     *
     * @param ackTime the new acknowledge time
     */
    public void setAckTime(Date ackTime) {
        m_ackTime = ackTime;
    }

    /**
     * Sets the acknowledge user.
     *
     * @param ackUser the new acknowledge user
     */
    public void setAckUser(String ackUser) {
        m_ackUser = ackUser;
    }

    /**
     * Sets the alarm type.
     *
     * @param alarmType the new alarm type
     */
    public void setAlarmType(AlarmType alarmType) {
        m_alarmType = alarmType;
    }

    /**
     * Sets the App DN.
     *
     * @param appDn the new App DN
     */
    public void setAppDn(String appDn) {
        m_appDn = appDn;
    }

    /**
     * Sets the clear key.
     *
     * @param clearKey the new clear key
     */
    public void setClearKey(String clearKey) {
        m_clearKey = clearKey;
    }

    /**
     * Sets the count.
     *
     * @param count the new count
     */
    public void setCount(Integer count) {
        m_count = count;
    }

    /**
     * Sets the description.
     *
     * @param desc the new description
     */
    public void setDesc(String desc) {
        m_desc = desc;
    }

    /**
     * Sets the poller.
     *
     * @param poller the new poller
     */
    public void setPoller(OnmsDistPoller poller) {
        m_poller = poller;
    }

    /**
     * Sets the first occurrence.
     *
     * @param firstOccurrence the new first occurrence
     */
    public void setFirstOccurrence(Date firstOccurrence) {
        m_firstOccurrence = firstOccurrence;
    }

    /**
     * Sets the IP address.
     *
     * @param ipAddr the new IP address
     */
    public void setIpAddr(String ipAddr) {
        m_ipAddr = ipAddr;
    }

    /**
     * Sets the last occurrence.
     *
     * @param lastOccurrence the new last occurrence
     */
    public void setLastOccurrence(Date lastOccurrence) {
        m_lastOccurrence = lastOccurrence;
    }

    /**
     * Sets the log message.
     *
     * @param logMsg the new log message
     */
    public void setLogMsg(String logMsg) {
        m_logMsg = logMsg;
    }

    /**
     * Sets the object instance.
     *
     * @param objectInstance the new object instance
     */
    public void setObjectInstance(String objectInstance) {
        m_objectInstance = objectInstance;
    }

    /**
     * Sets the object type.
     *
     * @param objectType the new object type
     */
    public void setObjectType(String objectType) {
        m_objectType = objectType;
    }

    /**
     * Sets the operator instructions.
     *
     * @param operInst the new operator instructions
     */
    public void setOperInst(String operInst) {
        m_operInst = operInst;
    }

    /**
     * Sets the OSS key.
     *
     * @param ossKey the new OSS key
     */
    public void setOssKey(String ossKey) {
        m_ossKey = ossKey;
    }

    /**
     * Sets the OSS state.
     *
     * @param ossState the new OSS state
     */
    public void setOssState(String ossState) {
        m_ossState = ossState;
    }

    /**
     * Sets the alarm key.
     *
     * @param alarmKey the new alarm key
     */
    public void setAlarmKey(String alarmKey) {
        m_alarmKey = alarmKey;
    }

    /**
     * Sets the service.
     *
     * @param service the new service
     */
    public void setService(String service) {
        m_service = service;
    }

    /**
     * Sets the severity.
     *
     * @param severity the new severity
     */
    public void setSeverity(OnmsSeverity severity) {
        m_severity = severity;
    }

    /**
     * Sets the suppressed.
     *
     * @param suppressed the new suppressed
     */
    public void setSuppressed(Date suppressed) {
        m_suppressed = suppressed;
    }

    /**
     * Sets the suppressed until.
     *
     * @param suppressedUntil the new suppressed until
     */
    public void setSuppressedUntil(Date suppressedUntil) {
        m_suppressedUntil = suppressedUntil;
    }

    /**
     * Sets the suppressed by.
     *
     * @param suppressedBy the new suppressed by
     */
    public void setSuppressedBy(String suppressedBy) {
        m_suppressedBy = suppressedBy;
    }

    /**
     * Sets the ticket id.
     *
     * @param ticketId the new ticket id
     */
    public void setTicketId(String ticketId) {
        m_ticketId = ticketId;
    }

    /**
     * Sets the ticket state.
     *
     * @param ticketState the new ticket state
     */
    public void setTicketState(TroubleTicketState ticketState) {
        m_ticketState = ticketState;
    }

    /**
     * Sets the x733 type.
     *
     * @param x733Type the new x733 type
     */
    public void setx733Type(String x733Type) {
        m_x733Type = x733Type;
    }

    /**
     * Sets the x733 cause.
     *
     * @param x733Cause the new x733 cause
     */
    public void setx733Cause(int x733Cause) {
        m_x733Cause = x733Cause;
    }

    /**
     * Sets the event parameters map.
     *
     * @param eventParametersMap the event parameters map
     */
    public void setEventParametersMap(Map<String, String> eventParametersMap) {
        m_eventParametersMap = eventParametersMap;
    }

    /**
     * Sets the event parameters collection.
     *
     * @param eventParametersCollection the new event parameters collection
     */
    public void setEventParametersCollection(List<OnmsEventParameter> eventParametersCollection) {
        m_eventParametersCollection = eventParametersCollection;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("NorthboundAlarm[id=%d, uei='%s', nodeId=%d]", m_id, m_uei, m_nodeId);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_ackTime == null) ? 0 : m_ackTime.hashCode());
        result = prime * result + ((m_ackUser == null) ? 0 : m_ackUser.hashCode());
        result = prime * result + ((m_alarmKey == null) ? 0 : m_alarmKey.hashCode());
        result = prime * result + ((m_alarmType == null) ? 0 : m_alarmType.hashCode());
        result = prime * result + ((m_appDn == null) ? 0 : m_appDn.hashCode());
        result = prime * result + ((m_clearKey == null) ? 0 : m_clearKey.hashCode());
        result = prime * result + ((m_count == null) ? 0 : m_count.hashCode());
        result = prime * result + ((m_desc == null) ? 0 : m_desc.hashCode());
        result = prime * result + ((m_eventParametersCollection == null) ? 0 : m_eventParametersCollection.hashCode());
        result = prime * result + ((m_eventParametersMap == null) ? 0 : m_eventParametersMap.hashCode());
        result = prime * result + ((m_firstOccurrence == null) ? 0 : m_firstOccurrence.hashCode());
        result = prime * result + ((m_foreignId == null) ? 0 : m_foreignId.hashCode());
        result = prime * result + ((m_foreignSource == null) ? 0 : m_foreignSource.hashCode());
        result = prime * result + ((m_id == null) ? 0 : m_id.hashCode());
        result = prime * result + ((m_ipAddr == null) ? 0 : m_ipAddr.hashCode());
        result = prime * result + ((m_lastOccurrence == null) ? 0 : m_lastOccurrence.hashCode());
        result = prime * result + ((m_logMsg == null) ? 0 : m_logMsg.hashCode());
        result = prime * result + ((m_nodeId == null) ? 0 : m_nodeId.hashCode());
        result = prime * result + ((m_nodeLabel == null) ? 0 : m_nodeLabel.hashCode());
        result = prime * result + ((m_nodeSysObjectId == null) ? 0 : m_nodeSysObjectId.hashCode());
        result = prime * result + ((m_objectInstance == null) ? 0 : m_objectInstance.hashCode());
        result = prime * result + ((m_objectType == null) ? 0 : m_objectType.hashCode());
        result = prime * result + ((m_operInst == null) ? 0 : m_operInst.hashCode());
        result = prime * result + ((m_ossKey == null) ? 0 : m_ossKey.hashCode());
        result = prime * result + ((m_ossState == null) ? 0 : m_ossState.hashCode());
        result = prime * result + ((m_poller == null) ? 0 : m_poller.hashCode());
        result = prime * result + (m_preserved ? 1231 : 1237);
        result = prime * result + ((m_service == null) ? 0 : m_service.hashCode());
        result = prime * result + ((m_severity == null) ? 0 : m_severity.hashCode());
        result = prime * result + ((m_suppressed == null) ? 0 : m_suppressed.hashCode());
        result = prime * result + ((m_suppressedBy == null) ? 0 : m_suppressedBy.hashCode());
        result = prime * result + ((m_suppressedUntil == null) ? 0 : m_suppressedUntil.hashCode());
        result = prime * result + ((m_ticketId == null) ? 0 : m_ticketId.hashCode());
        result = prime * result + ((m_ticketState == null) ? 0 : m_ticketState.hashCode());
        result = prime * result + ((m_uei == null) ? 0 : m_uei.hashCode());
        result = prime * result + m_x733Cause;
        result = prime * result + ((m_x733Type == null) ? 0 : m_x733Type.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NorthboundAlarm other = (NorthboundAlarm) obj;
        if (m_ackTime == null) {
            if (other.m_ackTime != null)
                return false;
        } else if (!m_ackTime.equals(other.m_ackTime))
            return false;
        if (m_ackUser == null) {
            if (other.m_ackUser != null)
                return false;
        } else if (!m_ackUser.equals(other.m_ackUser))
            return false;
        if (m_alarmKey == null) {
            if (other.m_alarmKey != null)
                return false;
        } else if (!m_alarmKey.equals(other.m_alarmKey))
            return false;
        if (m_alarmType != other.m_alarmType)
            return false;
        if (m_appDn == null) {
            if (other.m_appDn != null)
                return false;
        } else if (!m_appDn.equals(other.m_appDn))
            return false;
        if (m_clearKey == null) {
            if (other.m_clearKey != null)
                return false;
        } else if (!m_clearKey.equals(other.m_clearKey))
            return false;
        if (m_count == null) {
            if (other.m_count != null)
                return false;
        } else if (!m_count.equals(other.m_count))
            return false;
        if (m_desc == null) {
            if (other.m_desc != null)
                return false;
        } else if (!m_desc.equals(other.m_desc))
            return false;
        if (m_eventParametersCollection == null) {
            if (other.m_eventParametersCollection != null)
                return false;
        } else if (!m_eventParametersCollection.equals(other.m_eventParametersCollection))
            return false;
        if (m_eventParametersMap == null) {
            if (other.m_eventParametersMap != null)
                return false;
        } else if (!m_eventParametersMap.equals(other.m_eventParametersMap))
            return false;
        if (m_firstOccurrence == null) {
            if (other.m_firstOccurrence != null)
                return false;
        } else if (!m_firstOccurrence.equals(other.m_firstOccurrence))
            return false;
        if (m_foreignId == null) {
            if (other.m_foreignId != null)
                return false;
        } else if (!m_foreignId.equals(other.m_foreignId))
            return false;
        if (m_foreignSource == null) {
            if (other.m_foreignSource != null)
                return false;
        } else if (!m_foreignSource.equals(other.m_foreignSource))
            return false;
        if (m_id == null) {
            if (other.m_id != null)
                return false;
        } else if (!m_id.equals(other.m_id))
            return false;
        if (m_ipAddr == null) {
            if (other.m_ipAddr != null)
                return false;
        } else if (!m_ipAddr.equals(other.m_ipAddr))
            return false;
        if (m_lastOccurrence == null) {
            if (other.m_lastOccurrence != null)
                return false;
        } else if (!m_lastOccurrence.equals(other.m_lastOccurrence))
            return false;
        if (m_logMsg == null) {
            if (other.m_logMsg != null)
                return false;
        } else if (!m_logMsg.equals(other.m_logMsg))
            return false;
        if (m_nodeId == null) {
            if (other.m_nodeId != null)
                return false;
        } else if (!m_nodeId.equals(other.m_nodeId))
            return false;
        if (m_nodeLabel == null) {
            if (other.m_nodeLabel != null)
                return false;
        } else if (!m_nodeLabel.equals(other.m_nodeLabel))
            return false;
        if (m_nodeSysObjectId == null) {
            if (other.m_nodeSysObjectId != null)
                return false;
        } else if (!m_nodeSysObjectId.equals(other.m_nodeSysObjectId))
            return false;
        if (m_objectInstance == null) {
            if (other.m_objectInstance != null)
                return false;
        } else if (!m_objectInstance.equals(other.m_objectInstance))
            return false;
        if (m_objectType == null) {
            if (other.m_objectType != null)
                return false;
        } else if (!m_objectType.equals(other.m_objectType))
            return false;
        if (m_operInst == null) {
            if (other.m_operInst != null)
                return false;
        } else if (!m_operInst.equals(other.m_operInst))
            return false;
        if (m_ossKey == null) {
            if (other.m_ossKey != null)
                return false;
        } else if (!m_ossKey.equals(other.m_ossKey))
            return false;
        if (m_ossState == null) {
            if (other.m_ossState != null)
                return false;
        } else if (!m_ossState.equals(other.m_ossState))
            return false;
        if (m_poller == null) {
            if (other.m_poller != null)
                return false;
        } else if (!m_poller.equals(other.m_poller))
            return false;
        if (m_preserved != other.m_preserved)
            return false;
        if (m_service == null) {
            if (other.m_service != null)
                return false;
        } else if (!m_service.equals(other.m_service))
            return false;
        if (m_severity != other.m_severity)
            return false;
        if (m_suppressed == null) {
            if (other.m_suppressed != null)
                return false;
        } else if (!m_suppressed.equals(other.m_suppressed))
            return false;
        if (m_suppressedBy == null) {
            if (other.m_suppressedBy != null)
                return false;
        } else if (!m_suppressedBy.equals(other.m_suppressedBy))
            return false;
        if (m_suppressedUntil == null) {
            if (other.m_suppressedUntil != null)
                return false;
        } else if (!m_suppressedUntil.equals(other.m_suppressedUntil))
            return false;
        if (m_ticketId == null) {
            if (other.m_ticketId != null)
                return false;
        } else if (!m_ticketId.equals(other.m_ticketId))
            return false;
        if (m_ticketState != other.m_ticketState)
            return false;
        if (m_uei == null) {
            if (other.m_uei != null)
                return false;
        } else if (!m_uei.equals(other.m_uei))
            return false;
        if (m_x733Cause != other.m_x733Cause)
            return false;
        if (m_x733Type == null) {
            if (other.m_x733Type != null)
                return false;
        } else if (!m_x733Type.equals(other.m_x733Type))
            return false;
        return true;
    }

}
