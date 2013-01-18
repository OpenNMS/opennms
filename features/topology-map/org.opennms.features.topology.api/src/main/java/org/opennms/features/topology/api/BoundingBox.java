package org.opennms.features.topology.api;

public class BoundingBox{
    private int m_left = Integer.MAX_VALUE;
    private int m_top = Integer.MAX_VALUE;
    private int m_right = Integer.MIN_VALUE;
    private int m_bottom = Integer.MIN_VALUE;
    
    public BoundingBox() {
        
    }

    public int getX() {
        return m_left;
    }

    public int getY() {
        return m_top;
    }

    public int getWidth() {
        return m_right - m_left;
    }

    public int getHeight() {
        return m_bottom - m_top;
    }

    public void addPoint(Point location) {
        m_left = Math.min(m_left, location.getX());
        m_right = Math.max(m_right, location.getX());
        m_top = Math.min(m_top,  location.getY());
        m_bottom = Math.max(m_bottom, location.getY());
    }
    
    
}