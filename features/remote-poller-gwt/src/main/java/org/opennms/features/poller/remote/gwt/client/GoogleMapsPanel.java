package org.opennms.features.poller.remote.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class GoogleMapsPanel extends Composite {

    private static GoogleMapsPanelUiBinder uiBinder = GWT.create(GoogleMapsPanelUiBinder.class);

    interface GoogleMapsPanelUiBinder extends
            UiBinder<Widget, GoogleMapsPanel> {
    }
    
    @UiField
    MapWidget m_mapWidget;

    public GoogleMapsPanel() {
        initWidget(uiBinder.createAndBindUi(this));
    }

}
