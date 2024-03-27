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
package org.opennms.features.topology.api.browsers;


import org.junit.Assert;
import org.junit.Test;

public class PageTest {

    @Test
    public void testUpdateOffset() {
        OnmsVaadinContainer.Page p = new OnmsVaadinContainer.Page(30, new OnmsVaadinContainer.Size(new OnmsVaadinContainer.SizeReloadStrategy() {
            @Override
            public int reload() {
                return 400;
            }
        }));

        // first page
        Assert.assertFalse(p.updateOffset(0));
        Assert.assertEquals(30, p.length);
        Assert.assertEquals(0, p.offset);

        // somewhere in between
        Assert.assertTrue(p.updateOffset(210));
        Assert.assertEquals(30, p.length);
        Assert.assertEquals(210 / 30 * 30, p.offset);

        // last page
        Assert.assertTrue(p.updateOffset(399));
        Assert.assertEquals(30, p.length);
        Assert.assertEquals(399 / 30 * 30, p.offset);
    }
}
