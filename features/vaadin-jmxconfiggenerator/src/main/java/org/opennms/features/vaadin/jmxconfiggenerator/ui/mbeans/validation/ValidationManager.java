package org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.validation;

import org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.MbeansHierarchicalContainer;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.NameProvider;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.SelectionManager;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Attrib;
import org.opennms.xmlns.xsd.config.jmx_datacollection.CompAttrib;
import org.opennms.xmlns.xsd.config.jmx_datacollection.CompMember;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Mbean;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ValidationManager {

    private Map<Class<?>, Validator> validatorMap = new HashMap<>();

    public ValidationManager(final NameProvider nameProvider, SelectionManager selectionManager) {
        validatorMap.put(Attrib.class, new AttributeValidator(nameProvider));
        validatorMap.put(CompMember.class, new CompMemberValidator(nameProvider));
        validatorMap.put(CompAttrib.class, new CompAttribValidator(nameProvider, selectionManager));
        validatorMap.put(Mbean.class, new MbeanValidator(nameProvider, selectionManager));
    }

    public ValidationResult validate(MbeansHierarchicalContainer container) {
        return validate(container.getSelectedMbeans());
    }

    private <T> ValidationResult validate(Collection<T> objectsToValidate) {
        ValidationResult result = new ValidationResult();
        for (T eachObject : objectsToValidate) {
            ValidationResult beanResult = validate(eachObject);
            result.merge(beanResult);
        }
        return result;
    }

    public <T> ValidationResult validate(T object) {
        return validate(getValidator(object), object);
    }

    private <T> ValidationResult validate(Validator<T> validator, T objectToValidate) {
        ValidationResult result = new ValidationResult();
        validator.validate(objectToValidate, result);
        return result;
    }

    protected final <T> Validator getValidator(T object) {
        Objects.requireNonNull(object);
        return getValidator(object.getClass());
    }

    protected final <T> Validator getValidator(Class<T> clazz) {
        Objects.requireNonNull(validatorMap.get(clazz));
        return validatorMap.get(clazz);
    }

    protected static abstract class Validator<T> {

        protected final NameProvider nameProvider;

        protected final SelectionManager selectionManager;

        protected Validator(NameProvider nameProvider, SelectionManager selectionManager) {
            Objects.requireNonNull(nameProvider);
            Objects.requireNonNull(selectionManager);
            this.nameProvider = nameProvider;
            this.selectionManager = selectionManager;
        }

        protected abstract void validate(T element, ValidationResult validationResult);

        protected void validateInternal(com.vaadin.data.Validator validator, Object itemId, Object value, ValidationResult result) {
            try {
                validator.validate(value); 
            } catch (com.vaadin.data.Validator.InvalidValueException ex) {
                result.add(itemId, ex);
            }
        }
    }

    protected static abstract class AbstractAttributeValidator<T> extends Validator<T> {
        protected final AttributeNameValidator attributeNameValidator;
        protected final MaximumLengthValidator attributeLengthValidator;
        protected final UniqueAttributeNameValidator attributeUniqueNameValidator;

        public AbstractAttributeValidator(final NameProvider nameProvider, final SelectionManager selectionManager) {
            super(nameProvider, selectionManager);
            attributeNameValidator = new AttributeNameValidator();
            attributeLengthValidator = new MaximumLengthValidator();
            attributeUniqueNameValidator = new UniqueAttributeNameValidator(nameProvider);
        }
    }

    protected static class MbeanValidator extends Validator<Mbean> {

        protected MbeanValidator(NameProvider nameProvider, SelectionManager selectionManager) {
            super(nameProvider, selectionManager);
        }

        @Override
        protected void validate(Mbean mbean, ValidationResult validationResult) {
            validateInternal(new NameValidator(), mbean, mbean.getName(), validationResult);

            // 2. validate each CompositeAttribute
            for (CompAttrib eachCompositeAttribute : selectionManager.getSelectedCompositeAttributes(mbean)) {
                new CompAttribValidator(nameProvider, selectionManager).validate(eachCompositeAttribute, validationResult);
            }

            // 3. validate each Attribute
            for (Attrib eachAttribute : selectionManager.getSelectedAttributes(mbean)) {
                new AttributeValidator(nameProvider).validate(eachAttribute, validationResult);
            }
        }
    }

    protected static class CompAttribValidator extends Validator<CompAttrib> {

        public CompAttribValidator(NameProvider nameProvider, SelectionManager selectionManager) {
            super(nameProvider, selectionManager);
        }

        @Override
        protected void validate(CompAttrib compAttrib, ValidationResult validationResult) {
            validateInternal(new NameValidator(), compAttrib, compAttrib.getName(), validationResult);
            for (CompMember eachCompMember : selectionManager.getSelectedCompositeMembers(compAttrib)) {
                new CompMemberValidator(nameProvider).validate(eachCompMember, validationResult);
            }
        }
    }

    protected static class CompMemberValidator extends AbstractAttributeValidator<CompMember> {

        public CompMemberValidator(NameProvider nameProvider) {
            super(nameProvider, SelectionManager.EMPTY);
        }

        @Override
        protected void validate(CompMember compMember, ValidationResult validationResult) {
            validateInternal(attributeNameValidator, compMember, compMember.getAlias(), validationResult); 
            validateInternal(attributeLengthValidator, compMember, compMember.getAlias(), validationResult); 
            validateInternal(attributeUniqueNameValidator, compMember, compMember.getAlias(), validationResult); 
        }
    }

    protected static class AttributeValidator extends AbstractAttributeValidator<Attrib> {

        public AttributeValidator(NameProvider nameProvider) {
            super(nameProvider, SelectionManager.EMPTY);
        }

        @Override
        protected void validate(Attrib attrib, ValidationResult validationResult) {
            validateInternal(attributeNameValidator, attrib, attrib.getAlias(), validationResult); 
            validateInternal(attributeLengthValidator, attrib, attrib.getAlias(), validationResult); 
            validateInternal(attributeUniqueNameValidator, attrib, attrib.getAlias(), validationResult); 
        }
    }
}
