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
package org.opennms.protocols.xml.dao.jaxb;

import java.io.File;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.protocols.xml.config.XmlDataCollection;
import org.opennms.protocols.xml.config.XmlDataCollectionConfig;
import org.opennms.protocols.xml.config.XmlGroups;
import org.opennms.protocols.xml.config.XmlSource;
import org.opennms.protocols.xml.dao.XmlDataCollectionConfigDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

/**
 * The Class XmlDataCollectionConfigDaoJaxb.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class XmlDataCollectionConfigDaoJaxb extends AbstractJaxbConfigDao<XmlDataCollectionConfig, XmlDataCollectionConfig> implements XmlDataCollectionConfigDao {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(XmlDataCollectionConfigDaoJaxb.class);

    /**
     * Instantiates a new XML data collection configuration DAO using JAXB.
     */
    public XmlDataCollectionConfigDaoJaxb() {
        super(XmlDataCollectionConfig.class, "XML Data Collection Configuration");
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.dao.XmlDataCollectionConfigDao#getDataCollectionByName(java.lang.String)
     */
    @Override
    public XmlDataCollection getDataCollectionByName(String name) {
        XmlDataCollectionConfig config = getContainer().getObject();
        for (XmlDataCollection dataCol : config.getXmlDataCollections()) {
            if(dataCol.getName().equals(name)) {
                return dataCol;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.dao.XmlDataCollectionConfigDao#getDataCollectionByIndex(int)
     */
    @Override
    public XmlDataCollection getDataCollectionByIndex(int idx) {
        XmlDataCollectionConfig config = getContainer().getObject();
        return config.getXmlDataCollections().get(idx);
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.dao.XmlDataCollectionConfigDao#getConfig()
     */
    @Override
    public XmlDataCollectionConfig getConfig() {
        return getContainer().getObject();
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.dao.jaxb.AbstractJaxbConfigDao#translateConfig(java.lang.Object)
     */
    @Override
    protected XmlDataCollectionConfig translateConfig(XmlDataCollectionConfig config) {
        for (XmlDataCollection collection : config.getXmlDataCollections()) {
            for (XmlSource source : collection.getXmlSources()) {
                parseXmlGroups(source);
            }
        }
        return config;
    }

    /**
     * Parses the XML groups.
     *
     * @param source the XML source
     */
    private void parseXmlGroups(XmlSource source) {
        if (!source.hasImportGroups()) {
            return;
        }
        for (String importGroup : source.getImportGroupsList()) {
            File file = new File(ConfigFileConstants.getHome(), "/etc/" + importGroup);
            LOG.debug("parseXmlGroups: parsing {}", file);
            XmlGroups groups = JaxbUtils.unmarshal(XmlGroups.class, new FileSystemResource(file));
            source.getXmlGroups().addAll(groups.getXmlGroups());
        }
    }

}
