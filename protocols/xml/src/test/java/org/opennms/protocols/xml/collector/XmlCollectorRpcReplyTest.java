/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.protocols.xml.collector;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * The Test class for XML Collector for Juniper RPC Replay Statistics
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class XmlCollectorRpcReplyTest extends AbstractXmlCollectorTest {

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.AbcstractXmlCollectorTest#getXmlConfigFileName()
     */
    @Override
    public String getXmlConfigFileName() {
        return "src/test/resources/rpc-reply-datacollection-config.xml";
    }
    
    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.AbcstractXmlCollectorTest#getXmlSampleFileName()
     */
    @Override
    public String getXmlSampleFileName() {
        return "src/test/resources/rpc-reply.xml";
    }

    /**
     * Test XML collector with Standard handler.
     *
     * @throws Exception the exception
     */
    @Test
    public void testDefaultXmlCollector() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("collection", "RPC");
        parameters.put("handler-class", "org.opennms.protocols.xml.collector.MockDefaultXmlCollectionHandler");
        executeCollectorTest(parameters, 1);
        File file = new File("target/snmp/1/cfmEntry/D3456_ddd11/rpc-reply.jrb");
        String[] dsnames = new String[] { "V01", "V02", "V03", "V04", "V05", "V06", "V07", "V08", "V09", "V10", "V11",
                "V12", "V13", "V14", "V15", "V16", "V17", "V18", "V19", "V20", "V21", "V22" };
        Double[] dsvalues = new Double[] { 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0,
                19.0, 20.0, 20.0, 21.0, 21.0, 22.0, 22.0, 23.0, 24.0, 25.0, 26.0 };
        validateJrb(file, dsnames, dsvalues);
    }

}
