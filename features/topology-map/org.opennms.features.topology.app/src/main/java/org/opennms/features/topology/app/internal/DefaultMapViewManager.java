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
    private Set<MapViewManagerListener> m_listeners = new CopyOnWriteArraySet<MapViewManagerListener>();
    
    public void addListener(MapViewManagerListener listener) {
        m_listeners.add(listener);
    }
    
    public void removeListener(MapViewManagerListener listener) {
        m_listeners.remove(listener);
    }
    
    private void fireUpdate() {
        for(MapViewManagerListener listener : m_listeners) {
            listener.boundingBoxChanged(this);
        }
    }
    public void setMapBounds(BoundingBox boundingBox) {
        m_mapBounds = boundingBox;
        m_center = m_mapBounds.getCenter();
        
        fireUpdate();
    }
    public void setViewPort(int width, int height) {
        int oldWidth = m_viewPortWidth;
        int oldHeight = m_viewPortHeight;
                
        m_viewPortWidth = width;
        m_viewPortHeight = height;
        
        if(oldWidth != m_viewPortWidth || oldHeight != m_viewPortHeight) {
            fireUpdate();
        }
    }
    
    public double getViewPortAspectRatio() {
        return m_viewPortWidth < 0 ? -1 : m_viewPortWidth / (double)m_viewPortHeight;
    }
    public void setCenter(Point point) {
        Point oldCenter = m_center;
        m_center = point;
        if(!oldCenter.equals(m_center)) {
            fireUpdate();
        }
    }
    
    public void zoomToPoint(double scale, Point center) {
        double oldScale = m_scale;
        m_scale = scale;
        m_scale = Math.min(1.0, m_scale);
        m_scale = Math.max(0.0, m_scale);
        m_scale = ((int)Math.round(m_scale*10))/10.0;
        Point oldCenter = m_center;
        m_center = center;
        
        if(m_scale != oldScale || !oldCenter.equals(m_center)) {
            fireUpdate();
        }
    }
    
    public BoundingBox getCurrentBoundingBox() {
        if(m_viewPortWidth < 0 || m_mapBounds == null) {
            //return m_mapBounds;
            //throw new IllegalStateException("View port and maps bounds must be set");
        }
        BoundingBox mPrime = m_mapBounds.computeWithAspectRatio(getViewPortAspectRatio());
        int width = (int) (Math.pow(mPrime.getWidth(), 1 - m_scale) * Math.pow(m_viewPortWidth/2, m_scale));
        int height = (int) (Math.pow(mPrime.getHeight(), 1 - m_scale) * Math.pow(m_viewPortHeight/2, m_scale));
        
        return new BoundingBox(m_center, width, height); 
    }
    
    public double getScale() { 
        return m_scale;
        
    }
    
    public void setScale(double scale) {
        double oldScale = m_scale;
        m_scale = scale;
        m_scale = Math.min(1.0, m_scale);
        m_scale = Math.max(0.0, m_scale);
        if(oldScale != m_scale) {
            fireUpdate();
        }
        
    }
    
    public void setBoundingBox(BoundingBox boundingBox) {
        BoundingBox oldBoundingBox = getCurrentBoundingBox();
        BoundingBox bbPrime = boundingBox.computeWithAspectRatio(getViewPortAspectRatio());
        BoundingBox mPrime = m_mapBounds.computeWithAspectRatio(getViewPortAspectRatio());
        double oldScale = m_scale;
        m_scale = Math.log(bbPrime.getWidth()/(double)mPrime.getWidth()) / Math.log( (m_viewPortWidth/2.0) / (double)mPrime.getWidth());
        m_scale = Math.min(1.0, m_scale);
        m_scale = Math.max(0.0, m_scale);
        m_scale = (int)(Math.round(m_scale*10))/10.0;
        
        Point oldCenter = m_center;
        m_center = boundingBox.getCenter();
        
        BoundingBox newBoundingBox = getCurrentBoundingBox();
        if(!oldCenter.equals(m_center) || oldScale != m_scale || !oldBoundingBox.equals(newBoundingBox)) {
            fireUpdate();
        }
        
    }

    public int getViewPortHeight() {
        return m_viewPortHeight;
    }

    public int getViewPortWidth() {
        return m_viewPortWidth;
    }
    
    public String toString() {
        return "Map bounds [ " + m_mapBounds + "]  || view [ width: " + getViewPortWidth() + " :: height: " + getViewPortHeight() + " ] || currentBoundingBox: [ " + getCurrentBoundingBox() + " ]" +
        		"  scale: " + getScale() + " || centerPoint: [ " + m_center + " ]" ;
    }
}