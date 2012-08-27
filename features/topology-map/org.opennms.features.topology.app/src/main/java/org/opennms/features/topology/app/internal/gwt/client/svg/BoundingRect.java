package org.opennms.features.topology.app.internal.gwt.client.svg;

import com.google.gwt.touch.client.Point;

public class BoundingRect {
    
    Point m_topLeft;
    Point m_bottomRight;
    
    public void addClientRect(ClientRect boundingClientRect) {
        
    }

    public double getCenterX() {
        return (m_topLeft.getX() + m_bottomRight.getX()) / 2;
    }

    public double getCenterY() {
        return (m_topLeft.getY() + m_bottomRight.getY()) / 2;
    }
    
    public double getWidth() {
        return m_bottomRight.getX() - m_topLeft.getX();
    }
    
    public double getHeight() {
        return m_bottomRight.getY() - m_topLeft.getY();
    }

    public void addPoint(Point point) {
        if(m_topLeft == null) {
            m_topLeft = point;
        }else {
            m_topLeft = new Point(Math.min(m_topLeft.getX(), point.getX()), Math.min(m_topLeft.getY(), point.getY()));
        }
        
        if(m_bottomRight == null) {
            m_bottomRight = point;
        }else {
            m_bottomRight = new Point(Math.max(m_bottomRight.getX(), point.getX()), Math.max(m_bottomRight.getY(), point.getY()));
        }
    }

}
