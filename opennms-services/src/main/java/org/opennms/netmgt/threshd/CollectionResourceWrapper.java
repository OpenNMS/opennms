package org.opennms.netmgt.threshd;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.collectd.AliasedResource;
import org.opennms.netmgt.collectd.CollectionAttribute;
import org.opennms.netmgt.collectd.CollectionResource;
import org.opennms.netmgt.collectd.IfInfo;
import org.opennms.netmgt.dao.support.ResourceTypeUtils;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.poller.LatencyCollectionResource;

/**
 * <p>CollectionResourceWrapper class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class CollectionResourceWrapper {
    
    private int m_nodeId;
    private String m_hostAddress;
    private String m_serviceName;
    private String m_label;
    private String m_iflabel;
    private String m_ifindex;
    private RrdRepository m_repository;
    private CollectionResource m_resource;
    private Map<String, CollectionAttribute> m_attributes;
    
    /*
     * Holds last values for counter attributes (in order to calculate delta)
     */
    static Map<String, Double> s_cache = new ConcurrentHashMap<String,Double>();
    
    /*
     * To avoid update static cache on every call of getAttributeValue.
     * In some cases, the same DS could be needed in many thresholds definitions for same resource.
     * See Bug 3193
     */
    private Map<String, Double> m_localCache = new HashMap<String,Double>();
    
    /*
     * Holds interface ifInfo data for interface resource only. This avoid multiple calls to database for same resource.
     */
    private Map<String, String> m_ifInfo;
    
    /*
     * Holds collection interval step. Counter attributes values must be returned as rates.
     */
    private long m_interval;
        
    /**
     * <p>Constructor for CollectionResourceWrapper.</p>
     *
     * @param interval a long.
     * @param nodeId a int.
     * @param hostAddress a {@link java.lang.String} object.
     * @param serviceName a {@link java.lang.String} object.
     * @param repository a {@link org.opennms.netmgt.model.RrdRepository} object.
     * @param resource a {@link org.opennms.netmgt.collectd.CollectionResource} object.
     * @param attributes a {@link java.util.Map} object.
     */
    public CollectionResourceWrapper(long interval, int nodeId, String hostAddress, String serviceName, RrdRepository repository, CollectionResource resource, Map<String, CollectionAttribute> attributes) {
        m_interval = interval;
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
        } catch(Exception e) {
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
        String id = m_resource.toString() + "." + ds;
        Double current = Double.parseDouble(numValue);
        if (m_attributes.get(ds).getType().toLowerCase().startsWith("counter") == false) {
            if (log().isDebugEnabled()) {
                log().debug("getAttributeValue: id=" + id + ", value= " + current);
            }
            return current;
        }
        return getCounterValue(id, current);
    }

    /*
     * This will return the rate based on configured collection step
     */
    private Double getCounterValue(String id, Double current) {
        if (m_localCache.containsKey(id) == false) {
            Double last = s_cache.get(id);
            if (log().isDebugEnabled()) {
                log().debug("getCounterValue: id=" + id + ", last=" + last + ", current=" + current);
            }
            s_cache.put(id, current);
            if (last == null) {
                m_localCache.put(id, Double.NaN);
                log().info("getCounterValue: unknown last value, ignoring current");
            } else {                
                Double delta = current.doubleValue() - last.doubleValue();
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
                    log().info("getCounterValue: " + id + "(counter) wrapped counter adjusted last=" + last + ", current=" + current + ", olddelta=" + delta + ", newdelta=" + newDelta);
                    delta = newDelta;
                }
                m_localCache.put(id, delta);
            }
        }
        return m_localCache.get(id) / m_interval;
    }

    /**
     * <p>getLabelValue</p>
     *
     * @param ds a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String getLabelValue(String ds) {
        if (ds == null)
            return null;
        if (log().isDebugEnabled()) {
            log().debug("getLabelValue: Getting Value for " + m_resource.getResourceTypeName() + "::" + ds);
        }
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
        } catch (Exception e) {
            log().info("getLabelValue: Can't get value for attribute " + ds + " for resource " + m_resource + ". " + e, e);
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
