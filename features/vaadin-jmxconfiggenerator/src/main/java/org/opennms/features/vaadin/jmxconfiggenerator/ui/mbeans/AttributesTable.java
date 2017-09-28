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

package org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.event.FieldEvents;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Table;
import com.vaadin.ui.TableFieldFactory;
import com.vaadin.ui.TextField;
import org.opennms.features.vaadin.jmxconfiggenerator.Config;
import org.opennms.features.vaadin.jmxconfiggenerator.data.MetaAttribItem;
import org.opennms.features.vaadin.jmxconfiggenerator.data.MetaAttribItem.AttribType;
import org.opennms.features.vaadin.jmxconfiggenerator.data.SelectableBeanItemContainer;
import org.opennms.features.vaadin.jmxconfiggenerator.data.SelectionValueChangedListener;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.UIHelper;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.validation.AttributeNameValidator;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.validation.UniqueAttributeNameValidator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Markus von Rüden
 */
public class AttributesTable<T> extends Table implements SelectionValueChangedListener {

	private static Object[] computeKey(Object... input) {
		return input;
	}

	//  This TableFieldFactory simply uses the already created fields and returns them (or null).
	private class ReferenceTableFieldFactory implements TableFieldFactory {

		@Override
		public Field<?> createField(Container container, Object itemId, Object propertyId, Component uiContext) {
			Object[] key = computeKey(itemId, propertyId);
			Field<?> field = findFieldByKey(fields, key);
			return field;
		}
	}

	// The default field factory for the attributes table.
	private class AttributesTableFieldFactory implements TableFieldFactory {

		private final Validator nameValidator = new AttributeNameValidator();
		private final Validator lengthValidator = new StringLengthValidator(String.format("Maximum length is %d", Config.ATTRIBUTES_ALIAS_MAX_LENGTH), 0, Config.ATTRIBUTES_ALIAS_MAX_LENGTH, false);
		private final Validator uniqueAttributeNameValidator =  new UniqueAttributeNameValidator(nameProvider, new UniqueAttributeNameValidator.FieldProvider() {
			public Map<Object, Field<String>> getObjectFieldMap() {
				Map<Object, Field<String>> validationMap = new HashMap<>();
				for (Object eachSelectedAttribute : getContainerDataSource().getSelectedAttributes()) {
					validationMap.put(eachSelectedAttribute, aliasFieldsMap.get(eachSelectedAttribute));
				}
				return validationMap;
			}});

		@Override
		public Field<?> createField(final Container container, final Object itemId, Object propertyId, Component uiContext) {
			Field<?> field = null;
			if (MetaAttribItem.ALIAS.equals(propertyId.toString())) {
				field = new TableTextFieldWrapper(createAlias(itemId));
			}
			if (MetaAttribItem.NAME.equals(propertyId.toString())) {
				field = createName(itemId);
			}
			if (MetaAttribItem.SELECTED.equals(propertyId.toString())) {
				field = createSelected(itemId);
			}
			if (MetaAttribItem.TYPE.equals(propertyId.toString())) {
				field = createType(itemId);
			}
			return field;
		}

		private TextField createName(final Object itemId) {
			TextField field = new TextField();
			field.setWidth(100, Unit.PERCENTAGE);
			field.setReadOnly(true);
			return field;
		}

