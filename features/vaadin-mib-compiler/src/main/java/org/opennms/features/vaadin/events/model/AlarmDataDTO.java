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

/**
 * The Class AlarmDataDTO.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public final class AlarmDataDTO implements Serializable {
    
    /** The reduction key. */
    private String reductionKey;
    
    /** The alarm type. */
    private Integer alarmType;
    
    /** The clear key. */
    private String clearKey;
    
    /** The auto clean. */
    private Boolean autoClean;
    
    /**
     * Instantiates a new alarm data DTO.
     */
    public AlarmDataDTO() {
        super();
    }
    
    /**
     * Gets the reduction key.
     *
     * @return the reduction key
     */
    public String getReductionKey() {
        return reductionKey;
    }
    
    /**
     * Sets the reduction key.
     *
     * @param reductionKey the new reduction key
     */
    public void setReductionKey(String reductionKey) {
        this.reductionKey = reductionKey;
    }
    
    /**
     * Gets the alarm type.
     *
     * @return the alarm type
     */
    public Integer getAlarmType() {
        return alarmType;
    }
    
    /**
     * Sets the alarm type.
     *
     * @param alarmType the new alarm type
     */
    public void setAlarmType(Integer alarmType) {
        this.alarmType = alarmType;
    }
    
    /**
     * Gets the clear key.
     *
     * @return the clear key
     */
    public String getClearKey() {
        return clearKey;
    }
    
    /**
     * Sets the clear key.
     *
     * @param clearKey the new clear key
     */
    public void setClearKey(String clearKey) {
        this.clearKey = clearKey;
    }
    
    /**
     * Gets the auto clean.
     *
     * @return the auto clean
     */
    public Boolean getAutoClean() {
        return autoClean;
    }
    
    /**
     * Sets the auto clean.
     *
     * @param autoClean the new auto clean
     */
    public void setAutoClean(Boolean autoClean) {
        this.autoClean = autoClean;
    }    
}
