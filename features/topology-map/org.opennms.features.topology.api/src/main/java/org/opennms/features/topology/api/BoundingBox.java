/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.topology.api;

public class BoundingBox{
    private int m_left = Integer.MAX_VALUE;
    private int m_top = Integer.MAX_VALUE;
    private int m_right = Integer.MIN_VALUE;
    private int m_bottom = Integer.MIN_VALUE;
    
    public BoundingBox() {
        
    }
    
    public BoundingBox(BoundingBox box) {
        m_left = box.m_left;
        m_right = box.m_right;
        m_top = box.m_top;
        m_bottom = box.m_bottom;
    }

    public BoundingBox(int x, int y, int width, int height) {
        m_left = x;
        m_top = y;
        m_right = x + width;
        m_bottom = y + height;
    }
    
    public BoundingBox(Point center, int width, int height) {
        m_left = (int)center.getX() - width /2 ;
        m_top = (int)center.getY() - height /2;
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
        //TODO cast to int for now
        m_left = Math.min(m_left, (int)location.getX());
        m_right = Math.max(m_right, (int)location.getX());
        m_top = Math.min(m_top,  (int)location.getY());
        m_bottom = Math.max(m_bottom, (int)location.getY());
    }
    
    public Point getCenter() {
    	return new Point(getX() + (getWidth()/2), getY() + (getHeight()/2));
    }
    
    public void setCenter(Point center) {
        //TODO cast to int for now
        m_left = (int)center.getX() - getWidth()/2;
        m_top = (int)center.getY() - getHeight()/2;
    }
    
    public BoundingBox computeWithAspectRatio(double R) {
        double r = getAspectRatio();
        int width =  (int) (r < R ? Math.round(getHeight() * R ): getWidth());
        int height = (int) (r < R ? getHeight() : Math.round(getWidth() / R));
        Point center = getCenter();
        //TODO cast to int for now
        int x = (int)center.getX() - width/2;
        int y = (int)center.getY() - height/2;
        return new BoundingBox(x, y, width, height);
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