package org.opennms.features.vaadin.nodemaps.gwt.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
            // VConsole.log("longitude = " + longitude + ", latitude = " + latitude);

            final String[] stringKeys = new String[] { "severityLabel", "nodeLabel", "foreignSource", "foreignId", "ipAddress" };
            final String[] intKeys    = new String[] { "severity", "nodeId", "unackedCount" };

            final Map<String,String> stringAttributes = new HashMap<String,String>(); 
            final Map<String,Integer> intAttributes = new HashMap<String,Integer>();
            for (final String key : stringKeys) {
                if (node.hasAttribute(key)) stringAttributes.put(key, node.getStringAttribute(key));
            }
            for (final String key : intKeys) {
                if (node.hasAttribute(key)) intAttributes.put(key, node.getIntAttribute(key));
            }

            final GeoJSONFeature feature = GeoJSONFeature.create(longitude, latitude, stringAttributes, intAttributes);
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
