/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.features.vaadin.events.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class EventDTO.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public final class EventDTO implements Serializable {
    
    /** The uei. */
    private String uei;
    
    /** The event label. */
    private String eventLabel;
    
    /** The severity. */
    private String severity;
    
    /** The descr. */
    private String descr;
    
    /** The logmsg. */
    private LogMsgDTO logmsg;
    
    /** The mask. */
    private MaskDTO mask;
    
    /** The alarm data. */
    private AlarmDataDTO alarmData;
    
    /** The varbindsdecode list. */
    private List<VarbindsDecodeDTO> varbindsdecodeList;
    
    /**
     * Instantiates a new event DTO.
     */
    public EventDTO() {
        super();
        logmsg = new LogMsgDTO();
        mask = new MaskDTO();
        alarmData = new AlarmDataDTO();
        varbindsdecodeList = new ArrayList<VarbindsDecodeDTO>();
    }
    
    /**
     * Gets the uei.
     *
     * @return the uei
     */
    public String getUei() {
        return uei;
    }
    
    /**
     * Sets the uei.
     *
     * @param uei the new uei
     */
    public void setUei(String uei) {
        this.uei = uei;
    }
    
    /**
     * Gets the event label.
     *
     * @return the event label
     */
    public String getEventLabel() {
        return eventLabel;
    }
    
    /**
     * Sets the event label.
     *
     * @param eventLabel the new event label
     */
    public void setEventLabel(String eventLabel) {
        this.eventLabel = eventLabel;
    }
    
    /**
     * Gets the severity.
     *
     * @return the severity
     */
    public String getSeverity() {
        return severity;
    }
    
    /**
     * Sets the severity.
     *
     * @param severity the new severity
     */
    public void setSeverity(String severity) {
        this.severity = severity;
    }
    
    /**
     * Gets the descr.
     *
     * @return the descr
     */
    public String getDescr() {
        return descr;
    }
    
    /**
     * Sets the descr.
     *
     * @param descr the new descr
     */
    public void setDescr(String descr) {
        this.descr = descr;
    }
    
    /**
     * Gets the logmsg.
     *
     * @return the logmsg
     */
    public LogMsgDTO getLogmsg() {
        return logmsg;
    }
    
    /**
     * Sets the logmsg.
     *
     * @param logmsg the new logmsg
     */
    public void setLogmsg(LogMsgDTO logmsg) {
        this.logmsg = logmsg;
    }
    
    /**
     * Gets the mask.
     *
     * @return the mask
     */
    public MaskDTO getMask() {
        return mask;
    }
    
    /**
     * Sets the mask.
     *
     * @param mask the new mask
     */
    public void setMask(MaskDTO mask) {
        this.mask = mask;
    }
    
    /**
     * Gets the alarm data.
     *
     * @return the alarm data
     */
    public AlarmDataDTO getAlarmData() {
        return alarmData;
    }
    
    /**
     * Sets the alarm data.
     *
     * @param alarmData the new alarm data
     */
    public void setAlarmData(AlarmDataDTO alarmData) {
        this.alarmData = alarmData;
    }
    
    /**
     * Gets the varbindsdecode collection.
     *
     * @return the varbindsdecode collection
     */
    public List<VarbindsDecodeDTO> getVarbindsdecodeCollection() {
        return varbindsdecodeList;
    }
    
    /**
     * Sets the varbindsdecode collection.
     *
     * @param varbindsdecodeList the new varbindsdecode collection
     */
    public void setVarbindsdecodeCollection(List<VarbindsDecodeDTO> varbindsdecodeList) {
        this.varbindsdecodeList = varbindsdecodeList;
    }
}