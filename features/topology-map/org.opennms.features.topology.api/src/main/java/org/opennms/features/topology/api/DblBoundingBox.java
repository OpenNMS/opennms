/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.api;

import java.awt.geom.Point2D;

public class DblBoundingBox{
    private double m_left = Double.MAX_VALUE;
    private double m_top = Double.MAX_VALUE;
    private double m_right = Double.MIN_VALUE;
    private double m_bottom = Double.MIN_VALUE;
    
    public DblBoundingBox() {
        
    }
    
    public DblBoundingBox(DblBoundingBox box) {
        m_left = box.m_left;
        m_right = box.m_right;
        m_top = box.m_top;
        m_bottom = box.m_bottom;
    }

    public DblBoundingBox(double x, double y, double width, double height) {
        m_left = x;
        m_top = y;
        m_right = x + width;
        m_bottom = y + height;
    }
    
    public DblBoundingBox(Point2D center, double width, double height) {
        m_left   = center.getX() - width /2 ;
        m_top    = center.getY() - height /2;
        m_right  = m_left + width;
        m_bottom = m_top + height;
    }

    public double getX() {
        return m_left;
    }

    public double getY() {
        return m_top;
    }

    public double getWidth() {
        return m_right - m_left;
    }

    public double getHeight() {
        return m_bottom - m_top;
    }

    public void addPoint(Point2D location) {
        m_left = Math.min(m_left, location.getX());
        m_right = Math.max(m_right, location.getX());
        m_top = Math.min(m_top,  location.getY());
        m_bottom = Math.max(m_bottom, location.getY());
    }
    
    public Point2D getCenter() {
        return new Point2D.Double(getX() + (getWidth()/2), getY() + (getHeight()/2));
    }
    
    public void setCenter(Point center) {
        m_left = center.getX() - getWidth()/2;
        m_top  = center.getY() - getHeight()/2;
    }
    
    public DblBoundingBox computeWithAspectRatio(double R) {
        double r = getAspectRatio();
        double width =  r < R ? Math.round(getHeight() * R ): getWidth();
        double height = r < R ? getHeight() : Math.round(getWidth() / R);
        Point2D center = getCenter();
        return new DblBoundingBox(center, width, height);
    }

    private double getAspectRatio() {
        return getHeight() == 0 ? 0 : (double)getWidth()/(double)getHeight();
    }
    
    @Override
    public String toString() {
        return "x: " + getX() + " y: " + getY() + " width: " + getWidth() + " height: " + getHeight();
    }
    
    public String fragment() {
        return "(" + getX() + "," + getY() + "," + getWidth() + "," + getHeight() + ")";
    }

    public void addBoundingbox(DblBoundingBox box) {
        m_left = Math.min(m_left, box.m_left);
        m_right = Math.max(m_right, box.m_right);
        m_top = Math.min(m_top, box.m_top);
        m_bottom = Math.max(m_bottom, box.m_bottom);
        
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        double result = 1;
        result = prime * result + m_bottom;
        result = prime * result + m_left;
        result = prime * result + m_right;
        result = prime * result + m_top;
        return Double.valueOf(result).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DblBoundingBox other = (DblBoundingBox) obj;
        if (m_bottom != other.m_bottom)
            return false;
        if (m_left != other.m_left)
            return false;
        if (m_right != other.m_right)
            return false;
        if (m_top != other.m_top)
            return false;
        return true;
    }
    
    
    
    
}