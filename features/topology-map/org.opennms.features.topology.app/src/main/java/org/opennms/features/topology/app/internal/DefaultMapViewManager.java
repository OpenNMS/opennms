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
        if(m_viewPortWidth < 0 || m_mapBounds == null) {
            //return m_mapBounds;
            //throw new IllegalStateException("View port and maps bounds must be set");
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