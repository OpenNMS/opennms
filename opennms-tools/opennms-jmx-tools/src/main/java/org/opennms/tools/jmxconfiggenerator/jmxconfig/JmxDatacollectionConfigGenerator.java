/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.tools.jmxconfiggenerator.jmxconfig;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.xml.bind.JAXB;

import org.apache.commons.lang3.StringUtils;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Attrib;
import org.opennms.xmlns.xsd.config.jmx_datacollection.CompAttrib;
import org.opennms.xmlns.xsd.config.jmx_datacollection.CompMember;
import org.opennms.xmlns.xsd.config.jmx_datacollection.JmxCollection;
import org.opennms.xmlns.xsd.config.jmx_datacollection.JmxDatacollectionConfig;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Mbean;
import org.opennms.xmlns.xsd.config.jmx_datacollection.ObjectFactory;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Rrd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Simon Walter <simon.walter@hp-factory.de>
 * @author Markus Neumann <markus@opennms.com>
 */

public class JmxDatacollectionConfigGenerator {

	private static Logger logger = LoggerFactory.getLogger(JmxDatacollectionConfigGenerator.class);
	private static ObjectFactory xmlObjectFactory = new ObjectFactory();
	
	private static ArrayList<String> standardVmBeans = new ArrayList<String>();
	private static ArrayList<String> ignores = new ArrayList<String>();
	private static ArrayList<String> numbers = new ArrayList<String>();
	private static ArrayList<String> justDo = new ArrayList<String>();

	private static ArrayList<String> rras = new ArrayList<String>();

	private static HashMap<String, Integer> aliasMap = new HashMap<String, Integer>();
	private static ArrayList<String> aliasList = new ArrayList<String>();

	private static Rrd rrd = new Rrd();
	private static MBeanServerConnection jmxServerConnection;

	static {

		justDo.add("java.lang");

		ignores.add("");

		standardVmBeans.add("JMImplementation");
		standardVmBeans.add("com.sun.management");
		standardVmBeans.add("java.lang");
		standardVmBeans.add("java.util.logging");

		numbers.add("int");
		numbers.add("long");
		numbers.add("double");
		numbers.add("float");
		numbers.add("class java.lang.Long");

		// rrd setup
		rrd.setStep(300);
		rras.add("RRA:AVERAGE:0.5:1:2016");
		rras.add("RRA:AVERAGE:0.5:12:1488");
		rras.add("RRA:AVERAGE:0.5:288:366");
		rras.add("RRA:MAX:0.5:288:366");
		rras.add("RRA:MIN:0.5:288:366");
		rrd.getRra().addAll(rras);
	}

