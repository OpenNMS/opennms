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

import java.util.Collection;

import org.opennms.gwt.web.ui.asset.client.tools.validation.StringMaxLengthValidator;

import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

/**
 * @author <a href="mailto:MarkusNeumannMarkus@gmail.com">Markus
 *         Neumann</a></br> {@link FieldSet} for displaying and editing text.
 *         Additional a suggestion box will support the user.
 */
public class FieldSetSuggestBox extends AbstractFieldSet implements FieldSet, ValueChangeHandler<String>,
		SelectionHandler<Suggestion>, KeyUpHandler, MouseUpHandler {

	private SuggestBox suggBox;
	private Collection<String> suggestions;
	private MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();

	public FieldSetSuggestBox(String name, String value, String helpText) {
		super(name, helpText);
		init(value, null, -1);
	}

	public FieldSetSuggestBox(String name, String value, String helpText, Collection<String> suggestions) {
		super(name, helpText);
		init(value, suggestions, -1);
	}

	@UiConstructor
	public FieldSetSuggestBox(String name, String value, String helpText, int maxLength) {
		super(name, helpText);
		init(value, null, maxLength);
	}

	public Collection<String> getSuggestions() {
		return suggestions;
	}

	@Override
	public String getValue() {
		return suggBox.getText();
	}

	private void init(String value, Collection<String> suggestions, int maxLength) {
		if (maxLength > 0) {
			addErrorValidator(new StringMaxLengthValidator(maxLength));
		}
		if (suggestions != null) {
			oracle.addAll(suggestions);
			oracle.setDefaultSuggestionsFromText(suggestions);
		}
		inititalValue = value;
		suggBox = new SuggestBox(oracle);

		suggBox.setText(value);

		suggBox.getValueBox().addFocusHandler(this);
		suggBox.getValueBox().addChangeHandler(this);
		suggBox.getValueBox().addValueChangeHandler(this);
		suggBox.getValueBox().addMouseUpHandler(this);
		suggBox.addValueChangeHandler(this);
		suggBox.addKeyUpHandler(this);
		suggBox.addSelectionHandler(this);

		suggBox.addStyleName("form-control");
		panel.add(suggBox);
	}

	@Override
	public void onFocus(FocusEvent event) {
		suggBox.showSuggestionList();
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
	public void onSelection(SelectionEvent<Suggestion> event) {
		String selected = event.getSelectedItem().getReplacementString();
		ValueChangeEvent.fire(suggBox, selected);
		checkField();
	}

	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		checkField();
	}

	@Override
	public void setEnabled(Boolean enabled) {
		suggBox.getValueBox().setEnabled(enabled);
	}
	
	/**
	 * Takes a Collection of Strings as suggestion model to support the uses.
	 * @param suggestions
	 */
	public void setSuggestions(Collection<String> suggestions) {
		this.suggestions = suggestions;
		oracle.clear();
		if (suggestions != null) {
			oracle.addAll(suggestions);
			oracle.setDefaultSuggestionsFromText(suggestions);
		}
	}

	@Override
	public void setValue(String value) {
		suggBox.setText(value);
		inititalValue = value;
		validate(this.getValue());
	}
}
