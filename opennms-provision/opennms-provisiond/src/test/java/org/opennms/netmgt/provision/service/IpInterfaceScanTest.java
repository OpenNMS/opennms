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
package org.opennms.netmgt.provision.service;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;

/**
 * The Class IpInterfaceScanTest.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class IpInterfaceScanTest {

    /**
     * Check regular expression rule.
     */
    @Test
    public void checkRegexRule() {
        String ipMatch = "~11\\.[23]\\.1\\.1.*";
        runTest("11.3.1.12", ipMatch, true);
        runTest("12.3.1.12", ipMatch, false);
    }

    /**
     * Check simple IP like rule.
     */
    @Test
    public void checkSimpleIPLikeRule() {
        String ipMatch = "10.0,1.*.1-10";
        runTest("10.1.100.2", ipMatch, true);
        runTest("192.168.1.1", ipMatch, false);
    }

    /**
     * Check multiple IP like rules.
     */
    @Test
    public void checkMultipleIPLikeRules() {
        String ipMatch = "(10.0.1.1-10 || 11.0.2.100-200) && !192.168.*.*";
        runTest("11.0.2.100", ipMatch, true);
        runTest("192.168.1.1", ipMatch, false);
        runTest("10.0.1.2", ipMatch, true);
        runTest("12.0.0.1", ipMatch, false);
    }

    /**
     * Check invalid IP like rules.
     */
    @Test
    public void checkInvalidIPLikeRules() {
        String ipMatch = "(10.0.1.1-10 | 11.0.2.100-200) blah";
        runTest("11.0.2.100", ipMatch, false);
    }

    @Test
    public void checkIPv6() {
        runTest("2600:5800:f2a2::1cef:6376:349f:5f6d", "2600:*:*:*:*:*:*:*", true);
        runTest("2600:5800:f2a2::1cef:6376:349f:5f6d", "*:5555:*:*:*:*:*:*", false);


    }

    @Test
    public void checkIPVersionMixing() {
        runTest("127.0.0.1", "127.0.0.1 || 0:0:0:0:0:0:0:1", true);
        runTest("::1", "127.0.0.1 || 0:0:0:0:0:0:0:1", true);
        // An IP address can't belong to both classes!
        runTest("127.0.0.1", "127.0.0.1 && 0:0:0:0:0:0:0:1", false);
        runTest("::1", "127.0.0.1 && 0:0:0:0:0:0:0:1", false);
        // Type mismatches
        runTest("::1", "127.0.0.1", false);
        runTest("127.0.0.1", "0:0:0:0:0:0:0:1", false);
    }

    /**
     * Run test.
     *
     * @param ipAddress the IP address
     * @param ipMatch the IP match
     * @param expectedResult the expected result
     */
    private void runTest(String ipAddress, String ipMatch, boolean expectedResult) {
        PluginConfig detectorConfig = new PluginConfig();
        detectorConfig.addParameter("ipMatch", ipMatch);
        Assert.assertEquals(expectedResult, IpInterfaceScan.shouldDetect(detectorConfig, InetAddressUtils.addr(ipAddress)));
    }

}
