package org.opennms.features.topology.api;

public interface MapViewManager {

    public void addListener(MapViewManagerListener listener);
    public void removeListener(MapViewManagerListener listener);
    public void setMapBounds(BoundingBox boundingBox);
    public void setBoundingBox(BoundingBox boundingBox);
    public void setViewPort(int width, int height);
    public double getViewPortAspectRatio();
    public void setCenter(Point point);
    public void zoomToPoint(double scale, Point center);
    public BoundingBox getCurrentBoundingBox();
    public double getScale();
    public void setScale(double scale);
    public int getViewPortHeight();
    public int getViewPortWidth();
}
