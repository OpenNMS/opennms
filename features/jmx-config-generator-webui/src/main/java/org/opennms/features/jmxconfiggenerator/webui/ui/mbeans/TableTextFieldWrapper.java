/**
 * *****************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc. OpenNMS(R) is Copyright (C)
 * 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 *
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/ http://www.opennms.com/
 * *****************************************************************************
 */
package org.opennms.features.jmxconfiggenerator.webui.ui.mbeans;

import java.util.Collection;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;

/**
 * This class wraps a {@link TextField} so it is laid out correctly inside a
 * editable Table. Because by default a {@link TextField} inside an editable
 * table does not show any error indicator on a failed validation. The Vertical-
 * or HorizontalLayout does show an error indicator, so we wrap the layout
 * around the text field.
 * 
 */
public class TableTextFieldWrapper extends HorizontalLayout implements Field<String> {

	private TextField textField;

	public TableTextFieldWrapper(final TextField field) {
		this.textField = field;
		addComponent(field);
	}

	@Override
	public boolean isInvalidCommitted() {
		return this.textField.isInvalidCommitted();
	}

	@Override
	public void setInvalidCommitted(final boolean isCommitted) {
		this.textField.setInvalidCommitted(isCommitted);
	}

	@Override
	public void commit() throws SourceException, InvalidValueException {
		this.textField.commit();
	}

	@Override
	public void discard() throws SourceException {
		this.textField.discard();
	}

	@Override
	public boolean isBuffered() {
		return this.textField.isBuffered();
	}

	@Override
	public void setBuffered(final boolean readThrough) throws SourceException {
		this.textField.setBuffered(readThrough);
	}

	@Override
	public boolean isModified() {
		return this.textField.isModified();
	}

	@Override
	public void addValidator(final Validator validator) {
		this.textField.addValidator(validator);
	}

	@Override
	public void removeValidator(final Validator validator) {
		this.textField.removeValidator(validator);
	}

	@Override
	public void removeAllValidators() {
		this.textField.removeAllValidators();
	}

	@Override
	public Collection<Validator> getValidators() {
		return this.textField.getValidators();
	}

	@Override
	public boolean isValid() {
		return this.textField.isValid();
	}

	@Override
	public void validate() throws InvalidValueException {
		this.textField.validate();
	}

	@Override
	public boolean isInvalidAllowed() {
		return this.textField.isInvalidAllowed();
	}

	@Override
	public void setInvalidAllowed(final boolean invalidValueAllowed) throws UnsupportedOperationException {
		this.textField.setInvalidAllowed(invalidValueAllowed);
	}

	@Override
	public String getValue() {
		return this.textField.getValue();
	}

	@Override
	public void setValue(final String newValue) throws ReadOnlyException {
		this.textField.setValue(newValue);
	}

	@Override
	public Class<String> getType() {
		return this.textField.getType();
	}

	@Override
	public void addListener(final ValueChangeListener listener) {
		addValueChangeListener(listener);
	}

	@Override
	public void addValueChangeListener(final ValueChangeListener listener) {
		this.textField.addValueChangeListener(listener);
	}

	@Override
	public void removeListener(final ValueChangeListener listener) {
		removeValueChangeListener(listener);
	}

	@Override
	public void removeValueChangeListener(final ValueChangeListener listener) {
		this.textField.removeValueChangeListener(listener);
	}

	@Override
	public void valueChange(final com.vaadin.data.Property.ValueChangeEvent event) {
		this.textField.valueChange(event);
	}

	@Override
	public void setPropertyDataSource(final Property newDataSource) {
		this.textField.setPropertyDataSource(newDataSource);
	}

	@Override
	public Property getPropertyDataSource() {
		return this.textField.getPropertyDataSource();
	}

	@Override
	public int getTabIndex() {
		return this.textField.getTabIndex();
	}

	@Override
	public void setTabIndex(final int tabIndex) {
		this.textField.setTabIndex(tabIndex);
	}

	@Override
	public boolean isRequired() {
		return this.textField.isRequired();
	}

	@Override
	public void setRequired(final boolean required) {
		this.textField.setRequired(required);
	}

	@Override
	public void setRequiredError(final String requiredMessage) {
		this.textField.setRequiredError(requiredMessage);
	}

	@Override
	public String getRequiredError() {
		return this.textField.getRequiredError();
	}

	@Override
	public void focus() {
		super.focus();
	}
}
