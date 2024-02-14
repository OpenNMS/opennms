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
package org.opennms.features.vaadin.dashboard.config.ui.editors;

import com.vaadin.v7.ui.AbstractField;
import com.vaadin.v7.ui.NativeSelect;
import com.vaadin.v7.ui.TextField;

/**
 * This {@link Enum} is used to represent the different types of parameters for a criteria.
 *
 * @author Christian Pape
 */
public enum CriteriaEntry {
    Property() {
        public AbstractField<Object> getComponent(CriteriaBuilderHelper criteriaBuilderHelper) {
            NativeSelect nativeSelect = new NativeSelect();

            nativeSelect.setCaption("Property");

            nativeSelect.setNullSelectionAllowed(false);
            nativeSelect.setMultiSelect(false);
            nativeSelect.setNewItemsAllowed(false);
            nativeSelect.setInvalidAllowed(false);
            nativeSelect.setStyleName("small");
            nativeSelect.setDescription("Property selection");

            boolean first = true;
            for (String property : criteriaBuilderHelper.getEntities()) {
                nativeSelect.addItem(property);
                if (first) {
                    nativeSelect.select(property);
                    first = false;
                }
            }

            return nativeSelect;
        }
    },

    Value() {
        public AbstractField<String> getComponent(CriteriaBuilderHelper criteriaBuilderHelper) {
            TextField textField = new TextField();

            textField.setCaption("Value");
            textField.setStyleName("small");
            textField.setDescription("Value");
            return textField;
        }
    },

    StringValue() {
        public AbstractField<String> getComponent(CriteriaBuilderHelper criteriaBuilderHelper) {
            TextField textField = new TextField();

            textField.setCaption("Text value");
            textField.setStyleName("small");
            textField.setDescription("Text value");

            return textField;
        }
    },

    IntegerValue() {
        public AbstractField<String> getComponent(CriteriaBuilderHelper criteriaBuilderHelper) {
            TextField textField = new TextField();

            textField.setCaption("Integer value");
            textField.setStyleName("small");
            textField.setDescription("Integer value");

            return textField;
        }
    };

    /**
     * This method returns a component for editing the type of content.
     *
     * @param criteriaBuilderHelper the {@link CriteriaBuilderHelper} to be used
     * @return an {@link AbstractField} component
     */
    public abstract AbstractField<?> getComponent(CriteriaBuilderHelper criteriaBuilderHelper);
}