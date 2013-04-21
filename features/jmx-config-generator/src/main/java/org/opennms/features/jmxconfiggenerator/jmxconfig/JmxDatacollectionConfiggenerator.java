/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.features.jmxconfiggenerator.jmxconfig;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.management.*;
import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.xml.bind.JAXB;
import org.apache.commons.lang3.StringUtils;
import org.opennms.features.namecutter.NameCutter;
import org.opennms.xmlns.xsd.config.jmx_datacollection.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Simon Walter <simon.walter@hp-factory.de>
 * @author Markus Neumann <markus@opennms.com>
 */
public class JmxDatacollectionConfiggenerator {

    private static Logger logger = LoggerFactory.getLogger(JmxDatacollectionConfiggenerator.class);
    private static ObjectFactory xmlObjectFactory = new ObjectFactory();
    private static ArrayList<String> standardVmBeans = new ArrayList<String>();
    private static ArrayList<String> ignores = new ArrayList<String>();
    private static ArrayList<String> numbers = new ArrayList<String>();
    private static ArrayList<String> rras = new ArrayList<String>();
    private static HashMap<String, Integer> aliasMap = new HashMap<String, Integer>();
    private static ArrayList<String> aliasList = new ArrayList<String>();
    private static Rrd rrd = new Rrd();
    private static NameCutter nameCutter = new NameCutter();

    static {
        // Domanis directly from JVMs
        standardVmBeans.add("JMImplementation");
        standardVmBeans.add("com.sun.management");
        standardVmBeans.add("java.lang");
        standardVmBeans.add("java.nio");
        standardVmBeans.add("java.util.logging");

        // valid numbertyps
        numbers.add("int");
        numbers.add("long");
        numbers.add("double");
        numbers.add("float");
        numbers.add("java.lang.Long");
        numbers.add("java.lang.Integer");

        // rrd setup
        rrd.setStep(300);
        rras.add("RRA:AVERAGE:0.5:1:2016");
        rras.add("RRA:AVERAGE:0.5:12:1488");
        rras.add("RRA:AVERAGE:0.5:288:366");
        rras.add("RRA:MAX:0.5:288:366");
        rras.add("RRA:MIN:0.5:288:366");
        rrd.getRra().addAll(rras);
    }

