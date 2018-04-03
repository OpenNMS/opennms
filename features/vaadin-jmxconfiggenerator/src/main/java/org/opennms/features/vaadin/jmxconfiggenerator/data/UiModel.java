/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.jmxconfiggenerator.data;

import java.util.HashMap;
import java.util.Map;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.opennms.netmgt.config.collectd.Collector;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.config.collectd.Parameter;
import org.opennms.netmgt.config.collectd.Service;
import org.opennms.netmgt.config.collectd.jmx.JmxDatacollectionConfig;

/**
 * This class wraps the <code>JmxDatacollectionConfig</code> and provides some
 * methods to make life a little easier. So in the future we may support
 * multiple data soruces (and therefore multiple JmxDatacollectionConfigs), and
 * so on. Or we find out, that we do not need this class, then we will remove
 * it.
 * 
 * @author Markus von Rüden
 */
public class UiModel {

	public enum OutputDataKey {

		JmxDataCollectionConfig("jmx-datacollection-config.xml"),
		SnmpGraphProperties("snmp-graph.properties"),
		CollectdConfigSnippet("collectd-configuration.xml");

		private final String title;

		OutputDataKey(String title) {
			this.title = title;
		}

		public String getDescriptionFilename() {
			return "/descriptions/" + name() + ".html";
		}

		public String getDownloadFilename() {
			return title;
		}

		public String getTitle() {
			return title;
		}
	}

	private JmxDatacollectionConfig rawModel;
	private ServiceConfig configModel = new ServiceConfig();
	private final Map<OutputDataKey, String> outputMap = new HashMap<>();
	private JmxDatacollectionConfig outputConfig;
	private String snmpGraphProperties;

	/**
	 * Set the real model and get the data we need out of it
	 */
	public UiModel setRawModel(JmxDatacollectionConfig rawModel) {
		if (!isValid(rawModel)) {
			throw new IllegalArgumentException("Model is not valid.");
		}
		this.rawModel = rawModel;
		return this;
	}

	/**
	 * Checks if the given <code>rawModel</code> is not null and does have
	 * Mbeans (count can be 0, but not NULL).
	 * 
	 * @param rawModel
	 * @return true if valid, false otherwise
	 */
	private boolean isValid(JmxDatacollectionConfig rawModel) {
		return !(rawModel.getJmxCollectionList().isEmpty()
				|| rawModel.getJmxCollectionList().get(0) == null
				|| rawModel.getJmxCollectionList().get(0).getMbeans() == null);
	}

	public JmxDatacollectionConfig getRawModel() {
		return rawModel;
	}

	public String getServiceName() {
		return this.configModel.getServiceName();
	}

	public void setOutput(OutputDataKey output, String value) {
		outputMap.put(output, value);
	}

	public Map<OutputDataKey, String> getOutputMap() {
		return outputMap;
	}

	public ServiceConfig getServiceConfig() {
		return configModel;
	}

	public void setJmxDataCollectionAccordingToSelection(JmxDatacollectionConfig outputConfig) {
		this.outputConfig = outputConfig;
	}

	public JmxDatacollectionConfig getOutputConfig() {
		return outputConfig;
	}

	public void updateOutput() {
		setOutput(OutputDataKey.JmxDataCollectionConfig, marshal(getOutputConfig()));
		setOutput(OutputDataKey.SnmpGraphProperties, snmpGraphProperties);
		setOutput(OutputDataKey.CollectdConfigSnippet, marshal(getCollectdConfiguration()));
	}

	public void setSnmpGraphProperties(String generatedSnmpGraphProperties) {
		snmpGraphProperties = generatedSnmpGraphProperties;
	}

	public void setServiceConfig(ServiceConfig serviceConfig) {
		this.configModel = serviceConfig;
	}

	/**
	 * Creates a CollectdConfiguration snippet depending on the data saved here.
	 * 
	 * @return The CollecdConfiguration snippet depending on the data saved in
	 *         this model.
	 */
	 public CollectdConfiguration getCollectdConfiguration() {
		 CollectdConfiguration config = new CollectdConfiguration();

		 // set default package
		 Package defaultPackage = new Package();
		 defaultPackage.setName("default");

		 // set service
		 Service service = new Service();
		 service.setName(getServiceName());
		 service.setInterval(300000L);
		 service.setUserDefined(Boolean.TRUE.toString());
		 service.setStatus("on");

		 // add parameters to service
		 service.addParameter(createParameter("url", getServiceConfig().getConnection()));
		 service.addParameter(createParameter("retry", "2"));
		 service.addParameter(createParameter("timeout", "3000"));
		 service.addParameter(createParameter("rrd-base-name", "java"));
		 service.addParameter(createParameter("ds-name", getServiceName()));
		 service.addParameter(createParameter("friendly-name", getServiceName()));
		 service.addParameter(createParameter("collection", getServiceName()));
		 service.addParameter(createParameter("thresholding-enabled", Boolean.TRUE.toString()));

		 // If we used credentials, we set them here as well
		 if (getServiceConfig().isAuthenticate()) {
			 service.addParameter(createParameter("factory", "PASSWORD-CLEAR"));
			 service.addParameter(createParameter("username", getServiceConfig().getUser()));
			 service.addParameter(createParameter("password", getServiceConfig().getPassword()));
		 }

		 // create Collector
		 Collector collector = new Collector();
		 collector.setService(getServiceName());
		 collector.setClassName("org.opennms.netmgt.collectd.Jsr160Collector");

		 // register service, package and collector to configuration
		 config.addPackage(defaultPackage);
		 config.addCollector(collector);
		 defaultPackage.addService(service);

		 return config;
	 }

	 /**
	 * Creates a Parameter object and sets the key and value.
	 *
	 * @param key
	 * The key for the Parameter object. Should not be null.
	 * @param value
	 * The value for the Parameter object. Should not be null.
	 * @return The Parameter object with key value according to method
	 * arguments.
	 */
	 private static Parameter createParameter(final String key, final String value) {
		 Parameter parameter = new Parameter();
		 parameter.setKey(key);
		 parameter.setValue(value);
		 return parameter;
	 }

	private static String marshal(Object anyObject) {
		return JaxbUtils.marshal(anyObject);
	}
}
