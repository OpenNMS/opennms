/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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
public class NodeLevelDataTest extends AbstractXmlCollectorTest {

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
    public String getXmlConfigFileName() {
        return "src/test/resources/node-level-datacollection-config.xml";
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.AbcstractXmlCollectorTest#getXmlSampleFileName()
     */
    @Override
    public String getXmlSampleFileName() {
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
        File file = new File("target/snmp/1/node-level-stats.jrb");
        Assert.assertTrue(file.exists());
        String[] dsnames = new String[] { "v1", "v2", "v3", "v4", "v5", "v6" };
        Double[] dsvalues = new Double[] { 10.0, 11.0, 12.0, 13.0, 14.0, 15.0 };
        validateJrb(file, dsnames, dsvalues);
    }
}
