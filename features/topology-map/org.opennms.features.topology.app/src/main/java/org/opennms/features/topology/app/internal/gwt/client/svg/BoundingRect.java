package org.opennms.features.topology.app.internal.gwt.client.svg;

import com.google.gwt.touch.client.Point;

public class BoundingRect {
    
    Point m_topLeft;
    Point m_bottomRight;
    
    public double getCenterX() {
        return (m_topLeft.getX() + m_bottomRight.getX()) / 2;
    }

    public double getCenterY() {
        return (m_topLeft.getY() + m_bottomRight.getY()) / 2;
    }
    
    public double getWidth() {
        double width = m_bottomRight.getX() - m_topLeft.getX();
        
        return width;
    }
    
    public double getHeight() {
        return m_bottomRight.getY() - m_topLeft.getY();
    }

    public void addPoint(Point point) {
        if(m_topLeft == null) {
            m_topLeft = point;
        }else {
            m_topLeft = new Point(Math.min(m_topLeft.getX(), point.getX()) - 50, Math.min(m_topLeft.getY(), point.getY()) - 50);
        }
        
        if(m_bottomRight == null) {
            m_bottomRight = point;
        }else {
            m_bottomRight = new Point(Math.max(m_bottomRight.getX(), point.getX() + 50), Math.max(m_bottomRight.getY(), point.getY()) + 50);
        }
    }

}
