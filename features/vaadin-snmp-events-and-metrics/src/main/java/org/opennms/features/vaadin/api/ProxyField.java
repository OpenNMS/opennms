/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.features.vaadin.api;

import java.util.Collection;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.ui.Field;
import com.vaadin.ui.VerticalLayout;

// TODO: I'm not sure if this is the best way to do that but it works
/**
 * The Proxy Field Class.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class ProxyField<T> extends VerticalLayout implements Field<T> {

    /**
     * Instantiates a new proxy field.
     *
     * @param field the field
     */
    public ProxyField(Field<T> field) {
        super();
        addComponent(field);
        setSizeFull();
    }

    /**
     * Gets the field.
     *
     * @return the field
     */
    public Field<T> getField() {
        return (Field<T>)getComponent(0);
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.BufferedValidatable#isInvalidCommitted()
     */
    @Override
    public boolean isInvalidCommitted() {
        return getField().isInvalidCommitted();
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.BufferedValidatable#setInvalidCommitted(boolean)
     */
    @Override
    public void setInvalidCommitted(boolean isCommitted) {
        getField().setInvalidCommitted(isCommitted);
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.Buffered#commit()
     */
    @Override
    public void commit() throws SourceException, InvalidValueException {
        getField().commit();
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.Buffered#discard()
     */
    @Override
    public void discard() throws SourceException {
        getField().discard();
    }

    @Override
    public boolean isBuffered() {
        return getField().isBuffered();
    }

    @Override
    public void setBuffered(boolean writeThrough) throws SourceException, InvalidValueException {
        getField().setBuffered(writeThrough);
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.Buffered#isModified()
     */
    @Override
    public boolean isModified() {
        return getField().isModified();
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.Validatable#addValidator(com.vaadin.data.Validator)
     */
    @Override
    public void addValidator(Validator validator) {
        getField().addValidator(validator);
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.Validatable#removeValidator(com.vaadin.data.Validator)
     */
    @Override
    public void removeValidator(Validator validator) {
        getField().removeValidator(validator);
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.Validatable#removeValidator(com.vaadin.data.Validator)
     */
    @Override
    public void removeAllValidators() {
        getField().removeAllValidators();
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.Validatable#getValidators()
     */
    @Override
    public Collection<Validator> getValidators() {
        return getField().getValidators();
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.Validatable#isValid()
     */
    @Override
    public boolean isValid() {
        return getField().isValid();
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.Validatable#validate()
     */
    @Override
    public void validate() throws InvalidValueException {
        getField().validate();
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.Validatable#isInvalidAllowed()
     */
    @Override
    public boolean isInvalidAllowed() {
        return getField().isInvalidAllowed();
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.Validatable#setInvalidAllowed(boolean)
     */
    @Override
    public void setInvalidAllowed(boolean invalidValueAllowed) throws UnsupportedOperationException {
        getField().setInvalidAllowed(invalidValueAllowed);
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.Property#getValue()
     */
    @Override
    public T getValue() {
        return (T)getField().getValue();
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.Property#setValue(java.lang.Object)
     */
    @Override
    public void setValue(T newValue) throws ReadOnlyException, ConversionException {
        getField().setValue(newValue);
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.Property#getType()
     */
    @Override
    public Class<? extends T> getType() {
        return getField().getType();
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.Property.ValueChangeNotifier#addListener(com.vaadin.data.Property.ValueChangeListener)
     */
    @Override
    public void addListener(ValueChangeListener listener) {
        getField().addValueChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.Property.ValueChangeNotifier#removeListener(com.vaadin.data.Property.ValueChangeListener)
     */
    @Override
    public void removeListener(ValueChangeListener listener) {
        getField().removeValueChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.Property.ValueChangeNotifier#addListener(com.vaadin.data.Property.ValueChangeListener)
     */
    @Override
    public void addValueChangeListener(ValueChangeListener listener) {
        getField().addValueChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.Property.ValueChangeNotifier#removeListener(com.vaadin.data.Property.ValueChangeListener)
     */
    @Override
    public void removeValueChangeListener(ValueChangeListener listener) {
        getField().removeValueChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.Property.ValueChangeListener#valueChange(com.vaadin.data.Property.ValueChangeEvent)
     */
    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        getField().valueChange(event);
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.Property.Viewer#setPropertyDataSource(com.vaadin.data.Property)
     */
    @Override
    // Because of {@link com.vaadin.data.Property.Viewer#setPropertyDataSource(com.vaadin.data.Property)} API
    @SuppressWarnings("unchecked") 
    public void setPropertyDataSource(Property newDataSource) {
        getField().setPropertyDataSource(newDataSource);
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.Property.Viewer#getPropertyDataSource()
     */
    @Override
    public Property<?> getPropertyDataSource() {
        return getField().getPropertyDataSource();
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.Component.Focusable#getTabIndex()
     */
    @Override
    public int getTabIndex() {
        return getField().getTabIndex();
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.Component.Focusable#setTabIndex(int)
     */
    @Override
    public void setTabIndex(int tabIndex) {
        getField().setTabIndex(tabIndex);
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.Field#isRequired()
     */
    @Override
    public boolean isRequired() {
        return getField().isRequired();
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.Field#setRequired(boolean)
     */
    @Override
    public void setRequired(boolean required) {
        getField().setRequired(required);
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.Field#setRequiredError(java.lang.String)
     */
    @Override
    public void setRequiredError(String requiredMessage) {
        getField().setRequiredError(requiredMessage);
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.Field#getRequiredError()
     */
    @Override
    public String getRequiredError() {
        return getField().getRequiredError();
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractComponent#focus()
     */
    @Override
    public void focus() {
        super.focus();
    }

}
