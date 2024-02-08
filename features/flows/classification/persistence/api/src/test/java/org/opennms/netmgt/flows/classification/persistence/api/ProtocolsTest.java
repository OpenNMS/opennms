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
package org.opennms.netmgt.flows.classification.persistence.api;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class ProtocolsTest {

    @Test
    public void verifyProtocolExistance() {
        Assert.assertThat(Protocols.getProtocols(), Matchers.hasSize(147));
    }

    @Test
    public void verifyDecimalLookup() {
        Assert.assertEquals("HOPOPT", Protocols.getProtocol(0).getKeyword());
        Assert.assertEquals("ICMP", Protocols.getProtocol(1).getKeyword());
        Assert.assertEquals("TCP", Protocols.getProtocol(6).getKeyword());
        Assert.assertEquals("UDP", Protocols.getProtocol(17).getKeyword());
        Assert.assertEquals("HMP", Protocols.getProtocol(20).getKeyword());
        Assert.assertEquals("Reserved", Protocols.getProtocol(255).getKeyword());
    }

    @Test
    public void verifyCaseInsensitiveLookup() {
        Assert.assertEquals(6, Protocols.getProtocol("tcp").getDecimal());
        Assert.assertEquals(6, Protocols.getProtocol("tCp").getDecimal());
        Assert.assertEquals(6, Protocols.getProtocol("TCP").getDecimal());
    }

    @Test(expected=IllegalArgumentException.class)
    public void verifyEmptyStringLookupFails() {
        Protocols.getProtocol("");
    }

    @Test(expected=IllegalArgumentException.class)
    public void verifyNullStringLookupFails() {
        Protocols.getProtocol((String) null);
    }

}
