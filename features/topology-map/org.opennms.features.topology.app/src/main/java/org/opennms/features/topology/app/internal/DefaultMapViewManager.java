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
package org.opennms.features.topology.app.internal;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.opennms.features.topology.api.BoundingBox;
import org.opennms.features.topology.api.MapViewManager;
import org.opennms.features.topology.api.MapViewManagerListener;
import org.opennms.features.topology.api.Point;

public class DefaultMapViewManager implements MapViewManager{
    
    private BoundingBox m_mapBounds  = new BoundingBox(0, 0, 100, 100);
    private int m_viewPortWidth = 100;
    private int m_viewPortHeight = 100;
    private double m_scale = 0.0;
    private Point m_center = new Point(0,0);
    private Set<MapViewManagerListener> m_listeners = new CopyOnWriteArraySet<>();
    
    @Override
    public void addListener(MapViewManagerListener listener) {
        m_listeners.add(listener);
    }
    
    @Override
    public void removeListener(MapViewManagerListener listener) {
        m_listeners.remove(listener);
    }
    
    private void fireUpdate() {
        for(MapViewManagerListener listener : m_listeners) {
            listener.boundingBoxChanged(this);
        }
    }
    @Override
    public void setMapBounds(BoundingBox boundingBox) {
        // If the bounding box is smaller than the viewport, the bounding box is scaled that it
        // maximizes the used area but still has the same aspect ratio as the viewport.
        // In earlier versions this was done afterwards but that leads to boundingBoxChanged events fired (due to fireUpdate())
        // and that caused the UI to behave not correctly. Changing the boundingBox before avoids unnecessary listener
        // notifications, because the m_mapBounds equals the bounding box with aspect ratio
        if(boundingBox.getWidth() < m_viewPortWidth && boundingBox.getHeight() < m_viewPortHeight) {
            boundingBox = boundingBox.computeWithAspectRatio(getViewPortAspectRatio());
        }
        if (!m_mapBounds.equals(boundingBox)) {
            if(boundingBox.getHeight() < m_viewPortHeight/2){
                //Don't allow the height to be less than half the viewport height
                m_mapBounds = new BoundingBox(boundingBox.getCenter(), boundingBox.getWidth(), m_viewPortHeight/2);
            } else {
                m_mapBounds = boundingBox;
            }

            m_center = m_mapBounds.getCenter();
            fireUpdate();
        }
    }
    @Override
    public void setViewPort(int width, int height) {
        int oldWidth = m_viewPortWidth;
        int oldHeight = m_viewPortHeight;
                
        m_viewPortWidth = width;
        m_viewPortHeight = height;
        
        if(oldWidth != m_viewPortWidth || oldHeight != m_viewPortHeight) {
            fireUpdate();
        }
    }
    
    @Override
    public double getViewPortAspectRatio() {
        return m_viewPortWidth < 0 ? -1 : m_viewPortWidth / (double)m_viewPortHeight;
    }
    @Override
    public void setCenter(Point point) {
        Point oldCenter = m_center;
        m_center = point;
        if(!oldCenter.equals(m_center)) {
            fireUpdate();
        }
    }
    
    @Override
    public void zoomToPoint(double scale, Point center) {
        double oldScale = m_scale;
        m_scale = scale;
        m_scale = Math.min(1.0, m_scale);
        m_scale = Math.max(0.0, m_scale);
        m_scale = ((double)Math.round(m_scale * 10.0))/10.0;

        Point oldCenter = m_center;
        m_center = center;

        // TODO: Sonar is warning on the equals comparison of m_scale and oldScale
        if(m_scale != oldScale || !oldCenter.equals(m_center)) {
            fireUpdate();
        }
    }
    
    @Override
    public BoundingBox getCurrentBoundingBox() {
        if(m_mapBounds == null) {
            throw new IllegalStateException("map boundaries bounds must be set");
        }
        BoundingBox mPrime = m_mapBounds.computeWithAspectRatio(getViewPortAspectRatio());
        int width = (int)Math.round(Math.pow((double)mPrime.getWidth(), 1.0 - m_scale) * Math.pow((double)m_viewPortWidth/2.0, m_scale));
        int height = (int)Math.round(Math.pow((double)mPrime.getHeight(), 1.0 - m_scale) * Math.pow((double)m_viewPortHeight/2.0, m_scale));
        
        return new BoundingBox(m_center, width, height);
    }
    
    @Override
    public double getScale() { 
        return m_scale;
        
    }
    
    @Override
    public void setScale(double scale) {
        double oldScale = m_scale;
        m_scale = scale;
        m_scale = Math.min(1.0, m_scale);
        m_scale = Math.max(0.0, m_scale);
        m_scale = ((double)Math.round(m_scale * 10.0))/10.0;

        // TODO: Sonar is warning on the equals comparison of m_scale and oldScale
        if(oldScale != m_scale) {
            fireUpdate();
        }
    }
    
    @Override
    public void setBoundingBox(BoundingBox boundingBox) {
        BoundingBox oldBoundingBox = getCurrentBoundingBox();
        BoundingBox bbPrime = boundingBox.computeWithAspectRatio(getViewPortAspectRatio());
        BoundingBox mPrime = m_mapBounds.computeWithAspectRatio(getViewPortAspectRatio());
        double oldScale = m_scale;
        m_scale = Math.log(bbPrime.getWidth()/(double)mPrime.getWidth()) / Math.log( (m_viewPortWidth/2.0) / (double)mPrime.getWidth());
        m_scale = Math.min(1.0, m_scale);
        m_scale = Math.max(0.0, m_scale);
        m_scale = ((double)Math.round(m_scale * 10.0))/10.0;
        
        Point oldCenter = m_center;
        m_center = boundingBox.getCenter();
        
        BoundingBox newBoundingBox = getCurrentBoundingBox();
        // TODO: Sonar is warning on the equals comparison of m_scale and oldScale
        if(!oldCenter.equals(m_center) || oldScale != m_scale || !oldBoundingBox.equals(newBoundingBox)) {
            fireUpdate();
        }
    }

    @Override
    public int getViewPortHeight() {
        return m_viewPortHeight;
    }

    @Override
    public int getViewPortWidth() {
        return m_viewPortWidth;
    }
    
    @Override
    public String toString() {
        return "Map bounds [ " + m_mapBounds + "]  || view [ width: " + getViewPortWidth() + " :: height: " + getViewPortHeight() + " ] || currentBoundingBox: [ " + getCurrentBoundingBox() + " ]" +
        		"  scale: " + getScale() + " || centerPoint: [ " + m_center + " ]" ;
    }
}
