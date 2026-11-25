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
package org.opennms.web.rest.v2.plugins;

/** Body of POST plugin-management/fetch: which release asset to download. */
public class FetchRequest {
    private String catalogId;
    private String repository;
    private String tag;
    private String assetName;

    public String getCatalogId() { return catalogId; }
    public void setCatalogId(final String catalogId) { this.catalogId = catalogId; }
    public String getRepository() { return repository; }
    public void setRepository(final String repository) { this.repository = repository; }
    public String getTag() { return tag; }
    public void setTag(final String tag) { this.tag = tag; }
    public String getAssetName() { return assetName; }
    public void setAssetName(final String assetName) { this.assetName = assetName; }
}
