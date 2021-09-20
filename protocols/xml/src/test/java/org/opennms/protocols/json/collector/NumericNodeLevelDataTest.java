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

package org.opennms.protocols.json.collector;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * The Class NumericNodeLevelDataTest.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class NumericNodeLevelDataTest extends JsonCollectorITCase {

    /* (non-Javadoc)
     * @see org.opennms.protocols.json.collector.AbstractJsonCollectorTest#getJSONConfigFileName()
     */
    @Override
    public String getConfigFileName() {
        return "src/test/resources/sample-node-level-data.xml";
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.json.collector.AbstractJsonCollectorTest#getJSONSampleFileName()
     */
    @Override
    public String getSampleFileName() {
        return "src/test/resources/sample-node-level-data.json";
    }

    /**
     * Test default JSON collector.
     *
     * @throws Exception the exception
     */
    @Test
    public void testDefaultJsonCollector() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("collection", "Jeff");
        parameters.put("handler-class", "org.opennms.protocols.json.collector.MockDefaultJsonCollectionHandler");
        executeCollectorTest(parameters, 1);
        File file = new File(getSnmpRootDirectory(), "1/natStats.jrb");
        Assert.assertTrue(file.exists());
        String[] dsnames = new String[] { "ariNatTotalConx", "ariNatConnLimit" };
        Double[] dsvalues = new Double[] { 10.0, 20.0 };
        validateJrb(file, dsnames, dsvalues);
    }

}
