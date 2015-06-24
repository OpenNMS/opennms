/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.threshd;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import org.opennms.core.utils.ParameterMap;

/**
 * <p>SnmpThresholdConfiguration class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class SnmpThresholdConfiguration {

    /**
     * Default thresholding group.
     */
    private static final String DEFAULT_GROUP = "default";

    /**
     * Default thresholding interval (in milliseconds).
     * 
     */
    private static final int DEFAULT_INTERVAL = 300000; // 300s or 5m

    /**
     * Default age before which a data point is considered "out of date"
     */
    private static final int DEFAULT_RANGE = 0; 

    private ThresholdGroup m_thresholdGroup;

    private int m_range;

    private int m_interval;

    /**
     * <p>Constructor for SnmpThresholdConfiguration.</p>
     *
     * @param thresholdsDao a {@link org.opennms.netmgt.threshd.ThresholdsDao} object.
     * @param parms a {@link java.util.Map} object.
     */
    public SnmpThresholdConfiguration(ThresholdsDao thresholdsDao, Map<?,?> parms) {
        setRange(ParameterMap.getKeyedInteger(parms, "range", SnmpThresholdConfiguration.DEFAULT_RANGE));
        setInterval(ParameterMap.getKeyedInteger(parms, "interval", SnmpThresholdConfiguration.DEFAULT_INTERVAL));
        setThresholdGroup(thresholdsDao.get(ParameterMap.getKeyedString(parms, "thresholding-group", DEFAULT_GROUP)));
    }

    /**
     * <p>getRange</p>
     *
     * @return a int.
     */
    public int getRange() {
        return m_range;
    }

    /**
     * <p>setRange</p>
     *
     * @param range a int.
     */
    public void setRange(int range) {
        m_range = range;
    }

    /**
     * <p>getInterval</p>
     *
     * @return a int.
     */
    public int getInterval() {
        return m_interval;
    }

    /**
     * <p>setInterval</p>
     *
     * @param interval a int.
     */
    public void setInterval(int interval) {
        m_interval = interval;
    }

    /**
     * <p>getThresholdGroup</p>
     *
     * @return a {@link org.opennms.netmgt.threshd.ThresholdGroup} object.
     */
    public ThresholdGroup getThresholdGroup() {
        return m_thresholdGroup;
    }

    /**
     * <p>setThresholdGroup</p>
     *
     * @param thresholdGroup a {@link org.opennms.netmgt.threshd.ThresholdGroup} object.
     */
    public void setThresholdGroup(ThresholdGroup thresholdGroup) {
        m_thresholdGroup = thresholdGroup;
    }

    /**
     * <p>getRrdRepository</p>
     *
     * @return a {@link java.io.File} object.
     */
    public File getRrdRepository() {
        return m_thresholdGroup.getRrdRepository();
    }

    /**
     * <p>getGroupName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getGroupName() {
        return m_thresholdGroup.getName();
    }

    /**
     * <p>getIfResourceType</p>
     *
     * @return a {@link org.opennms.netmgt.threshd.ThresholdResourceType} object.
     */
    public ThresholdResourceType getIfResourceType() {
        return m_thresholdGroup.getIfResourceType();
    }

    /**
     * <p>setIfResourceType</p>
     *
     * @param ifResourceType a {@link org.opennms.netmgt.threshd.ThresholdResourceType} object.
     */
    public void setIfResourceType(ThresholdResourceType ifResourceType) {
        m_thresholdGroup.setIfResourceType(ifResourceType);
    }

    /**
     * <p>getNodeResourceType</p>
     *
     * @return a {@link org.opennms.netmgt.threshd.ThresholdResourceType} object.
     */
    public ThresholdResourceType getNodeResourceType() {
        return m_thresholdGroup.getNodeResourceType();
    }

    /**
     * <p>setNodeResourceType</p>
     *
     * @param nodeResourceType a {@link org.opennms.netmgt.threshd.ThresholdResourceType} object.
     */
    public void setNodeResourceType(ThresholdResourceType nodeResourceType) {
        m_thresholdGroup.setNodeResourceType(nodeResourceType);
    }

    /**
     * <p>getGenericResourceTypeMap</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String,ThresholdResourceType> getGenericResourceTypeMap() {
        return Collections.unmodifiableMap(m_thresholdGroup.getGenericResourceTypeMap());
    }

    /**
     * <p>setGenericResourceTypeMap</p>
     *
     * @param genericResourceTypeMap a {@link java.util.Map} object.
     */
    public void setGenericResourceTypeMap(Map<String,ThresholdResourceType> genericResourceTypeMap) {
        m_thresholdGroup.setGenericResourceTypeMap(genericResourceTypeMap);
    }


}
