/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.api;

public class BoundingBox{
    private int m_left = Integer.MAX_VALUE;
    private int m_top = Integer.MAX_VALUE;
    private int m_right = Integer.MIN_VALUE;
    private int m_bottom = Integer.MIN_VALUE;
    
    public BoundingBox() {
        
    }

    public BoundingBox(int x, int y, int width, int height) {
        m_left = x;
        m_top = y;
        m_right = x + width;
        m_bottom = y + height;
    }
    
    public BoundingBox(Point center, int width, int height) {
        m_left = center.getX() - width /2 ;
        m_top = center.getY() - height /2;
        m_right = m_left + width;
        m_bottom = m_top + height;
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
    
    public Point getCenter() {
        Point p = new Point(getX() + (getWidth()/2), getY() + (getHeight()/2));
        return p;
    }
    
    public void setCenter(Point center) {
        m_left = center.getX() - getWidth()/2;
        m_top = center.getY() - getHeight()/2; 
    }
    
    public BoundingBox computeWithAspectRatio(double R) {
        double r = getAspectRatio();
        int width =  (int) (r < R ? Math.round(getHeight() * R ): getWidth());
        int height = (int) (r < R ? getHeight() : Math.round(getWidth() / R));
        Point center = getCenter();
        int x = center.getX() - width/2;
        int y = center.getY() - height/2;
        return new BoundingBox(x, y, width, height);
    }

    private double getAspectRatio() {
        return getHeight() == 0 ? 0 : (double)getWidth()/(double)getHeight();
    }
    
    public String toString() {
        return "x: " + getX() + " y: " + getY() + " width: " + getWidth() + " height: " + getHeight();
    }
    
    public String fragment() {
        return "(" + getX() + "," + getY() + "," + getWidth() + "," + getHeight() + ")";
    }

    public void addBoundingbox(BoundingBox box) {
        m_left = Math.min(m_left, box.m_left);
        m_right = Math.max(m_right, box.m_right);
        m_top = Math.min(m_top, box.m_top);
        m_bottom = Math.max(m_bottom, box.m_bottom);
        
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + m_bottom;
        result = prime * result + m_left;
        result = prime * result + m_right;
        result = prime * result + m_top;
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
        BoundingBox other = (BoundingBox) obj;
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