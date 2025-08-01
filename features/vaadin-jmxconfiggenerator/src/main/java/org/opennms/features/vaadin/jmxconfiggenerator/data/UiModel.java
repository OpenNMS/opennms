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
			 service.addParameter(createParameter("factory", "PASSWORD_CLEAR"));
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
