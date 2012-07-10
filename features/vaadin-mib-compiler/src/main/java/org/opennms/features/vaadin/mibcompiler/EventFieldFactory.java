/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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
package org.opennms.features.vaadin.mibcompiler;

import java.util.List;

import org.opennms.features.vaadin.mibcompiler.model.MaskElementDTO;
import org.opennms.features.vaadin.mibcompiler.model.VarbindDTO;
import org.opennms.features.vaadin.mibcompiler.model.VarbindsDecodeDTO;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.Action;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormFieldFactory;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.Runo;

// FIXME Alarm Data ?
/**
 * A factory for creating EventField objects.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public final class EventFieldFactory implements FormFieldFactory {

    /** The Constant ACTION_ADD_MASK_ELEMENT. */
    private static final Action ACTION_ADD_MASK_ELEMENT = new Action("Add New Element");

    /** The Constant ACTION_DELETE_MASK_ELEMENT. */
    private static final Action ACTION_DELETE_MASK_ELEMENT = new Action("Delete Selected Element");

    /** The Constant ACTION_ADD_MASK_VARBIND. */
    private static final Action ACTION_ADD_MASK_VARBIND = new Action("Add New Mask Varbind");

    /** The Constant ACTION_DELETE_MASK_VARBIND. */
    private static final Action ACTION_DELETE_MASK_VARBIND = new Action("Delete Selected Mask Varbind");

    /** The Constant ACTION_ADD_VARBIND_DECODE. */
    private static final Action ACTION_ADD_VARBIND_DECODE = new Action("Add New Varbind Decode");

    /** The Constant ACTION_DELETE_VARBIND_DECODE. */
    private static final Action ACTION_DELETE_VARBIND_DECODE = new Action("Delete Selected Varbind Decode");

    /* (non-Javadoc)
     * @see com.vaadin.ui.FormFieldFactory#createField(com.vaadin.data.Item, java.lang.Object, com.vaadin.ui.Component)
     */
    @SuppressWarnings("unchecked")
    public Field createField(Item item, Object propertyId, Component uiContext) {
        if ("logmsgDest".equals(propertyId)) {
            ComboBox dest = new ComboBox("Destination");
            dest.addItem("logndisplay");
            dest.addItem("donotpersist");
            dest.addItem("discardtraps");
            dest.setNullSelectionAllowed(false);
            return dest;
        }
        if ("logmsgContent".equals(propertyId)) {
            TextArea content = new TextArea("Log Message");
            content.setWidth("100%");
            content.setRows(5);
            return content;
        }
        if ("severity".equals(propertyId)) {
            ComboBox severity = new ComboBox("Severity");
            severity.addItem("Indeterminate");
            severity.addItem("Clear");
            severity.addItem("Normal");
            severity.addItem("Warning");
            severity.addItem("Minor");
            severity.addItem("Mayor");
            severity.addItem("Critical");
            severity.setNullSelectionAllowed(false);
            return severity;
        }
        if ("descr".equals(propertyId)) {
            TextArea descr = new TextArea("Description");
            descr.setWidth("100%");
            descr.setRows(5);
            return descr;
        }
        if ("maskElements".equals(propertyId)) {
            List<MaskElementDTO> maskList = (List<MaskElementDTO>) item.getItemProperty(propertyId).getValue();
            final BeanItemContainer<MaskElementDTO> container = new BeanItemContainer<MaskElementDTO>(MaskElementDTO.class);
            container.addAll(maskList);
            final Table elements = new Table("Mask Elements");
            elements.setStyleName(Runo.TABLE_SMALL);
            elements.setContainerDataSource(container);
            elements.setVisibleColumns(new Object[]{"mename", "mevalueCollection"});
            elements.setColumnHeader("mename", "Element Name");
            elements.setColumnHeader("mevalueCollection", "Element Values");
            elements.setColumnExpandRatio("mevalueCollection", 1);
            elements.setSelectable(true);
            elements.setEditable(true);
            elements.setImmediate(true);
            elements.setWriteThrough(false);
            elements.setInvalidCommitted(false);
            elements.setHeight("125px");
            elements.setWidth("100%");
            elements.setTableFieldFactory(new DefaultFieldFactory() {
                @Override
                public Field createField(Container container, Object itemId, Object propertyId, Component uiContext) {
                    if (propertyId.equals("mevalueCollection")) {
                        return new CustomListField();
                    }
                    return super.createField(container, itemId, propertyId, uiContext);
                }
            });
            elements.addActionHandler(new Action.Handler() {
                public Action[] getActions(Object target, Object sender) {
                    return new Action[] { ACTION_ADD_MASK_ELEMENT, ACTION_DELETE_MASK_ELEMENT };
                }
                public void handleAction(Action action, Object sender, Object target) {
                    if (elements.isReadOnly()) {
                        elements.getApplication().getMainWindow().showNotification("This table is read-only. Click on edit.");
                    } else {
                        if (action == ACTION_ADD_MASK_ELEMENT) {
                            // TODO Apparently this is not working or it is not enough
                            container.addBean(new MaskElementDTO());
                        }
                        if (action == ACTION_DELETE_MASK_ELEMENT) { // FIXME Confirm ?
                            MaskElementDTO me = ((MaskElementDTO) target);
                            container.removeItem(me);
                            elements.select(null);
                        }
                    }
                }
            });
            return elements;
        }
        if ("maskVarbinds".equals(propertyId)) {
            List<VarbindDTO> varbindList = (List<VarbindDTO>) item.getItemProperty(propertyId).getValue();
            final BeanItemContainer<VarbindDTO> container = new BeanItemContainer<VarbindDTO>(VarbindDTO.class);
            container.addAll(varbindList);
            final Table varbinds = new Table("Mask Varbinds");
            varbinds.setStyleName(Runo.TABLE_SMALL);
            varbinds.setContainerDataSource(container);
            varbinds.setVisibleColumns(new Object[]{"vbnumber", "vbvalueCollection"});
            varbinds.setColumnHeader("vbnumber", "Varbind Number");
            varbinds.setColumnHeader("vbvalueCollection", "Varbind Values");
            varbinds.setColumnExpandRatio("vbvalueCollection", 1);
            varbinds.setSelectable(true);
            varbinds.setEditable(true);
            varbinds.setImmediate(true);
            varbinds.setHeight("125px");
            varbinds.setWidth("100%");
            varbinds.setTableFieldFactory(new DefaultFieldFactory() {
                @Override
                public Field createField(Container container, Object itemId, Object propertyId, Component uiContext) {
                    if (propertyId.equals("vbvalueCollection")) {
                        return new CustomListField();
                    }
                    return super.createField(container, itemId, propertyId, uiContext);
                }
            });
            varbinds.addActionHandler(new Action.Handler() {
                public Action[] getActions(Object target, Object sender) {
                    return new Action[] { ACTION_ADD_MASK_VARBIND, ACTION_DELETE_MASK_VARBIND };
                }
                public void handleAction(Action action, Object sender, Object target) {
                    if (varbinds.isReadOnly()) {
                        varbinds.getApplication().getMainWindow().showNotification("This table is read-only. Click on edit.");
                    } else {
                        if (action == ACTION_ADD_MASK_VARBIND) {
                            // TODO Apparently this is not working or it is not enough
                            container.addBean(new VarbindDTO());
                        }
                        if (action == ACTION_DELETE_MASK_VARBIND) { // FIXME Confirm ?
                            VarbindDTO vb = ((VarbindDTO) target);
                            container.removeItem(vb);
                            varbinds.select(null);
                        }
                    }
                }
            });
            return varbinds;
        }
        if ("varbindsdecodeCollection".equals(propertyId)) {
            List<VarbindsDecodeDTO> varbindList = (List<VarbindsDecodeDTO>) item.getItemProperty(propertyId).getValue();
            final BeanItemContainer<VarbindsDecodeDTO> container = new BeanItemContainer<VarbindsDecodeDTO>(VarbindsDecodeDTO.class);
            container.addAll(varbindList);
            final Table varbinds = new Table("Varbind Decodes");
            varbinds.setStyleName(Runo.TABLE_SMALL);
            varbinds.setContainerDataSource(container);
            varbinds.setVisibleColumns(new Object[]{"parmid", "decodeCollection"});
            varbinds.setColumnHeader("parmid", "Parameter ID");
            varbinds.setColumnHeader("decodeCollection", "Decode Values");
            varbinds.setColumnExpandRatio("decodeCollection", 1);
            varbinds.setSelectable(true);
            varbinds.setEditable(true);
            varbinds.setImmediate(true);
            varbinds.setHeight("125px");
            varbinds.setWidth("100%");
            varbinds.setTableFieldFactory(new DefaultFieldFactory() {
                @Override
                public Field createField(Container container, Object itemId, Object propertyId, Component uiContext) {
                    if (propertyId.equals("decodeCollection")) {
                        return new VarbindDecodeListField();
                    }
                    return super.createField(container, itemId, propertyId, uiContext);
                }
            });
            varbinds.addActionHandler(new Action.Handler() {
                public Action[] getActions(Object target, Object sender) {
                    return new Action[] { ACTION_ADD_VARBIND_DECODE, ACTION_DELETE_VARBIND_DECODE };
                }
                public void handleAction(Action action, Object sender, Object target) {
                    if (varbinds.isReadOnly()) {
                        varbinds.getApplication().getMainWindow().showNotification("This table is read-only. Click on edit.");
                    } else {
                        if (action == ACTION_ADD_VARBIND_DECODE) {
                            // TODO Apparently this is not working or it is not enough
                            container.addBean(new VarbindsDecodeDTO());
                        }
                        if (action == ACTION_DELETE_VARBIND_DECODE) { // FIXME Confirm ?
                            VarbindsDecodeDTO vb = ((VarbindsDecodeDTO) target);
                            container.removeItem(vb);
                            varbinds.select(null);
                        }
                    }
                }
            });
            return varbinds;
        }
        TextField f = new TextField((String)propertyId);
        if ("uei".equals(propertyId)) {
            f.setCaption("Event UEI");
            f.setRequired(true);
        }
        if ("eventLabel".equals(propertyId)) {
            f.setCaption("Event Label");
            f.setRequired(true);
        }
        f.setWidth("100%");
        return f;
    }
}
