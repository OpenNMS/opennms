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

import org.opennms.gwt.web.ui.asset.client.tools.validation.StringMaxLengthValidator;

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.TextArea;

/**
 * @author <a href="mailto:MarkusNeumannMarkus@gmail.com">Markus Neumann</a>
 *         </br> {@link FieldSet} for displaying and editing text as a textarea.
 */
public class FieldSetTextArea extends AbstractFieldSet implements FieldSet, KeyUpHandler, MouseUpHandler {

	private TextArea textArea = new TextArea();

	public FieldSetTextArea(String name, String value, String helpText) {
		super(name, helpText);
		init(value, -1);
	}

	@UiConstructor
	public FieldSetTextArea(String name, String value, String helpText, int maxLength) {
		super(name, helpText);
		init(value, maxLength);
	}

	@Override
	public String getValue() {
		return textArea.getText();
	}

	private void init(String value, int maxLength) {

		if (maxLength > 0) {
			addErrorValidator(new StringMaxLengthValidator(maxLength));
		}
		inititalValue = value;
		textArea.setText(value);
		textArea.setEnabled(enabled);
		textArea.addChangeHandler(this);
		textArea.addKeyUpHandler(this);
		textArea.addMouseUpHandler(this);
		textArea.addStyleName("form-control");
		textArea.setHeight("20em");

		panel.add(textArea);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.gwt.event.dom.client.KeyUpHandler#onKeyUp(com.google.gwt.event
	 * .dom.client.KeyUpEvent)
	 */
	@Override
	public void onKeyUp(KeyUpEvent event) {
		checkField();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.gwt.event.dom.client.MouseUpHandler#onMouseUp(com.google.gwt
	 * .event.dom.client.MouseUpEvent)
	 */
	@Override
	public void onMouseUp(MouseUpEvent event) {
		checkField();
	}

	@Override
	public void setEnabled(Boolean enabled) {
		textArea.setEnabled(enabled);
	}

	@Override
	public void setValue(String value) {
		textArea.setText(value);
		inititalValue = value;
		validate(this.getValue());
	}
}
