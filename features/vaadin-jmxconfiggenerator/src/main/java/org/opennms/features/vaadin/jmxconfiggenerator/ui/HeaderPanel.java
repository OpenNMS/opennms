/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.vaadin.jmxconfiggenerator.ui;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
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
