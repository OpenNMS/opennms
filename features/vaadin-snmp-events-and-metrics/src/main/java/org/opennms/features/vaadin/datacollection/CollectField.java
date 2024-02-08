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
import java.util.Set;
import java.util.TreeSet;

import org.opennms.netmgt.config.datacollection.Collect;

import com.vaadin.ui.Component;
import com.vaadin.v7.ui.CustomField;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.TwinColSelect;

/**
 * The Collect Field.
 * 
 * TODO: when a new group is added, the groupField must be updated.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class CollectField extends CustomField<Collect> {

    /** The selection field. */
    private final TwinColSelect selectField = new TwinColSelect();

    /**
     * Instantiates a new collect field.
     *
     * @param caption the caption
     * @param groups the available groups
     */
    public CollectField(String caption, List<String> groups) {
        setCaption(caption);
        selectField.setRows(10);
        selectField.setLeftColumnCaption("Available");
        selectField.setRightColumnCaption("Selected");

        for (String group : groups) {
            selectField.addItem(group);
        }

    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.CustomField#initContent()
     */
    @Override
    public Component initContent() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.addComponent(selectField);
        return layout;
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractField#getType()
     */
    @Override
    public Class<Collect> getType() {
        return Collect.class;
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractField#getInternalValue()
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Collect getInternalValue() {
        Collect collect = new Collect();
        if (selectField.getValue() instanceof Set) {
            Set<String> selected = (Set<String>) selectField.getValue();
            for (String value : selected) {
                collect.addIncludeGroup(value);
            }
        } else {
            collect.addIncludeGroup((String)selectField.getValue());
        }
        return collect;
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractField#setInternalValue(java.lang.Object)
     */
    @Override
    protected void setInternalValue(Collect value) {
        selectField.setValue(new TreeSet<String>(value.getIncludeGroups()));
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractField#setReadOnly(boolean)
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        selectField.setReadOnly(readOnly);
        super.setReadOnly(readOnly);
    }

}
