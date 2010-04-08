package org.opennms.features.poller.remote.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.gwtmapquest.transaction.MQAInfoWindow;
import com.googlecode.gwtmapquest.transaction.MQALargeZoomControl;
import com.googlecode.gwtmapquest.transaction.MQALatLng;
import com.googlecode.gwtmapquest.transaction.MQAPoi;
import com.googlecode.gwtmapquest.transaction.MQASize;
import com.googlecode.gwtmapquest.transaction.MQATileMap;

public class MapQuestMapPanel extends Composite {

    private static MapQuestMapPanelUiBinder uiBinder = GWT.create(MapQuestMapPanelUiBinder.class);
    
    @UiField
    SimplePanel m_mapHolder;
    
    private MQATileMap m_map;
    
    interface MapQuestMapPanelUiBinder extends UiBinder<Widget, MapQuestMapPanel> {}

    public MapQuestMapPanel() {
        initWidget(uiBinder.createAndBindUi(this));
        setMapWidget(MQATileMap.newInstance(m_mapHolder.getElement()));
    }

    private void setMapWidget(MQATileMap map) {
        m_map = map;
    }

    public MQATileMap getMapWidget() {
        return m_map;
    }

    void setSize(int offsetWidth, int offsetHeight) {
        getMapWidget().setSize(MQASize.newInstance(offsetWidth, offsetHeight));
    }

    void removeShape(MQAPoi marker) {
        getMapWidget().removeShape(marker);
    }

    void addShape(final MQAPoi point) {
        getMapWidget().addShape(point);
    }

    void setCenter(final GWTLatLng latLng) {
        getMapWidget().setCenter(MQALatLng.newInstance(latLng.getLatitude(), latLng.getLongitude()));
    }

    MQAInfoWindow getInfoWindow() {
        return getMapWidget().getInfoWindow();
    }

    void addControl(MQALargeZoomControl zoomControl) {
        getMapWidget().addControl(zoomControl);
    }

    void setZoomLevel(int level) {
        getMapWidget().setZoomLevel(level);
    }

}
