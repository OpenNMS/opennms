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
import org.opennms.netmgt.config.datacollection.ResourceType;

import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Window.Notification;
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
// TODO when a new resource type is added, the resourceType list passed to GroupFieldFactory must be updated.
@SuppressWarnings("serial")
public abstract class GroupForm extends Form implements ClickListener {

    /** The Constant FORM_ITEMS. */
    public static final String[] FORM_ITEMS = new String[] {
        "name",
        "ifType",
        "mibObjCollection"
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
     * Instantiates a new group form.
     * 
     * @param dataCollectionConfigDao the OpenNMS Data Collection Configuration DAO
     * @param source the OpenNMS Data Collection Group object
     */
    public GroupForm(final DataCollectionConfigDao dataCollectionConfigDao, final DatacollectionGroup source) {
        setCaption("MIB Group Detail");
        setWriteThrough(false);
        setVisible(false);

        // Adding all resource types already defined on this source
        final List<String> resourceTypes = new ArrayList<String>();
        for (ResourceType type : source.getResourceTypeCollection()) {
            resourceTypes.add(type.getName());
        }
        // Adding all defined resource types
        resourceTypes.addAll(dataCollectionConfigDao.getConfiguredResourceTypes().keySet());

        setFormFieldFactory(new GroupFieldFactory(resourceTypes));
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
     * Gets the group.
     *
     * @return the group
     */
    @SuppressWarnings("unchecked")
    private Group getGroup() {
        if (getItemDataSource() instanceof BeanItem) {
            BeanItem<Group> item = (BeanItem<Group>) getItemDataSource();
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
    public void buttonClick(ClickEvent event) {
        Button source = event.getButton();
        if (source == save) {
            if (isValid()) {
                commit();
                setReadOnly(true);
                saveGroup(getGroup());
            } else {
                getWindow().showNotification("There are errors on the MIB Groups", Notification.TYPE_WARNING_MESSAGE);
            }
        }
        if (source == cancel) {
            discard();
            setReadOnly(true);
        }
        if (source == edit) {
            setReadOnly(false);
        }
        if (source == delete) {
            // FIXME You cannot delete a group if it is being used on any systemDef
            MessageBox mb = new MessageBox(getApplication().getMainWindow(),
                                           "Are you sure?",
                                           MessageBox.Icon.QUESTION,
                                           "Do you really want to remove the Group " + getGroup().getName() + "?<br/>This action cannot be undone.",
                                           new MessageBox.ButtonConfig(MessageBox.ButtonType.YES, "Yes"),
                                           new MessageBox.ButtonConfig(MessageBox.ButtonType.NO, "No"));
            mb.addStyleName(Runo.WINDOW_DIALOG);
            mb.show(new EventListener() {
                public void buttonClicked(ButtonType buttonType) {
                    if (buttonType == MessageBox.ButtonType.YES) {
                        setVisible(false);
                        deleteGroup(getGroup());
                    }
                }
            });
        }
    }

    /**
     * Save group.
     *
     * @param group the group
     */
    public abstract void saveGroup(Group group);

    /**
     * Delete group.
     *
     * @param group the group
     */
    public abstract void deleteGroup(Group group);

}
