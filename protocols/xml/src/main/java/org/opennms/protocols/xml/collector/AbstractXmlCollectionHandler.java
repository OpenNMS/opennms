/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.protocols.xml.collector;

import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.PersistAllSelectorStrategy;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.collector.AttributeGroupType;
import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.StorageStrategy;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.protocols.xml.config.XmlGroup;
import org.opennms.protocols.xml.config.XmlObject;
import org.opennms.protocols.xml.config.XmlSource;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The Abstract Class XML Collection Handler.
 * <p>All XmlCollectionHandler should extend this class.</p>
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public abstract class AbstractXmlCollectionHandler implements XmlCollectionHandler {

    /** The Service Name associated with this Collection Handler. */
    private String m_serviceName;

    /** The RRD Repository. */
    private RrdRepository m_rrdRepository;

    /** The XML resource type Map. */
    private HashMap<String, XmlResourceType> m_resourceTypeList = new HashMap<String, XmlResourceType>();

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.XmlCollectionHandler#setServiceName(java.lang.String)
     */
    public void setServiceName(String serviceName) {
        this.m_serviceName = serviceName;
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.XmlCollectionHandler#setRrdRepository(org.opennms.netmgt.model.RrdRepository)
     */
    public void setRrdRepository(RrdRepository m_rrdRepository) {
        this.m_rrdRepository = m_rrdRepository;
    }

    /**
     * Gets the service name.
     *
     * @return the service name
     */
    public String getServiceName() {
        return m_serviceName;
    }

    /**
     * Gets the RRD repository.
     *
     * @return the RRD repository
     */
    public RrdRepository getRrdRepository() {
        return m_rrdRepository;
    }

    /**
     * Fill collection set.
     *
     * @param agent the agent
     * @param collectionSet the collection set
     * @param source the source
     * @param doc the doc
     * @throws XPathExpressionException the x path expression exception
     * @throws ParseException the parse exception
     */
    protected void fillCollectionSet(CollectionAgent agent, XmlCollectionSet collectionSet, XmlSource source, Document doc) throws XPathExpressionException, ParseException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        for (XmlGroup group : source.getXmlGroups()) {
            log().debug("fillCollectionSet: getting resources for XML group " + group.getName() + " using XPATH " + group.getResourceXpath());
            Date timestamp = getTimeStamp(doc, xpath, group);
            NodeList resourceList = (NodeList) xpath.evaluate(group.getResourceXpath(), doc, XPathConstants.NODESET);
            for (int j = 0; j < resourceList.getLength(); j++) {
                Node resource = resourceList.item(j);
                String resourceName = getResourceName(xpath, group, resource);
                log().debug("fillCollectionSet: processing XML resource " + resourceName);
                XmlCollectionResource collectionResource = getCollectionResource(agent, resourceName, group.getResourceType(), timestamp);
                AttributeGroupType attribGroupType = new AttributeGroupType(group.getName(), group.getIfType());
                for (XmlObject object : group.getXmlObjects()) {
                    String value = (String) xpath.evaluate(object.getXpath(), resource, XPathConstants.STRING);
                    XmlCollectionAttributeType attribType = new XmlCollectionAttributeType(object, attribGroupType);
                    collectionResource.setAttributeValue(attribType, value);
                }
                processXmlResource(collectionResource, attribGroupType);
                collectionSet.getCollectionResources().add(collectionResource);
            }
        }
    }

    /**
     * Gets the resource name.
     *
     * @param xpath the Xpath
     * @param group the group
     * @param resource the resource
     * @return the resource name
     * @throws XPathExpressionException the x path expression exception
     */
    private String getResourceName(XPath xpath, XmlGroup group, Node resource) throws XPathExpressionException {
        // Processing multiple-key resource name.
        if (group.hasMultipleResourceKey()) {
            List<String> keys = new ArrayList<String>();
            for (String key : group.getXmlResourceKey().getKeyXpathList()) {
                log().debug("getResourceName: getting key for resource's name using " + key);
                Node keyNode = (Node) xpath.evaluate(key, resource, XPathConstants.NODE);
                keys.add(keyNode.getNodeValue() == null ? keyNode.getTextContent() : keyNode.getNodeValue());
            }
            return StringUtils.join(keys, "_");
        }
        // If key-xpath doesn't exist or not found, a node resource will be assumed.
        if (group.getKeyXpath() == null) {
            return "node";
        }
        // Processing single-key resource name.
        log().debug("getResourceName: getting key for resource's name using " + group.getKeyXpath());
        Node keyNode = (Node) xpath.evaluate(group.getKeyXpath(), resource, XPathConstants.NODE);
        return keyNode.getNodeValue() == null ? keyNode.getTextContent() : keyNode.getNodeValue();
    }

    /**
     * Process XML resource.
     *
     * @param collectionResource the collection resource
     * @param attribGroupType the attribute group type
     */
    protected abstract void processXmlResource(XmlCollectionResource collectionResource, AttributeGroupType attribGroupType);

    /**
     * Gets the collection resource.
     *
     * @param agent the collection agent
     * @param instance the resource instance
     * @param resourceType the resource type
     * @param timestamp the timestamp
     * @return the collection resource
     */
    protected XmlCollectionResource getCollectionResource(CollectionAgent agent, String instance, String resourceType, Date timestamp) {
        XmlCollectionResource resource = null;
        if (resourceType.toLowerCase().equals("node")) {
            resource = new XmlSingleInstanceCollectionResource(agent);
        } else {
            XmlResourceType type = getXmlResourceType(agent, resourceType);
            resource = new XmlMultiInstanceCollectionResource(agent, instance, type);
        }
        if (timestamp != null) {
            log().debug("getCollectionResource: the date that will be used when updating the RRDs is " + timestamp);
            resource.setTimeKeeper(new ConstantTimeKeeper(timestamp));
        }
        return resource;
    }

    /**
     * Gets the time stamp.
     * 
     * @param doc the doc
     * @param xpath the xpath
     * @param group the group
     * @return the time stamp
     * @throws XPathExpressionException the x path expression exception
     */
    protected Date getTimeStamp(Document doc, XPath xpath, XmlGroup group) throws XPathExpressionException {
        if (group.getTimestampXpath() == null) {
            return null;
        }
        String pattern = group.getTimestampFormat() == null ? "yyyy-MM-dd HH:mm:ss" : group.getTimestampFormat();
        log().debug("getTimeStamp: retrieving custom timestamp to be used when updating RRDs using XPATH " + group.getTimestampXpath() + " and pattern " + pattern);
        Node tsNode = (Node) xpath.evaluate(group.getTimestampXpath(), doc, XPathConstants.NODE);
        if (tsNode == null) {
            log().warn("getTimeStamp: can't find the custom timestamp using XPATH " +  group.getTimestampXpath());
            return null;
        }
        Date date = null;
        String value = tsNode.getNodeValue() == null ? tsNode.getTextContent() : tsNode.getNodeValue();
        try {
            DateTimeFormatter dtf = DateTimeFormat.forPattern(pattern);
            DateTime dateTime = dtf.parseDateTime(value);
            date = dateTime.toDate();
        } catch (Exception e) {
            log().warn("getTimeStamp: can't convert custom timetime " + value + " using pattern " + pattern);
        }
        return date;
    }

    /**
     * Parses the URL.
     * 
     * <p>Valid placeholders are:</p>
     * <ul>
     * <li><b>ipaddr</b>, The Node IP Address</li>
     * <li><b>step</b>, The Collection Step in seconds</li>
     * <li><b>nodeId</b>, The Node ID</li>
     * <li><b>nodeLabel</b>, The Node Label</li>
     * <li><b>foreignId</b>, The Node Foreign ID</li>
     * <li><b>foreignSource</b>, The Node Foreign Source</li>
     * <li>Any asset property defined on the node.</li>
     * </ul>
     *
     * @param unformattedUrl the unformatted URL
     * @param agent the collection agent
     * @param collectionStep the collection step (in seconds)
     * @return the string
     * 
     * @throws IllegalArgumentException the illegal argument exception
     */
    protected String parseUrl(final String unformattedUrl, final CollectionAgent agent, final Integer collectionStep) throws IllegalArgumentException {
        NodeDao nodeDao = BeanUtils.getBean("daoContext", "nodeDao", NodeDao.class);
        OnmsNode node = nodeDao.get(agent.getNodeId());
        String url = unformattedUrl.replace("{ipaddr}", agent.getHostAddress());
        url = url.replace("{step}", collectionStep.toString());
        url = url.replace("{nodeId}", node.getNodeId());
        if (node.getLabel() != null)
            url = url.replace("{nodeLabel}", node.getLabel());
        if (node.getForeignId() != null)
            url = url.replace("{foreignId}", node.getForeignId());
        if (node.getForeignSource() != null)
            url = url.replace("{foreignSource}", node.getForeignSource());
        if (node.getAssetRecord() != null) {
            BeanWrapper wrapper = new BeanWrapperImpl(node.getAssetRecord());
            for (PropertyDescriptor p : wrapper.getPropertyDescriptors()) {
                Object obj = wrapper.getPropertyValue(p.getName());
                if (obj != null)
                    url = url.replace('{' + p.getName() + '}', obj.toString());
            }
        }
        if (url.matches(".*\\{.+\\}.*"))
            throw new IllegalArgumentException("The URL " + url + " contains unknown placeholders.");
        return url;
    }

    /**
     * Gets the XML document.
     *
     * @param urlString the URL string
     * @return the XML document
     */
    protected Document getXmlDocument(String urlString) {
        InputStream is = null;
        try {
            URL url = UrlFactory.getUrl(urlString);
            URLConnection c = url.openConnection();
            is = c.getInputStream();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringComments(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);
            UrlFactory.disconnect(c);
            return doc;
        } catch (Exception e) {
            throw new XmlCollectorException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * Gets the XML resource type.
     *
     * @param agent the collection agent
     * @param resourceType the resource type
     * @return the XML resource type
     */
    protected XmlResourceType getXmlResourceType(CollectionAgent agent, String resourceType) {
        if (!m_resourceTypeList.containsKey(resourceType)) {
            ResourceType rt = DataCollectionConfigFactory.getInstance().getConfiguredResourceTypes().get(resourceType);
            if (rt == null) {
                log().debug("getXmlResourceType: using default XML resource type strategy.");
                rt = new ResourceType();
                rt.setName(resourceType);
                rt.setStorageStrategy(new StorageStrategy());
                rt.getStorageStrategy().setClazz(XmlStorageStrategy.class.getName());
                rt.setPersistenceSelectorStrategy(new PersistenceSelectorStrategy());
                rt.getPersistenceSelectorStrategy().setClazz(PersistAllSelectorStrategy.class.getName());
            }
            XmlResourceType type = new XmlResourceType(agent, rt);
            m_resourceTypeList.put(resourceType, type);
        }
        return m_resourceTypeList.get(resourceType);
    }

    /**
     * Log.
     *
     * @return the thread category
     */
    protected ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

}
