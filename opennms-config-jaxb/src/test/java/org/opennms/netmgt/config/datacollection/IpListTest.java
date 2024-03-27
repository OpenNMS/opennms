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
package org.opennms.netmgt.config.datacollection;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class IpListTest extends XmlTestNoCastor<IpList> {

    public IpListTest(final IpList sampleObject, final String sampleXml, final String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }
    
    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        final IpList list = new IpList();
        list.addIpAddress("192.168.0.1");
        list.addIpAddress("192.168.0.2");
        list.addIpAddressMask("255.255.255.0");

        return Arrays.asList(new Object[][] { {
                list,
                "<ipList><ipAddr>192.168.0.1</ipAddr><ipAddr>192.168.0.2</ipAddr><ipAddrMask>255.255.255.0</ipAddrMask></ipList>",
                "target/classes/xsds/datacollection-config.xsd" } });
    }


}
