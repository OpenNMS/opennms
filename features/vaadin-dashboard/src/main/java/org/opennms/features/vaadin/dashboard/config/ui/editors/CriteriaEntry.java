/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.vaadin.dashboard.config.ui.editors;

import com.vaadin.ui.AbstractField;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;

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