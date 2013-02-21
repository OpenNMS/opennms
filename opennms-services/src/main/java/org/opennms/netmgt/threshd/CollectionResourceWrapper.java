/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.collectd.AliasedResource;
import org.opennms.netmgt.collectd.IfInfo;
import org.opennms.netmgt.config.collector.CollectionAttribute;
import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.dao.support.ResourceTypeUtils;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.poller.LatencyCollectionResource;

/**
 * <p>CollectionResourceWrapper class.</p>
 * 
 * Wraps a CollectionResource with some methods and caching for the efficient application of thresholds (without
 * pulling thresholding code into CollectionResource itself)
 * 
 * A fresh instance should be created for each collection cycle (assumptions are made based on that premise)
 *
 * @author ranger
 * @version $Id: $
 */
public class CollectionResourceWrapper {
    
    private final int m_nodeId;
    private final String m_hostAddress;
    private final String m_serviceName;
    private String m_label;
    private String m_iflabel;
    private String m_ifindex;
    private final RrdRepository m_repository;
    private final CollectionResource m_resource;
    private final Map<String, CollectionAttribute> m_attributes;
    
    /**
     * Keeps track of both the Double value, and when it was collected, for the static cache of attributes
     * 
     * This is necessary for the *correct* calculation of Counter rates, across variable collection times and possible
     * collection failures (see NMS-4244)
     * 
     * Just a holder class for two associated values; no need for the formality of accessors
     */
    static class CacheEntry {
        final Date timestamp;
        final Double value;
        public CacheEntry(final Date timestamp, final Double value) {
            if (timestamp == null) {
                throw new IllegalArgumentException("Illegal null timestamp in cache value");
            } else if (value == null) {
                throw new IllegalArgumentException("Illegal null value in cache value");
            }
            this.timestamp = timestamp;
            this.value = value;
        }
    }

    /*
     * Holds last values for counter attributes (in order to calculate delta)
     */
    static final ConcurrentHashMap<String, CacheEntry> s_cache = new ConcurrentHashMap<String,CacheEntry>();
    
    /*
     * To avoid update static cache on every call of getAttributeValue.
     * In some cases, the same DS could be needed in many thresholds definitions for same resource.
     * See Bug 3193
     */
    private final Map<String, Double> m_localCache = new HashMap<String,Double>();
    
    /*
     * Holds interface ifInfo data for interface resource only. This avoid multiple calls to database for same resource.
     */
    private Map<String, String> m_ifInfo;
    
    /*
	 * Holds the timestamp of the collection being thresholded, for the calculation of counter rates
     */
    private final Date m_collectionTimestamp;
        
    /**
     * <p>Constructor for CollectionResourceWrapper.</p>
     *
     * @param interval a long.
     * @param nodeId a int.
     * @param hostAddress a {@link java.lang.String} object.
     * @param serviceName a {@link java.lang.String} object.
     * @param repository a {@link org.opennms.netmgt.model.RrdRepository} object.
     * @param resource a {@link org.opennms.netmgt.config.collector.CollectionResource} object.
     * @param attributes a {@link java.util.Map} object.
     */
    public CollectionResourceWrapper(Date collectionTimestamp, int nodeId, String hostAddress, String serviceName, RrdRepository repository, CollectionResource resource, Map<String, CollectionAttribute> attributes) {
        if (collectionTimestamp == null) {
            throw new IllegalArgumentException(String.format("%s: Null collection timestamp when thresholding service %s on node %d (%s)", this.getClass().getSimpleName(), serviceName, nodeId, hostAddress));
        }

        m_collectionTimestamp = collectionTimestamp;
        m_nodeId = nodeId;
        m_hostAddress = hostAddress;
        m_serviceName = serviceName;
        m_repository = repository;
        m_resource = resource;
        m_attributes = attributes;
        if (isAnInterfaceResource()) {
            if (resource instanceof AliasedResource) { // TODO What about AliasedResource's custom attributes?
                m_iflabel = ((AliasedResource) resource).getLabel();
                m_ifInfo = ((AliasedResource) resource).getIfInfo().getAttributesMap();
                m_ifInfo.put("domain", ((AliasedResource) resource).getDomain());
            }
            if (resource instanceof IfInfo) {
                m_iflabel = ((IfInfo) resource).getLabel();
                m_ifInfo = ((IfInfo) resource).getAttributesMap();
            }
            if (resource instanceof LatencyCollectionResource) {
                JdbcIfInfoGetter ifInfoGetter = new JdbcIfInfoGetter();
                String ipAddress = ((LatencyCollectionResource) resource).getIpAddress();
                m_iflabel = ifInfoGetter.getIfLabel(getNodeId(), ipAddress);
                if (m_iflabel != null) { // See Bug 3488
                    m_ifInfo = ifInfoGetter.getIfInfoForNodeAndLabel(getNodeId(), m_iflabel);
                } else {
                    log().info("Can't find ifLabel for latency resource " + resource.getInstance() + " on node " + getNodeId());                    
                }
            }
            if (m_ifInfo != null) {
                m_ifindex = m_ifInfo.get("snmpifindex");
            } else {
                log().info("Can't find ifInfo for " + resource);
            }
        }
    }    
    
