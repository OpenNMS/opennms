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

import org.opennms.netmgt.config.datacollection.Collect;
import org.opennms.netmgt.config.datacollection.SystemDef;

import com.vaadin.v7.data.fieldgroup.BeanFieldGroup;
import com.vaadin.v7.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.v7.ui.TextField;

/**
 * The Class System Definition Form.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
// TODO when a new group is added, the group list passed to this form (i.e. groupNames) must be updated.
@SuppressWarnings("serial")
public class SystemDefForm extends CustomComponent {

    /** The name. */
    final TextField name = new TextField("Group Name");

    /** The system definition choice. */
    final SystemDefChoiceField systemDefChoice = new SystemDefChoiceField("System OID/Mask");

    /** The collect field. */
    final CollectField collect;

    /** The Event editor. */
    final BeanFieldGroup<SystemDef> systemDefEditor = new BeanFieldGroup<SystemDef>(SystemDef.class);

    /** The event layout. */
    final FormLayout systemDefLayout = new FormLayout();

    /**
     * Instantiates a new system definition form.
     *
     * @param groupNames the group names
     */
    public SystemDefForm(final List<String> groupNames) {
        setCaption("System Definition Detail");
        systemDefLayout.setMargin(true);

        name.setRequired(true);
        name.setWidth("100%");
        systemDefLayout.addComponent(name);

        systemDefChoice.setRequired(true);
        systemDefLayout.addComponent(systemDefChoice);

        collect = new CollectField("MIB Groups", groupNames);
        collect.setRequired(true);
        systemDefLayout.addComponent(collect);

        setSystemDef(createBasicSystemDef());

        systemDefEditor.bind(name, "name");
        systemDefEditor.bind(systemDefChoice, "systemDefChoice");
        systemDefEditor.bind(collect, "collect");

        setCompositionRoot(systemDefLayout);
    }

    /**
     * Gets the system definition.
     *
     * @return the system definition
     */
    public SystemDef getSystemDef() {
        return systemDefEditor.getItemDataSource().getBean();
    }

    /**
     * Sets the system definition.
     *
     * @param systemDef the new system definition
     */
    public void setSystemDef(SystemDef systemDef) {
        systemDefEditor.setItemDataSource(systemDef);
    }

    /**
     * Creates the basic system definition.
     *
     * @return the system definition
     */
    public SystemDef createBasicSystemDef() {
        SystemDef sysDef = new SystemDef();
        sysDef.setName("New System Definition");
        sysDef.setSysoidMask(".1.3.6.1.4.1.");
        sysDef.setCollect(new Collect());
        return sysDef;
    }

    /**
     * Discard.
     */
    public void discard() {
        systemDefEditor.discard();
    }

    /**
     * Commit.
     *
     * @throws CommitException the commit exception
     */
    public void commit() throws CommitException {
        systemDefEditor.commit();
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractComponent#setReadOnly(boolean)
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        systemDefEditor.setReadOnly(readOnly);
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractComponent#isReadOnly()
     */
    @Override
    public boolean isReadOnly() {
        return systemDefEditor.isReadOnly();
    }

    /**
     * Gets the system definition name.
     *
     * @return the system definition name
     */
    public String getSystemDefName() {
        return name.getValue();
    }
}
