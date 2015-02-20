/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.protocols.xml.collector;

import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.collectd.PersistAllSelectorStrategy;
import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionException;
import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.StorageStrategy;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.protocols.xml.config.Content;
import org.opennms.protocols.xml.config.Header;
import org.opennms.protocols.xml.config.Parameter;
import org.opennms.protocols.xml.config.Request;
import org.opennms.protocols.xml.config.XmlDataCollection;
import org.opennms.protocols.xml.config.XmlGroup;
import org.opennms.protocols.xml.config.XmlObject;
import org.opennms.protocols.xml.config.XmlSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractXmlCollectionHandler.class);

    /** The Service Name associated with this Collection Handler. */
    private String m_serviceName;

    /** OpenNMS Node DAO. */
    private NodeDao m_nodeDao;

    /** The RRD Repository. */
    private RrdRepository m_rrdRepository;

    /** The XML resource type Map. */
    private Map<String, XmlResourceType> m_resourceTypeList = new HashMap<String, XmlResourceType>();

    /** The Node Level Resource. */
    private XmlSingleInstanceCollectionResource m_nodeResource;

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.XmlCollectionHandler#setServiceName(java.lang.String)
     */
    @Override
    public void setServiceName(String serviceName) {
        this.m_serviceName = serviceName;
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.XmlCollectionHandler#setRrdRepository(org.opennms.netmgt.model.RrdRepository)
     */
    @Override
    public void setRrdRepository(RrdRepository rrdRepository) {
        this.m_rrdRepository = rrdRepository;
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
     * Gets the Node DAO.
     *
     * @return the Node DAO
     */
    public NodeDao getNodeDao() {
        if (m_nodeDao == null) {
            m_nodeDao = BeanUtils.getBean("daoContext", "nodeDao", NodeDao.class);
        }
        return m_nodeDao;
    }

    /**
     * Sets the Node DAO.
     *
     * @param nodeDao the new Node DAO
     */
    public void setNodeDao(NodeDao nodeDao) {
        this.m_nodeDao = nodeDao;
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.XmlCollectionHandler#collect(org.opennms.netmgt.collectd.CollectionAgent, org.opennms.protocols.xml.config.XmlDataCollection, java.util.Map)
     */
    @Override
    public XmlCollectionSet collect(CollectionAgent agent, XmlDataCollection collection, Map<String, Object> parameters) throws CollectionException {
        XmlCollectionSet collectionSet = new XmlCollectionSet();
        collectionSet.setCollectionTimestamp(new Date());
        collectionSet.setStatus(ServiceCollector.COLLECTION_UNKNOWN);
        DateTime startTime = new DateTime();
        try {
            LOG.debug("collect: looping sources for collection {}", collection.getName());
            for (XmlSource source : collection.getXmlSources()) {
                LOG.debug("collect: starting source url '{}' collection", source.getUrl());
                String urlStr = parseUrl(source.getUrl(), agent, collection.getXmlRrd().getStep());
                LOG.debug("collect: parsed url for source url '{}'", source.getUrl());
                Request request = parseRequest(source.getRequest(), agent, collection.getXmlRrd().getStep());
                LOG.debug("collect: parsed request for source url '{}'", source.getUrl());
                fillCollectionSet(urlStr, request, agent, collectionSet, source);
                LOG.debug("collect: finished source url '{}' collection", source.getUrl());
            }
            collectionSet.setStatus(ServiceCollector.COLLECTION_SUCCEEDED);
            return collectionSet;
        } catch (Exception e) {
            collectionSet.setStatus(ServiceCollector.COLLECTION_FAILED);
            throw new CollectionException(e.getMessage(), e);
        } finally {
            String status = collectionSet.getStatus() == ServiceCollector.COLLECTION_SUCCEEDED ? "finished" : "failed";
            DateTime endTime = new DateTime();
            LOG.debug("collect: {} collection {}: duration: {} ms", status, collection.getName(), endTime.getMillis()-startTime.getMillis());
        }
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
        NamespaceContext nc = new DocumentNamespaceResolver(doc);
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(nc);
        for (XmlGroup group : source.getXmlGroups()) {
            LOG.debug("fillCollectionSet: getting resources for XML group {} using XPATH {}", group.getName(), group.getResourceXpath());
            Date timestamp = getTimeStamp(doc, xpath, group);
            NodeList resourceList = (NodeList) xpath.evaluate(group.getResourceXpath(), doc, XPathConstants.NODESET);
            for (int j = 0; j < resourceList.getLength(); j++) {
                Node resource = resourceList.item(j);
                String resourceName = getResourceName(xpath, group, resource);
                LOG.debug("fillCollectionSet: processing XML resource {}", resourceName);
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
                LOG.debug("getResourceName: getting key for resource's name using {}", key);
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
        LOG.debug("getResourceName: getting key for resource's name using {}", group.getKeyXpath());
        Node keyNode = (Node) xpath.evaluate(group.getKeyXpath(), resource, XPathConstants.NODE);
        return keyNode.getNodeValue() == null ? keyNode.getTextContent() : keyNode.getNodeValue();
    }

    /**
     * Fill collection set.
     *
     * @param urlString the URL string
     * @param request the request
     * @param agent the collection agent
     * @param collectionSet the collection set
     * @param source the XML source
     * @throws Exception the exception
     */
    protected abstract void fillCollectionSet(String urlString, Request request, CollectionAgent agent, XmlCollectionSet collectionSet, XmlSource source) throws Exception;

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
        if (resourceType.equalsIgnoreCase("node")) {
            if (m_nodeResource == null) {
                m_nodeResource = new XmlSingleInstanceCollectionResource(agent);
            }
            resource = m_nodeResource;
        } else {
            XmlResourceType type = getXmlResourceType(agent, resourceType);
            resource = new XmlMultiInstanceCollectionResource(agent, instance, type);
        }
        if (timestamp != null) {
            LOG.debug("getCollectionResource: the date that will be used when updating the RRDs is {}", timestamp);
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
        LOG.debug("getTimeStamp: retrieving custom timestamp to be used when updating RRDs using XPATH {} and pattern {}", group.getTimestampXpath(), pattern);
        Node tsNode = (Node) xpath.evaluate(group.getTimestampXpath(), doc, XPathConstants.NODE);
        if (tsNode == null) {
            LOG.warn("getTimeStamp: can't find the custom timestamp using XPATH {}",  group.getTimestampXpath());
            return null;
        }
        Date date = null;
        String value = tsNode.getNodeValue() == null ? tsNode.getTextContent() : tsNode.getNodeValue();
        LOG.debug("getTimeStamp: time stamp value is {}", value);
        try {
            DateTimeFormatter dtf = DateTimeFormat.forPattern(pattern);
            DateTime dateTime = dtf.parseDateTime(value);
            date = dateTime.toDate();
        } catch (Exception e) {
            LOG.warn("getTimeStamp: can't convert custom timetime {} using pattern {}", value,  pattern);
        }
        return date;
    }

    /**
     * Parses the URL.
     * 
     * <p>Additional placeholders:</p>
     * <ul>
     * <li><b>step</b>, The Collection Step in seconds</li>
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
        final OnmsNode node = getNodeDao().get(agent.getNodeId());
        return parseString("URL", unformattedUrl, node, agent.getHostAddress(), collectionStep);
    }

    /**
     * Parses the request.
     *
     * @param unformattedRequest the unformatted request
     * @param agent the agent
     * @param collectionStep the collection step
     * @return the request
     * @throws IllegalArgumentException the illegal argument exception
     */
    protected Request parseRequest(final Request unformattedRequest, final CollectionAgent agent, final Integer collectionStep) throws IllegalArgumentException {
        if (unformattedRequest == null)
            return null;
        final OnmsNode node = getNodeDao().get(agent.getNodeId());
        final Request request = new Request();
        for (Header header : unformattedRequest.getHeaders()) {
            request.addHeader(header.getName(), parseString(header.getName(), header.getValue(), node, agent.getHostAddress(), collectionStep));
        }
        for (Parameter param : unformattedRequest.getParameters()) {
            request.addParameter(param.getName(), parseString(param.getName(), param.getValue(), node, agent.getHostAddress(), collectionStep));
        }
        final Content cnt = unformattedRequest.getContent();
        if (cnt != null)
            request.setContent(new Content(cnt.getType(), parseString("Content", cnt.getData(), node, agent.getHostAddress(), collectionStep)));
        return request;
    }

    /**
     * Parses the string.
     * 
     * <p>Valid placeholders are:</p>
     * <ul>
     * <li><b>ipAddr|ipAddress</b>, The Node IP Address</li>
     * <li><b>step</b>, The Collection Step in seconds</li>
     * <li><b>nodeId</b>, The Node ID</li>
     * <li><b>nodeLabel</b>, The Node Label</li>
     * <li><b>foreignId</b>, The Node Foreign ID</li>
     * <li><b>foreignSource</b>, The Node Foreign Source</li>
     * <li>Any asset property defined on the node.</li>
     * </ul>
     *
     * @param reference the reference
     * @param unformattedString the unformatted string
     * @param node the node
     * @param ipAddress the IP address
     * @param collectionStep the collection step
     * @return the string
     * @throws IllegalArgumentException the illegal argument exception
     */
    protected String parseString(final String reference, final String unformattedString, final OnmsNode node, final String ipAddress, final Integer collectionStep) throws IllegalArgumentException {
        if (unformattedString == null || node == null)
            return null;
        String formattedString = unformattedString.replaceAll("[{](?i)(ipAddr|ipAddress)[}]", ipAddress);
        formattedString = formattedString.replaceAll("[{](?i)step[}]", collectionStep.toString());
        formattedString = formattedString.replaceAll("[{](?i)nodeId[}]", node.getNodeId());
        if (node.getLabel() != null)
            formattedString = formattedString.replaceAll("[{](?i)nodeLabel[}]", node.getLabel());
        if (node.getForeignId() != null)
            formattedString = formattedString.replaceAll("[{](?i)foreignId[}]", node.getForeignId());
        if (node.getForeignSource() != null)
            formattedString = formattedString.replaceAll("[{](?i)foreignSource[}]", node.getForeignSource());
        if (node.getAssetRecord() != null) {
            BeanWrapper wrapper = new BeanWrapperImpl(node.getAssetRecord());
            for (PropertyDescriptor p : wrapper.getPropertyDescriptors()) {
                Object obj = wrapper.getPropertyValue(p.getName());
                if (obj != null){
                    String objStr = obj.toString();
                    try {
                        //NMS-7381 - if pulling from asset info you'd expect to not have to encode reserved words yourself.  
                        objStr = URLEncoder.encode(obj.toString(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    formattedString = formattedString.replaceAll("[{](?i)" + p.getName() + "[}]", objStr);
                }
            }
        }
        if (formattedString.matches(".*[{].+[}].*"))
            throw new IllegalArgumentException("The " + reference + " " + formattedString + " contains unknown placeholders.");
        return formattedString;
    }

    /**
     * Gets the XML document.
     *
     * @param urlString the URL string
     * @param request the request
     * @return the XML document
     */
    protected Document getXmlDocument(String urlString, Request request) {
        InputStream is = null;
        URLConnection c = null;
        try {
            URL url = UrlFactory.getUrl(urlString, request);
            c = url.openConnection();
            is = c.getInputStream();
            final Document doc = getXmlDocument(is, request);
            return doc;
        } catch (Exception e) {
            throw new XmlCollectorException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(is);
            UrlFactory.disconnect(c);
        }
    }

    /**
     * Gets the XML document.
     *
     * @param is the input stream
     * @param request the request
     * @return the XML document
     */
    protected Document getXmlDocument(InputStream is, Request request) {
        try {
            is = preProcessHtml(request, is);
            is = applyXsltTransformation(request, is);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringComments(true);
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            StringWriter writer = new StringWriter();
            IOUtils.copy(is, writer, "UTF-8");
            String contents = writer.toString();
            Document doc = builder.parse(IOUtils.toInputStream(contents, "UTF-8"));
            // Ugly hack to deal with DOM & XPath 1.0's battle royale 
            // over handling namespaces without a prefix. 
            if(doc.getNamespaceURI() != null && doc.getPrefix() == null){
                factory.setNamespaceAware(false);
                builder = factory.newDocumentBuilder();
                doc = builder.parse(IOUtils.toInputStream(contents, "UTF-8"));
            }
            return doc;
        } catch (Exception e) {
            throw new XmlCollectorException(e.getMessage(), e);
        }
    }

    /**
     * Apply XSLT transformation.
     *
     * @param request the request
     * @param is the input stream
     * @return the input stream
     * @throws Exception the exception
     */
    protected InputStream applyXsltTransformation(Request request, InputStream is) throws Exception {
        if (request == null || is == null)
            return is;
        String xsltFilename = request.getParameter("xslt-source-file");
        if (xsltFilename == null)
            return is;
        File xsltFile = new File(xsltFilename);
        if (!xsltFile.exists())
            return is;
        TransformerFactory factory = TransformerFactory.newInstance();
        Source xslt = new StreamSource(xsltFile);
        Transformer transformer = factory.newTransformer(xslt);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            transformer.transform(new StreamSource(is), new StreamResult(baos));
            return new ByteArrayInputStream(baos.toByteArray());
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * Pre-process HTML.
     *
     * @param request the request
     * @param is the input stream
     * @return the updated input stream
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected InputStream preProcessHtml(Request request, InputStream is) throws IOException {
        if (request == null || is == null || !Boolean.parseBoolean(request.getParameter("pre-parse-html"))) {
            return is;
        }
        try {
            org.jsoup.nodes.Document doc = Jsoup.parse(is, "UTF-8", "/");
            return new ByteArrayInputStream(doc.outerHtml().getBytes());
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
                LOG.debug("getXmlResourceType: using default XML resource type strategy.");
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

}