    /**
     * <p>getNodeId</p>
     *
     * @return a int.
     */
    public int getNodeId() {
        return m_nodeId;
    }

    /**
     * <p>getHostAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getHostAddress() {
        return m_hostAddress;
    }

    /**
     * <p>getServiceName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceName() {
        return m_serviceName;
    }

    /**
     * <p>getRepository</p>
     *
     * @return a {@link org.opennms.netmgt.model.RrdRepository} object.
     */
    public RrdRepository getRepository() {
        return m_repository;
    }

    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLabel() {
        return m_label;
    }

    /**
     * <p>setLabel</p>
     *
     * @param label a {@link java.lang.String} object.
     */
    public void setLabel(String label) {
        m_label = label;
    }

    /**
     * <p>getInstance</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getInstance() {
        return m_resource != null ? m_resource.getInstance() : null;
    }
    
    /**
     * <p>getResourceTypeName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getResourceTypeName() {
        return m_resource != null ? m_resource.getResourceTypeName() : null;
    }
    
    /**
     * <p>getIfLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIfLabel() {
        return m_iflabel;
    }
    
    /**
     * <p>getIfIndex</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIfIndex() {
        return m_ifindex;
    }
    
    /**
     * <p>getIfInfoValue</p>
     *
     * @param attribute a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    protected String getIfInfoValue(String attribute) {
        if (m_ifInfo != null)
            return m_ifInfo.get(attribute);
        return null;
    }
    
    /**
     * <p>isAnInterfaceResource</p>
     *
     * @return a boolean.
     */
    public boolean isAnInterfaceResource() {
        return getResourceTypeName() != null && getResourceTypeName().equals("if");
    }

    /**
     * <p>isValidInterfaceResource</p>
     *
     * @return a boolean.
     */
    public boolean isValidInterfaceResource() {
        if (m_ifInfo == null) {
            return false;
        }
        try {
            if(null == m_ifindex)
                return false;
            if(Integer.parseInt(m_ifindex) < 0)
                return false;
        } catch(Throwable e) {
            return false;
        }
        return true;
    }

    /*
     * FIXME What happen with numeric fields from strings.properties ?
     */ 
    /**
     * <p>getAttributeValue</p>
     *
     * @param ds a {@link java.lang.String} object.
     * @return a {@link java.lang.Double} object.
     */
    public Double getAttributeValue(String ds) {
        if (m_attributes == null || m_attributes.get(ds) == null) {
            log().warn("getAttributeValue: can't find attribute called " + ds + " on " + m_resource);
            return null;
        }
        String numValue = m_attributes.get(ds).getNumericValue();
        if (numValue == null) {
            log().warn("getAttributeValue: can't find numeric value for " + ds + " on " + m_resource);
            return null;
        }
        // Generating a unique ID for the node/resourceType/resource/metric combination.
        String id =  "node[" + m_nodeId + "].resourceType[" + m_resource.getResourceTypeName() + "].instance[" + m_resource.getLabel() + "].metric[" + ds + "]";
        Double current = null;
        try {
            current = Double.parseDouble(numValue);
        } catch (NumberFormatException e) {
            log().error(id + " does not have a numeric value: " + numValue);
            return null;
        }
        if (m_attributes.get(ds).getType().toLowerCase().startsWith("counter") == false) {
            if (log().isDebugEnabled()) {
                log().debug("getAttributeValue: id=" + id + ", value= " + current);
            }
            return current;
        } else {
            return getCounterValue(id, current);
        }
    }

