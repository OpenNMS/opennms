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

import com.vaadin.data.Item;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormFieldFactory;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.AbstractSelect.NewItemHandler;

/**
 * A factory for creating Event Field objects.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public final class EventFormFieldFactory implements FormFieldFactory {

    /* (non-Javadoc)
     * @see com.vaadin.ui.FormFieldFactory#createField(com.vaadin.data.Item, java.lang.Object, com.vaadin.ui.Component)
     */
    @Override
    public Field<?> createField(Item item, Object propertyId, Component uiContext) {
        if ("logMsgDest".equals(propertyId)) {
            final ComboBox dest = new ComboBox("Destination");
            dest.addItem("logndisplay");
            dest.addItem("logonly");
            dest.addItem("suppress");
            dest.addItem("donotpersist");
            dest.addItem("discardtraps");
            dest.setNullSelectionAllowed(false);
            dest.setRequired(true);
            return dest;
        }
        if ("logMsgContent".equals(propertyId)) {
            final TextArea content = new TextArea("Log Message");
            content.setWidth("100%");
            content.setRows(10);
            content.setRequired(true);
            content.setNullRepresentation("");
            return content;
        }
        if ("alarmDataAlarmType".equals(propertyId)) {
            final ComboBox f = new ComboBox("Alarm Type");
            f.addItem(new Integer(1));
            f.addItem(new Integer(2));
            f.addItem(new Integer(3));
            f.setNewItemHandler(new NewItemHandler() {
                @Override
                public void addNewItem(String newItemCaption) {
                    try {
                        f.addItem(new Integer(newItemCaption));
                    } catch (Exception e) {}
                }
            });
            f.setDescription("<b>1</b> to be a problem that has a possible resolution, alarm-type set to <b>2</b> to be a resolution event, and alarm-type set to <b>3</b> for events that have no possible resolution");
            f.setNullSelectionAllowed(false);
            return f;
        }
        if ("alarmDataAutoClean".equals(propertyId)) {
            final CheckBox f = new CheckBox("Auto Clean");
            f.setWidth("100%");
            return f;
        }
        if ("alarmDataReductionKey".equals(propertyId)) {
            final TextField f = new TextField("Reduction Key");
            f.setWidth("100%");
            f.setNullRepresentation("");
            return f;
        }
        if ("alarmDataClearKey".equals(propertyId)) {
            final TextField f = new TextField("Clear Key");
            f.setWidth("100%");
            f.setNullRepresentation("");
            return f;
        }
        if ("severity".equals(propertyId)) {
            final ComboBox severity = new ComboBox("Severity");
            for (String sev : OnmsSeverity.names()) {
                severity.addItem(sev.substring(0, 1).toUpperCase() + sev.substring(1).toLowerCase());
            }
            severity.setNullSelectionAllowed(false);
            severity.setRequired(true);
            return severity;
        }
        if ("descr".equals(propertyId)) {
            final TextArea descr = new TextArea("Description");
            descr.setWidth("100%");
            descr.setRows(10);
            descr.setRequired(true);
            descr.setNullRepresentation("");
            return descr;
        }
        if ("operinstruct".equals(propertyId)) {
            final TextArea oper = new TextArea("Operator Instructions") {
                @Override
                public String getValue() { // This is because of the intern usage on Event.setOperInstruct()
                    return super.getValue() == null ? "" : super.getValue();
                }
            };
            oper.setWidth("100%");
            oper.setRows(10);
            oper.setNullRepresentation("");
            return oper;
        }
        if ("maskElements".equals(propertyId)) {
            final MaskElementField field = new MaskElementField();
            field.setCaption("Mask Elements");
            return field;
        }
        if ("maskVarbinds".equals(propertyId)) {
            final MaskVarbindField field = new MaskVarbindField();
            field.setCaption("Mask Varbinds");
            return field;
        }
        if ("varbindsdecodeCollection".equals(propertyId)) {
            final VarbindsDecodeField field = new VarbindsDecodeField();
            field.setCaption("Varbind Decodes");
            return field;
        }
        if ("uei".equals(propertyId)) {
            final TextField f = new TextField("Event UEI");
            f.setRequired(true);
            f.setWidth("100%");
            return f;
        }
        if ("eventLabel".equals(propertyId)) {
            final TextField f = new TextField("Event Label");
            f.setRequired(true);
            f.setWidth("100%");
            return f;
        }
        final Field<?> f = DefaultFieldFactory.get().createField(item, propertyId, uiContext);
        f.setWidth("100%");
        return f;
    }
}
