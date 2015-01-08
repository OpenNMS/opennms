/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.gwt.web.ui.asset.client.tools.fieldsets;

import java.util.Date;

import org.opennms.gwt.web.ui.asset.client.tools.validation.StringDateLocalValidator;
import org.opennms.gwt.web.ui.asset.client.tools.validation.StringMaxLengthValidator;

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.datepicker.client.DateBox;

/**
 * @author <a href="mailto:MarkusNeumannMarkus@gmail.com">Markus
 *         Neumann</a></br> {@link FieldSet} for displaying and editing
 *         {@link Dates}. It's working on stings for compatibility reasons with
 *         old db-code. Works with an internal date format of "yyyy-MM-dd".
 *         Displays the Date as i18n date format. If the given string is not in
 *         "yyyy-MM-dd" format, or the input by the ui is not compatible with
 *         "yyyy-MM-dd" a warning will be set but no errors. So even with
 *         strange or non date format the {@link FieldSetDateBox} will not block
 *         the work.
 */
public class FieldSetDateBox extends AbstractFieldSet implements FieldSet, ValueChangeHandler<Date>, MouseUpHandler,
		KeyUpHandler {

	private DateBox dateBox = new DateBox();

	private final DateTimeFormat localFormater = DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM);
	private final DateTimeFormat onmsFormater = DateTimeFormat.getFormat("yyyy-MM-dd");

	public FieldSetDateBox(String name, String value, String helpText) {
		super(name, helpText);
		init(value, -1);
	}

	@UiConstructor
	public FieldSetDateBox(String name, String value, String helpText, int maxLength) {
		super(name, helpText);
		init(value, maxLength);
	}

	/**
	 * Returns internal value, if possible as "yyyy-MM-dd" like sting
	 * representation of date. But returned string can be any string if the
	 * users is not following the warnings.
	 * 
	 * @return String value
	 */
	@Override
	public String getValue() {
		String result;
		try {
			result = onmsFormater.format(dateBox.getValue());
		} catch (Exception e) {
			result = dateBox.getTextBox().getValue();
		}
		return result;
	}

	private void init(String value, int maxLength) {

		if (maxLength > 0) {
			addErrorValidator(new StringMaxLengthValidator(maxLength));
		}
		addWarningValidator(new StringDateLocalValidator());

		try {
			dateBox.setValue(onmsFormater.parseStrict(value));
		} catch (IllegalArgumentException e) {
			dateBox.getTextBox().setText(value);
		}
		inititalValue = value;
		dateBox.setFormat(new DateBox.DefaultFormat(localFormater));
		dateBox.getTextBox().addFocusHandler(this);
		dateBox.getTextBox().addChangeHandler(this);
		dateBox.getTextBox().addMouseUpHandler(this);
		dateBox.getTextBox().addKeyUpHandler(this);

		dateBox.addValueChangeHandler(this);
		dateBox.addStyleName("form-control");

		panel.add(dateBox);
	}

	@Override
	public void onKeyUp(KeyUpEvent event) {
		checkField();
	}

	@Override
	public void onMouseUp(MouseUpEvent event) {
		checkField();
	}

	@Override
	public void onValueChange(ValueChangeEvent<Date> event) {
		checkField();
	}

	@Override
	public void setEnabled(Boolean enabled) {
		dateBox.getTextBox().setEnabled(enabled);
	}

	/**
	 * To get a valid input without warnings use "yyyy-MM-dd" formated string
	 * representation of date. But any string can be set to the value.
	 * 
	 * @param String
	 *            value
	 */
	@Override
	public void setValue(String value) {
		try {
			dateBox.setValue(onmsFormater.parseStrict(value));
		} catch (Exception e) {
			dateBox.getTextBox().setText(value);
		}
		inititalValue = value;
		validate(this.getValue());
	}
}
