package org.opennms.netmgt.provision.service.operations;

import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.model.OnmsNode;

/**
 * <p>ScanResource class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ScanResource {
    private String m_type;
    private OnmsNode m_node = null;
    private final Map<String,String> m_attributes = new HashMap<String,String>();

    /**
     * <p>Constructor for ScanResource.</p>
     *
     * @param type a {@link java.lang.String} object.
     */
    public ScanResource(String type) {
        m_type = type;
    }
    
    /**
     * <p>getType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getType() {
        return m_type;
    }

    /**
     * <p>setNode</p>
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    public void setNode(OnmsNode node) {
        m_node = node;
    }

    /**
     * <p>getNode</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    public OnmsNode getNode() {
        return m_node;
    }

    // TODO: change node comparison to use spring
    /**
     * <p>setAttribute</p>
     *
     * @param key a {@link java.lang.String} object.
     * @param value a {@link java.lang.String} object.
     */
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

    /**
     * <p>getAttribute</p>
     *
     * @param key a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String getAttribute(String key) {
        return m_attributes.get(key);
    }
}
