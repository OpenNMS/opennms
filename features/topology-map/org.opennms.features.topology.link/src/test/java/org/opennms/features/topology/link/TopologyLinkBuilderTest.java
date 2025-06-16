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
package org.opennms.features.topology.link;

import org.junit.Assert;
import org.junit.Test;

public class TopologyLinkBuilderTest {

    @Test
    public void verifyGetLink() {
        Assert.assertEquals("/opennms/topology?provider=Enhanced+Linkd&szl=1", new TopologyLinkBuilder().getLink());
        Assert.assertEquals("/opennms/topology?provider=Enhanced+Linkd&szl=1", new TopologyLinkBuilder().provider(null).focus((String[]) null).layout((String) null).getLink());
        Assert.assertEquals(
                "/opennms/topology?provider=Application&szl=10&focus-vertices=bsm%3A1%2Cbsm%3A2&layout=Circle+Layout",
                new TopologyLinkBuilder().layout(Layout.CIRCLE).provider(TopologyProvider.APPLICATION).focus("bsm:1","bsm:2").szl(10).getLink());
    }
}