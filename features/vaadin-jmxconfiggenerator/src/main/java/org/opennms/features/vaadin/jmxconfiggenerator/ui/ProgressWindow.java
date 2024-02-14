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

import org.opennms.netmgt.vaadin.core.UIHelper;
import org.opennms.features.vaadin.jmxconfiggenerator.JmxConfigGeneratorUI;
import org.opennms.features.vaadin.jmxconfiggenerator.jobs.JobManager;

import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.ProgressBar;
import com.vaadin.v7.ui.VerticalLayout;
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
