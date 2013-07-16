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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.opennms.features.jmxconfiggenerator.webui.JmxConfigGeneratorApplication;
import org.opennms.features.jmxconfiggenerator.webui.data.ModelChangeListener;
import org.opennms.features.jmxconfiggenerator.webui.data.UiModel;
import org.opennms.features.jmxconfiggenerator.webui.data.UiModel.OutputDataKey;

import com.vaadin.server.DownloadStream;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;

/**
 * Represents the result view. It shows all generated configurations (including
 * some description texts) to the user.
 * 
 * @author Markus von Rüden <mvr@opennms.com>
 */
public class ConfigResultView extends CustomComponent implements ModelChangeListener<UiModel>, Button.ClickListener {

	/**
	 * The name of the downlaodable zip archive.
	 */
	private static String DOWNLOAD_FILE_NAME = "jmx-config-files.zip";

	/**
	 * The TabSheet for the config and description content.
	 */
	private TabSheet tabSheet = new TabSheet();

	/**
	 * Stores the content.
	 */
	private Map<UiModel.OutputDataKey, TabContent> tabContentMap = new HashMap<UiModel.OutputDataKey, TabContent>();

	/**
	 * Panel for previous and download buttons.
	 */
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
			tabSheet.addTab(eachContent, eachContent.getCaption());
		tabSheet.setSelectedTab(0); // select first component!

		buttonPanel.getNext().setVisible(false); // TODO MVR enable button again and allow to download
		buttonPanel.getNext().setCaption("download all");
		buttonPanel.getNext().setIcon(IconProvider.getIcon(IconProvider.BUTTON_SAVE));

		mainLayout.setExpandRatio(tabSheet, 1);
		setCompositionRoot(mainLayout);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getSource().equals(buttonPanel.getPrevious())) app.updateView(UiState.MbeansView);
//		if (event.getSource().equals(buttonPanel.getNext())) downloadConfigFile(event);
	}

	/**
	 * Initiates the download of the String data shown in the currently selected
	 * tab.
	 * 
	 * @param event
	 *            The ClickEvent which indicates the download action.
	 */
	private void downloadConfigFile(ClickEvent event) {
		// key: filename, value: file content
		Map<String, String> zipContentMap = new HashMap<String, String>();
		// create map for the downloadable zip file
		for (OutputDataKey eachKey : tabContentMap.keySet()) {
			// config file
			zipContentMap.put(eachKey.getDownloadFilename(), tabContentMap.get(eachKey).getConfigContent());
			// description file
			zipContentMap.put(flatten(eachKey.getDescriptionFilename()), tabContentMap.get(eachKey)
					.getDescriptionText());
		}
		// initiate download
//                new FileDownloader(new DownloadResource(zipContentMap, DOWNLOAD_FILE_NAME, getUI())).extend(event.getButton());
//		event.getButton().getUI().open(new DownloadResource(zipContentMap, DOWNLOAD_FILE_NAME, getUI()));
	}

	/**
	 * Removes all directory-entries if there are any and simply returns the
	 * filename.
	 * 
	 * @param filename
	 *            The path to the file including the filename (e.g.
	 *            /descriptions/abc.txt)
	 * @return returns only the filename (e.g. abc.txt)
	 */
	private String flatten(String filename) {
		return new File(filename).getName();
	}

	@Override
	public void modelChanged(UiModel newValue) {
		if (newValue == null) return;
		for (Entry<UiModel.OutputDataKey, String> eachEntry : newValue.getOutputMap().entrySet()) {
			if (tabContentMap.get(eachEntry.getKey()) != null) {
				tabContentMap.get(eachEntry.getKey()).setConfigContent(eachEntry.getValue());
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

		/**
		 * 
		 * @param zipContentMap
		 *            key: Filename, value: File content
		 * @param application
		 * @param filename
		 *            The filename for the downloadable zip file.
		 */
		public DownloadResource(final Map<String, String> zipContentMap, final String filename,
				final UI application) {
			super(new StreamSource() {
				@Override
				public InputStream getStream() {

					return new ByteArrayInputStream(getZipByteArray(zipContentMap));
				}
			}, filename);
			// for "older" browsers to force a download,
			// otherwise it may not be downloaded
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

		/**
		 * Creates a byte-Array which represents the content from the
		 * zipContentMap in zipped form. The files are defined by the given Map.
		 * The key of the map defines the name in the zip archive. The value of
		 * the map defines file's content.
		 * 
		 * @param zipContentMap
		 *            The map which contains the filenames and file contents for
		 *            the zip archive to create.
		 * @return a byte-Array which represents the zip file.
		 */
		private static byte[] getZipByteArray(Map<String, String> zipContentMap) {
			try {
				// create output streams ...
				ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
				ZipOutputStream out = new ZipOutputStream(arrayOutputStream);

				// Compress the files
				for (Entry<String, String> eachEntry : zipContentMap.entrySet()) {
					out.putNextEntry(new ZipEntry(eachEntry.getKey()));
					out.write(eachEntry.getValue().getBytes());
					out.closeEntry();
				}
				out.close(); // Complete the ZIP file
				arrayOutputStream.close();
				return arrayOutputStream.toByteArray();
			} catch (IOException e) {
				; // TODO error Handling
			}
			return new byte[0];
		}
	}

	private class TabContent extends HorizontalSplitPanel {

		/**
		 * TextArea for the configuration content (e.g. the SNMP-Graph-Properties)
		 */
		private final TextArea configTextArea = new TextArea();

		/**
		 * Label for the description content (e.g. an explanation of the SNMP-Graph-Properties and what to do with that file).
		 */
		private final Label descriptionLabel;

		private TabContent(OutputDataKey key) {
			setSizeFull();
			setLocked(false);
			setSplitPosition(50, Unit.PERCENTAGE);
			configTextArea.setSizeFull();
			descriptionLabel = new Label(UIHelper.loadContentFromFile(getClass(), key.getDescriptionFilename()),
					ContentMode.HTML);
			addComponent(configTextArea);
			addComponent(descriptionLabel);
			setCaption(key.name());
		}

		public String getDescriptionText() {
			return (String) descriptionLabel.getValue();
		}

		/**
		 * Sets the content of the {@linkplain #configTextArea}.
		 * 
		 * @param newConfigContent The new configuration content.
		 */
		public void setConfigContent(String newConfigContent) {
			configTextArea.setValue(newConfigContent);
		}

		public String getConfigContent() {
			return configTextArea.getValue() == null ? "" : (String) configTextArea.getValue();
		}
	}
}
