package org.opennms.features.vaadin.nodemaps.gwt.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.opennms.features.vaadin.nodemaps.gwt.client.openlayers.FeatureCollection;
import org.opennms.features.vaadin.nodemaps.gwt.client.openlayers.GeoJSONFeature;

import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.VConsole;

public class VOpenlayersWidget extends GWTOpenlayersWidget implements Paintable {

    @SuppressWarnings("unused")
    private ApplicationConnection m_client;
    @SuppressWarnings("unused")
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

        final List<GeoJSONFeature> features = new ArrayList<GeoJSONFeature>();

        // debugUIDL(uidl, 0);

        final UIDL nodeUIDL = uidl.getChildByTagName("nodes");
        
        for (final Iterator<?> iterator = nodeUIDL.getChildIterator(); iterator.hasNext(); ) {
            final UIDL node = (UIDL)iterator.next();
            
            final float longitude = node.getFloatAttribute("longitude");
            final float latitude = node.getFloatAttribute("latitude");

            VConsole.log("longitude = " + longitude + ", latitude = " + latitude);
            final GeoJSONFeature feature = GeoJSONFeature.create(longitude, latitude, Collections.singletonMap("label", node.getTag()));
            features.add(feature);
        }

        setFeatureCollection(FeatureCollection.create(features));
        updateFeatureLayer();
    }

    public void debugUIDL(final UIDL uidl, final int indent) {
        final String indentString = "                                                ".substring(0, indent);
        VConsole.log(indentString + "---- " + uidl.getTag() + " ----");
        for (final String variable: uidl.getVariableNames()) {
            VConsole.log(indentString + "V: " + variable + ": " + uidl.getStringVariable(variable));
        }
        for (final String attribute: uidl.getAttributeNames()) {
            VConsole.log(indentString + "A: " + attribute + ": " + uidl.getStringAttribute(attribute));
        }
        final Iterator<?> iterator = uidl.getChildIterator();
        if (iterator.hasNext()) {
            VConsole.log(indentString + "Children:");
            while (iterator.hasNext()) {
                debugUIDL((UIDL)iterator.next(), indent + 2);
            }
        }
    }
}
