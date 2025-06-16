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

import com.vaadin.v7.data.Validator;

public class ValidationManager {

    protected final AttributeNameValidator attributeNameValidator;
    protected final MaximumLengthValidator attributeLengthValidator;
    protected final Validator attributeUniqueNameValidator;

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

    protected void validateInternal(Validator validator, Object itemId, Object value, ValidationResult result) {
        try {
            validator.validate(value);
        } catch (Validator.InvalidValueException ex) {
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
