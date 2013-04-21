/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.features.jmxconfiggenerator.webui.ui.mbeans;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.terminal.ErrorMessage;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.Paintable.RepaintRequestListener;
import com.vaadin.terminal.UserError;
import com.vaadin.ui.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opennms.features.jmxconfiggenerator.webui.Config;
import org.opennms.features.jmxconfiggenerator.webui.data.MetaAttribItem;
import org.opennms.features.jmxconfiggenerator.webui.data.MetaAttribItem.AttribType;
import org.opennms.features.jmxconfiggenerator.webui.ui.mbeans.MBeansController.Callback;
import org.opennms.features.jmxconfiggenerator.webui.ui.validators.AttributeNameValidator;
import org.opennms.features.jmxconfiggenerator.webui.ui.validators.UniqueAttributeNameValidator;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Mbean;

/**
 *
 * @author Markus von Rüden
 */
public class AttributesTable extends Table {

	final private Map<Object, Field> fieldsToValidate = new HashMap<Object, Field>();
	private List<Field> fields = new ArrayList<Field>();
	private final UniqueAttributeNameValidator uniqueAttributeNameValidator;
	private final Callback callback;

	public AttributesTable(NameProvider provider, MBeansController.Callback callback) {
		this.callback = callback;
		this.uniqueAttributeNameValidator =  new UniqueAttributeNameValidator(provider, fieldsToValidate);
		setSizeFull();
		setSelectable(false);
		setEditable(false);
		setValidationVisible(true);
		setReadOnly(true);
		setImmediate(true);
		setTableFieldFactory(new AttributesTableFieldFactory());		
	}

	public void modelChanged(Mbean bean) {
		if (getData() == bean) return;
		setData(bean);
		fieldsToValidate.clear();
		fields.clear();
		setContainerDataSource(callback.getContainer());
		if (getContainerDataSource() == MBeansController.AttributesContainerCache.NULL) return;
		setVisibleColumns(new Object[]{MetaAttribItem.SELECTED, MetaAttribItem.NAME, MetaAttribItem.ALIAS, MetaAttribItem.TYPE});
	}

	void viewStateChanged(ViewStateChangedEvent event) {
		switch (event.getNewState()) {
			case Init:
				fieldsToValidate.clear();
				fields.clear();
			case NonLeafSelected:
				modelChanged(null);
				break;
			case LeafSelected:
				setReadOnly(true);
				break;
//			case Edit:
//				setReadOnly(event.getSource() != this);
//				break;
		}
	}

	private class AttributesTableFieldFactory implements TableFieldFactory {

		final private Validator nameValidator = new AttributeNameValidator();
		final private Validator lengthValidator = new StringLengthValidator(String.format("Maximal length is %d", Config.ATTRIBUTES_ALIAS_MAX_LENGTH), 0, Config.ATTRIBUTES_ALIAS_MAX_LENGTH, false); 

		@Override
		public Field createField(Container container, Object itemId, Object propertyId, Component uiContext) {
			Field field = null;
			if (propertyId.toString().equals(MetaAttribItem.ALIAS)) {
				Field tf = new TableTextFieldWrapper(createAlias(itemId));
				fieldsToValidate.put(itemId, tf); //is needed to decide if this table is valid or not
				field = tf;
			}
			if (propertyId.toString().equals(MetaAttribItem.SELECTED)) {
				CheckBox c = new CheckBox();
				c.setWriteThrough(false);
				field = c;
			}
			if (propertyId.toString().equals(MetaAttribItem.TYPE))
				field = createType(itemId);
			if (field == null) return null;
			fields.add(field);
			return field;
		}

		private Select createType(Object itemId) {
			Select select = new Select();
			for (AttribType type : AttribType.values())
				select.addItem(type.name());
			select.setValue(AttribType.valueOf(itemId).name());
			select.setNullSelectionAllowed(false);
			select.setData(itemId);
			select.setWriteThrough(false);
			return select;
		}

		private TextField createAlias(Object itemId) {
			final TextField tf = new TextField();
			tf.setValidationVisible(true);
			tf.setWriteThrough(false);
			tf.setImmediate(true);
			tf.setRequired(true);
			tf.setWidth(100, UNITS_PERCENTAGE);
			tf.setMaxLength(Config.ATTRIBUTES_ALIAS_MAX_LENGTH);
			tf.setRequiredError("You must provide an attribute name.");
			tf.addValidator(nameValidator);
			tf.addValidator(lengthValidator);
			tf.addValidator(uniqueAttributeNameValidator);
			tf.setData(itemId);
			return tf;
		}
	}

	@Override
	public void commit() throws SourceException, InvalidValueException {
		super.commit();
		if (isReadOnly()) return; //we do not commit on read only
		for (Field f : fields) f.commit();
	}

	@Override
	public void discard() throws SourceException {
		super.discard();
		for (Field f : fields) f.discard();
	}
	
	@Override
	public void validate() throws InvalidValueException {
		super.validate();
		InvalidValueException validationException = null;
		//validators must be invoked manually
		for (Field tf : fieldsToValidate.values()) {
			try {
				tf.validate();
			} catch (InvalidValueException ex) {
				validationException = ex;
			}
		}
		if (validationException != null) throw validationException;
	}

	@Override
	public boolean isValid() {
		try {
			validate();
		} catch (InvalidValueException invex) {
			return false;
		}
		return true;
	}
}
