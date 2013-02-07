/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.vaadin.nodemaps.gwt.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opennms.features.vaadin.nodemaps.gwt.client.openlayers.FeatureCollection;
import org.opennms.features.vaadin.nodemaps.gwt.client.openlayers.NodeFeature;

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

    public native final void log(final String message) /*-{
        console.log(message);
    }-*/;

    @Override
    public void updateFromUIDL(final UIDL uidl, final ApplicationConnection client) {
        if (client.updateComponent(this, uidl, true)) return;
        m_client = client;
        m_uidlId = uidl.getId();

        final UIDL nodeUIDL = uidl.getChildByTagName("nodes");

        final FeatureCollection featureCollection = FeatureCollection.create();
        for (final Iterator<?> iterator = nodeUIDL.getChildIterator(); iterator.hasNext();) {
            final UIDL node = (UIDL) iterator.next();
            final NodeFeature feature = NodeFeature.create(node).cast();
            log(feature.asString());
            featureCollection.add(feature);
        }
        setFeatures(featureCollection);

        final UIDL alarmUIDL = uidl.getChildByTagName("alarms");

        final Map<Integer,List<Alarm>> alarms = new HashMap<Integer,List<Alarm>>();
        for (final Iterator<?> iterator = alarmUIDL.getChildIterator(); iterator.hasNext(); ) {
            final UIDL uidlAlarm = (UIDL) iterator.next();
            final Alarm alarm = Alarm.create(uidlAlarm);
            addAlarm(alarms, alarm);
        }
        setAlarms(alarms);

        updateFeatureLayer();
    }

    private void addAlarm(final Map<Integer, List<Alarm>> alarms, final Alarm alarm) {
        final List<Alarm> alarmList;
        final Integer nodeId = alarm.getNodeId();
        if (alarms.containsKey(nodeId)) {
            alarmList = alarms.get(nodeId);
        } else {
            alarmList = new ArrayList<Alarm>();
            alarms.put(nodeId, alarmList);
        }
        alarmList.add(alarm);
    }
}
