package org.opennms.netmgt.model.topology;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.model.BridgeElement;

public class Bridge {
    final Integer m_id;
    Integer m_rootPort;
    boolean m_isRootBridge=false;
    List<BridgeElement> m_elements = new ArrayList<BridgeElement>();

    public Bridge(Integer id) {
        super();
        m_id = id;
    }

    public Integer getRootPort() {
        return m_rootPort;
    }

    public void setRootPort(Integer rootPort) {
        m_rootPort = rootPort;
    }

    public boolean isRootBridge() {
        return m_isRootBridge;
    }

    public void setRootBridge(boolean isRootBridge) {
        m_isRootBridge = isRootBridge;
    }

    public Integer getId() {
        return m_id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_id == null) ? 0 : m_id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Bridge other = (Bridge) obj;
        if (m_id == null) {
            if (other.m_id != null)
                return false;
        } else if (!m_id.equals(other.m_id))
            return false;
        return true;
    }
    
    public String printTopology() {
    	StringBuffer strbfr = new StringBuffer();
        strbfr.append("bridge:[");
        strbfr.append(m_id);
        strbfr.append("], isrootbridge:");
        strbfr.append(m_isRootBridge);
        strbfr.append(" designated port:");
        strbfr.append(m_rootPort);
        strbfr.append("]\n");
        return strbfr.toString();

    }

    public void clearBridgeElement() {
    	m_elements.clear();
    }
	public List<BridgeElement> getBridgeElements() {
		return m_elements;
	}
	
	public boolean addBridgeElement(BridgeElement element) {
		if (element.getNode().getId().intValue() == m_id.intValue()) {
			m_elements.add(element);
			return true;
		}
		return false;
	}

}