		private CheckBox createSelected(final Object itemId) {
			final CheckBox c = new CheckBox();
			c.setBuffered(false);
			c.setImmediate(true);
			c.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					Collection<Field<?>> fields = Collections2.filter(getFieldsForItemId(itemId), new Predicate<Field<?>>() {
						@Override
						public boolean apply(Field<?> eachField) {
							return eachField != c;
						}
					});
					enableFields(fields, c.getValue());
				}
			});
			c.addValueChangeListener(validateOnValueChangeListener);
			return c;
		}

		private ComboBox createType(Object itemId) {
			ComboBox select = new ComboBox();
			for (AttribType type : AttribType.values()) {
				select.addItem(type.name());
			}
			select.setValue(AttribType.gauge);
			select.setNullSelectionAllowed(false);
			select.setData(itemId);
			select.setBuffered(false);
			select.setImmediate(true);
			select.addValueChangeListener(validateOnValueChangeListener);
			return select;
		}

		private TextField createAlias(Object itemId) {
			final TextField tf = new TextField();
			tf.setValidationVisible(false);
			tf.setBuffered(false);
			tf.setImmediate(true);
			tf.setRequired(true);
			tf.setWidth(300, Unit.PIXELS);
			tf.setMaxLength(Config.ATTRIBUTES_ALIAS_MAX_LENGTH);
			tf.setRequiredError("You must provide a name.");
			tf.addValidator(nameValidator);
			tf.addValidator(lengthValidator);
			tf.addValidator(uniqueAttributeNameValidator);
			tf.addValueChangeListener(validateOnValueChangeListener);
			tf.setData(itemId);
			tf.setTextChangeTimeout(200);
			// by default there is no validation when updating a field, so we manually apply that
			tf.addTextChangeListener(new FieldEvents.TextChangeListener() {
				@Override
				public void textChange(FieldEvents.TextChangeEvent event) {
					tf.setValue(event.getText());
					UIHelper.validateField(tf, true);
				}
			});
			return tf;
		}
	}

	private final Map<Object, Field<String>> aliasFieldsMap = new HashMap<>();
	private final Map<Object[], Field<?>> fields = new HashMap<>();
	private final NameProvider nameProvider;
	private final TableFieldFactory tableFieldFactory;
	private final MBeansController controller;
	private final ValueChangeListener validateOnValueChangeListener = new ValueChangeListener() {
		@Override
		public void valueChange(Property.ValueChangeEvent event) {
			if (!blockValidation && isVisible()) {
				controller.validateCurrentSelection();
			}
		}
	};
	private Item parentItem;
	private Object parentData;
	private boolean blockValidation;

	public AttributesTable(NameProvider nameProvider, MBeansController controller) {
		this.nameProvider = nameProvider;
		this.controller = controller;
		this.tableFieldFactory = new AttributesTableFieldFactory();
		setSizeFull();
		setSelectable(false);
		setEditable(true);
		setValidationVisible(true);
		setReadOnly(true);
		setImmediate(true);
		setTableFieldFactory(new ReferenceTableFieldFactory());
		setValidationVisible(false);
	}

	public void modelChanged(Item parentItem, Object parentData, SelectableBeanItemContainer<T> container) {
		try {
			blockValidation = true;
			if (getParentItem() == parentItem) return;
			setParentItem(parentItem);
			setParentData(parentData);

			// we initialize the tableFieldFactory because by default pagination is enabled, which
			// causes the table not to know all fields. But the validation is bound to a field, therefore
			// we need to create all fields in advance.
			aliasFieldsMap.clear();
			fields.clear();
			for (Object eachItemId : container.getItemIds()) {
				for (Object eachPropertyId : container.getContainerPropertyIds()) {
					Field<?> eachField = tableFieldFactory.createField(container, eachItemId, eachPropertyId, this);
					if (eachField != null) {
						fields.put(computeKey(eachItemId, eachPropertyId), eachField);
						if (MetaAttribItem.ALIAS.equals(eachPropertyId)) {
							aliasFieldsMap.put(eachItemId, (Field<String>) eachField);

							// we have to set this manually, otherwise validation does not work
							Item item = container.getItem(eachItemId);
							Property property = item.getItemProperty(eachPropertyId);
							((Field<String>) eachField).setValue((String) property.getValue());
						}
					}
				}
			}

			setContainerDataSource(container);

			setVisibleColumns(
					MetaAttribItem.SELECTED,
					MetaAttribItem.NAME,
					MetaAttribItem.ALIAS,
					MetaAttribItem.TYPE);

			// enable/disable fields according to parent selection
			Property itemProperty = parentItem.getItemProperty(MBeansTree.MetaMBeansTreeItem.SELECTED);
			enableFields(fields.values(), (Boolean) itemProperty.getValue());
			if ((Boolean) itemProperty.getValue()) {
				updateCheckBoxes();
			}

			// we initially validate to ensure the ui is ok ant not in a broken state.
			// Only to allow this validation we have to do the initialization of the fields (See above) manually.
			// This is not ideal, but it seems to be the only way.
			validateFields(true);
		} finally {
			blockValidation = false;
		}
	}

	public SelectableBeanItemContainer<T> getContainerDataSource() {
		return (SelectableBeanItemContainer<T>) super.getContainerDataSource();
	}

	private static Field<?> findFieldByKey(Map<Object[], Field<?>> inputMap, Object... key) {
		for (Object[] eachKey : inputMap.keySet()) {
			if (Arrays.equals(eachKey, key)) {
				return inputMap.get(eachKey);
			}
		}
		return null;
	}

	private void enableFields(Collection<Field<?>> fields, boolean enabled) {
		for (Field<?> eachField : fields) {
			enableField(eachField, enabled);
		}
	}

	private void enableField(Field<?> field, boolean enabled) {
		field.setEnabled(enabled);
		// we revert changes, when we disable the fields
		if (!enabled) {
			field.discard();
			// hide validation errors when disabled
			if (field instanceof AbstractComponent) {
				((AbstractComponent) field).setComponentError(null);
			}
		}
	}
	@Override
	public void selectionValueChanged(SelectionValueChangedEvent selectionValueChangedEvent) {
		if (selectionValueChangedEvent.getBean() == getParentData()) {
			for (Object eachRowId : getItemIds()) {
				CheckBox checkBox = getCheckBoxForRow(eachRowId);
				updateCheckBox(checkBox, eachRowId, selectionValueChangedEvent.getNewValue());
				checkBox.setEnabled(selectionValueChangedEvent.getNewValue()); // we have to check/uncheck the checkbox according to the parent's selection
			}
		}
	}

	private void updateCheckBoxes() {
		for(Object eachRowId : getItemIds()) {
			CheckBox checkBox = getCheckBoxForRow(eachRowId);
			updateCheckBox(checkBox, eachRowId, checkBox.getValue());
		}
	}

	private void updateCheckBox(CheckBox checkBox, Object rowId, boolean enabled) {
		checkBox.setValue(enabled);
		Collection<Field<?>> columns = getFieldsForItemId(rowId);
		columns.remove(checkBox);
		enableFields(columns, checkBox.getValue());
	}

	@Override
	public void validate() throws InvalidValueException {
		super.validate();
		validateFields(false);
	}

	private void validateFields(boolean swallowValidationExceptions) throws InvalidValueException {
		// Some fields may or may not be selected. We have to consider this in the overall validation
		// therefore we filter out all not selected element.
		final Map<Object, Field<String>> filteredFieldsToValidate = Maps.filterEntries(aliasFieldsMap, new Predicate<Map.Entry<Object, Field<String>>>() {
			@Override
			public boolean apply(Map.Entry<Object, Field<String>> input) {
				return getContainerDataSource().isSelected((T) input.getKey());
			}
		});

		UIHelper.validateFields(new ArrayList<Field<?>>(filteredFieldsToValidate.values()), swallowValidationExceptions);
	}

	@Override
	public void discard() throws SourceException {
		super.discard();
		for (Field<?> eachField : fields.values()) {
			eachField.discard();
		}
	}

	@Override
	public boolean isValid() {
		try {
			validate();
			return true;
		} catch (InvalidValueException invex) {
			return false;
		}
	}

	boolean isDirty() {
		for (Field<?> eachField : fields.values()) {
			if (eachField.isModified()) {
				return true;
			}
		}
		return false;
	}

	private List<Field<?>> getFieldsForItemId(Object itemId) {
		List<Field<?>> fieldList = new ArrayList<>();
		for (Map.Entry<Object[], Field<?>> eachEntry : fields.entrySet()) {
			if (Objects.equals(itemId, eachEntry.getKey()[0])) {
				fieldList.add(eachEntry.getValue());
			}
		}
		return fieldList;
	}

	private CheckBox getCheckBoxForRow(Object rowId) {
		for (Field<?> eachField : getFieldsForItemId(rowId)) {
			if (eachField instanceof CheckBox) {
				return (CheckBox) eachField;
			}
		}
		return null;
	}

	private Item getParentItem() {
		return parentItem;
	}

	private void setParentItem(Item parentItem) {
		this.parentItem = parentItem;
	}

	private Object getParentData() {
		return parentData;
	}

	private void setParentData(Object parentData) {
		this.parentData = parentData;
	}
}