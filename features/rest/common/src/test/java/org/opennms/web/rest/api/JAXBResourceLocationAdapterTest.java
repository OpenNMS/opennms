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
import org.opennms.web.rest.api.support.JAXBResourceLocationAdapter;

public class JAXBResourceLocationAdapterTest {

    @Test
    public void testMarshal() throws Exception {
        ResourceLocation location = new ResourceLocation(ApiVersion.Version2, "business-services");

        JAXBResourceLocationAdapter adapter = new JAXBResourceLocationAdapter();
        Assert.assertEquals("/api/v2/business-services", adapter.marshal(location));
    }

    @Test
    public void testUnmarshal() throws Exception {
        JAXBResourceLocationAdapter adapter = new JAXBResourceLocationAdapter();
        ResourceLocation location = adapter.unmarshal("/api/v2/business-services");
        Assert.assertEquals("/api/v2/business-services", location.toString());

        ResourceLocation location2 = new ResourceLocation(ApiVersion.Version2, "business-services");
        Assert.assertEquals(location, location2);
    }
}
