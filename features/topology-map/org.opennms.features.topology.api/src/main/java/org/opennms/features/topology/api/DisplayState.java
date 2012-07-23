package org.opennms.features.topology.api;

import com.vaadin.data.Property;

public interface DisplayState {
    
    public static final String SEMANTIC_ZOOM_LEVEL = "semanticZoomLevel";
    public static final String SCALE = "scale";
    public static final String LAYOUT_ALGORITHM = "layoutAlgorithm";
    
    public abstract Integer getSemanticZoomLevel();
    
    public abstract void setSemanticZoomLevel(Integer level);

    public abstract void setLayoutAlgorithm(LayoutAlgorithm layoutAlgorithm);
    
    public abstract LayoutAlgorithm getLayoutAlgorithm();

    public abstract void redoLayout();

    public abstract Property getProperty(String property);

}