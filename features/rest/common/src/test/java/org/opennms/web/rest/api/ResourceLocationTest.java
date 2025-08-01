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
package org.opennms.web.rest.api;

import org.junit.Assert;
import org.junit.Test;

public class ResourceLocationTest {

    @Test
    public void testToString() {
        ResourceLocation location = new ResourceLocation(ApiVersion.Version1, "business-services", "1");
        Assert.assertEquals("/rest/business-services/1", location.toString());

        ResourceLocation location2 = new ResourceLocation(ApiVersion.Version2, "some/path/with/slashes", "and-even-more");
        Assert.assertEquals("/api/v2/some/path/with/slashes/and-even-more", location2.toString());

        ResourceLocation location3 = new ResourceLocation(ApiVersion.Version2, "some/path/with/slash/");
        Assert.assertEquals("/api/v2/some/path/with/slash", location3.toString());
    }

    @Test
    public void testEqualsAndHashCode() {
        ResourceLocation location1 = new ResourceLocation(ApiVersion.Version2, "business-services", "1");
        ResourceLocation location2 = new ResourceLocation(ApiVersion.Version2, "business-services", "1");
        Assert.assertEquals(location1, location1);
        Assert.assertEquals(location1.hashCode(), location2.hashCode());
        Assert.assertEquals(location1, location2);

        ResourceLocation location3 = new ResourceLocation(ApiVersion.Version1, "business-services", "1");
        Assert.assertFalse("Is equals, but should not be equal", location1.equals(location3));
        Assert.assertFalse("HashCode is equal, but should not be equal", location1.hashCode() == location3.hashCode());

        ResourceLocation location4 = new ResourceLocation(ApiVersion.Version2, "business-services");
        Assert.assertFalse("Is equals, but should not be equal", location1.equals(location4));
        Assert.assertFalse("HashCode is equal, but should not be equal", location1.hashCode() == location4.hashCode());
    }
}
