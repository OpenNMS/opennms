package org.opennms.features.poller.remote.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class MapQuestMapPanel extends Composite {

    private static MapQuestMapPanelUiBinder uiBinder = GWT.create(MapQuestMapPanelUiBinder.class);

    interface MapQuestMapPanelUiBinder extends
            UiBinder<Widget, MapQuestMapPanel> {
    }

    public MapQuestMapPanel() {
        initWidget(uiBinder.createAndBindUi(this));
    }

}
