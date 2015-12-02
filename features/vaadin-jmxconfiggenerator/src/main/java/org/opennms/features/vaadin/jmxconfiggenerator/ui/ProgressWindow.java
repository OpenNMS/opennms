/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

import org.opennms.netmgt.vaadin.core.UIHelper;
import org.opennms.features.vaadin.jmxconfiggenerator.JmxConfigGeneratorUI;
import org.opennms.features.vaadin.jmxconfiggenerator.jobs.JobManager;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * A modal "PopUp" which shows some text to the user and a "sandglass" (ok it is
 * a turning circle.. but you get my point ;)).
 * 
 * @author Markus von Rüden
 */
public class ProgressWindow extends Window {

	private final ProgressBar progress = new ProgressBar();
	private final VerticalLayout layout = new VerticalLayout();
	private final Label label = new Label("Long running process", ContentMode.HTML);

	public ProgressWindow(final JobManager jobManager) {
		setCaption("processing...");
		setModal(true);
		setResizable(false);
		setClosable(false);
		setWidth(400, Unit.PIXELS);
		setHeight(150, Unit.PIXELS);

		Button cancelButton = UIHelper.createButton("cancel", "Cancels the current process", null, new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				jobManager.cancelAllJobs();
				UIHelper.getCurrent(JmxConfigGeneratorUI.class).updateView(UiState.ServiceConfigurationView);
			}
		});

		progress.setIndeterminate(true); // infinite wheel, instead of 100% bar

		HorizontalLayout contentLayout = new HorizontalLayout(progress, label);
		contentLayout.setSpacing(true);

		layout.addComponent(contentLayout);
		layout.addComponent(cancelButton);
		layout.setComponentAlignment(cancelButton, Alignment.BOTTOM_RIGHT);
		layout.setSpacing(true);
		layout.setMargin(true);

		setContent(layout);
		center();

		addCloseListener(new CloseListener() {
			@Override
			public void windowClose(CloseEvent e) {
				// By default polling is disabled
				UIHelper.getCurrent(JmxConfigGeneratorUI.class).setPollInterval(-1);
			}
		});
	}

	@Override
	public void attach() {
		super.attach();
		// We need to poll, otherwise hiding the progress window will not be
		// visible to the client, because it is triggered by the server.
		// Polling fixes this. But do not forget to disable polling when closing the window (see above)
		UIHelper.getCurrent(JmxConfigGeneratorUI.class).setPollInterval(250);
	}

	public void setLabelText(String label) {
		label = String.format("%s. This may take a while ...", label);
		this.label.setValue(label);
	}
}
