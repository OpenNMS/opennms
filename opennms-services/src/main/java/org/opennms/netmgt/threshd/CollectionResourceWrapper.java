package org.opennms.netmgt.threshd;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.collectd.CollectionAttribute;
import org.opennms.netmgt.collectd.CollectionResource;
import org.opennms.netmgt.dao.support.ResourceTypeUtils;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;

public class CollectionResourceWrapper {
    
    private int m_nodeId;
    private String m_hostAddress;
    private String m_serviceName;
    private RrdRepository m_repository;
    private CollectionResource m_resource;
    private Map<String, CollectionAttribute> m_attributes;
    
    /*
     * Holds last values for counter attributes (in order to calculate delta)
     */
    static private Map<String, Double> s_cache = new ConcurrentHashMap<String,Double>();

    public CollectionResourceWrapper(int nodeId, String hostAddress, String serviceName, RrdRepository repository, CollectionResource resource, Map<String, CollectionAttribute> attributes) {
        m_nodeId = nodeId;
        m_hostAddress = hostAddress;
        m_serviceName = serviceName;
        m_repository = repository;
        m_resource = resource;
        m_attributes = attributes;
    }
    
    public Double getAttributeValue(String ds) {
        if (m_attributes.get(ds) == null) {
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
        Double last = s_cache.get(id);
        if (log().isDebugEnabled()) {
            log().debug("getAttributeValue: " + id + "(counter) last=" + last + ", current=" + current);
        }
        s_cache.put(id, current);
        if (last == null) {
            return Double.NaN;
        }
        if (current < last) {
            log().info("getAttributeValue: counter reset detected, ignoring value");
            return Double.NaN;
        }
        return current - last;
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
            if (m_resource.getResourceTypeName().equals("if")) {
                String ifLabel = resourceDirectory.getName();
                value = getIfInfo(m_nodeId, ifLabel, ds);
            }
            if (value == null) { // Find value on collected string attributes
                value = m_attributes.containsKey(ds) ? m_attributes.get(ds).getStringValue() : null;
            }
            if (value == null) { // Find value on saved string attributes                
                value = ResourceTypeUtils.getStringProperty(resourceDirectory, ds);
            }
        } catch (Exception e) {
            log().info("getLabelValue: Can't get value for attribute " + ds + " for resource " + m_resource + ". " + e, e);
        }
        return value;
    }

    /*
     * FIXME Why ?
     * I think that this should be part of ThresholdEntity implementation
     */
    public void completeEventList(List<Event> eventList, String dsLabel) {
        String dsLabelValue = getLabelValue(dsLabel);
        if (dsLabelValue == null) dsLabelValue = "Unknown";
        for (Event event : eventList) {
            event.setNodeid(m_nodeId);
            event.setService(m_serviceName);
            event.setInterface(m_hostAddress);
            Parms eventParms = event.getParms();
            Parm eventParm;
            Value parmValue;
            if (dsLabelValue != null) {
                eventParm = new Parm();
                eventParm.setParmName("label");
                parmValue = new Value();
                parmValue.setContent(dsLabelValue);
                eventParm.setValue(parmValue);
                eventParms.addParm(eventParm);
            }
            if (m_resource.getResourceTypeName().equals("if")) {
                File resourceDir = m_resource.getResourceDir(m_repository);
                String ifLabel = resourceDir.getName();
                String snmpIfIndex = getIfInfo(m_nodeId, ifLabel, "snmpifindex");
                if (ifLabel != null) {
                    eventParm = new Parm();
                    eventParm.setParmName("ifLabel");
                    parmValue = new Value();
                    parmValue.setContent(ifLabel);
                    eventParm.setValue(parmValue);
                    eventParms.addParm(eventParm);
                }
                if (snmpIfIndex != null) {
                    eventParm = new Parm();
                    eventParm.setParmName("ifIndex");
                    parmValue = new Value();
                    parmValue.setContent(snmpIfIndex);
                    eventParm.setValue(parmValue);
                    eventParms.addParm(eventParm);
                }                
            }
        }
    }
    
    private String getIfInfo(final int nodeid, final String ifLabel, final String attributeName) {
        return new JdbcIfInfoGetter().getIfInfoForNodeAndLabel(m_nodeId, ifLabel).get(attributeName);
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

}
