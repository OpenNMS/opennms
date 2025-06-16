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
package org.opennms.features.vaadin.datacollection;

import java.util.List;

import org.opennms.features.vaadin.api.OnmsBeanContainer;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;

import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.fieldgroup.BeanFieldGroup;
import com.vaadin.v7.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.v7.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * The Include Collection Field.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
// FIXME: What about exclude fields ?
@SuppressWarnings("serial")
public abstract class IncludeCollectionWindow extends Window implements Button.ClickListener {

    /** The form layout. */
    private final FormLayout formLayout = new FormLayout();
    
    /** The form editor. */
    private final BeanFieldGroup<IncludeCollectionWrapper> formEditor = new BeanFieldGroup<IncludeCollectionWrapper>(IncludeCollectionWrapper.class);
    
    /** The OK button. */
    private final Button okButton = new Button("Update", this);

    /** The CANCEL button. */
    private final Button cancelButton = new Button("Cancel", this);

    /**
     * Instantiates a new include collection window.
     * 
     * @param dataCollectionConfigDao the data collection configuration DAO
     * @param container the source list of elements
     * @param wrapper the current selected value
     */
    public IncludeCollectionWindow(final DataCollectionConfigDao dataCollectionConfigDao,
            final OnmsBeanContainer<IncludeCollectionWrapper> container,
            final IncludeCollectionWrapper wrapper) {

        setCaption("Include SystemDef/DataCollectionGroup");
        setModal(true);
        setWidth("400px");
        setHeight("2000px");
        setResizable(false);
        setClosable(false);
        addStyleName("dialog");

        final ComboBox valueField = new ComboBox("Value");
        valueField.setEnabled(false);
        valueField.setRequired(true);
        valueField.setImmediate(true);
        valueField.setNewItemsAllowed(false);
        valueField.setNullSelectionAllowed(false);

        final ComboBox typeField = new ComboBox("Type");
        typeField.setRequired(true);
        typeField.setImmediate(true);
        typeField.setNewItemsAllowed(false);
        typeField.setNullSelectionAllowed(false);
        typeField.addItem(IncludeCollectionWrapper.DC_GROUP);
        typeField.addItem(IncludeCollectionWrapper.SYSTEM_DEF);
        typeField.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                String selected = (String) typeField.getValue();
                if (selected == null) {
                    return;
                }
                // Get available fields.
                // FIXME If a new dcGroup is added, DataCollectionConfigDao is not able to reach it.
                List<String> values = selected.equals(IncludeCollectionWrapper.SYSTEM_DEF) ? dataCollectionConfigDao.getAvailableSystemDefs()
                    : dataCollectionConfigDao.getAvailableDataCollectionGroups();
                // Remove already selected
                for (IncludeCollectionWrapper obj : container.getOnmsBeans()) {
                    if (obj.getType().equals(selected)) {
                        values.remove(obj.getValue());
                    }
                }
                // Updating combo-box
                valueField.removeAllItems();
                for (String v : values) {
                    valueField.addItem(v);
                }
                if (wrapper.getValue() != null) {
                    valueField.addItem(wrapper.getValue());
                }
                valueField.setEnabled(valueField.getItemIds().size() > 1);
            }
        });

        formLayout.setWidth("100%");
        formLayout.addComponent(typeField);
        formLayout.addComponent(valueField);

        formEditor.bind(typeField, "type");
        formEditor.bind(valueField, "value");
        formEditor.setItemDataSource(wrapper);

        final HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.addComponent(okButton);
        toolbar.addComponent(cancelButton);

        final VerticalLayout layout = new VerticalLayout();
        layout.addComponent(formLayout);
        layout.addComponent(toolbar);
        layout.setComponentAlignment(toolbar, Alignment.BOTTOM_RIGHT);
        layout.setMargin(true);
        setContent(layout);
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.Button.ClickListener#buttonClick(com.vaadin.ui.Button.ClickEvent)
     */
    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button btn = event.getButton();
        if (btn == okButton) {
            try {
                formEditor.commit();
                fieldChanged();
            } catch (CommitException e) {
                Notification.show("Can't save include collection because " + e.getMessage(), Notification.Type.ERROR_MESSAGE);
            }
        }
        close();
    }

    /**
     * Fired when the field has been changed.
     */
    public abstract void fieldChanged();

}
