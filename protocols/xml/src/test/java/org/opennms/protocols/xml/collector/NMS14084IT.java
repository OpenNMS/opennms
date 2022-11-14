/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

public class NMS14084IT extends XmlCollectorITCase {

    @Override
    public String getConfigFileName() {
        return "src/test/resources/NMS-14084-xml-datacollection-config.xml";
    }

    @Override
    public String getSampleFileName() {
        return "src/test/resources/NMS-14084.xml";
    }

    @Test
    public void testDefaultXmlCollector() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();

        parameters.put("collection", "xml-mapping-test");
        parameters.put("handler-class", "org.opennms.protocols.xml.collector.MockDefaultXmlCollectionHandler");

        executeCollectorTest(parameters, 4);

        Assert.assertTrue(new File(getSnmpRootDirectory(), "1/input/blupp/xml-mapping-test.jrb").exists());
        Assert.assertTrue(new File(getSnmpRootDirectory(), "1/input/bar/xml-mapping-test.jrb").exists());
        Assert.assertTrue(new File(getSnmpRootDirectory(), "1/input/foo/xml-mapping-test.jrb").exists());
        Assert.assertTrue(new File(getSnmpRootDirectory(), "1/input/bla/xml-mapping-test.jrb").exists());

        validateJrb(new File(getSnmpRootDirectory(), "1/input/foo/xml-mapping-test.jrb"), new String[]{"input", "read", "write"}, new Double[]{100.0, 10.0, 10.0});
        validateJrb(new File(getSnmpRootDirectory(), "1/input/bar/xml-mapping-test.jrb"), new String[]{"input", "read", "write"}, new Double[]{200.0, 20.0, 20.0});
        validateJrb(new File(getSnmpRootDirectory(), "1/input/blupp/xml-mapping-test.jrb"), new String[]{"input", "read", "write"}, new Double[]{300.0, 30.0, 30.0});
        validateJrb(new File(getSnmpRootDirectory(), "1/input/bla/xml-mapping-test.jrb"), new String[]{"input", "read", "write"}, new Double[]{400.0, 40.0, 40.0});
    }
}
