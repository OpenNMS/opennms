package org.opennms.netmgt.threshd;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.collectd.CollectionAttribute;
import org.opennms.netmgt.collectd.CollectionResource;
import org.opennms.netmgt.dao.support.ResourceTypeUtils;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.poller.LatencyCollectionResource;

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
    static private Map<String, Double> s_cache = new ConcurrentHashMap<String,Double>();
    
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
    
    public CollectionResourceWrapper(int nodeId, String hostAddress, String serviceName, RrdRepository repository, CollectionResource resource, Map<String, CollectionAttribute> attributes) {
        m_nodeId = nodeId;
        m_hostAddress = hostAddress;
        m_serviceName = serviceName;
        m_repository = repository;
        m_resource = resource;
        m_attributes = attributes;
        if (isAnInterfaceResource()) {
            File resourceDir = getResourceDir();
            if (resourceDir != null) {
                JdbcIfInfoGetter ifInfoGetter = new JdbcIfInfoGetter();
                if (resource instanceof LatencyCollectionResource) {
                    m_iflabel = ifInfoGetter.getIfLabel(getNodeId(), resourceDir.getName());
                } else {
                    m_iflabel = resourceDir.getName();
                }
                m_ifInfo = ifInfoGetter.getIfInfoForNodeAndLabel(getNodeId(), m_iflabel);
                if (m_ifInfo != null) {
                    m_hostAddress = m_ifInfo.get("ipaddr"); // See Bug 2711
                    m_ifindex = m_ifInfo.get("snmpifindex");
                } else {
                    log().info("Can't find ifInfo for " + m_iflabel);
                }
            } else {
                log().info("Can't find resource directory for " + m_resource);
            }        
        }
    }    
    
    public int getNodeId() {
        return m_nodeId;
    }

    public String getHostAddress() {
        return m_hostAddress;
    }

    public String getServiceName() {
        return m_serviceName;
    }

    public RrdRepository getRepository() {
        return m_repository;
    }

    public String getLabel() {
        return m_label;
    }

    public void setLabel(String label) {
        m_label = label;
    }

    public String getInstance() {
        return m_resource != null ? m_resource.getInstance() : null;
    }
    
    public String getResourceTypeName() {
        return m_resource != null ? m_resource.getResourceTypeName() : null;
    }
    
    public File getResourceDir() {
        return m_resource != null ? m_resource.getResourceDir(m_repository) : null;
    }
    
    public String getIfLabel() {
        return m_iflabel;
    }
    
    public String getIfIndex() {
        return m_ifindex;
    }
    
    public String getIfInfoValue(String attribute) {
        return m_ifInfo.get(attribute);
    }
    
    public boolean isAnInterfaceResource() {
        return getResourceTypeName() != null && getResourceTypeName().equals("if");
    }

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
                log().debug("getAttributeValue: " + id + "(gauge) value= " + current);
            }
            return current;
        }
        return getCounterValue(id, current);
    }

    private Double getCounterValue(String id, Double current) {
        if (m_localCache.containsKey(id) == false) {
            Double last = s_cache.get(id);
            if (log().isDebugEnabled()) {
                log().debug("getAttributeValue: " + id + "(counter) last=" + last + ", current=" + current);
            }
            s_cache.put(id, current);
            if (last == null) {
                m_localCache.put(id, Double.NaN);
                log().info("getAttributeValue: unknown last value, ignoring current");
            } else if (current < last) {
                log().info("getAttributeValue: counter reset detected, ignoring value");
                m_localCache.put(id, Double.NaN);
            } else {
                m_localCache.put(id, current - last);
            }
        }
        return m_localCache.get(id);
    }

    public String getLabelValue(String ds) {
        if (ds == null)
            return null;
        if (log().isDebugEnabled()) {
            log().debug("getLabelValue: Getting Value for " + m_resource.getResourceTypeName() + "::" + ds);
        }
        String value = null;
        File resourceDirectory = m_resource.getResourceDir(m_repository);
        if (ds.equals("ID")) {
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
    
    @Override
    public String toString() {
        return m_resource.toString();
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

}
