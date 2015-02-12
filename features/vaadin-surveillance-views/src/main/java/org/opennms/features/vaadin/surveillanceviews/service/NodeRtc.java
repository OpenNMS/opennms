package org.opennms.features.vaadin.surveillanceviews.service;

import org.opennms.netmgt.model.OnmsNode;

import java.text.DecimalFormat;

/**
 * Created by chris on 12.02.15.
 */
public class NodeRtc {
    private static final DecimalFormat AVAILABILITY_FORMAT = new DecimalFormat("0.000%");

    static {
        AVAILABILITY_FORMAT.setMultiplier(100);
    }

    private OnmsNode m_node;
    private int m_serviceCount;
    private int m_downServiceCount;
    private double m_availability;

    public NodeRtc(OnmsNode node, int serviceCount, int downServiceCount, double availability) {
        m_node = node;
        m_serviceCount = serviceCount;
        m_downServiceCount = downServiceCount;
        m_availability = availability;
    }

    public double getAvailability() {
        return m_availability;
    }

    public String getAvailabilityAsString() {
        return AVAILABILITY_FORMAT.format(m_availability);
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

    @Override
    public String toString() {
        return m_node.getLabel() + ": " + m_downServiceCount + " of " + m_serviceCount + ": " + getAvailabilityAsString();
    }
}
