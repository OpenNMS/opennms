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
package org.opennms.protocols.xml.collector;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.support.AbstractCollectionSetVisitor;

/**
 * The Test class for XML Collector for Node Level Statistics.
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class NodeLevelDataTest extends XmlCollectorITCase {

    /**
     * The Class CountVisitor.
     */
    private static class CountVisitor extends AbstractCollectionSetVisitor {

        /** The resource count. */
        private int resourceCount = 0;

        /** The attribute count. */
        private int attributeCount = 0;

        /* (non-Javadoc)
         * @see org.opennms.netmgt.collection.support.AbstractCollectionSetVisitor#visitResource(org.opennms.netmgt.collection.api.CollectionResource)
         */
        @Override
        public void visitResource(CollectionResource resource) {
            resourceCount++;
        }

        /* (non-Javadoc)
         * @see org.opennms.netmgt.collection.support.AbstractCollectionSetVisitor#visitAttribute(org.opennms.netmgt.collection.api.CollectionAttribute)
         */
        @Override
        public void visitAttribute(CollectionAttribute attribute) {
            attributeCount++;
        }

        /**
         * Gets the resource count.
         *
         * @return the resource count
         */
        public int getResourceCount() {
            return resourceCount;
        }

        /**
         * Gets the attribute count.
         *
         * @return the attribute count
         */
        public int getAttributeCount() {
            return attributeCount;
        }
    }


    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.AbcstractXmlCollectorTest#getXmlConfigFileName()
     */
    @Override
    public String getConfigFileName() {
        return "src/test/resources/node-level-datacollection-config.xml";
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.AbcstractXmlCollectorTest#getXmlSampleFileName()
     */
    @Override
    public String getSampleFileName() {
        return "src/test/resources/node-level.xml";
    }

    /**
     * Test XML collector with Standard handler.
     *
     * @throws Exception the exception
     */
    @Test
    public void testDefaultXmlCollector() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("collection", "NodeLevel");
        parameters.put("handler-class", "org.opennms.protocols.xml.collector.MockDefaultXmlCollectionHandler");
        CollectionSet collectionSet = executeCollectorTest(parameters, 1);
        CountVisitor visitor = new CountVisitor();
        collectionSet.visit(visitor);
        Assert.assertEquals(1, visitor.getResourceCount());
        Assert.assertEquals(6, visitor.getAttributeCount());
        File file = new File(getSnmpRootDirectory(), "1/node-level-stats.jrb");
        Assert.assertTrue(file.exists());
        String[] dsnames = new String[] { "v1", "v2", "v3", "v4", "v5", "v6" };
        Double[] dsvalues = new Double[] { 10.0, 11.0, 12.0, 13.0, 14.0, 15.0 };
        validateJrb(file, dsnames, dsvalues);
    }
}
