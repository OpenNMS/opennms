/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.validation;

import java.util.Collection;
import java.util.Objects;

import org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.MbeansHierarchicalContainer;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.NameProvider;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.SelectionManager;
import org.opennms.netmgt.config.collectd.jmx.Attrib;
import org.opennms.netmgt.config.collectd.jmx.CompAttrib;
import org.opennms.netmgt.config.collectd.jmx.CompMember;
import org.opennms.netmgt.config.collectd.jmx.Mbean;

public class ValidationManager {

    protected final AttributeNameValidator attributeNameValidator;
    protected final MaximumLengthValidator attributeLengthValidator;
    protected final com.vaadin.data.Validator attributeUniqueNameValidator;

    protected final NameProvider nameProvider;

    protected final SelectionManager selectionManager;

    public ValidationManager(final NameProvider nameProvider, SelectionManager selectionManager) {
        Objects.requireNonNull(nameProvider);
        Objects.requireNonNull(selectionManager);
        this.nameProvider = nameProvider;
        this.selectionManager = selectionManager;

        attributeNameValidator = new AttributeNameValidator();
        attributeLengthValidator = new MaximumLengthValidator();
        attributeUniqueNameValidator = new UniqueAttributeNameValidator(nameProvider);
    }

    public ValidationResult validate(MbeansHierarchicalContainer container) {
        return validate(container.getSelectedMbeans());
    }

    private ValidationResult validate(Collection<Mbean> objectsToValidate) {
        ValidationResult result = new ValidationResult();
        for (Mbean eachMbean : objectsToValidate) {
            validate(eachMbean, result);
        }
        return result;
    }

    protected void validateInternal(com.vaadin.data.Validator validator, Object itemId, Object value, ValidationResult result) {
        try {
            validator.validate(value);
        } catch (com.vaadin.data.Validator.InvalidValueException ex) {
            result.add(itemId, ex);
        }
    }

    public void validate(Mbean mbean, ValidationResult validationResult) {
        validateInternal(new NameValidator(), mbean, mbean.getName(), validationResult);

        // 2. validate each CompositeAttribute
        for (CompAttrib eachCompositeAttribute : selectionManager.getSelectedCompositeAttributes(mbean)) {
            validate(eachCompositeAttribute, validationResult);
        }

        // 3. validate each Attribute
        for (Attrib eachAttribute : selectionManager.getSelectedAttributes(mbean)) {
            validate(eachAttribute, validationResult);
        }
    }


    public void validate(CompAttrib compAttrib, ValidationResult validationResult) {
        validateInternal(new NameValidator(), compAttrib, compAttrib.getName(), validationResult);
        for (CompMember eachCompMember : selectionManager.getSelectedCompositeMembers(compAttrib)) {
           validate(eachCompMember, validationResult);
        }
    }


    protected void validate(CompMember compMember, ValidationResult validationResult) {
        validateInternal(attributeNameValidator, compMember, compMember.getAlias(), validationResult);
        validateInternal(attributeLengthValidator, compMember, compMember.getAlias(), validationResult);
        validateInternal(attributeUniqueNameValidator, compMember, compMember.getAlias(), validationResult);
    }

    protected void validate(Attrib attrib, ValidationResult validationResult) {
        validateInternal(attributeNameValidator, attrib, attrib.getAlias(), validationResult);
        validateInternal(attributeLengthValidator, attrib, attrib.getAlias(), validationResult);
        validateInternal(attributeUniqueNameValidator, attrib, attrib.getAlias(), validationResult);
    }
}
