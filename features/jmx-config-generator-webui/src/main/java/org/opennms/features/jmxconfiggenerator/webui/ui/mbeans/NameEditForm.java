/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

import org.opennms.features.jmxconfiggenerator.webui.Config;
import org.opennms.features.jmxconfiggenerator.webui.data.MetaMBeanItem;
import org.opennms.features.jmxconfiggenerator.webui.data.ModelChangeListener;
import org.opennms.features.jmxconfiggenerator.webui.ui.UIHelper;
import org.opennms.features.jmxconfiggenerator.webui.ui.mbeans.EditControls.ButtonType;
import org.opennms.features.jmxconfiggenerator.webui.ui.validators.NameValidator;

import com.vaadin.data.Item;
import com.vaadin.data.Validator;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.FormFieldFactory;
import com.vaadin.ui.TextField;

/**
 * Handles the editing of the MBeans name.
 * 
 * @author Markus von Rüden
 */
public class NameEditForm extends Form implements ModelChangeListener<Item>, ViewStateChangedListener,
		EditControls.Callback {

	private final EditControls footer = new EditControls(this);
	private final MBeansController controller;
	private final Validator nameValidator = new NameValidator();
	private final FormParameter parameter;

	public NameEditForm(MBeansController controller, final FormParameter parameter) {
		this.controller = controller;
		this.parameter = parameter;
		setFormFieldFactory(new FormFieldFactory() {
			@Override
			public Field createField(Item item, Object propertyId, Component uiContext) {
				if (propertyId.toString().equals(MetaMBeanItem.SELECTED)) {
					CheckBox c = new CheckBox(MetaMBeanItem.SELECTED);
					return c;
				}
				if (propertyId.toString().equals(parameter.getNonEditablePropertyName())) {
					final TextField tf = new TextField(parameter.getNonEditablePropertyName()) {
						@Override
						public void setReadOnly(boolean readOnly) {
							super.setReadOnly(true); // never ever edit me
						}
					};
					tf.setWidth(100, UNITS_PERCENTAGE);
					return tf;
				}
				if (propertyId.toString().equals(parameter.getEditablePropertyName())) {
					TextField tf = new TextField(parameter.getEditablePropertyName());
					tf.setWidth(100, UNITS_PERCENTAGE);
					tf.setValidationVisible(true);
					tf.setRequired(true);
					tf.setRequiredError("You must provide a name.");
					tf.addValidator(nameValidator);
					return tf;
				}
				return null;
			}
		});
		setWidth(100, UNITS_PERCENTAGE);
		setHeight(Config.NAME_EDIT_FORM_HEIGHT + (parameter.hasFooter() ? 0 : -60), UNITS_PIXELS);
		setReadOnly(true);
		setImmediate(true);
		setBuffered(true);
		if (parameter.hasFooter()) setFooter(footer);
		addFooterHooks();
		setCaption(parameter.getCaption());
		setVisibleItemProperties(parameter.getVisiblePropertieNames());
	}

	@Override
	public void modelChanged(Item newItem) {
		setItemDataSource(newItem);
		setVisibleItemProperties(getVisibleItemProperties());
	}

	@Override
	public void viewStateChanged(ViewStateChangedEvent event) {
		switch (event.getNewState()) {
			case Init:
			case NonLeafSelected:
				modelChanged(null); // reset
			case Edit: // no reset, just hide
				setEnabled(event.getSource() == this);
				getFooter().setVisible(event.getSource() == this);
				break;
			// activate
			case LeafSelected:
				setReadOnly(true);
				setEnabled(true);
				getFooter().setVisible(true);
				break;
		}
	}

	// lock or unlock the whole view!
	private void addFooterHooks() {
		footer.addCancelHook(this);
		footer.addEditHook(this);
		footer.addSaveHook(this);
	}

	@Override
	public void callback(ButtonType type, Component outer) {
		if (type == ButtonType.cancel) {
			controller.fireViewStateChanged(ViewState.LeafSelected, this);
			callAdditionalCallbacksIfThereAreAny(type, outer);
		}
		if (type == ButtonType.edit) {
			controller.fireViewStateChanged(ViewState.Edit, this);
			callAdditionalCallbacksIfThereAreAny(type, outer);
		}
		// save must be handled with care
		if (type == ButtonType.save) {
			if (isValid()) {
				commit();
				controller.fireViewStateChanged(ViewState.LeafSelected, this);
				callAdditionalCallbacksIfThereAreAny(type, outer);
			} else {
				UIHelper.showValidationError(
						"There are errors in this view. Please fix them first or cancel.");
			}
		}
	}

	private void callAdditionalCallbacksIfThereAreAny(ButtonType type, Component outer) {
		if (parameter.getAdditionalCallback() == null) return;
		parameter.getAdditionalCallback().callback(type, outer);
	}
	
	protected FormParameter getFormParameter() {
		return parameter;
	}
}
