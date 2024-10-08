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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.features.vaadin.api.Logger;
import org.opennms.features.vaadin.config.EditorToolbar;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.opennms.netmgt.config.datacollection.SystemDef;

import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * The Class GroupPanel.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class GroupPanel extends Panel {

    /** The isNew flag. True, if the group is new. */
    private boolean isNew = false;

    /** The group table. */
    private final GroupTable groupTable;

    /** The selected group ID. */
    private Object selectedGroupId;

    /**
     * Instantiates a new group panel.
     *
     * @param dataCollectionConfigDao the OpenNMS Data Collection Configuration DAO
     * @param source the OpenNMS Data Collection Group object
     * @param logger the logger object
     * @param mibGroupEditable true, if the MIB group can be modified
     */
    public GroupPanel(final DataCollectionConfigDao dataCollectionConfigDao, final DatacollectionGroup source, final Logger logger, boolean mibGroupEditable) {

        if (dataCollectionConfigDao == null) {
            throw new RuntimeException("dataCollectionConfigDao cannot be null.");
        }

        if (source == null) {
            throw new RuntimeException("source cannot be null.");
        }

        addStyleName("light");

        // Adding all resource types already defined on this source
        final List<String> resourceTypes = new ArrayList<>();
        for (ResourceType type : source.getResourceTypes()) {
            resourceTypes.add(type.getName());
        }

        // Adding all defined resource types
        resourceTypes.addAll(dataCollectionConfigDao.getConfiguredResourceTypes().keySet());

        groupTable = new GroupTable(source.getGroups());

        final GroupForm groupForm = new GroupForm(resourceTypes, mibGroupEditable);
        groupForm.setVisible(false);

        final EditorToolbar bottomToolbar = new EditorToolbar() {
            @Override
            public boolean save() {
                Group group = groupForm.getGroup();
                if (!isNew && !group.getName().equals(groupForm.getGroupName())) {
                    Set<String> systemDefMap = getParentSystemDefs(dataCollectionConfigDao, group.getName());
                    if (!systemDefMap.isEmpty()) {
                        final String msg = "The group cannot be renamed because it is being referenced by:\n" + systemDefMap.toString();
                        Notification.show(msg, Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                }
                logger.info("SNMP Group " + group.getName() + " has been " + (isNew ? "created." : "updated."));
                try {
                    groupForm.commit();
                    groupForm.setReadOnly(true);
                    groupTable.refreshRowCache();
                    return true;
                } catch (CommitException e) {
                    String msg = "Can't save the changes: " + e.getMessage();
                    logger.error(msg);
                    Notification.show(msg, Notification.Type.ERROR_MESSAGE);
                    return false;
                }
            }
            @Override
            public boolean delete() {
                Object groupId = groupTable.getValue();
                if (groupId != null) {
                    Group group = groupTable.getGroup(groupId);
                    Set<String> systemDefMap = getParentSystemDefs(dataCollectionConfigDao, group.getName());
                    if (!systemDefMap.isEmpty()) {
                        final String msg = "The group cannot be deleted because it is being referenced by:\n" + systemDefMap.toString();
                        Notification.show(msg, Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                    logger.info("SNMP Group " + group.getName() + " has been removed.");
                    groupTable.select(null);
                    groupTable.removeItem(groupId);
                    groupTable.refreshRowCache();
                }
                return true;
            }
            @Override
            public boolean edit() {
                groupForm.setReadOnly(false);
                return true;
            }
            @Override
            public boolean cancel() {
                groupForm.discard();
                groupForm.setReadOnly(true);
                return true;
            }
        };
        bottomToolbar.setVisible(false);

        groupTable.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                if (groupForm.isVisible() && !groupForm.isReadOnly()) {
                    groupTable.select(selectedGroupId);
                    Notification.show("A group seems to be being edited.\nPlease save or cancel your current changes.", Notification.Type.WARNING_MESSAGE);
                } else {
                    Object groupId = groupTable.getValue();
                    if (groupId != null) {
                        selectedGroupId = groupId;
                        groupForm.setGroup(groupTable.getGroup(groupId));
                    }
                    groupForm.setReadOnly(true);
                    groupForm.setVisible(groupId != null);
                    bottomToolbar.setReadOnly(true);
                    bottomToolbar.setVisible(groupId != null);
                }
            }
        });   

        final Button add = new Button("Add SNMP Group", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                groupTable.addGroup(groupForm.createBasicGroup());
                groupForm.setReadOnly(false);
                bottomToolbar.setReadOnly(false);
                setIsNew(true);
            }
        });

        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);
        mainLayout.addComponent(groupTable);
        mainLayout.addComponent(add);
        mainLayout.addComponent(groupForm);
        mainLayout.addComponent(bottomToolbar);
        mainLayout.setComponentAlignment(add, Alignment.MIDDLE_RIGHT);
        setContent(mainLayout);
    }

    /**
     * Sets the value of the ifNew flag.
     *
     * @param isNew true, if the group is new.
     */
    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }

    /**
     * Gets the groups.
     *
     * @return the groups
     */
    public List<Group> getGroups() {
        return groupTable.getGroups();
    }

    /**
     * Gets the parent system definitions.
     * <p>The list of systemDef per SNMP collection that are referencing a given groupName</p>
     *
     * @param dataCollectionConfigDao the data collection configuration DAO
     * @param groupName the group name
     * @return the parent system definitions.
     */
    private Set<String> getParentSystemDefs(final DataCollectionConfigDao dataCollectionConfigDao, String groupName) {
        Set<String> systemDefMap = new TreeSet<>();
        for (final SnmpCollection collection : dataCollectionConfigDao.getRootDataCollection().getSnmpCollections()) {
            for (final SystemDef systemDef : collection.getSystems().getSystemDefs()) {
                for (final String group : systemDef.getCollect().getIncludeGroups()) {
                    if (group.equals(groupName)) {
                        systemDefMap.add(systemDef.getName() + '@' + collection.getName());
                    }
                }
            }
        }
        return systemDefMap;
    }

}
