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
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.model.Row;
import org.opennms.netmgt.rrd.model.RrdConvertUtils;
import org.opennms.netmgt.rrd.model.v3.RRDv3;
import org.opennms.netmgt.rrd.rrdtool.JniRrdStrategy;

/**
 * The Test class for XML Collector for Node Level Statistics using RRDtool
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class NodeLevelDataWithRrdtoolTest extends XmlCollectorITCase {

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

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.AbstractXmlCollectorTest#initializeRrdStrategy()
     */
    @Override
    protected RrdStrategy<?, ?> getRrdStrategy() throws Exception {
        setRrdBinary();
        setJniRrdLibrary();
        return new JniRrdStrategy();
    }

    /**
     * Sets the RRD binary.
     */
    protected void setRrdBinary() {
        String[] rrdLocations = {
                "/opt/local/bin/rrdtool",
                "/sw/bin/rrdtool",
                "/usr/bin/rrdtool",
                "/usr/local/rrdtool/bin/rrdtool",
                "/opt/csw/bin/rrdtool"
        };
        for(String location : rrdLocations) {
            File file = new File(location);
            if (file.exists()) {
                System.setProperty("rrd.binary", file.getAbsolutePath());
                System.err.printf("setRrdBinary: found rrdtool binary at %s\n", file.getAbsolutePath());
                return;
            }
        }
        throw new RuntimeException("Can't find RRDTOOL binary file");
    }

    /**
     * Sets the JNI RRD library.
     */
    protected void setJniRrdLibrary() {
        String[] rrdLocations = {
                "/opt/jrrd/lib/libjrrd.jnilib",
                "/opt/local/lib/libjrrd.jnilib",
                "/usr/lib/libjrrd.so",
                "/usr/lib64/libjrrd.so",
                "/usr/local/lib/libjrrd.so"
        };
        for(String location : rrdLocations) {
            File file = new File(location);
            if (file.exists()) {
                System.setProperty("opennms.library.jrrd", file.getAbsolutePath());
                System.setProperty("org.opennms.rrd.usejni", "true");
                System.err.printf("setJniRrdLibrary: found jrrd binary at %s\n", file.getAbsolutePath());
                return;
            }
        }
        throw new RuntimeException("Can't find JNI RRD or JRRD Jar Library file)");
    }

    /**
     * Test XML collector with Standard handler.
     *
     * @throws Exception the exception
     */
    @Test
    @Ignore("manual test relies on the RRDtool binary and JRRD")
    public void testDefaultXmlCollector() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("collection", "NodeLevel");
        parameters.put("handler-class", "org.opennms.protocols.xml.collector.MockDefaultXmlCollectionHandler");
        for (int i = 0; i < 4; i++) {
            executeCollectorTest(parameters, 1);
            Thread.sleep(1000);
        }
        File file = new File("target/snmp/1/node-level-stats.rrd");
        Assert.assertTrue(file.exists());
        String[] dsnames = new String[] { "v1", "v2", "v3", "v4", "v5", "v6" };
        Double[] dsvalues = new Double[] { 10.0, 11.0, 12.0, 13.0, 14.0, 15.0 };
        validateRrd(file, dsnames, dsvalues);
    }
}
