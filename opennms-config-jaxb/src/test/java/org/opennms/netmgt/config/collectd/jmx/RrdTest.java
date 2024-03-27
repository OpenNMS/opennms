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
package org.opennms.netmgt.config.collectd.jmx;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

/**
 * The Test Class for Rrd.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class RrdTest extends XmlTestNoCastor<Rrd> {

    /**
     * Instantiates a new attribute test.
     *
     * @param sampleObject the sample object
     * @param sampleXml the sample XML
     * @param schemaFile the schema file
     */
    public RrdTest(Rrd sampleObject, String sampleXml, String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    /**
     * Data.
     *
     * @return the collection
     * @throws java.text.ParseException the parse exception
     */
    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        final Rrd rrd = new Rrd();
        rrd.setStep(300);
        rrd.addRra("RRA:AVERAGE:0.5:1:2016");

        return Arrays.asList(new Object[][] { {
            rrd,
            "<rrd step=\"300\"><rra>RRA:AVERAGE:0.5:1:2016</rra></rrd>",
            "target/classes/xsds/jmx-datacollection-config.xsd" } });
    }
}
