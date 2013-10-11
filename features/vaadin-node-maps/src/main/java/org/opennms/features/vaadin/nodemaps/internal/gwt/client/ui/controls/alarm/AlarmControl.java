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

package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.controls.alarm;

import java.util.logging.Logger;

import org.discotools.gwt.leaflet.client.controls.Control;
import org.discotools.gwt.leaflet.client.jsobject.JSObject;
import org.discotools.gwt.leaflet.client.map.Map;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.OpenNMSEventManager;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.AlarmSeverityUpdatedEvent;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.AlarmSeverityUpdatedEventHandler;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.ComponentInitializedEvent;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

public class AlarmControl extends Control implements AlarmSeverityUpdatedEventHandler {
    Logger logger = Logger.getLogger(getClass().getName());

    private ListBox m_severityBox;
    private OpenNMSEventManager m_eventManager;

    public AlarmControl(final OpenNMSEventManager eventManager) {
        super(JSObject.createJSObject());
        setJSObject(AlarmControlImpl.create(this, new AlarmControlOptions().getJSObject()));
        m_eventManager = eventManager;
    }

    public Element doOnAdd(final JavaScriptObject map) {
        logger.info("AlarmControl.doOnAdd()");
        final AlarmControlCss css = AlarmControlBundle.INSTANCE.css();
        css.ensureInjected();

        final Element element = AlarmControlImpl.createElement("leaflet-control-alarm");
        element.addClassName("leaflet-control");

        final Label label = new Label("Show Severity >=");
        label.getElement().setAttribute("for", "alarmControl");
        label.addStyleName(css.label());
        element.appendChild(label.getElement());

        m_severityBox = new ListBox(false);
        m_severityBox.getElement().setId("alarmControl");
        m_severityBox.addItem("Normal", "0");
        m_severityBox.addItem("Warning", "4");
        m_severityBox.addItem("Minor", "5");
        m_severityBox.addItem("Major", "6");
        m_severityBox.addItem("Critical", "7");

        m_severityBox.addChangeHandler(new ChangeHandler() {
            @Override public void onChange(final ChangeEvent event) {
                final int selected = m_severityBox.getSelectedIndex();
                logger.info("new selection index = " + selected);
                final String value = m_severityBox.getValue(selected);
                logger.info("new severity = " + value);
                final int intValue = value == null? 0 : Integer.valueOf(value).intValue();
                m_eventManager.fireEvent(new AlarmSeverityUpdatedEvent(intValue));
            }
        });

        m_severityBox.addStyleName(css.label());
        element.appendChild(m_severityBox.getElement());

        logger.info("AlarmControl.doOnAdd(): finished, returning: " + element);
        
        m_eventManager.fireEvent(new ComponentInitializedEvent(AlarmControl.class.getName()));
        return element;
    }

    public void doOnRemove(final JavaScriptObject map) {
        logger.info("doOnRemove() called");
        m_eventManager.fireEvent(new AlarmSeverityUpdatedEvent(0));
    }

    @Override
    public AlarmControl addTo(final Map map) {
        return (AlarmControl)super.addTo(map);
    }

    @Override
    public AlarmControl setPosition(final String position) {
        return (AlarmControl)super.setPosition(position);
    }

    @Override
    public AlarmControl removeFrom(final Map map) {
        return (AlarmControl)super.removeFrom(map);
    }

    @Override
    public void onAlarmSeverityUpdated(final AlarmSeverityUpdatedEvent event) {
        m_severityBox.setItemSelected(event.getSeverity(), true);
    }
}
