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
package org.opennms.features.vaadin.datacollection;

import org.opennms.netmgt.config.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.SnmpCollection;

import com.vaadin.data.util.BeanItem;
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
 * The Class SNMP Collection Form.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public abstract class SnmpCollectionForm extends Form implements ClickListener {

    /** The Constant FORM_ITEMS. */
    public static final String[] FORM_ITEMS = new String[] {
        "name",
        "snmpStorageFlag",
        "rrd",
        "includeCollectionCollection"
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
     * Instantiates a new SNMP collection form.
     *
     * @param dataCollectionConfigDao the data collection configuration DAO
     */
    public SnmpCollectionForm(final DataCollectionConfigDao dataCollectionConfigDao) {
        setCaption("SNMP Collection Detail");
        setWriteThrough(false);
        setVisible(false);
        setFormFieldFactory(new SnmpCollectionFieldFactory(dataCollectionConfigDao));
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
     * Gets the SNMP Collection.
     *
     * @return the SNMP Collection
     */
    @SuppressWarnings("unchecked")
    private SnmpCollection getSnmpCollection() {
        if (getItemDataSource() instanceof BeanItem) {
            BeanItem<SnmpCollection> item = (BeanItem<SnmpCollection>) getItemDataSource();
            return item.getBean();
        }
        return null;
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
    @Override
    public void buttonClick(ClickEvent event) {
        Button source = event.getButton();
        if (source == save) {
            commit();
            setReadOnly(true);
            saveSnmpCollection(getSnmpCollection());
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
                                           "Do you really want to remove the selected SNMP Collection?<br/>This action cannot be undone.",
                                           new MessageBox.ButtonConfig(MessageBox.ButtonType.YES, "Yes"),
                                           new MessageBox.ButtonConfig(MessageBox.ButtonType.NO, "No"));
            mb.addStyleName(Runo.WINDOW_DIALOG);
            mb.show(new EventListener() {
                @Override
                public void buttonClicked(ButtonType buttonType) {
                    if (buttonType == MessageBox.ButtonType.YES) {
                        setVisible(false);
                        deleteSnmpCollection(getSnmpCollection());
                    }
                }
            });
        }
    }

    /**
     * Save SNMP collection.
     *
     * @param snmpCollection the SNMP collection
     */
    public abstract void saveSnmpCollection(SnmpCollection snmpCollection);

    /**
     * Delete SNMP collection.
     *
     * @param snmpCollection the SNMP collection
     */
    public abstract void deleteSnmpCollection(SnmpCollection snmpCollection);

}