	public static void generateJmxConfig(String serviceName, String hostName, String port, Boolean runStandardVmBeans, Boolean runCompositeData) throws AttributeNotFoundException, MBeanException {
	logger.debug("Startup values: \n serviceName: " + serviceName + "\n hostName: " + hostName + "\n port:" + port + "\n runStandardVmBeans: " + runStandardVmBeans + "\n runCompositeData: " + runCompositeData);
		JMXServiceURL jmxServiceURL;
		JmxDatacollectionConfig xmlJmxDatacollectionConfig = xmlObjectFactory.createJmxDatacollectionConfig();
		JmxCollection xmlJmxCollection = xmlObjectFactory.createJmxCollection();

		xmlJmxCollection.setName("JSR160-" + serviceName);
		xmlJmxCollection.setRrd(rrd);
		xmlJmxDatacollectionConfig.getJmxCollection().add(xmlJmxCollection);
		xmlJmxCollection.setMbeans(xmlObjectFactory.createMbeans());

		if (!runStandardVmBeans) {
			ignores.addAll(standardVmBeans);
		}
		
		try {
			jmxServiceURL = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + hostName + ":" + port + "/jmxrmi");
			JMXConnector jmxConnector = JMXConnectorFactory.connect(jmxServiceURL);
			jmxConnector.connect();
			jmxServerConnection = jmxConnector.getMBeanServerConnection();
			logger.debug("count: " + jmxServerConnection.getMBeanCount());
			for (String domainName : jmxServerConnection.getDomains()) {

				// just domains that are relevant for the service
				if (!ignores.contains(domainName)) {
					logger.debug("domain: " + domainName);

					// for all mBeans of the actual domain
					for (ObjectInstance jmxObjectInstance : jmxServerConnection.queryMBeans(new ObjectName(domainName + ":*"), null)) {
						Mbean xmlMbean = xmlObjectFactory.createMbean();
						xmlMbean.setObjectname(jmxObjectInstance.getObjectName().toString());
						String typeAndOthers = StringUtils.substringAfterLast(jmxObjectInstance.getObjectName().getCanonicalName(), "=");
						xmlMbean.setName(domainName + "." + typeAndOthers);

						logger.debug("\t" + jmxObjectInstance.getObjectName());
						MBeanInfo jmxMbeanInfo = jmxServerConnection.getMBeanInfo(jmxObjectInstance.getObjectName());
						logger.debug("--- Attributes for " + jmxObjectInstance.getObjectName());

						for (MBeanAttributeInfo jmxBeanAttributeInfo : jmxMbeanInfo.getAttributes()) {

							// process just readable and not writable mbeans
							if (jmxBeanAttributeInfo.isReadable() && !jmxBeanAttributeInfo.isWritable()) {
								logger.info("Add Elements for mBean: '{}'", jmxObjectInstance.getObjectName().toString());
								
								// just process CompositeData if activated
								if ( runCompositeData && "javax.management.openmbean.CompositeData".equals(jmxBeanAttributeInfo.getType())) {
									logger.error("actual mBean: '{}'", jmxObjectInstance.getObjectName());
									CompAttrib compAttrib = createCompAttrib(jmxObjectInstance, jmxBeanAttributeInfo);
									if (compAttrib != null) {
										logger.debug("xmlMbean got CompAttrib");
										xmlMbean.getCompAttrib().add(compAttrib);
									}
								}

								if (numbers.contains(jmxBeanAttributeInfo.getType())) {
									Attrib xmlJmxAttribute = createAttr(jmxBeanAttributeInfo);
									// logger.info("\tAdded attribute: '{}' with alias: '{}'",
									// xmlJmxAttribute.getName(),
									// xmlJmxAttribute.getAlias());
									xmlMbean.getAttrib().add(xmlJmxAttribute);
								}
							}
						}

						if (xmlMbean.getAttrib().size() > 0 || xmlMbean.getCompAttrib().size() > 0) {
							xmlJmxCollection.getMbeans().getMbean().add(xmlMbean);
						} else {
							logger.debug("mbean: " + xmlMbean.getName() + " has no relavant attributes.");
						}
					}
				} else {
					logger.debug("ignored: " + domainName);
				}
			}

			JAXB.marshal(xmlJmxDatacollectionConfig, new File("test.xml"));

		} catch (MalformedURLException e) {
			logger.error("MalformedURLException '{}'", e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("IOException '{}'", e.getMessage());
			e.printStackTrace();
		} catch (MalformedObjectNameException e) {
			logger.error("MalformedObjectNameException '{}'", e.getMessage());
			e.printStackTrace();
		} catch (InstanceNotFoundException e) {
			logger.error("InstanceNotFoundException '{}'", e.getMessage());
			e.printStackTrace();
		} catch (IntrospectionException e) {
			logger.error("IntrospectionException '{}'", e.getMessage());
			e.printStackTrace();
		} catch (ReflectionException e) {
			logger.error("ReflectionException '{}'", e.getMessage());
			e.printStackTrace();
		}

		logger.debug(xmlJmxDatacollectionConfig.toString());

		logger.info("Thx for computing with us!");
	}

