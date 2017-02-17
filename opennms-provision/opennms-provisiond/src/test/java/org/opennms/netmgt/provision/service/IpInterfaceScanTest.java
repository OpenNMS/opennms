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

package org.opennms.netmgt.provision.service;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.support.AbstractDetector;

/**
 * The Class IpInterfaceScanTest.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class IpInterfaceScanTest {

    /**
     * The Class MockServiceDetector.
     */
    private static final class MockServiceDetector extends AbstractDetector {

        /**
         * Instantiates a new mock service detector.
         *
         * @param serviceName the service name
         * @param ipMatch the IP match
         */
        protected MockServiceDetector(String serviceName, String ipMatch) {
            super(serviceName, 100);
            setIpMatch(ipMatch);
        }

        /* (non-Javadoc)
         * @see org.opennms.netmgt.provision.support.AbstractDetector#onInit()
         */
        @Override
        protected void onInit() {}

        /* (non-Javadoc)
         * @see org.opennms.netmgt.provision.support.AbstractDetector#dispose()
         */
        @Override
        public void dispose() {}
    }

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
        IpInterfaceScan scan = new IpInterfaceScan(1, InetAddressUtils.addr(ipAddress), "JUnit", null);
        Assert.assertTrue(expectedResult == scan.shouldDetect(new MockServiceDetector("TEST_SVC", ipMatch)));
    }

}
