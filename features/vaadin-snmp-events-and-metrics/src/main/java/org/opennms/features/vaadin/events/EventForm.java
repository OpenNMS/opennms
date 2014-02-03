/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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
package org.opennms.features.vaadin.events;

import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.xml.eventconf.AlarmData;
import org.opennms.netmgt.xml.eventconf.Logmsg;
import org.opennms.netmgt.xml.eventconf.Mask;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.NestedMethodProperty;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

/*
 * According with the following JUnit test, the nested properties binding is supported:
 * https://github.com/vaadin/vaadin/blob/master/uitest/src/com/vaadin/tests/fieldgroup/FormWithNestedProperties.java
 */
/**
 * The Class Event Form.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class EventForm extends CustomComponent {

    /** The event uei. */
    @PropertyId("uei")
    final TextField eventUei = new TextField("Event UEI");

    /** The event label. */
    @PropertyId("eventLabel")
    final TextField eventLabel = new TextField("Event Label");

    /** The descr. */
    @PropertyId("descr")
    final TextArea eventDescr = new TextArea("Description");

    /** The logmsg dest. */
    @PropertyId("logMsgDest")
    final ComboBox logMsgDest = new ComboBox("Destination");

    /** The logmsg content. */
    @PropertyId("logMsgContent")
    final TextArea logMsgContent = new TextArea("Log Message");

    /** The severity. */
    @PropertyId("severity")
    final ComboBox eventSeverity = new ComboBox("Severity");

    /** The alarm data alarm type. */
    @PropertyId("alarmDataAlarmType")
    final ComboBox alarmDataAlarmType = new ComboBox("Alarm Type");

    /** The alarm data auto clean. */
    @PropertyId("alarmDataAutoClean")
    final CheckBox alarmDataAutoClean = new CheckBox("Auto Clean");

    /** The alarm data reduction key. */
    @PropertyId("alarmDataReductionKey")
    final TextField alarmDataReductionKey = new TextField("Reduction Key");

    /** The alarm data clear key. */
    @PropertyId("alarmDataClearKey")
    final TextField alarmDataClearKey = new TextField("Clear Key");

    /** The oper. */
    @PropertyId("operinstruct")
    final TextArea eventOperInstruct = new TextArea("Operator Instructions");

    /** The mask elements. */
    @PropertyId("maskElements")
    final MaskElementField maskElements = new MaskElementField("Mask Elements");

    /** The mask varbinds. */
    @PropertyId("maskVarbinds")
    final MaskVarbindField maskVarbinds = new MaskVarbindField("Mask Varbinds");

    /** The varbinds decodes. */
    @PropertyId("varbindsdecodeCollection")
    final VarbindsDecodeField varbindsDecodes = new VarbindsDecodeField("Varbind Decodes");

    /** The Event editor. */
    private final FieldGroup eventEditor = new FieldGroup();

    /** The event layout. */
    private final FormLayout eventLayout = new FormLayout();

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

        alarmDataAlarmType.addItem(new Integer(1));
        alarmDataAlarmType.addItem(new Integer(2));
        alarmDataAlarmType.addItem(new Integer(3));
        alarmDataAlarmType.setDescription("<b>1</b> to be a problem that has a possible resolution, alarm-type set to <b>2</b> to be a resolution event, and alarm-type set to <b>3</b> for events that have no possible resolution");
        alarmDataAlarmType.setNullSelectionAllowed(false);
        eventLayout.addComponent(alarmDataAlarmType);

        alarmDataAutoClean.setWidth("100%");
        eventLayout.addComponent(alarmDataAutoClean);

        alarmDataReductionKey.setWidth("100%");
        alarmDataReductionKey.setNullRepresentation("");
        eventLayout.addComponent(alarmDataReductionKey);

        alarmDataClearKey.setWidth("100%");
        alarmDataClearKey.setNullRepresentation("");
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
        eventEditor.bindMemberFields(this);

        setCompositionRoot(eventLayout);
    }

    /**
     * Gets the event.
     *
     * @return the OpenNMS event
     */
    @SuppressWarnings("unchecked")
    public org.opennms.netmgt.xml.eventconf.Event getEvent() {
        return ((BeanItem<org.opennms.netmgt.xml.eventconf.Event>) eventEditor.getItemDataSource()).getBean();
    }

    /**
     * Sets the event.
     *
     * @param event the new OpenNMS event
     */
    public void setEvent(org.opennms.netmgt.xml.eventconf.Event event) {
        // Normalize the Event Content (required to avoid UI problems)
        if (event.getMask() == null)
            event.setMask(new Mask());
        if (event.getAlarmData() == null)
            event.setAlarmData(new AlarmData());
        // Create the BeanItem
        BeanItem<org.opennms.netmgt.xml.eventconf.Event> item = new BeanItem<org.opennms.netmgt.xml.eventconf.Event>(event);
        item.addItemProperty("logMsgContent", new NestedMethodProperty<String>(item.getBean(), "logmsg.content"));
        item.addItemProperty("logMsgDest", new NestedMethodProperty<String>(item.getBean(), "logmsg.dest"));
        item.addItemProperty("alarmDataReductionKey", new NestedMethodProperty<String>(item.getBean(), "alarmData.ReductionKey"));
        item.addItemProperty("alarmDataClearKey", new NestedMethodProperty<String>(item.getBean(), "alarmData.ClearKey"));
        item.addItemProperty("alarmDataAlarmType", new NestedMethodProperty<String>(item.getBean(), "alarmData.AlarmType"));
        item.addItemProperty("alarmDataAutoClean", new NestedMethodProperty<String>(item.getBean(), "alarmData.AutoClean"));
        item.addItemProperty("maskElements", new NestedMethodProperty<String>(item.getBean(), "mask.maskelementCollection"));
        item.addItemProperty("maskVarbinds", new NestedMethodProperty<String>(item.getBean(), "mask.varbindCollection"));
        eventEditor.setItemDataSource(item);
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
        e.getLogmsg().setDest("logndisplay");
        e.setSeverity("Indeterminate");
        e.setMask(new Mask());
        e.setAlarmData(new AlarmData());
        return e;
    }

    /**
     * Gets the field group.
     *
     * @return the field group
     */
    public FieldGroup getFieldGroup() {
        return eventEditor;
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractComponent#setReadOnly(boolean)
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        eventEditor.setReadOnly(readOnly);
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractComponent#isReadOnly()
     */
    @Override
    public boolean isReadOnly() {
        return super.isReadOnly() && eventEditor.isReadOnly();
    }
    
}
