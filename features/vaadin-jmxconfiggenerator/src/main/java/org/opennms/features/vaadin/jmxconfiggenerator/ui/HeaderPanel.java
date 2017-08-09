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

package org.opennms.features.vaadin.jmxconfiggenerator.ui;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupView;

import org.opennms.netmgt.vaadin.core.UIHelper;
import org.opennms.features.vaadin.jmxconfiggenerator.Config;

/**
 * This class represents the header panel of the JMX config UI tool. It simply
 * creates a label with the text according to the {@link UiState}s.
 * 
 * @author Markus von Rüden
 * @see #updateLabel(String)
 * 
 */
public class HeaderPanel extends Panel implements View {

	private PopupView helpPopupView;

	private Label label;

	public HeaderPanel() {
		label = new Label();
		label.setContentMode(ContentMode.HTML);

		helpPopupView = new PopupView(new HelpContent(UiState.ServiceConfigurationView));
		helpPopupView.setVisible(false);
		helpPopupView.setPopupVisible(false);

		Button popupButton = UIHelper.createButton("", "help", Config.Icons.HELP, new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				helpPopupView.setPopupVisible(true);
				helpPopupView.setVisible(true);
			}
		});

		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);
		layout.addComponent(popupButton);
		layout.addComponent(helpPopupView);
		layout.addComponent(label);
		layout.setComponentAlignment(label, Alignment.MIDDLE_LEFT);

		setContent(layout);
	}

	private void updateLabel(String viewName) {
		label.setValue(generateLabel(viewName));
		UiState uiState = UiState.valueOf(viewName);
		if (uiState.hasUi()) {
			helpPopupView.setContent(new HelpContent(UiState.valueOf(viewName)));
		}
	}

	/**
	 * This method generates the label of the header panel. The header panel shows
	 * all UI steps e.g. (1. Step 1 / 2. Step 2 ... / Step n). For this purpose
	 * the enum {@link UiState} is used and each UiState which has a ui is
	 * printed with a number prefix. If one uiState from {@link UiState} matches
	 * the parameter <code>state</code> it is highlighted as a link. Updates the
	 * label.
	 *
	 * @param viewName The current view name.
	 */
	protected String generateLabel(String viewName) {
		final String selected = "<a href=\"#!%s\">%d. %s</a>";
		final String notSelected = "%d. %s";

		final StringBuilder labelString = new StringBuilder(100);
		int i = 1;
		for (UiState eachState : UiState.values()) {
			if (eachState.hasUi()) {
				final String labelSegment = eachState.name().equals(viewName)
						? String.format(selected, eachState.name(), i, eachState.getDescription())
						: String.format(notSelected, i, eachState.getDescription());
				if (labelString.length() > 0) {
					labelString.append(" / ");
				}
				labelString.append(labelSegment);
				i++;
			}
		}
		return labelString.toString().trim();
	}

	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event) {
		updateLabel(event.getViewName());
	}
}
