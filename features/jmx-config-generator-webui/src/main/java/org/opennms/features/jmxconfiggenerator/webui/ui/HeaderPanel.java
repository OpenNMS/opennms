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
package org.opennms.features.jmxconfiggenerator.webui.ui;

import org.opennms.features.jmxconfiggenerator.webui.data.ModelChangeListener;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * This class represents the header panel of the JMX config UI tool. It simply
 * creates a label with the text according to the {@link UiState}s.
 * 
 * @author Markus von Rüden
 * @see #updateLabel(UiState)
 * 
 */
public class HeaderPanel extends Panel implements ModelChangeListener<UiState> {

	private Label label;

	public HeaderPanel() {
		Layout layout = new VerticalLayout();
		label = new Label();
		label.setContentMode(ContentMode.HTML);
		layout.addComponent(label);
		setContent(layout);
	}

	/**
	 * This method updates the label of the header panel. The header panel shows
	 * all UI steps e.g. (1. Step 1 / 2. Step 2 ... / Step n). For this purpose
	 * the enum {@link UiState} is used and each UiState which has a ui is
	 * printed with a number prefix. If one uiState from {@link UiState} matches
	 * the parameter <code>state</code> it is highlighted as a link. Updates the
	 * label.
	 * 
	 * @param state
	 *            The current ui State.
	 */
	private void updateLabel(UiState state) {
		final String selected = "<a href=\"#\">%d. %s</a>";
		final String notSelected = "%d. %s";

		String labelString = "";
		int i = 1;
		for (UiState eachState : UiState.values()) {
			if (!eachState.hasUi()) continue;
			String renderString = eachState.equals(state) ? selected : notSelected;
			if (!labelString.isEmpty()) labelString += " / ";
			labelString += String.format(renderString, i, eachState.getDescription());
			i++;
		}
		label.setValue(labelString.trim());
	}
	
	@Override
	/**
	 * Is invoked when the uiState changes and updates the label.
	 */
	public void modelChanged(UiState newModel) {
		updateLabel(newModel);
	}
}