    /*
     * This will return the rate based on configured collection step
     */
    private Double getCounterValue(String id, Double current) {
        synchronized (m_localCache) {

        if (m_localCache.containsKey(id) == false) {
            // Atomically replace the CacheEntry with the new value
            CacheEntry last = s_cache.put(id, new CacheEntry(m_collectionTimestamp, current));
            if (log().isDebugEnabled()) {
                log().debug("getCounterValue: id=" + id + ", last=" + 
                		(last==null ? last : last.value +"@"+last.timestamp) + 
                		", current=" + current);
            }
            if (last == null) {
                log().info("getCounterValue: unknown last value, ignoring current");
                m_localCache.put(id, Double.NaN);
            } else {                
                Double delta = current.doubleValue() - last.value.doubleValue();
                // wrapped counter handling(negative delta), rrd style
                if (delta < 0) {
                    double newDelta = delta.doubleValue();
                    // 2-phase adjustment method
                    // try 32-bit adjustment
                    newDelta += Math.pow(2, 32);
                    if (newDelta < 0) {
                        // try 64-bit adjustment
                        newDelta += Math.pow(2, 64) - Math.pow(2, 32);
                    }
                    log().info("getCounterValue: " + id + 
                    		"(counter) wrapped counter adjusted last=" + 
                    		last.value +"@"+last.timestamp +
                    		", current=" + current + 
                    		", olddelta=" + delta + 
                    		", newdelta=" + newDelta);
                    delta = newDelta;
                }
                // Get the interval between when this current collection was taken, and the last time this
                // value was collected (and had a counter rate calculated for it).
                // If the interval is zero, than the current rate must returned as 0.0 since there can be 
                // no delta across a time interval of zero.
                long interval = ( m_collectionTimestamp.getTime() - last.timestamp.getTime() ) / 1000;
                if (interval > 0) {
                    m_localCache.put(id, delta / interval);
                } else {
                    log().warn("getCounterValue: invalid zero-length rate interval for " + id + ", returning rate of zero");
                    m_localCache.put(id, 0.0);
                    // Restore the original value inside the static cache
                    s_cache.put(id, last);
                }
            }
        }
        Double value = m_localCache.get(id);
        // This is just a sanity check, we should never have a value of null for the value at this point
        if (value == null) {
            log().error("getCounterValue: value was not calculated correctly for " + id + ", using NaN");
            m_localCache.put(id, Double.NaN);
            return Double.NaN;
        } else {
            return value;
        }

        }
    }

    /**
     * <p>getLabelValue</p>
     *
     * @param ds a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String getLabelValue(String ds) {
        if (ds == null || ds.equals(""))
            return null;
        if (log().isDebugEnabled()) {
            log().debug("getLabelValue: Getting Value for " + m_resource.getResourceTypeName() + "::" + ds);
        }
        if ("nodeid".equals(ds))
            return Integer.toString(m_nodeId);
        if ("ipaddress".equals(ds))
            return m_hostAddress;
        if ("iflabel".equals(ds))
            return getIfLabel();
        String value = null;
        File resourceDirectory = m_resource.getResourceDir(m_repository);
        if ("ID".equals(ds)) {
            return resourceDirectory.getName();
        }
        try {
            if (isAnInterfaceResource()) { // Get Value from ifInfo only for Interface Resource
                value = getIfInfoValue(ds);
            }
            if (value == null) { // Find value on saved string attributes                
                value = ResourceTypeUtils.getStringProperty(resourceDirectory, ds);
            }
        } catch (Throwable e) {
            log().info("getLabelValue: Can't get value for attribute " + ds + " for resource " + m_resource + ". " + e, e);
        }
        if (value == null) {
            log().debug("getLabelValue: The field " + ds + " is not a string property. Trying to parse it as numeric metric.");
            Double d = getAttributeValue(ds);
            if (d != null)
                value = d.toString();
        }
        return value;
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return m_resource.toString();
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

}
