/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.events;

import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.xml.eventconf.AlarmData;
import org.opennms.netmgt.xml.eventconf.LogDestType;
import org.opennms.netmgt.xml.eventconf.Logmsg;
import org.opennms.netmgt.xml.eventconf.Mask;

import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

/**
 * The Class Event Form.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class EventForm extends CustomComponent {

    /** The event UEI. */
    final TextField eventUei = new TextField("Event UEI");

    /** The event label. */
    final TextField eventLabel = new TextField("Event Label");

    /** The event description. */
    final TextArea eventDescr = new TextArea("Description");

    /** The log message destination. */
    final ComboBox logMsgDest = new ComboBox("Destination");

    /** The log message content. */
    final TextArea logMsgContent = new TextArea("Log Message");

    /** The event severity. */
    final ComboBox eventSeverity = new ComboBox("Severity");

    /** The has alarm data field. */
    final CheckBox hasAlarmData = new CheckBox("Add Alarm Data");

    /** The alarm data type. */
    final ComboBox alarmDataAlarmType = new ComboBox("Alarm Type");

    /** The alarm data clean. */
    final CheckBox alarmDataAutoClean = new CheckBox("Auto Clean");

    /** The alarm data reduction key. */
    final TextField alarmDataReductionKey = new TextField("Reduction Key");

    /** The alarm data clear key. */
    final TextField alarmDataClearKey = new TextField("Clear Key");

    /** The operator instructions. */
    final TextArea eventOperInstruct = new TextArea("Operator Instructions");

    /** The mask element collection. */
    final MaskElementField maskElements = new MaskElementField("Mask Elements");

    /** The mask varbind collection. */
    final MaskVarbindField maskVarbinds = new MaskVarbindField("Mask Varbinds");

    /** The varbinds decodes collection. */
    final VarbindsDecodeField varbindsDecodes = new VarbindsDecodeField("Varbind Decodes");

    /** The Event editor. */
    final BeanFieldGroup<org.opennms.netmgt.xml.eventconf.Event> eventEditor =
            new BeanFieldGroup<org.opennms.netmgt.xml.eventconf.Event>(org.opennms.netmgt.xml.eventconf.Event.class);

    /** The event layout. */
    final FormLayout eventLayout = new FormLayout();

    /**
     * Instantiates a new event form.
     */
    public EventForm() {
        setCaption("Event Detail");
        eventLayout.setSpacing(true);

        eventUei.setRequired(true);
        eventUei.setWidth("100%");
        eventLayout.addComponent(eventUei);

        eventLabel.setRequired(true);
        eventLabel.setWidth("100%");
        eventLayout.addComponent(eventLabel);

        eventDescr.setWidth("100%");
        eventDescr.setRows(10);
        eventDescr.setRequired(true);
        eventDescr.setNullRepresentation("");
        eventLayout.addComponent(eventDescr);

        logMsgDest.addItem("logndisplay");
        logMsgDest.addItem("logonly");
        logMsgDest.addItem("suppress");
        logMsgDest.addItem("donotpersist");
        logMsgDest.addItem("discardtraps");
        logMsgDest.setNullSelectionAllowed(false);
        logMsgDest.setRequired(true);
        eventLayout.addComponent(logMsgDest);

        logMsgContent.setWidth("100%");
        logMsgContent.setRows(10);
        logMsgContent.setRequired(true);
        logMsgContent.setNullRepresentation("");
        eventLayout.addComponent(logMsgContent);

        hasAlarmData.setWidth("100%");
        hasAlarmData.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                enableAlarmData(hasAlarmData.getValue());
            }
        });
        eventLayout.addComponent(hasAlarmData);

        alarmDataAlarmType.addItem(Integer.valueOf(1));
        alarmDataAlarmType.addItem(Integer.valueOf(2));
        alarmDataAlarmType.addItem(Integer.valueOf(3));
        alarmDataAlarmType.setDescription("<b>1</b> to be a problem that has a possible resolution, alarm-type set to <b>2</b> to be a resolution event, and alarm-type set to <b>3</b> for events that have no possible resolution");
        alarmDataAlarmType.setNullSelectionAllowed(false);
        alarmDataAlarmType.setVisible(false);
        alarmDataAlarmType.setRequired(true);
        eventLayout.addComponent(alarmDataAlarmType);

        alarmDataAutoClean.setWidth("100%");
        alarmDataAutoClean.setVisible(false);
        eventLayout.addComponent(alarmDataAutoClean);

        alarmDataReductionKey.setWidth("100%");
        alarmDataReductionKey.setNullRepresentation("");
        alarmDataReductionKey.setVisible(false);
        alarmDataReductionKey.setRequired(true);
        eventLayout.addComponent(alarmDataReductionKey);

        // TODO the clear-key is required only when the alarm-type is 2
        alarmDataClearKey.setWidth("100%");
        alarmDataClearKey.setNullRepresentation("");
        alarmDataClearKey.setVisible(false);
        eventLayout.addComponent(alarmDataClearKey);

        for (String sev : OnmsSeverity.names()) {
            eventSeverity.addItem(sev.substring(0, 1).toUpperCase() + sev.substring(1).toLowerCase());
        }
        eventSeverity.setNullSelectionAllowed(false);
        eventSeverity.setRequired(true);
        eventLayout.addComponent(eventSeverity);

        eventDescr.setWidth("100%");
        eventDescr.setRows(10);
        eventDescr.setRequired(true);
        eventDescr.setNullRepresentation("");
        eventLayout.addComponent(eventDescr);

        eventOperInstruct.setWidth("100%");
        eventOperInstruct.setRows(10);
        eventOperInstruct.setNullRepresentation("");
        eventLayout.addComponent(eventOperInstruct);

        eventLayout.addComponent(maskElements);
        eventLayout.addComponent(maskVarbinds);
        eventLayout.addComponent(varbindsDecodes);

        setEvent(createBasicEvent());

        eventEditor.bind(eventUei, "uei");
        eventEditor.bind(eventLabel, "eventLabel");
        eventEditor.bind(eventDescr, "descr");
        eventEditor.bind(logMsgDest, "logmsg.dest");
        eventEditor.bind(logMsgContent, "logmsg.content");
        eventEditor.bind(eventSeverity, "severity");
        eventEditor.bind(eventOperInstruct, "operinstruct");
        eventEditor.bind(maskElements, "mask.maskelements");
        eventEditor.bind(maskVarbinds, "mask.varbinds");
        eventEditor.bind(varbindsDecodes, "varbindsdecodes");

        setCompositionRoot(eventLayout);
    }

    /**
     * Enable alarm data.
     *
     * @param enable the enable
     */
    public void enableAlarmData(boolean enable) {
        if (hasAlarmData.getValue() != enable) {
            hasAlarmData.setValue(enable);
        }
        alarmDataAlarmType.setVisible(enable);
        alarmDataAutoClean.setVisible(enable);
        alarmDataReductionKey.setVisible(enable);
        alarmDataClearKey.setVisible(enable);
        if (enable) {
            if (getEvent().getAlarmData() == null) {
                getEvent().setAlarmData(new AlarmData());
            }
            eventEditor.bind(alarmDataAlarmType, "alarmData.alarmType");
            eventEditor.bind(alarmDataAutoClean, "alarmData.autoClean");
            eventEditor.bind(alarmDataReductionKey, "alarmData.reductionKey");
            eventEditor.bind(alarmDataClearKey, "alarmData.clearKey");
        } else {
            if (eventEditor.getPropertyId(alarmDataAlarmType) != null)
                eventEditor.unbind(alarmDataAlarmType);
            if (eventEditor.getPropertyId(alarmDataAutoClean) != null)
                eventEditor.unbind(alarmDataAutoClean);
            if (eventEditor.getPropertyId(alarmDataReductionKey) != null)
                eventEditor.unbind(alarmDataReductionKey);
            if (eventEditor.getPropertyId(alarmDataClearKey) != null)
                eventEditor.unbind(alarmDataClearKey);
        }
    }

    /**
     * Gets the event.
     *
     * @return the OpenNMS event
     */
    public org.opennms.netmgt.xml.eventconf.Event getEvent() {
        return eventEditor.getItemDataSource().getBean();
    }

    /**
     * Sets the event.
     *
     * @param event the new OpenNMS event
     */
    public void setEvent(org.opennms.netmgt.xml.eventconf.Event event) {
        // Normalize the Event Content (required to avoid UI problems)
        if (event.getMask() == null) {
            event.setMask(new Mask());
        }
        enableAlarmData(event.getAlarmData() != null);
        eventEditor.setItemDataSource(event);
    }

    /**
     * Creates the OpenNMS event.
     *
     * @return the basic example OpenNMS event
     */
    public org.opennms.netmgt.xml.eventconf.Event createBasicEvent() {
        org.opennms.netmgt.xml.eventconf.Event e = new org.opennms.netmgt.xml.eventconf.Event();
        e.setUei("uei.opennms.org/newEvent");
        e.setEventLabel("New Event");
        e.setDescr("New Event Description");
        e.setLogmsg(new Logmsg());
        e.getLogmsg().setContent("New Event Log Message");
        e.getLogmsg().setDest(LogDestType.LOGNDISPLAY);
        e.setSeverity("Indeterminate");
        e.setMask(new Mask());
        return e;
    }

    /**
     * Discard.
     */
    public void discard() {
        eventEditor.discard();
    }

    /**
     * Commit.
     *
     * @throws CommitException the commit exception
     */
    public void commit() throws CommitException {
        eventEditor.commit();
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractComponent#setReadOnly(boolean)
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        eventEditor.setReadOnly(readOnly);
        hasAlarmData.setVisible(!readOnly);
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractComponent#isReadOnly()
     */
    @Override
    public boolean isReadOnly() {
        return super.isReadOnly() && eventEditor.isReadOnly();
    }

}
