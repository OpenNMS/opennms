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
        double width = m_bottomRight.getX() - m_topLeft.getX() + 100;
        
        return width;
    }
    
    public double getHeight() {
        return m_bottomRight.getY() - m_topLeft.getY() + 100;
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
    
    public boolean isEmpty() {
        return m_bottomRight == null && m_topLeft == null;
    }

}
