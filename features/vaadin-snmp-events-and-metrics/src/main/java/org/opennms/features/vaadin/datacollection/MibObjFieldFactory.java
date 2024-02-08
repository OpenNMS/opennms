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

import com.vaadin.v7.data.Container;
import com.vaadin.v7.data.validator.RegexpValidator;
import com.vaadin.v7.data.validator.StringLengthValidator;
import com.vaadin.v7.ui.AbstractSelect.NewItemHandler;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.v7.ui.Field;
import com.vaadin.v7.ui.TableFieldFactory;
import com.vaadin.v7.ui.TextField;

/**
 * The MIB Object Field.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class MibObjFieldFactory implements TableFieldFactory {

    /** The resource types. */
    private final List<String> resourceTypes;

    /**
     * Instantiates a new MIB Object field factory.
     *
     * @param resourceTypes the available resource types
     */
    public MibObjFieldFactory(List<String> resourceTypes) {
        this.resourceTypes = resourceTypes;
    }

    @Override
    public Field<?> createField(Container container, Object itemId, Object propertyId, Component uiContext) {
        if (propertyId.equals("oid")) {
            final TextField field = new TextField();
            field.setSizeFull();
            field.setRequired(true);
            field.setImmediate(true);
            field.addValidator(new RegexpValidator("^\\.[.\\d]+$", "Invalid OID {0}"));
            return field;
        }
        if (propertyId.equals("instance")) {
            final ComboBox field = new ComboBox();
            field.setSizeFull();
            field.setRequired(true);
            field.setImmediate(true);
            field.setNullSelectionAllowed(false);
            field.setNewItemsAllowed(true);
            field.setNewItemHandler(new NewItemHandler() {
                @Override
                public void addNewItem(String newItemCaption) {
                    if (!field.containsId(newItemCaption)) {
                        field.addItem(newItemCaption);
                        field.setValue(newItemCaption);
                    }
                }
            });
            field.addItem("0");
            field.addItem("ifIndex");
            for (String rt : resourceTypes) {
                field.addItem(rt);
            }
            return field;
        }
        if (propertyId.equals("alias")) {
            final TextField field = new TextField();
            field.setSizeFull();
            field.setRequired(true);
            field.setImmediate(true);
            field.addValidator(new StringLengthValidator("Invalid alias. It should not contain more than 19 characters.", 1, 19, false));
            return field;
        }
        if (propertyId.equals("type")) {
            final TextField field = new TextField();
            field.setSizeFull();
            field.setRequired(true);
            field.setImmediate(true);
            field.addValidator(new RegexpValidator("^(?i)(counter|gauge|timeticks|integer|octetstring|string)?\\d*$", // Based on NumericAttributeType and StringAttributeType
                    "Invalid type {0}. Valid types are: counter, gauge, timeticks, integer, octetstring, string"));
            return field;
        }
        return null;
    }

}
