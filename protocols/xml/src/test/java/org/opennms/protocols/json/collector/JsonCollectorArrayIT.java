/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

import net.sf.json.JSONObject;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class JsonCollectorArrayIT extends JsonCollectorITCase {

    @Override
    public String getConfigFileName() {
        return "src/test/resources/json-array-datacollection-config.xml";
    }

    @Override
    public String getSampleFileName() {
        return "src/test/resources/array.json";
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testXpath() throws Exception {
        JSONObject json = MockDocumentBuilder.getJSONDocument();
        JXPathContext context = JXPathContext.newContext(json);

        Iterator<Pointer> itr = context.iteratePointers("/elements[4]/it");

        Assert.assertTrue(itr.hasNext());
        Assert.assertEquals(itr.next().getValue(), "works");

        Assert.assertFalse(itr.hasNext());
    }

    @Test
    public void testJsonCollector() throws Exception {
        Map<String, Object> parameters = new HashMap();
        parameters.put("collection", "json-array");
        parameters.put("handler-class", "org.opennms.protocols.json.collector.MockDefaultJsonCollectionHandler");

        executeCollectorTest(parameters, 4);
        Assert.assertTrue(new File(getSnmpRootDirectory(), "1/jsonArrayStats/foo/json-array-stats.jrb").exists());
        Assert.assertTrue(new File(getSnmpRootDirectory(), "1/jsonArrayStats/bar/json-array-stats.jrb").exists());
        Assert.assertTrue(new File(getSnmpRootDirectory(), "1/jsonArrayStats/baz/json-array-stats.jrb").exists());
        Assert.assertTrue(new File(getSnmpRootDirectory(), "1/jsonArrayStats/works/json-array-stats.jrb").exists());

        validateJrb(new File(getSnmpRootDirectory(), "1/jsonArrayStats/foo/json-array-stats.jrb"), new String[] {"val"}, new Double[] {0.0});
        validateJrb(new File(getSnmpRootDirectory(), "1/jsonArrayStats/bar/json-array-stats.jrb"), new String[] {"val"}, new Double[] {1.0});
        validateJrb(new File(getSnmpRootDirectory(), "1/jsonArrayStats/baz/json-array-stats.jrb"), new String[] {"val"}, new Double[] {2.0});
        validateJrb(new File(getSnmpRootDirectory(), "1/jsonArrayStats/works/json-array-stats.jrb"), new String[] {"val"}, new Double[] {1337.0});
    }
}
