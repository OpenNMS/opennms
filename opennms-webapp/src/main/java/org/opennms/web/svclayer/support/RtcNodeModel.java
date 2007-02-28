package org.opennms.web.svclayer.support;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.model.OnmsNode;

public class RtcNodeModel {
    private static final DecimalFormat AVAILABILITY_FORMAT = new DecimalFormat("0.000%");
    
    private List<RtcNode> m_nodeList = new ArrayList<RtcNode>();
    
    static {
        AVAILABILITY_FORMAT.setMultiplier(100);
    }
    
    public void addNode(RtcNode node) {
        m_nodeList.add(node);
    }
    
    public List<RtcNode> getNodeList() {
        return m_nodeList;
    }
    
    public static class RtcNode {
        private OnmsNode m_node;
        private int m_serviceCount;
        private int m_downServiceCount;
        private double m_availability;
        
        public RtcNode(OnmsNode node, int serviceCount, int downServiceCount, double availability) {
            m_node = node;
            m_serviceCount = serviceCount;
            m_downServiceCount = downServiceCount;
            m_availability = availability;
        }

        public double getAvailability() {
            return m_availability;
        }

        public int getDownServiceCount() {
            return m_downServiceCount;
        }

        public OnmsNode getNode() {
            return m_node;
        }

        public int getServiceCount() {
            return m_serviceCount;
        }
        
        public String toString() {
            return m_node.getLabel() + ": " + m_downServiceCount + " of " + m_serviceCount + ": " + AVAILABILITY_FORMAT.format(m_availability);
        }
    }
}
