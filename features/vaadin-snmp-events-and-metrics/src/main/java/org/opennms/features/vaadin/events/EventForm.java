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

import java.util.Arrays;

import org.opennms.netmgt.xml.eventconf.AlarmData;
import org.opennms.netmgt.xml.eventconf.Logmsg;
import org.opennms.netmgt.xml.eventconf.Mask;

import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.NestedMethodProperty;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.Runo;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;
import de.steinwedel.vaadin.MessageBox.EventListener;

/**
 * The Class Event Form.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public abstract class EventForm extends Form implements ClickListener {

    /** The Constant FORM_ITEMS. */
    public static final String[] FORM_ITEMS = new String[] {
        "uei",
        "eventLabel",
        "descr",
        "logMsgContent",         // Embedded from LogMsg object
        "logMsgDest",            // Embedded from LogMsg object
        "severity",
        "alarmDataReductionKey", // Embedded from AlarmData object
        "alarmDataClearKey",     // Embedded from AlarmData object
        "alarmDataAlarmType",    // Embedded from AlarmData object
        "alarmDataAutoClean",    // Embedded from AlarmData object
        "operinstruct",
        "maskElements",
        "maskVarbinds",
        "varbindsdecodeCollection"
        /*
         * Not Implemented:
         * 
         * autoactionCollection (CustomField)
         * operactionCollection (CustomField)
         * correlation (CustomField)
         * autoacknowledge (CustomField)
         */
    };

    /** The Edit button. */
    private final Button edit = new Button("Edit");

    /** The Delete button. */
    private final Button delete = new Button("Delete");

    /** The Save button. */
    private final Button save = new Button("Save");

    /** The Cancel button. */
    private final Button cancel = new Button("Cancel");

    /**
     * Instantiates a new event form.
     */
    public EventForm() {
        setCaption("Event Detail");
        setWriteThrough(false);
        setVisible(false);
        setFormFieldFactory(new EventFormFieldFactory());
        initToolbar();
    }

    /**
     * Initialize the Toolbar.
     */
    private void initToolbar() {
        save.addListener((ClickListener)this);
        cancel.addListener((ClickListener)this);
        edit.addListener((ClickListener)this);
        delete.addListener((ClickListener)this);

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setSpacing(true);
        toolbar.addComponent(edit);
        toolbar.addComponent(delete);
        toolbar.addComponent(save);
        toolbar.addComponent(cancel);

        setFooter(toolbar);
    }

    /**
     * Gets the event.
     *
     * @return the event
     */
    @SuppressWarnings("unchecked")
    private org.opennms.netmgt.xml.eventconf.Event getEvent() {
        if (getItemDataSource() instanceof BeanItem) {
            BeanItem<org.opennms.netmgt.xml.eventconf.Event> item = (BeanItem<org.opennms.netmgt.xml.eventconf.Event>) getItemDataSource();
            return item.getBean();
        }
        return null;
    }

    /**
     * Creates the event item.
     *
     * @param event the event
     * @return the bean item
     */
    private BeanItem<org.opennms.netmgt.xml.eventconf.Event> createEventItem(org.opennms.netmgt.xml.eventconf.Event event) {
        // Be sure that the nested elements exists to avoid problems with vaadin fields.
        if (event.getMask() == null) {
            event.setMask(new Mask());
        }
        if (event.getLogmsg() == null) {
            event.setLogmsg(new Logmsg());
        }
        if (event.getAlarmData() == null) {
            AlarmData a = new AlarmData();
            a.setAutoClean(Boolean.FALSE);
            event.setAlarmData(a);
        }
        // Creating BeanItem
        BeanItem<org.opennms.netmgt.xml.eventconf.Event> item = new BeanItem<org.opennms.netmgt.xml.eventconf.Event>(event);
        item.addItemProperty("logMsgContent", new NestedMethodProperty(item.getBean(), "logmsg.content"));
        item.addItemProperty("logMsgDest", new NestedMethodProperty(item.getBean(), "logmsg.dest"));
        item.addItemProperty("alarmDataReductionKey", new NestedMethodProperty(item.getBean(), "alarmData.ReductionKey"));
        item.addItemProperty("alarmDataClearKey", new NestedMethodProperty(item.getBean(), "alarmData.ClearKey"));
        item.addItemProperty("alarmDataAlarmType", new NestedMethodProperty(item.getBean(), "alarmData.AlarmType"));
        item.addItemProperty("alarmDataAutoClean", new NestedMethodProperty(item.getBean(), "alarmData.AutoClean"));
        item.addItemProperty("maskElements", new NestedMethodProperty(item.getBean(), "mask.maskelementCollection"));
        item.addItemProperty("maskVarbinds", new NestedMethodProperty(item.getBean(), "mask.varbindCollection"));
        return item;
    }

    /**
     * Sets the Event Data Source
     * 
     * @param event the OpenNMS event
     */
    public void setEventDataSource(org.opennms.netmgt.xml.eventconf.Event event) {
        BeanItem<org.opennms.netmgt.xml.eventconf.Event> item = createEventItem(event);
        setItemDataSource(item, Arrays.asList(FORM_ITEMS));
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.Form#setReadOnly(boolean)
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        save.setVisible(!readOnly);
        cancel.setVisible(!readOnly);
        edit.setVisible(readOnly);
        delete.setVisible(readOnly);
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.Button.ClickListener#buttonClick(com.vaadin.ui.Button.ClickEvent)
     */
    public void buttonClick(ClickEvent event) {
        Button source = event.getButton();
        if (source == save) {
            commit();
            setReadOnly(true);
            saveEvent(getEvent());
        }
        if (source == cancel) {
            discard();
            setReadOnly(true);
        }
        if (source == edit) {
            setReadOnly(false);
        }
        if (source == delete) {
            MessageBox mb = new MessageBox(getApplication().getMainWindow(),
                                           "Are you sure?",
                                           MessageBox.Icon.QUESTION,
                                           "Do you really want to remove the event definition " + getEvent().getUei() + "?<br/>This action cannot be undone.",
                                           new MessageBox.ButtonConfig(MessageBox.ButtonType.YES, "Yes"),
                                           new MessageBox.ButtonConfig(MessageBox.ButtonType.NO, "No"));
            mb.addStyleName(Runo.WINDOW_DIALOG);
            mb.show(new EventListener() {
                public void buttonClicked(ButtonType buttonType) {
                    if (buttonType == MessageBox.ButtonType.YES) {
                        setVisible(false);
                        deleteEvent(getEvent());
                    }
                }
            });
        }
    }

    /**
     * Save event.
     *
     * @param event the event
     */
    public abstract void saveEvent(org.opennms.netmgt.xml.eventconf.Event event);

    /**
     * Delete event.
     *
     * @param event the event
     */
    public abstract void deleteEvent(org.opennms.netmgt.xml.eventconf.Event event);

}
