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
package org.opennms.protocols.xml.vtdxml;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

/**
 * The Abstract Class VTD-XML Collection Handler.
 * <p>All CollectionHandler based on VTD-XML should extend this class.</p>
 *
 * @author <a href="mailto:ronald.roskens@gmail.com">Ronald Roskens</a>
 */
public abstract class AbstractVTDXmlCollectionHandler extends AbstractXmlCollectionHandler {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractVTDXmlCollectionHandler.class);

    /**
     * Gets the resource name.
     *
     * @param resource the resource
     * @param group the group
     * @return the resource name
     * @throws XPathParseException the x-path parse exception
     */
    private String getResourceName(VTDNav resource, XmlGroup group) throws XPathParseException {
        // Processing multiple-key resource name.
        if (group.hasMultipleResourceKey()) {
            List<String> keys = new ArrayList<>();
            for (String key : group.getXmlResourceKey().getKeyXpathList()) {
                LOG.debug("getResourceName: getting key for resource's name using {}", key);
                resource.push();
                AutoPilot ap = new AutoPilot();
                ap.bind(resource);
                ap.selectXPath(key);
                keys.add(ap.evalXPathToString());
                resource.pop();
            }
            return StringUtils.join(keys, "_");
        }
        // If key-xpath doesn't exist or not found, a node resource will be assumed.
        if (group.getKeyXpath() == null) {
            return "node";
        }
        // Processing single-key resource name.
        LOG.debug("getResourceName: getting key for resource's name using {}", group.getKeyXpath());

        resource.push();
        AutoPilot ap = new AutoPilot();
        ap.bind(resource);
        ap.selectXPath(group.getKeyXpath());
        String s = ap.evalXPathToString();
        resource.pop();

        return s;
    }

    /**
     * Fill collection set.
     *
     * @param agent the agent
     * @param collectionSet the collection set
     * @param source the source
     * @param document the document
     * @throws ParseException the parse exception
     * @throws XPathParseException the x-path parse exception
     * @throws XPathEvalException the x-path evaluation exception
     * @throws NavException the navigation exception
     */
    protected void fillCollectionSet(CollectionAgent agent, CollectionSetBuilder builder, XmlSource source, VTDNav document) throws ParseException, XPathParseException, XPathEvalException, NavException {
        AutoPilot resAP = new AutoPilot(document);
        for (XmlGroup group : source.getXmlGroups()) {
            LOG.debug("fillCollectionSet: getting resources for XML group {} using XPATH {}", group.getName(), group.getResourceXpath());
            Date timestamp = getTimeStamp(document, group);
            resAP.selectXPath(group.getResourceXpath());
            while(resAP.evalXPath() != -1) {
                String resourceName = getResourceName(document, group);
                LOG.debug("fillCollectionSet: processing XML resource {}", resourceName);
                final Resource collectionResource = getCollectionResource(agent, resourceName, group.getResourceType(), timestamp);
                LOG.debug("fillCollectionSet: processing resource {}", collectionResource);
                for (XmlObject object : group.getXmlObjects()) {
                    document.push();
                    AutoPilot ap = new AutoPilot();
                    ap.bind(document);
                    ap.selectXPath(object.getXpath());
                    String value = ap.evalXPathToString();
                    document.pop();

                    builder.withAttribute(collectionResource, group.getName(), object.getName(), value, object.getDataType());
                }
                processXmlResource(builder, collectionResource, resourceName, group.getName());
            }
        }
    }

    /**
     * Gets the time stamp.
     *
     * @param document the document
     * @param group the group
     * @return the time stamp
     * @throws XPathParseException the x-path parse exception
     */
    protected Date getTimeStamp(VTDNav document, XmlGroup group) throws XPathParseException {
        if (group.getTimestampXpath() == null) {
            return null;
        }
        String pattern = group.getTimestampFormat() == null ? "yyyy-MM-dd HH:mm:ss" : group.getTimestampFormat();
        LOG.debug("getTimeStamp: retrieving custom timestamp to be used when updating RRDs using XPATH {} and pattern {}", group.getTimestampXpath(), pattern);

        document.push();
        AutoPilot ap = new AutoPilot();
        ap.bind(document);
        ap.selectXPath(group.getTimestampXpath());
        String value = ap.evalXPathToString();
        document.pop();

        if (value == null || value.isEmpty()) {
            LOG.warn("getTimeStamp: can't find the custom timestamp using XPATH {}",  group.getTimestampXpath());
            return null;
        }
        Date date = null;
        try {
            DateTimeFormatter dtf = DateTimeFormat.forPattern(pattern);
            DateTime dateTime = dtf.parseDateTime(value);
            date = dateTime.toDate();
        } catch (Exception e) {
            LOG.warn("getTimeStamp: can't convert custom timetime {} using pattern {}", value,  pattern);
            date = DateTime.now().toDate();
        }
        return date;
    }

    /**
     * Gets the XML document.
     *
     * @param urlString the URL string
     * @param request the request
     * @return the XML document
     * @throws Exception the exception
     */
    protected VTDNav getVTDXmlDocument(String urlString, Request request) throws Exception {
        InputStream is = null;
        URLConnection c = null;
        try {
            URL url = UrlFactory.getUrl(urlString, request);
            LOG.debug("getXmlDocument: got url");
            c = url.openConnection();
            LOG.debug("getXmlDocument: got connection");
            is = c.getInputStream();
            LOG.debug("getXmlDocument: got input stream");
            VTDNav nav = getVTDXmlDocument(is, request);
            LOG.debug("getXmlDocument: returning VTDNav");
            return nav;
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
     * @throws Exception the exception
     */
    protected VTDNav getVTDXmlDocument(InputStream is, Request request) throws Exception {
        is = preProcessHtml(request, is);
        is = applyXsltTransformation(request, is);
        VTDGen vg = new VTDGen();
        vg.setDoc(IOUtils.toByteArray(is));
        vg.parse(true);
        final VTDNav nav = vg.getNav();
        return nav;
    }

}
