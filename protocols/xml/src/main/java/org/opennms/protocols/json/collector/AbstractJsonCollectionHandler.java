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

package org.opennms.protocols.json.collector;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.lang.StringUtils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.config.collector.AttributeGroupType;
import org.opennms.protocols.xml.collector.AbstractXmlCollectionHandler;
import org.opennms.protocols.xml.collector.UrlFactory;
import org.opennms.protocols.xml.collector.XmlCollectionAttributeType;
import org.opennms.protocols.xml.collector.XmlCollectionResource;
import org.opennms.protocols.xml.collector.XmlCollectionSet;
import org.opennms.protocols.xml.collector.XmlCollectorException;
import org.opennms.protocols.xml.config.Request;
import org.opennms.protocols.xml.config.XmlGroup;
import org.opennms.protocols.xml.config.XmlObject;
import org.opennms.protocols.xml.config.XmlSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Abstract Class JSON Collection Handler.
 * <p>All JsonCollectionHandler should extend this class.</p>
 * 
 * @author <a href="mailto:ronald.roskens@gmail.com">Ronald Roskens</a>
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public abstract class AbstractJsonCollectionHandler extends AbstractXmlCollectionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractJsonCollectionHandler.class);

    /**
     * Fill collection set.
     *
     * @param agent the agent
     * @param collectionSet the collection set
     * @param source the source
     * @param json the JSON Object
     * @throws ParseException the parse exception
     */
    @SuppressWarnings("unchecked")
    protected void fillCollectionSet(CollectionAgent agent, XmlCollectionSet collectionSet, XmlSource source, JSONObject json) throws ParseException {
        JXPathContext context = JXPathContext.newContext(json);
        for (XmlGroup group : source.getXmlGroups()) {
            LOG.debug("fillCollectionSet: getting resources for XML group {} using XPATH {}", group.getName(), group.getResourceXpath());
            Date timestamp = getTimeStamp(context, group);
            Iterator<Pointer> itr = context.iteratePointers(group.getResourceXpath());
            while (itr.hasNext()) {
                JXPathContext relativeContext = context.getRelativeContext(itr.next());
                String resourceName = getResourceName(relativeContext, group);
                LOG.debug("fillCollectionSet: processing XML resource {}", resourceName);
                XmlCollectionResource collectionResource = getCollectionResource(agent, resourceName, group.getResourceType(), timestamp);
                AttributeGroupType attribGroupType = new AttributeGroupType(group.getName(), group.getIfType());
                for (XmlObject object : group.getXmlObjects()) {
                    String value = (String) relativeContext.getValue(object.getXpath());
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
     * @param context the JXpath context
     * @param group the group
     * @return the resource name
     */
    private String getResourceName(JXPathContext context, XmlGroup group) {
        // Processing multiple-key resource name.
        if (group.hasMultipleResourceKey()) {
            List<String> keys = new ArrayList<String>();
            for (String key : group.getXmlResourceKey().getKeyXpathList()) {
                LOG.debug("getResourceName: getting key for resource's name using {}", key);
                String keyName = (String)context.getValue(key);
                keys.add(keyName);
            }
            return StringUtils.join(keys, "_");
        }
        // If key-xpath doesn't exist or not found, a node resource will be assumed.
        if (group.getKeyXpath() == null) {
            return "node";
        }
        // Processing single-key resource name.
        LOG.debug("getResourceName: getting key for resource's name using {}", group.getKeyXpath());
        String keyName = (String)context.getValue(group.getKeyXpath());
        return keyName;
    }

    /**
     * Gets the time stamp.
     * 
     * @param context the JXPath context
     * @param group the group
     * @return the time stamp
     */
    protected Date getTimeStamp(JXPathContext context, XmlGroup group) {
        if (group.getTimestampXpath() == null) {
            return null;
        }
        String pattern = group.getTimestampFormat() == null ? "yyyy-MM-dd HH:mm:ss" : group.getTimestampFormat();
        LOG.debug("getTimeStamp: retrieving custom timestamp to be used when updating RRDs using XPATH {} and pattern {}", group.getTimestampXpath(), pattern);
        Date date = null;
        String value = (String)context.getValue(group.getTimestampXpath());
        try {
            DateTimeFormatter dtf = DateTimeFormat.forPattern(pattern);
            DateTime dateTime = dtf.parseDateTime(value);
            date = dateTime.toDate();
        } catch (Exception e) {
            LOG.warn("getTimeStamp: can't convert custom timestamp {} using pattern {}", value, pattern);
        }
        return date;
    }

    /**
     * Gets the JSON object.
     *
     * @param urlString the URL string
     * @param request the request
     * @return the JSON object
     */
    protected JSONObject getJSONObject(String urlString, Request request) {
        InputStream is = null;
        try {
            URL url = UrlFactory.getUrl(urlString, request);
            URLConnection c = url.openConnection();
            is = c.getInputStream();
            StringWriter writer = new StringWriter();
            IOUtils.copy(is, writer);
            JSONObject jsonObject = JSONObject.fromObject(writer.toString());
            UrlFactory.disconnect(c);
            return jsonObject;
        } catch (Exception e) {
            throw new XmlCollectorException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

}
