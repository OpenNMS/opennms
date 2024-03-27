/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.threshd;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
	    final StringBuilder buf = new StringBuilder();
	    buf.append(getName() + "={node:{");
	    if (getNodeResourceType() != null) {
	        buf.append(getNodeResourceType().getThresholdMap().values());
	    }
	    buf.append("}; iface:{");
	    if (getIfResourceType() != null) {
	        buf.append(getIfResourceType().getThresholdMap().values());
	    }
	    if (getGenericResourceTypeMap() != null) {
	        for (final Entry<String, ThresholdResourceType> entry : getGenericResourceTypeMap().entrySet()) {
	            final String rType = entry.getKey();
	            buf.append("}; " + rType + ":{");
	            final ThresholdResourceType value = entry.getValue();
                    buf.append(value.getThresholdMap().values());
	            buf.append("}");
	        }
	    }
	    buf.append("}");
	    return buf.toString();
	}
	
	/**
	 * <p>delete</p>
	 */
	public void delete() {
	    delete(getNodeResourceType());
	    delete(getIfResourceType());
	    for (final Entry<String, ThresholdResourceType> entry : getGenericResourceTypeMap().entrySet()) {
	        final ThresholdResourceType value = entry.getValue();
	        delete(value);
	    }
	}

	private void delete(ThresholdResourceType type) {
	    final Map<String,Set<ThresholdEntity>> entityMap = type.getThresholdMap();
	    for (final Entry<String, Set<ThresholdEntity>> entry : entityMap.entrySet()) {
	        for (final ThresholdEntity e : entry.getValue()) {
	            e.delete();
	        }
	    }
	}
	
}
