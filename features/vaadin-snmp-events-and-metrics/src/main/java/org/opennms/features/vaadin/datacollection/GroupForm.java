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

import org.opennms.netmgt.config.datacollection.Group;

import com.vaadin.v7.data.fieldgroup.BeanFieldGroup;
import com.vaadin.v7.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.v7.ui.TextField;

/**
 * The Class Event Form.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
// TODO when a new resource type is added, the resourceType list passed to GroupFieldFactory must be updated.
@SuppressWarnings("serial")
public class GroupForm extends CustomComponent {

    /** The name. */
    final TextField name = new TextField("Group Name");

    /** The ifType. */
    final ComboBox ifType = new ComboBox("ifType Filter");

    /** The MIB Objects. */
    final MibObjField mibObjs; 

    /** The Event editor. */
    final BeanFieldGroup<Group> groupEditor = new BeanFieldGroup<Group>(Group.class);

    /** The event layout. */
    final FormLayout groupLayout = new FormLayout();

    /**
     * Instantiates a new group form.
     *
     * @param resourceTypes the resource types
     * @param mibGroupEditable true, if the MIB group can be modified
     */
    public GroupForm(final List<String> resourceTypes, boolean mibGroupEditable) {
        setCaption("MIB Group Detail");
        groupLayout.setMargin(true);

        name.setRequired(true);
        name.setWidth("100%");
        groupLayout.addComponent(name);

        ifType.addItem("ignore");
        ifType.addItem("all");
        ifType.setNullSelectionAllowed(false);
        ifType.setRequired(true);
        ifType.setImmediate(true);
        ifType.setNewItemsAllowed(true);
        groupLayout.addComponent(ifType);

        mibObjs = new MibObjField(resourceTypes, mibGroupEditable);
        mibObjs.setCaption("MIB Objects");
        mibObjs.setRequired(true);
        mibObjs.setImmediate(true);
        mibObjs.setWidth("100%");
        groupLayout.addComponent(mibObjs);

        setGroup(createBasicGroup());

        groupEditor.bind(name, "name");
        groupEditor.bind(ifType, "ifType");
        groupEditor.bind(mibObjs, "mibObjs");

        setCompositionRoot(groupLayout);
    }

    /**
     * Gets the group.
     *
     * @return the group
     */
    public Group getGroup() {
        return groupEditor.getItemDataSource().getBean();
    }

    /**
     * Sets the group.
     *
     * @param group the new group
     */
    public void setGroup(Group group) {
        groupEditor.setItemDataSource(group);
    }

    /**
     * Creates the basic group.
     *
     * @return the group
     */
    public Group createBasicGroup() {
        Group group = new Group();
        group.setName("New Group");
        group.setIfType("ignore");
        return group;
    }

    /**
     * Discard.
     */
    public void discard() {
        groupEditor.discard();
    }

    /**
     * Commit.
     *
     * @throws CommitException the commit exception
     */
    public void commit() throws CommitException {
        groupEditor.commit();
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractComponent#setReadOnly(boolean)
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        groupEditor.setReadOnly(readOnly);
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractComponent#isReadOnly()
     */
    @Override
    public boolean isReadOnly() {
        return groupEditor.isReadOnly();
    }

    /**
     * Gets the group name.
     *
     * @return the group name
     */
    public String getGroupName() {
        return name.getValue();
    }
}
