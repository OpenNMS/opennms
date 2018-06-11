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

package org.opennms.features.vaadin.jmxconfiggenerator;

import org.opennms.features.vaadin.jmxconfiggenerator.data.UiModel;
import org.opennms.features.vaadin.jmxconfiggenerator.jobs.DetectMBeansJob;
import org.opennms.features.vaadin.jmxconfiggenerator.jobs.GenerateConfigsJob;
import org.opennms.features.vaadin.jmxconfiggenerator.jobs.JobManager;
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
import com.vaadin.ui.VerticalLayout;

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
					enqueue(new DetectMBeansJob(getUiModel().getServiceConfig()));
					return false;
				}
				if (UiState.ResultConfigGeneration == uiState) {
					showProgressWindow(uiState.getDescription());
					enqueue(new GenerateConfigsJob(getUiModel()));
					return false;
				}
				return true;
			}

			@Override
			public void afterViewChange(ViewChangeEvent event) {

			}
		});
	}

	private void enqueue(JobManager.Task task) {
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
