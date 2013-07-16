/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.threshd;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p>ThresholdGroup class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ThresholdGroup {
    
	private String m_name;
	private File m_rrdRepository;
	private ThresholdResourceType m_nodeResourceType;
	private ThresholdResourceType m_ifResourceType;
	private Map<String,ThresholdResourceType> m_genericResourceTypeMap = new HashMap<String,ThresholdResourceType>();

	/**
	 * <p>getIfResourceType</p>
	 *
	 * @return a {@link org.opennms.netmgt.threshd.ThresholdResourceType} object.
	 */
	public ThresholdResourceType getIfResourceType() {
		return m_ifResourceType;
	}

	/**
	 * <p>setIfResourceType</p>
	 *
	 * @param ifResourceType a {@link org.opennms.netmgt.threshd.ThresholdResourceType} object.
	 */
	public void setIfResourceType(ThresholdResourceType ifResourceType) {
		m_ifResourceType = ifResourceType;
	}

	/**
	 * <p>setName</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 */
	public void setName(String name) {
		m_name = (name == null ? null : name.intern());
	}

	/**
	 * <p>Constructor for ThresholdGroup.</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 */
	public ThresholdGroup(String name) {
		m_name = (name == null ? null : name.intern());
	}

	/**
	 * <p>getName</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getName() {
		return m_name;
	}
	
	
	/**
	 * <p>setRrdRepository</p>
	 *
	 * @param rrdRepository a {@link java.io.File} object.
	 */
	public void setRrdRepository(File rrdRepository) {
		m_rrdRepository = rrdRepository;
	}

	/**
	 * <p>getRrdRepository</p>
	 *
	 * @return a {@link java.io.File} object.
	 */
	public File getRrdRepository() {
		return m_rrdRepository;
	}

	/**
	 * <p>setNodeResourceType</p>
	 *
	 * @param nodeResourceType a {@link org.opennms.netmgt.threshd.ThresholdResourceType} object.
	 */
	public void setNodeResourceType(ThresholdResourceType nodeResourceType) {
		m_nodeResourceType = nodeResourceType;
		
	}

	/**
	 * <p>getNodeResourceType</p>
	 *
	 * @return a {@link org.opennms.netmgt.threshd.ThresholdResourceType} object.
	 */
	public ThresholdResourceType getNodeResourceType() {
		return m_nodeResourceType;
	}

	/*
	 * There are many GenericResourceTypes, for this reason, this will be mapped using a Map indexed by GenericResourceType name.
	 */
	/**
	 * <p>getGenericResourceTypeMap</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<String,ThresholdResourceType> getGenericResourceTypeMap() {
	    return m_genericResourceTypeMap;
	}

	/**
	 * <p>setGenericResourceTypeMap</p>
	 *
	 * @param genericResourceTypeMap a {@link java.util.Map} object.
	 */
	public void setGenericResourceTypeMap(Map<String,ThresholdResourceType> genericResourceTypeMap) {
	    m_genericResourceTypeMap = genericResourceTypeMap;
	}
	
	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
        @Override
	public String toString() {
	    StringBuilder buf = new StringBuilder();
	    buf.append(getName() + "={node:{");
	    if (getNodeResourceType() != null) {
	        buf.append(getNodeResourceType().getThresholdMap().values());
	    }
	    buf.append("}; iface:{");
	    if (getIfResourceType() != null) {
	        buf.append(getIfResourceType().getThresholdMap().values());
	    }
	    if (getGenericResourceTypeMap() != null) {
	        for (String rType : getGenericResourceTypeMap().keySet()) {
	            buf.append("}; " + rType + ":{");
	            buf.append(getGenericResourceTypeMap().get(rType).getThresholdMap().values());
	            buf.append("}");
	        }
	    }
	    buf.append("}");
	    String toString = buf.toString();
	    return toString;
	}
	
	/**
	 * <p>delete</p>
	 */
	public void delete() {
	    delete(getNodeResourceType());
	    delete(getIfResourceType());
	    for (String type : getGenericResourceTypeMap().keySet())
	        delete(getGenericResourceTypeMap().get(type));
	}

	private void delete(ThresholdResourceType type) {
	    Map<String,Set<ThresholdEntity>> entityMap = type.getThresholdMap();
	    for (String key : entityMap.keySet()) 
	        for (ThresholdEntity e : entityMap.get(key))
	            e.delete();
	}
	
}
