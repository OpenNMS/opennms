package org.opennms.netmgt.provision.service.operations;

import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.model.OnmsNode;

public class ScanResource {
    private String m_type;
    private OnmsNode m_node = null;
    private final Map<String,String> m_attributes = new HashMap<String,String>();

    public ScanResource(String type) {
        m_type = type;
    }
    
    public String getType() {
        return m_type;
    }

    public void setNode(OnmsNode node) {
        m_node = node;
    }

    public OnmsNode getNode() {
        return m_node;
    }

    // TODO: change node comparison to use spring
    public void setAttribute(String key, String value) {
        m_attributes.put(key, value);
        if (m_node != null) {
            if (key.equals("sysContact")) {
                m_node.setSysContact(value);
            } else if (key.equals("sysDescription")) {
                m_node.setSysDescription(value);
            } else if (key.equals("sysLocation")) {
                m_node.setSysLocation(value);
            } else if (key.equals("sysObjectId")) {
                m_node.setSysObjectId(value);
            } else if (key.equals("sysName")) {
                m_node.setSysName(value);
            }
            
        }
    }

    public String getAttribute(String key) {
        return m_attributes.get(key);
    }
}
