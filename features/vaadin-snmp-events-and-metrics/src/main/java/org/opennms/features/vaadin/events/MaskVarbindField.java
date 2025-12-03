/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.vaadin.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.vaadin.v7.ui.ComboBox;
import org.opennms.features.vaadin.api.OnmsBeanContainer;
import org.opennms.netmgt.xml.eventconf.Varbind;
import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.v7.data.Container;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.v7.ui.CustomField;
import com.vaadin.v7.ui.DefaultFieldFactory;
import com.vaadin.v7.ui.Field;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.TextField;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * The Event's MaskVarbind Field.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class MaskVarbindField extends CustomField<List<MaskVarbindField.VarbindWithType>> implements Button.ClickListener {

    public static class VarbindWithType extends Varbind {
        private String vbtype = "vbnumber";

        public VarbindWithType() {
            setVbnumber(0);
        }

        public String getVbid() {
            if ("vboid".equals(this.vbtype)) {
                return this.getVboid();
            } else {
                return String.valueOf(this.getVbnumber());
            }
        }

        public void setVbid(final String varbindId) {
            if ("vboid".equals(this.vbtype)) {
                this.setVboid(varbindId);
                this.setVbnumber(null);
            } else {
                try {
                    this.setVbnumber(Integer.valueOf(varbindId));
                } catch (NumberFormatException e) {
                    this.setVbnumber(0);
                }
                this.setVboid(null);
            }
        }

        public String getVbtype() {
            return this.vbtype;
        }

        public void setVbtype(final String vbtype) {
            this.vbtype = vbtype;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            VarbindWithType that = (VarbindWithType) o;
            return Objects.equals(vbtype, that.vbtype);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), vbtype);
        }
    }

    /** The Container. */
    private final OnmsBeanContainer<VarbindWithType> container = new OnmsBeanContainer<VarbindWithType>(VarbindWithType.class);

    /** The Table. */
    private final Table table = new Table(null, container);

    /** The Toolbar. */
    private final HorizontalLayout toolbar = new HorizontalLayout();

    /** The add button. */
    private final Button add = new Button("Add", this);

    /** The delete button. */
    private final Button delete = new Button("Delete", this);

    /**
     * Instantiates a new mask varbind field.
     *
     * @param caption the caption
     */
    public MaskVarbindField(String caption) {
        setCaption(caption);
        table.addStyleName("light");
        table.setVisibleColumns(new Object[]{"vbtype", "vbid", "vbvalues"});
        table.setColumnHeader("vbtype", "Varbind Type");
        table.setColumnHeader("vbid", "Varbind OID or Number");
        table.setColumnHeader("vbvalues", "Varbind Values");
        table.setColumnExpandRatio("vbvalues", 1);
        table.setEditable(!isReadOnly());
        table.setSelectable(true);
        table.setHeight("125px");
        table.setWidth("100%");
        table.setTableFieldFactory(new DefaultFieldFactory() {
            @Override
            public Field<?> createField(Container container, Object itemId, Object propertyId, Component uiContext) {
                if (propertyId.equals("vbtype")) {
                    final ComboBox field = new ComboBox();
                    field.setSizeFull();
                    field.setRequired(true);
                    field.setImmediate(true);
                    field.setNullSelectionAllowed(false);
                    field.setNewItemsAllowed(false);
                    field.setTextInputAllowed(false);
                    field.addItem("vboid");
                    field.addItem("vbnumber");
                    return field;
                }

                if (propertyId.equals("vbvalues")) {
                    final TextField field = new TextField();
                    field.setConverter(new CsvListConverter());
                    return field;
                }

                if (propertyId.equals("vbid")) {
                    final VarbindWithType varbind = MaskVarbindField.this.container.getOnmsBean(itemId);
                }

                return super.createField(container, itemId, propertyId, uiContext);
            }
        });

        toolbar.addComponent(add);
        toolbar.addComponent(delete);
        toolbar.setVisible(table.isEditable());
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.CustomField#initContent()
     */
    @Override
    public Component initContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(table);
        layout.addComponent(toolbar);
        layout.setComponentAlignment(toolbar, Alignment.MIDDLE_RIGHT);
        return layout;
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractField#getType()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends List<VarbindWithType>> getType() {
        return (Class<ArrayList<VarbindWithType>>) new ArrayList<VarbindWithType>().getClass();
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractField#getInternalValue()
     */
    @Override
    protected List<VarbindWithType> getInternalValue() {
        return container.getOnmsBeans();
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractField#setInternalValue(java.lang.Object)
     */
    @Override
    protected void setInternalValue(List<VarbindWithType> varbinds) {
        container.removeAllItems();
        container.addAll(varbinds);
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractComponent#setReadOnly(boolean)
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        table.setEditable(!readOnly);
        toolbar.setVisible(!readOnly);
        super.setReadOnly(readOnly);
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.Button.ClickListener#buttonClick(com.vaadin.ui.Button.ClickEvent)
     */
    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button btn = event.getButton();
        if (btn == add) {
            addHandler();
        }
        if (btn == delete) {
            deleteHandler();
        }
    }

    /**
     * Adds the handler.
     */
    private void addHandler() {
        VarbindWithType v = new VarbindWithType();
        container.addOnmsBean(v);
    }

    /**
     * Delete handler.
     */
    private void deleteHandler() {
        final Object itemId = table.getValue();
        if (itemId == null) {
            Notification.show("Please select a Mask Varbind from the table.");
        } else {
            ConfirmDialog.show(getUI(),
                               "Are you sure?",
                               "Do you really want to remove the selected Mask Varbind field?\nThis action cannot be undone.",
                               "Yes",
                               "No",
                               new ConfirmDialog.Listener() {
                public void onClose(ConfirmDialog dialog) {
                    if (dialog.isConfirmed()) {
                        table.removeItem(itemId);
                    }
                }
            });
        }
    }
}
