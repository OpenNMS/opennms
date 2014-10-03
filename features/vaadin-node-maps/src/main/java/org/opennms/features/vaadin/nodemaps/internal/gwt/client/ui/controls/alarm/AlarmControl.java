/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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

import org.opennms.features.vaadin.nodemaps.internal.gwt.client.AlarmSeverity;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.ComponentTracker;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.OpenNMSEventManager;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.AlarmSeverityUpdatedEvent;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.AlarmSeverityUpdatedEventHandler;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

public class AlarmControl extends AbsolutePanel implements AlarmSeverityUpdatedEventHandler {
    private final Logger LOG = Logger.getLogger(getClass().getName());

    private ListBox m_severityBox;
    private OpenNMSEventManager m_eventManager;
    private ComponentTracker m_componentTracker;

    public AlarmControl(final OpenNMSEventManager eventManager, final ComponentTracker componentTracker) {
        m_eventManager = eventManager;
        m_componentTracker = componentTracker;
        addAttachHandler(new Handler() {
            @Override public void onAttachOrDetach(final AttachEvent event) {
                if (event.isAttached()) {
                    doOnAdd();
                } else {
                    doOnRemove();
                }
            }
        });
    }

    public Element doOnAdd() {
        LOG.info("AlarmControl.doOnAdd()");
        final AlarmControlCss css = AlarmControlBundle.INSTANCE.css();
        css.ensureInjected();

        m_eventManager.addHandler(AlarmSeverityUpdatedEvent.TYPE, this);

        this.setStylePrimaryName("leaflet-control-alarm");
        this.addStyleName("leaflet-bar");
        this.addStyleName("leaflet-control");

        final Label label = new Label("Show Severity >=");
        label.getElement().setAttribute("for", "alarmControl");
        label.addStyleName(css.label());

        m_severityBox = new ListBox(false);
        m_severityBox.getElement().setId("alarmControl");
        for (final AlarmSeverity sev : AlarmSeverity.values()) {
            m_severityBox.addItem(sev.getLabel());
        }

        m_severityBox.addChangeHandler(new ChangeHandler() {
            @Override public void onChange(final ChangeEvent event) {
                final int selected = m_severityBox.getSelectedIndex();
                LOG.info("new selection index = " + selected);
                final String value = m_severityBox.getValue(selected);
                LOG.info("new severity = " + value);
                m_eventManager.fireEvent(new AlarmSeverityUpdatedEvent(AlarmSeverity.get(value)));
                event.stopPropagation();
            }
        });

        m_severityBox.addStyleName(css.label());

        this.add(label);
        this.add(m_severityBox);

        LOG.info("AlarmControl.doOnAdd(): finished, returning: " + this.getElement());
        m_componentTracker.ready(getClass());
        return this.getElement();
    }

    public void doOnRemove() {
        LOG.info("doOnRemove() called");
        m_eventManager.removeHandler(AlarmSeverityUpdatedEvent.TYPE, this);
        m_eventManager.fireEvent(new AlarmSeverityUpdatedEvent(AlarmSeverity.NORMAL));
    }

    @Override
    public void onAlarmSeverityUpdated(final AlarmSeverityUpdatedEvent event) {
        setSeverity(event.getSeverity());
    }

    public void setSeverity(final AlarmSeverity severity) {
        m_severityBox.setItemSelected(severity.ordinal(), true);
    }
}
