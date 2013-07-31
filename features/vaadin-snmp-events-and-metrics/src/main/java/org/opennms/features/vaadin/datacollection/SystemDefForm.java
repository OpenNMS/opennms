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

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.config.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.SystemDef;

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
 * The Class System Definition Form.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
// TODO when a new group is added, the group list passed to SystemDefFieldFactory must be updated.
@SuppressWarnings("serial")
public abstract class SystemDefForm extends Form implements ClickListener {

    /** The Constant FORM_ITEMS. */
    public static final String[] FORM_ITEMS = new String[] {
        "name",
        "systemDefChoice",
        "collect"
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
     * Instantiates a new system definition form.
     *
     * @param dataCollectionConfigDao the OpenNMS Data Collection Configuration DAO
     * @param source the OpenNMS Data Collection Group object
     */
    public SystemDefForm(final DataCollectionConfigDao dataCollectionConfigDao, final DatacollectionGroup source) {
        setCaption("System Definition Detail");
        setBuffered(true);
        setVisible(false);

        // Adding all groups already defined on this source
        final List<String> groups = new ArrayList<String>();
        for (Group group : source.getGroupCollection()) {
            groups.add(group.getName());
        }
        // Adding all defined groups
        groups.addAll(dataCollectionConfigDao.getAvailableMibGroups());

        setFormFieldFactory(new SystemDefFieldFactory(groups));
        initToolbar();
    }

    /**
     * Initialize the Toolbar.
     */
    private void initToolbar() {
        save.addClickListener(this);
        cancel.addClickListener(this);
        edit.addClickListener(this);
        delete.addClickListener(this);

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setSpacing(true);
        toolbar.addComponent(edit);
        toolbar.addComponent(delete);
        toolbar.addComponent(save);
        toolbar.addComponent(cancel);

        setFooter(toolbar);
    }

    /**
     * Gets the system definition.
     *
     * @return the system definition
     */
    @SuppressWarnings("unchecked")
    private SystemDef getSystemDef() {
        if (getItemDataSource() instanceof BeanItem) {
            BeanItem<SystemDef> item = (BeanItem<SystemDef>) getItemDataSource();
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
            saveSystemDef(getSystemDef());
        }
        if (source == cancel) {
            discard();
            setReadOnly(true);
        }
        if (source == edit) {
            setReadOnly(false);
        }
        if (source == delete) {
            MessageBox mb = new MessageBox(getUI().getWindows().iterator().next(),
                                           "Are you sure?",
                                           MessageBox.Icon.QUESTION,
                                           "Do you really want to remove the System Definition" + getSystemDef().getName() + "?<br/>This action cannot be undone.",
                                           new MessageBox.ButtonConfig(MessageBox.ButtonType.YES, "Yes"),
                                           new MessageBox.ButtonConfig(MessageBox.ButtonType.NO, "No"));
            mb.addStyleName(Runo.WINDOW_DIALOG);
            mb.show(new EventListener() {
                @Override
                public void buttonClicked(ButtonType buttonType) {
                    if (buttonType == MessageBox.ButtonType.YES) {
                        setVisible(false);
                        deleteSystemDef(getSystemDef());
                    }
                }
            });
        }
    }

    /**
     * Save system definition.
     *
     * @param group the group
     */
    public abstract void saveSystemDef(SystemDef group);

    /**
     * Delete system definition.
     *
     * @param group the group
     */
    public abstract void deleteSystemDef(SystemDef group);

}
