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
package org.opennms.core.utils;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class SIUtilsTest extends TestCase {
    /**
     * 10 gigabit ethernet.
     */
    public void testTenGig() {
        assertEquals("10 Gbps", SIUtils.getHumanReadableIfSpeed(10000000000L));
    }

    /**
     * Gigabit Ethernet.
     */
    public void testOneGig() {
        assertEquals("1 Gbps", SIUtils.getHumanReadableIfSpeed(1000000000L));
    }

    /**
     * Fast Ethernet.
     */
    public void testOneHundredMeg() {
        assertEquals("100 Mbps", SIUtils.getHumanReadableIfSpeed(100000000L));
    }

    /**
     * Ethernet.
     */
    public void testTenMeg() {
        assertEquals("10 Mbps", SIUtils.getHumanReadableIfSpeed(10000000L));
    }

    /**
     * DS-1 line speed.
     * @link http://en.wikipedia.org/wiki/Digital_Signal_1
     */
    public void testOnePointFiveFourFourMeg() {
    	DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        assertEquals("1"+symbols.getDecimalSeparator()+"544 Mbps", SIUtils.getHumanReadableIfSpeed(1544000L));
    }

    /**
     * 1200bps modem line.
     */
    public void testTwelveHundred() {
    	DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        assertEquals("1"+symbols.getDecimalSeparator()+"2 kbps", SIUtils.getHumanReadableIfSpeed(1200L));
    }
}
