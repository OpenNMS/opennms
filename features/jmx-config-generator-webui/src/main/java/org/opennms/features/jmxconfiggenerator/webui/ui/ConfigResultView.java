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
 ******************************************************************************
 */
package org.opennms.features.jmxconfiggenerator.webui.ui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.opennms.features.jmxconfiggenerator.webui.JmxConfigGeneratorApplication;
import org.opennms.features.jmxconfiggenerator.webui.data.UiModel;
import org.opennms.features.jmxconfiggenerator.webui.data.UiModel.OutputDataKey;
import org.opennms.features.jmxconfiggenerator.webui.data.ModelChangeListener;

import com.vaadin.Application;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

/**
 * 
 * @author Markus von Rüden <mvr@opennms.com>
 */
public class ConfigResultView extends CustomComponent implements ModelChangeListener<UiModel>, Button.ClickListener {

	private TabSheet tabSheet = new TabSheet();
	private Map<UiModel.OutputDataKey, TabContent> tabContentMap = new HashMap<UiModel.OutputDataKey, TabContent>();
	private final ButtonPanel buttonPanel = new ButtonPanel(this);
	private final JmxConfigGeneratorApplication app;

	public ConfigResultView(JmxConfigGeneratorApplication app) {
		this.app = app;
		setSizeFull();

		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.addComponent(tabSheet);
		mainLayout.addComponent(buttonPanel);

		tabSheet.setSizeFull();
		// TODO set tab name differently (e.g. SNMP Graph properties snippet)
		tabContentMap.put(OutputDataKey.JmxDataCollectionConfig, new TabContent(OutputDataKey.JmxDataCollectionConfig));
		tabContentMap.put(OutputDataKey.SnmpGraphProperties, new TabContent(OutputDataKey.SnmpGraphProperties));
		tabContentMap.put(OutputDataKey.CollectdConfigSnippet, new TabContent(OutputDataKey.CollectdConfigSnippet));

		// add all tabs
		for (TabContent eachContent : tabContentMap.values())
			tabSheet.addTab(eachContent, eachContent.getLabelText());
		tabSheet.setSelectedTab(0); // select first component!

		buttonPanel.getNext().setCaption("download");
		buttonPanel.getNext().setIcon(IconProvider.getIcon(IconProvider.BUTTON_SAVE));
		
		mainLayout.setExpandRatio(tabSheet, 1);
		setCompositionRoot(mainLayout);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getSource().equals(buttonPanel.getPrevious())) app.updateView(UiState.MbeansView);
		if (event.getSource().equals(buttonPanel.getNext())) downloadConfigFile(event);
	}

	/**
	 * Initiates the download of the String data shown in the currently selected
	 * tab.
	 * 
	 * @param event
	 *            The ClickEvent which indicates the download action.
	 */
	private void downloadConfigFile(ClickEvent event) {
		final TabContent selectedTabContent = getSelectedTabContent();
		event.getButton()
				.getWindow()
				.open(new DownloadResource(selectedTabContent.getText(), selectedTabContent.getDownloadFilename(),
						getApplication()));
	}

	/**
	 * Returns the currently selected TabContent-Object from
	 * {@linkplain #tabContentMap}. If no Tab is selected null is returned.
	 * 
	 * @return The currently selected TabContent-Object, or null if there is no
	 *         tab selected.
	 */
	private TabContent getSelectedTabContent() {
		Component selectedTab = tabSheet.getSelectedTab();
		return selectedTab == null ? null : (TabContent) selectedTab;
	}

	@Override
	public void modelChanged(UiModel newValue) {
		if (newValue == null) return;
		for (Entry<UiModel.OutputDataKey, String> eachEntry : newValue.getOutputMap().entrySet()) {
			if (tabContentMap.get(eachEntry.getKey()) != null) {
				tabContentMap.get(eachEntry.getKey()).setText(eachEntry.getValue());
			}
		}
	}

	/**
	 * Represents a downloadable Resource. If opened in the Application Window a
	 * download via the browser is initiated. Usually a "save or open"-dialogue
	 * shows up.
	 * 
	 * @author Markus von Rüden <mvr@opennms.com>
	 * 
	 */
	private static class DownloadResource extends StreamResource {

		public DownloadResource(final String downloadString, final String filename, Application application) {
			super(new StreamSource() {
				@Override
				public InputStream getStream() {
					return new ByteArrayInputStream(downloadString.getBytes());
				}
			}, filename, application);
			// for "older" browsers to force a download, otherwise it may not be
			// downloaded
			setMIMEType("application/unknown");
		}

		/**
		 * Set DownloadStream-Parameter "Content-Disposition" to atachment,
		 * therefore the Stream is downloaded and is not parsed as for example
		 * "normal" xml.
		 */
		@Override
		public DownloadStream getStream() {
			DownloadStream ds = super.getStream();
			ds.setParameter("Content-Disposition", "attachment; filename=\"" + getFilename() + "\"");
			return ds;
		}
	}

	private class TabContent extends Panel {

		private final TextArea contentTextArea = new TextArea();

		private final String labelText;

		private final Label description;

		private final OutputDataKey key;

		private TabContent(OutputDataKey key) {
			this.key = key;
			setSizeFull();
			HorizontalSplitPanel contentPanel = new HorizontalSplitPanel();
			contentPanel.setLocked(false);
			contentPanel.setSplitPosition(50, UNITS_PERCENTAGE);
			setContent(contentPanel);
			getContent().setSizeFull();
			contentTextArea.setSizeFull();
			labelText = key.name();
			description = new Label(UIHelper.loadContentFromFile(getClass(), getDescriptionFilename()),
					Label.CONTENT_RAW);
			addComponent(contentTextArea);
			addComponent(description);
		}

		public String getLabelText() {
			return labelText;
		}

		public void setText(String newText) {
			contentTextArea.setValue(newText);
		}

		public String getText() {
			return contentTextArea.getValue() == null ? "" : (String) contentTextArea.getValue();
		}

		private String getDescriptionFilename() {
			return key.getDescriptionFilename();
		}

		private String getDownloadFilename() {
			return key.getDownloadFilename();
		}
	}
}
