package org.opennms.features.vaadin.nodemaps.gwt.client;

import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.VConsole;

public class VOpenlayersWidget extends GWTOpenlayersWidget implements Paintable {

    private ApplicationConnection m_client;
    private String m_uidlId;

    public VOpenlayersWidget() {
        super();
        setStyleName("v-openlayers");
        VConsole.log("div ID = " + getElement().getId());
    }

    @Override
    public void updateFromUIDL(final UIDL uidl, final ApplicationConnection client) {
        if (client.updateComponent(this, uidl, true)) return;
        m_client = client;
        m_uidlId = uidl.getId();
    }
}
