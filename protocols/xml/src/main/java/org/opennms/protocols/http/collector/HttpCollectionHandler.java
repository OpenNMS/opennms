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
package org.opennms.protocols.http.collector;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.select.Elements;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.Resource;
import org.opennms.protocols.xml.collector.AbstractXmlCollectionHandler;
import org.opennms.protocols.xml.collector.UrlFactory;
import org.opennms.protocols.xml.config.Request;
import org.opennms.protocols.xml.config.XmlGroup;
import org.opennms.protocols.xml.config.XmlObject;
import org.opennms.protocols.xml.config.XmlSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class HTTP Collection Handler.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class HttpCollectionHandler extends AbstractXmlCollectionHandler {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(HttpCollectionHandler.class);

    @Override
    protected void fillCollectionSet(String urlString, Request request, CollectionAgent agent, CollectionSetBuilder builder, XmlSource source) throws Exception {
        Document doc = getJsoupDocument(urlString, request);
        for (XmlGroup group : source.getXmlGroups()) {
            LOG.debug("fillCollectionSet: getting resources for XML group {} using selector {}", group.getName(), group.getResourceXpath());
            Date timestamp = getTimeStamp(doc, group);
            Elements elements = doc.select(group.getResourceXpath());
            LOG.debug("fillCollectionSet: {} => {}", group.getResourceXpath(), elements);
            String resourceName = getResourceName(elements, group);
            LOG.debug("fillCollectionSet: processing XML resource {}", resourceName);
            final Resource collectionResource = getCollectionResource(agent, resourceName, group.getResourceType(), timestamp);
            LOG.debug("fillCollectionSet: processing resource {}", collectionResource);
            for (XmlObject object : group.getXmlObjects()) {
                Elements el = elements.select(object.getXpath());
                if (el == null) {
                    LOG.info("No value found for object named '{}'. Skipping.", object.getName());
                    continue;
                }
                builder.withAttribute(collectionResource, group.getName(), object.getName(), el.html(), object.getDataType());
            }
            processXmlResource(builder, collectionResource, resourceName, group.getName());
        }
    }

    @Override
    protected void processXmlResource(CollectionSetBuilder builder, Resource collectionResource, String resourceTypeName, String group) { };

    /**
     * Gets the resource name.
     *
     * @param elements the JSoup elements
     * @param group the group
     * @return the resource name
     */
    private String getResourceName(Elements elements, XmlGroup group) {
        // Processing multiple-key resource name.
        if (group.hasMultipleResourceKey()) {
            List<String> keys = new ArrayList<>();
            for (String key : group.getXmlResourceKey().getKeyXpathList()) {
                LOG.debug("getResourceName: getting key for resource's name using selector {}", key);
                Elements el = elements.select(key);
                if (el != null) {
                    keys.add(el.html());
                }
            }
            return StringUtils.join(keys, "_");
        }
        // If key-xpath doesn't exist or not found, a node resource will be assumed.
        if (group.getKeyXpath() == null) {
            return "node";
        }
        // Processing single-key resource name.
        LOG.debug("getResourceName: getting key for resource's name using selector {}", group.getKeyXpath());
        Elements el = elements.select(group.getKeyXpath());
        return el == null ? null : el.html();
    }

    /**
     * Gets the time stamp.
     * 
     * @param document the JSoup document
     * @param group the group
     * @return the time stamp
     */
    protected Date getTimeStamp(Document doc, XmlGroup group) {
        if (group.getTimestampXpath() == null) {
            return null;
        }
        String pattern = group.getTimestampFormat() == null ? "yyyy-MM-dd HH:mm:ss" : group.getTimestampFormat();
        LOG.debug("getTimeStamp: retrieving custom timestamp to be used when updating RRDs using selector {} and pattern {}", group.getTimestampXpath(), pattern);
        Elements el = doc.select(group.getTimestampXpath());
        if (el == null) {
            return null;
        }
        String value = el.html();
        Date date = null;
        try {
            DateTimeFormatter dtf = DateTimeFormat.forPattern(pattern);
            DateTime dateTime = dtf.parseDateTime(value);
            date = dateTime.toDate();
        } catch (Exception e) {
            LOG.warn("getTimeStamp: can't convert custom timetime {} using pattern {}", value, pattern);
        }
        return date;
    }

    /**
     * Gets the JSoup document.
     *
     * @param urlString the URL string
     * @param request the request
     * @return the JSoup document
     * @throws Exception the exception
     */
    protected Document getJsoupDocument(String urlString, Request request) throws Exception {
        InputStream is = null;
        URLConnection c = null;
        try {
            URL url = UrlFactory.getUrl(urlString, request);
            c = url.openConnection();
            is = c.getInputStream();
            final Document doc = Jsoup.parse(is, "ISO-8859-9", "/");
            doc.outputSettings().escapeMode(EscapeMode.xhtml);
            return doc;
        } finally {
            IOUtils.closeQuietly(is);
            UrlFactory.disconnect(c);
        }
    }

}
