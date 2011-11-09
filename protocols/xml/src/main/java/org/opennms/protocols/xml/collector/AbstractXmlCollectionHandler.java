/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
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

package org.opennms.protocols.xml.collector;

import java.util.Date;
import java.util.HashMap;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.config.collector.AttributeGroupType;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.protocols.xml.config.XmlDataCollection;
import org.opennms.protocols.xml.config.XmlGroup;
import org.opennms.protocols.xml.config.XmlObject;
import org.opennms.protocols.xml.config.XmlSource;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

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

    /** The XML attribute type Map. */
    private HashMap<String, XmlCollectionAttributeType> m_attribTypeList = new HashMap<String, XmlCollectionAttributeType>();

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
     * Load attributes.
     *
     * @param collection the collection
     */
    protected void loadAttributes(XmlDataCollection collection) {
        if (m_attribTypeList.isEmpty()) {
            for (XmlSource source : collection.getXmlSources()) {
                for (XmlGroup group : source.getXmlGroups()) {
                    AttributeGroupType attribGroupType = new AttributeGroupType(group.getName(), group.getIfType());
                    for (XmlObject object : group.getXmlObjects()) {
                        XmlCollectionAttributeType attribType = new XmlCollectionAttributeType(object, attribGroupType);
                        m_attribTypeList.put(object.getName(), attribType);
                    }
                }
            }
        }
    }

    /**
     * Gets the collection attribute type.
     *
     * @param attributeTypeName the attribute type name
     * @return the collection attribute type
     */
    protected XmlCollectionAttributeType getCollectionAttributeType(String attributeTypeName) {
        return m_attribTypeList.get(attributeTypeName);
    }

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
            resource = new XmlMultiInstanceCollectionResource(agent, instance, resourceType);
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
     * </ul>
     * 
     * @param unformattedUrl the unformatted URL
     * @param agent the collection agent
     * @param collectionStep the collection step (in seconds)
     * @return the string
     */
    protected String parseUrl(final String unformattedUrl, final CollectionAgent agent, final Integer collectionStep) {
        NodeDao nodeDao = BeanUtils.getBean("daoContext", "nodeDao", NodeDao.class);
        OnmsNode node = nodeDao.get(agent.getNodeId());
        String url = unformattedUrl.replace("{ipaddr}", agent.getHostAddress());
        url = url.replace("{step}", collectionStep.toString());
        url = url.replace("{nodeId}", node.getNodeId());
        url = url.replace("{nodeLabel}", node.getLabel());
        url = url.replace("{foreignId}", node.getForeignId());
        url = url.replace("{foreignSource}", node.getForeignSource());
        return url;
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
