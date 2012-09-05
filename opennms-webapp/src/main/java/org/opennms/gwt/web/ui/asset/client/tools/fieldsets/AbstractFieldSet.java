/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.gwt.web.ui.asset.client.tools.fieldsets;

import java.util.ArrayList;

import org.opennms.gwt.web.ui.asset.client.tools.validation.Validator;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author <a href="mailto:MarkusNeumannMarkus@gmail.com">Markus Neumann</a>
 *         Implementation of {@link FieldSet} that creats an GWT
 *         {@link Composite} and {@link Panel} based {@link FieldSet}. The
 *         abstract implementation contains no field for value or value-input.
 *         Just extensions of abstract FieldSet will support values. The
 *         FieldSet contains label, help text, warning mechanism, error
 *         mechanism, change mechanism. Warning- and errors-mechanism contains
 *         {@link Validator}s, results will be displayed and marked up by css.
 *         Change-mechanism will markup fields by css if the value was changed
 *         by the user and is differed then original value.
 */
public abstract class AbstractFieldSet extends Composite implements FieldSet {

	protected VerticalPanel mainPanel = new VerticalPanel();
	protected HorizontalPanel panel = new HorizontalPanel();
	protected Label label = new Label();
	protected Boolean enabled = true;
	protected Boolean changed = false;
	protected Label errorLabel = new Label();
	protected Label warningLabel = new Label();
	protected String helpText = "";
	protected DecoratedPopupPanel popPanel = new DecoratedPopupPanel(true);
	protected ArrayList<Validator> errorValidators = new ArrayList<Validator>();
	protected ArrayList<Validator> warningValidators = new ArrayList<Validator>();
	protected Object inititalValue;

	public AbstractFieldSet(String name, final String helpText) {

		// helpText popup preperation
		this.helpText = helpText;
		popPanel.setWidth("400px");
		if ((helpText != null) && (!helpText.equals(""))) {
			label.addMouseOverHandler(new MouseOverHandler() {
				@Override
				public void onMouseOver(MouseOverEvent event) {
					Widget source = ((Widget) event.getSource()).getParent();
					int left = source.getAbsoluteLeft() + 10;
					int top = source.getAbsoluteTop() + source.getOffsetHeight();
					popPanel.setPopupPosition(left, top);
					popPanel.setWidget(new HTML(helpText));
					popPanel.show();
				}
			});

			label.addMouseOutHandler(new MouseOutHandler() {
				@Override
				public void onMouseOut(MouseOutEvent event) {
					popPanel.hide();
				}
			});
		}

		label.setText(name);
		label.setStyleName("label");

		panel.addStyleName("FieldSetHorizontalPanel");
		panel.add(label);

		errorLabel.setVisible(false);
		errorLabel.setText(null);
		errorLabel.setStyleName("FieldSetErrorLabel");

		warningLabel.setVisible(false);
		warningLabel.setText(null);
		warningLabel.setStyleName("FieldSetWarningLabel");

		mainPanel.add(errorLabel);
		mainPanel.add(warningLabel);
		mainPanel.add(panel);

		mainPanel.setStyleName("FieldSet");

		// All composites must call initWidget() in their constructors.
		initWidget(mainPanel);
	}

	public void addErrorValidator(Validator validator) {
		errorValidators.add(validator);
	}

	public void addWarningValidator(Validator validator) {
		warningValidators.add(validator);
	}

	/**
	 * checks if the value of fieldset has changed and starts validation if
	 * necessary.
	 * 
	 * @return true if fieldset was changed to a state not equales the initioal
	 *         state of the value.
	 */
	public boolean checkField() {
		// GWT.log("isValueChanged is called at " + this.getLabel() +
		// " this.getValue()->" + this.getValue() + " initValue->" +
		// inititalValue);
		if (this.getValue() != null) {
			if (!this.getValue().equals(inititalValue)) {
				mainPanel.setStyleDependentName("changed", true);
				changed = true;
				validate(this.getValue());
				return true;
			} else {
				// if a validate field if value is initialvalue again, but last
				// value wasn't valid
				// sample max chars=1; initialValue = 4; add an 2 so 42(error)
				// remode 2(is initailvalue but error still set)
				if (this.getError() != "") {
					validate(this.getValue());
				}
			}
		}
		mainPanel.setStyleDependentName("changed", false);
		changed = false;
		return false;
	}

	@Override
	public void clearChanged() {
		changed = false;
		mainPanel.setStyleDependentName("changed", false);
	}

	@Override
	public void clearErrors() {
		errorLabel.setText(null);
		mainPanel.setStyleDependentName("error", false);
	}

	public void clearErrorValidators() {
		errorValidators.clear();
	}

	@Override
	public void clearWarnings() {
		warningLabel.setText(null);
		mainPanel.setStyleDependentName("warning", false);
	}

	public void clearWarningValidators() {
		warningValidators.clear();
	}

	@Override
	public Boolean getEnabled() {
		return enabled;
	}

	@Override
	public String getError() {
		return errorLabel.getText();
	}

	public ArrayList<Validator> getErrorValidators() {
		return errorValidators;
	}

	@Override
	public String getLabel() {
		return label.getText();
	}

	@Override
	public String getWarning() {
		return warningLabel.getText();
	}

	@Override
	public void onChange(ChangeEvent event) {
		checkField();
	}

	@Override
	public void onFocus(FocusEvent event) {

	}

	@Override
	public void setError(String error) {
		errorLabel.setText(error);
		errorLabel.setVisible(true);
		mainPanel.setStyleDependentName("error", true);
	}

	public void setErrors(ArrayList<String> errors) {
		String allErrors = "";
		for (String error : errors) {
			allErrors += error + " ";
		}
		errorLabel.setText(allErrors);
		errorLabel.setVisible(true);
		mainPanel.setStyleDependentName("error", true);
	}

	public void setErrorValidators(ArrayList<Validator> validators) {
		errorValidators = validators;
	}

	@Override
	public void setLabel(String lable) {
		label.setText(lable);
	}

	@Override
	public void setWarning(String warning) {
		warningLabel.setText(warning);
		warningLabel.setVisible(true);
		mainPanel.setStyleDependentName("warning", true);
	}

	public void setWarnings(ArrayList<String> warnings) {
		String allWarnings = "";
		for (String warning : warnings) {
			allWarnings += warning + " ";
		}
		warningLabel.setText(allWarnings);
		warningLabel.setVisible(true);
		mainPanel.setStyleDependentName("warning", true);
	}

	public void setWarningValidators(ArrayList<Validator> validators) {
		warningValidators = validators;
	}

	/**
	 * Validates FieldSet. Warnings and errors will be checked. CSS tags will be
	 * set if necessary.
	 * 
	 * @param object
	 */
	protected void validate(Object object) {
		// GWT.log("validate is called at " + this.getLabel() +
		// " this.getValue()->" + this.getValue() + " initValue->" +
		// inititalValue);
		// validate errors
		ArrayList<String> errors = new ArrayList<String>();
		for (Validator validator2 : errorValidators) {
			Validator validator = validator2;
			if (validator.validate(object).length() > 0) {
				errors.add(validator.validate(object));
			}
		}
		if (errors.size() > 0) {
			setErrors(errors);
		} else {
			clearErrors();
		}

		// validate warnings
		ArrayList<String> warnings = new ArrayList<String>();
		for (Validator validator2 : warningValidators) {
			Validator validator = validator2;
			if (validator.validate(object).length() > 0) {
				warnings.add(validator.validate(object));
			}
		}
		if (warnings.size() > 0) {
			setWarnings(warnings);
		} else {
			clearWarnings();
		}
	}
}
