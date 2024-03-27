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
package org.opennms.features.vaadin.jmxconfiggenerator;

import org.opennms.features.vaadin.jmxconfiggenerator.data.UiModel;
import org.opennms.features.vaadin.jmxconfiggenerator.jobs.DetectMBeansJob;
import org.opennms.features.vaadin.jmxconfiggenerator.jobs.GenerateConfigsJob;
import org.opennms.features.vaadin.jmxconfiggenerator.jobs.JobManager;
import org.opennms.features.vaadin.jmxconfiggenerator.jobs.Task;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.ConfigView;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.HeaderPanel;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.ProgressWindow;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.ResultView;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.UiState;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.MBeansView;
import org.opennms.netmgt.config.collectd.jmx.JmxDatacollectionConfig;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.v7.ui.VerticalLayout;

@Theme("jmxconfiggenerator")
@Title("JMX Configuration Generator")
@SuppressWarnings("serial")
public class JmxConfigGeneratorUI extends UI {
	/**
	 * The Header panel which holds the steps which are necessary to complete
	 * the configuration for a new service to get collected.
	 * 
	 */
	private HeaderPanel headerPanel;

	/**
	 * The "content" panel which shows the view for the currently selected step
	 * of the configuration process.
	 */
	private Panel contentPanel;

	private ProgressWindow progressWindow;

	private UiModel model = new UiModel();

	private Navigator navigator;

	private final JobManager jobManager = new JobManager();

	@Override
	protected void init(VaadinRequest request) {
		initHeaderPanel();
		initContentPanel();
		initMainLayout();
		initNavigator();

		updateView(UiState.ServiceConfigurationView);
	}

	private void initNavigator() {
		navigator = new Navigator(this, contentPanel);
		// common views
		navigator.addView(UiState.ServiceConfigurationView.name(), new ConfigView(this));
		navigator.addView(UiState.MbeansView.name(), new MBeansView(this));
		navigator.addView(UiState.ResultView.name(), new ResultView());

		// "working views" need to be simulated, they do not actually exist, but we need them for navigation
		navigator.addView(UiState.MbeansDetection.name(), new Navigator.EmptyView());
		navigator.addView(UiState.ResultConfigGeneration.name(), new Navigator.EmptyView());


		// We need to hook into the "view change" process to prevent changing to the "working views"
		// Instead we trigger a long running task and show a "please wait" window.
		navigator.addViewChangeListener(new ViewChangeListener() {
			@Override
			public boolean beforeViewChange(ViewChangeEvent event) {
				hideProgressWindow();
				headerPanel.enter(event);

				final UiState uiState = UiState.valueOf(event.getViewName());
				if (UiState.ServiceConfigurationView == uiState) {
					UiModel newModel = new UiModel();
					if (model != null) {
						newModel.setServiceConfig(model.getServiceConfig());
					}
					model = newModel;
				}
				if (UiState.MbeansDetection == uiState) {
					showProgressWindow(uiState.getDescription());
					enqueue(new DetectMBeansJob((JmxConfigGeneratorUI) getUI(), getUiModel().getServiceConfig()));
					return false;
				}
				if (UiState.ResultConfigGeneration == uiState) {
					showProgressWindow(uiState.getDescription());
					enqueue(new GenerateConfigsJob((JmxConfigGeneratorUI) getUI(), getUiModel()));
					return false;
				}
				return true;
			}

			@Override
			public void afterViewChange(ViewChangeEvent event) {

			}
		});
	}

	private void enqueue(Task task) {
		jobManager.enqueue(task);
	}

	private void initHeaderPanel() {
		headerPanel = new HeaderPanel();
	}

	// the Main panel holds all views such as Config view, mbeans view, etc.
	private void initContentPanel() {
		contentPanel = new Panel();
		contentPanel.setSizeFull();
	}

	/**
	 * Creates the main window and adds the header, main and button panels to
	 * it.
	 */
	private void initMainLayout() {
		VerticalLayout layout = new VerticalLayout();
		layout.addComponent(headerPanel);
		layout.addComponent(contentPanel);
		layout.setSizeFull();
		// content Panel should use most of the space
		layout.setExpandRatio(contentPanel, 1);
		setContent(layout);
	}

	public void updateView(UiState uiState) {
		navigator.navigateTo(uiState.name());
	}

	private ProgressWindow getProgressWindow() {
		if (progressWindow == null) {
			progressWindow = new ProgressWindow(jobManager);
		}
		return progressWindow;
	}

	public void hideProgressWindow() {
		removeWindow(getProgressWindow());
		getProgressWindow().markAsDirtyRecursive();
	}

	private void showProgressWindow(String label) {
		getProgressWindow().setLabelText(label);
		addWindow(getProgressWindow());
		getProgressWindow().setVisible(true);
	}

	public void setRawModel(JmxDatacollectionConfig newModel) {
		model.setRawModel(newModel);
	}

	public UiModel getUiModel() {
		return model;
	}
}