	private static CompAttrib createCompAttrib(ObjectInstance jmxObjectInstance, MBeanAttributeInfo jmxMBeanAttributeInfo) {

		// <comp-attrib name="Usage" type="Composite" alias="EdenUsage">
		// <comp-member name="init" type="gauge" alias="EdenUsage.init"/>
		// <comp-member name="max" type="gauge" alias="EdenUsage.max"/>
		// <comp-member name="used" type="gauge" alias="EdenUsage.used"/>
		// <comp-member name="committed" type="gauge" alias="EdenUsg.cmmttd"/>
		// </comp-attrib>

		Boolean contentAdded = false;

		CompAttrib xmlCompAttrib = xmlObjectFactory.createCompAttrib();
		xmlCompAttrib.setName(jmxMBeanAttributeInfo.getName());
		xmlCompAttrib.setType("Composite");
		xmlCompAttrib.setAlias(jmxMBeanAttributeInfo.getName());

		// logger.error("\t Description: '{}'",
		// jmxMBeanAttributeInfo.getDescription());
		// logger.error("\t Descriptor: '{}'",
		// jmxMBeanAttributeInfo.getDescriptor());
		// logger.error("\t\t Descriptor(): '{}'",
		// jmxMBeanAttributeInfo.getDescriptor());
		// logger.error("\t\t Type: '{}'", jmxMBeanAttributeInfo.getType());

		// logger.error("found CompositeData at attrib: '{}'",
		// jmxMBeanAttributeInfo.getName());

		CompositeData compositeData;
		try {
			logger.error("Try to get data");
			compositeData = (CompositeData) jmxServerConnection.getAttribute(jmxObjectInstance.getObjectName(), jmxMBeanAttributeInfo.getName());

			logger.error("compositeData.getCompositeType: '{}'", compositeData.getCompositeType());

			Set<String> keys = compositeData.getCompositeType().keySet();

			for (String key : keys) {
				Object compositeEntry = compositeData.get(key);
				if (numbers.contains(compositeEntry.getClass().toString())) {
					contentAdded = true;
					CompMember xmlCompMember = xmlObjectFactory.createCompMember();
					xmlCompMember.setName(key);
					xmlCompMember.setAlias(key);
					xmlCompMember.setType("gauge");
					xmlCompAttrib.getCompMember().add(xmlCompMember);
				}
			}

		} catch (Exception e) {
			logger.error("killed in action: '{}'", e.getMessage());
			e.printStackTrace();
		}

		if (contentAdded) {
			logger.error("xmlCompAttrib returned by createCompAttrib it's '{}'", xmlCompAttrib);
			return xmlCompAttrib;
		}
		return null;
	}

	private static Attrib createAttr(MBeanAttributeInfo jmxMBeanAttributeInfo) {
		Attrib xmlJmxAttribute = xmlObjectFactory.createAttrib();
		xmlJmxAttribute.setName(jmxMBeanAttributeInfo.getName());
		xmlJmxAttribute.setType("gauge");
		if (!aliasMap.containsKey(jmxMBeanAttributeInfo.getName())) {
			xmlJmxAttribute.setAlias(0 + jmxMBeanAttributeInfo.getName());
			aliasMap.put(jmxMBeanAttributeInfo.getName(), 0);
		} else {
			aliasMap.put(jmxMBeanAttributeInfo.getName(), aliasMap.get(jmxMBeanAttributeInfo.getName()) + 1);
			xmlJmxAttribute.setAlias(aliasMap.get(jmxMBeanAttributeInfo.getName()).toString() + jmxMBeanAttributeInfo.getName());
		}

		//find alias crashes caused by cuting down alias length to 19 chars
		if (aliasList.contains(StringUtils.substring(xmlJmxAttribute.getAlias(), 0, 19))) {
			// logger.error("ALIAS CRASH AT :" + xmlJmxAttribute.getAlias() +
			// "\t as: " + StringUtils.substring(xmlJmxAttribute.getAlias(), 0,
			// 19));
			xmlJmxAttribute.setAlias(xmlJmxAttribute.getAlias() + "_NAME_CRASH_AS_19_CHAR_VALUE");
		} else {
			aliasList.add(StringUtils.substring(xmlJmxAttribute.getAlias(), 0, 19));
			// logger.debug("added alias    : " + xmlJmxAttribute.getAlias() +
			// "\t as: " + StringUtils.substring(xmlJmxAttribute.getAlias(), 0,
			// 19));
		}

		return xmlJmxAttribute;
	}
}
