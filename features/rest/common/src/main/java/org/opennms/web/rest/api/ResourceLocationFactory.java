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

/**
 * Factory to create {@link ResourceLocation} objects for certain REST endpoints.
 */
public class ResourceLocationFactory {

    public static ResourceLocation createIpServiceLocation(String id) {
        return new ResourceLocation(ApiVersion.Version1, "ifservices", id);
    }

    public static ResourceLocation createBusinessServiceLocation(String id) {
        return new ResourceLocation(ApiVersion.Version2, "business-services", id);
    }

    public static ResourceLocation createBusinessServiceLocation() {
        return new ResourceLocation(ApiVersion.Version2, "business-services");
    }

    public static ResourceLocation createBusinessServiceIpServiceLocation(int ipServiceId) {
        return new ResourceLocation(ApiVersion.Version2, "business-services", "ip-services", String.valueOf(ipServiceId));
    }

    public static ResourceLocation createBusinessServiceEdgeLocation(long bsId, long edgeId) {
        return new ResourceLocation(ApiVersion.Version2, "business-services", String.valueOf(bsId), "edges", String.valueOf(edgeId));
    }
}