    public JmxDatacollectionConfig generateJmxConfigModel(MBeanServerConnection mBeanServerConnection, String serviceName, Boolean runStandardVmBeans, Boolean runWritableMBeans, Map<String, String> dictionary) {

        logger.debug("Startup values: \n serviceName: " + serviceName + "\n runStandardVmBeans: " + runStandardVmBeans + "\n runWritableMBeans: " + runWritableMBeans + "\n dictionary" + dictionary);
        nameCutter.setDictionary(dictionary);
        JmxDatacollectionConfig xmlJmxDatacollectionConfig = xmlObjectFactory.createJmxDatacollectionConfig();
        JmxCollection xmlJmxCollection = xmlObjectFactory.createJmxCollection();

        xmlJmxCollection.setName("JSR160-" + serviceName);
        xmlJmxCollection.setRrd(rrd);
        xmlJmxDatacollectionConfig.getJmxCollection().add(xmlJmxCollection);
        xmlJmxCollection.setMbeans(xmlObjectFactory.createMbeans());

        if (runStandardVmBeans) {
            ignores.clear();
        } else {
            ignores.addAll(standardVmBeans);
        }

        try {
            for (String domainName : mBeanServerConnection.getDomains()) {

                // just domains that are relevant for the service
                if (!ignores.contains(domainName)) {
                    logger.debug("domain: " + domainName);

                    // for all mBeans of the actual domain
                    for (ObjectInstance jmxObjectInstance : mBeanServerConnection.queryMBeans(new ObjectName(domainName + ":*"), null)) {
                        Mbean xmlMbean = xmlObjectFactory.createMbean();
                        xmlMbean.setObjectname(jmxObjectInstance.getObjectName().toString());
                        String typeAndOthers = StringUtils.substringAfterLast(jmxObjectInstance.getObjectName().getCanonicalName(), "=");
                        xmlMbean.setName(domainName + "." + typeAndOthers);

                        logger.debug("\t" + jmxObjectInstance.getObjectName());

                        MBeanInfo jmxMbeanInfo;
                        try {
                            jmxMbeanInfo = mBeanServerConnection.getMBeanInfo(jmxObjectInstance.getObjectName());
                        } catch (InstanceNotFoundException e) {
                            logger.error("InstanceNotFoundException skipping MBean '{}' message: '{}'", jmxObjectInstance.getObjectName(), e.getMessage());
                            continue;
                        } catch (IntrospectionException e) {
                            logger.error("IntrospectionException skipping MBean '{}' message: '{}'", jmxObjectInstance.getObjectName(), e.getMessage());
                            continue;
                        } catch (ReflectionException e) {
                            logger.error("ReflectionException skipping MBean '{}' message: '{}'", jmxObjectInstance.getObjectName(), e.getMessage());
                            continue;
                        } catch (Throwable e) {
                            logger.error("problem during remote call to get MBeanInfo for '{}' skipping this MBean. Message '{}'", jmxObjectInstance.getObjectName(), e.getMessage());
                            continue;
                        }

                        logger.debug("--- Attributes for " + jmxObjectInstance.getObjectName());

                        for (MBeanAttributeInfo jmxBeanAttributeInfo : jmxMbeanInfo.getAttributes()) {

                            // process just readable mbeans
                            if (jmxBeanAttributeInfo.isReadable()) {
                                // precess writable mbeans if run writable mbeans is set
                                if (!jmxBeanAttributeInfo.isWritable() || runWritableMBeans) {

                                    logger.debug("Check mBean: '{}', attribute: '{}'", jmxObjectInstance.getObjectName().toString(), jmxBeanAttributeInfo.getName());
                                    logger.debug("isWritable: '{}', type: '{}'", jmxBeanAttributeInfo.isWritable(), jmxBeanAttributeInfo.getType());

                                    // check for CompositeData
                                    if ("javax.management.openmbean.CompositeData".equals(jmxBeanAttributeInfo.getType())) {
                                        logger.error("actual mBean: '{}'", jmxObjectInstance.getObjectName());
                                        CompAttrib compAttrib = createCompAttrib(mBeanServerConnection, jmxObjectInstance, jmxBeanAttributeInfo);
                                        if (compAttrib != null) {
                                            logger.debug("xmlMbean got CompAttrib");
                                            xmlMbean.getCompAttrib().add(compAttrib);
                                        }
                                    }

                                    if (numbers.contains(jmxBeanAttributeInfo.getType())) {
                                        Attrib xmlJmxAttribute = createAttr(jmxBeanAttributeInfo);
                                        logger.debug("Added MBean: '{}' Added attribute: '{}'", xmlMbean.getObjectname(), xmlJmxAttribute.getName() + " as " + xmlJmxAttribute.getAlias());
                                        xmlMbean.getAttrib().add(xmlJmxAttribute);
                                    }
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

        } catch (MalformedObjectNameException e) {
            logger.error("MalformedObjectNameException '{}'", e.getMessage());
        } catch (IOException e) {
            logger.error("IOException '{}'", e.getMessage());
        }

        return xmlJmxDatacollectionConfig;
    }

    public MBeanServerConnection createMBeanServerConnection(String hostName, String port, String username, String password, Boolean ssl, Boolean jmxmp) throws MalformedURLException, IOException {
        JMXConnector jmxConnector = getJmxConnector(hostName, port, username, password, ssl, jmxmp);
        MBeanServerConnection jmxServerConnection = jmxConnector.getMBeanServerConnection();
        logger.debug("jmxServerConnection: '{}'", jmxServerConnection);
        logger.debug("count: " + jmxServerConnection.getMBeanCount());
        return jmxServerConnection;
    }

    //TODO
    public JMXConnector getJmxConnector(String hostName, String port, String username, String password, Boolean ssl, Boolean jmxmp) throws MalformedURLException, IOException {
        JMXServiceURL jmxServiceURL = getJmxServiceURL(jmxmp, hostName, port);
        JMXConnector jmxConnector = getJmxConnector(username, password, jmxServiceURL);
        return jmxConnector;
    }

    /**
     * This method gets the JmxConnector to connect with the given jmxServiceURL.
     *
     * @param username may be null
     * @param password may be null
     * @param jmxServiceURL should not be null!
     * @return a jmxConnector
     * @throws IOException if the connection to the given jmxServiceURL fails (e.g. authentication failure or not reachable)
     */
    private JMXConnector getJmxConnector(String username, String password, JMXServiceURL jmxServiceURL) throws IOException {
        JMXConnector jmxConnector;
        if (username != null && password != null) {
            jmxConnector = JMXConnectorFactory.newJMXConnector(jmxServiceURL, null);
            HashMap<String, String[]> env = new HashMap<String, String[]>();
            String[] credentials = new String[]{username, password};
            env.put("jmx.remote.credentials", credentials);
            jmxConnector.connect(env);
        } else {
            jmxConnector = JMXConnectorFactory.connect(jmxServiceURL);
            jmxConnector.connect();
        }
        return jmxConnector;
    }

    /**
     * determines the jmxServiceUrl depending on jmxmp.
     *
     * @param jmxmp
     * @param hostName
     * @param port
     * @return
     * @throws MalformedURLException
     */
    public JMXServiceURL getJmxServiceURL(Boolean jmxmp, String hostName, String port) throws MalformedURLException {
        if (jmxmp) {
            return new JMXServiceURL("service:jmx:jmxmp://" + hostName + ":" + port);
        }
        return new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + hostName + ":" + port + "/jmxrmi");
    }

    public void writeJmxConfigFile(JmxDatacollectionConfig jmxDatacollectionConfigModel, String outFile) {
        JAXB.marshal(jmxDatacollectionConfigModel, new File(outFile));
    }

    private CompAttrib createCompAttrib(MBeanServerConnection jmxServerConnection, ObjectInstance jmxObjectInstance, MBeanAttributeInfo jmxMBeanAttributeInfo) {
        Boolean contentAdded = false;

        CompAttrib xmlCompAttrib = xmlObjectFactory.createCompAttrib();
        xmlCompAttrib.setName(jmxMBeanAttributeInfo.getName());
        xmlCompAttrib.setType("Composite");
        xmlCompAttrib.setAlias(jmxMBeanAttributeInfo.getName());

        CompositeData compositeData;
        try {
            logger.debug("Try to get composite data");
            compositeData = (CompositeData) jmxServerConnection.getAttribute(jmxObjectInstance.getObjectName(), jmxMBeanAttributeInfo.getName());
            if (compositeData == null) {
                logger.warn("compositeData is null. jmxObjectInstance.getObjectName: '{}', jmxMBeanAttributeInfo.getName: '{}'");
            }
            if (compositeData != null) {
                logger.debug("compositeData.getCompositeType: '{}'", compositeData.getCompositeType());
                Set<String> keys = compositeData.getCompositeType().keySet();
                for (String key : keys) {
                    Object compositeEntry = compositeData.get(key);
                    if (numbers.contains(compositeEntry.getClass().getName())) {
                        contentAdded = true;
                        CompMember xmlCompMember = xmlObjectFactory.createCompMember();
                        xmlCompMember.setName(key);

                        logger.debug("composite member pure alias: '{}'", jmxMBeanAttributeInfo.getName() + StringUtils.capitalize(key));
                        String alias = nameCutter.trimByDictionary(jmxMBeanAttributeInfo.getName() + StringUtils.capitalize(key));
                        alias = createAndRegisterUniqueAlias(alias);
                        xmlCompMember.setAlias(alias);
                        logger.debug("composite member trimmed alias: '{}'", alias);

                        xmlCompMember.setType("gauge");
                        xmlCompAttrib.getCompMember().add(xmlCompMember);

                    } else {
                        logger.debug("composite member key '{}' object's class '{}' was not a number.", key, compositeEntry.getClass().getName());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("killed in action: '{}'", e.getMessage());
        }

        if (contentAdded) {
            logger.debug("xmlCompAttrib returned by createCompAttrib it's '{}'", xmlCompAttrib);
            return xmlCompAttrib;
        }
        return null;
    }

    private Attrib createAttr(MBeanAttributeInfo jmxMBeanAttributeInfo) {
        Attrib xmlJmxAttribute = xmlObjectFactory.createAttrib();

        xmlJmxAttribute.setType("gauge");
        xmlJmxAttribute.setName(jmxMBeanAttributeInfo.getName());
        String alias = nameCutter.trimByDictionary(jmxMBeanAttributeInfo.getName());
        alias = createAndRegisterUniqueAlias(alias);
        xmlJmxAttribute.setAlias(alias);

        return xmlJmxAttribute;
    }

    private String createAndRegisterUniqueAlias(String originalAlias) {
        String uniqueAlias = originalAlias;
        if (!aliasMap.containsKey(originalAlias)) {
            aliasMap.put(originalAlias, 0);
            uniqueAlias = 0 + uniqueAlias;
        } else {
            aliasMap.put(originalAlias, aliasMap.get(originalAlias) + 1);
            uniqueAlias = aliasMap.get(originalAlias).toString() + originalAlias;
        }
        //find alias crashes caused by cuting down alias length to 19 chars
        if (aliasList.contains(nameCutter.trimByCamelCase(uniqueAlias, 19))) {
            logger.error("ALIAS CRASH AT :" + uniqueAlias + "\t as: " + nameCutter.trimByCamelCase(uniqueAlias, 19));
            uniqueAlias = uniqueAlias + "_NAME_CRASH_AS_19_CHAR_VALUE";
        } else {
            uniqueAlias = nameCutter.trimByCamelCase(uniqueAlias, 19);
            aliasList.add(uniqueAlias);
        }
        return uniqueAlias;
    }
}
