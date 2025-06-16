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
package org.opennms.features.datachoices.internal.usagestatistics;

import java.util.ArrayList;
import java.util.List;

public class UsageStatisticsMetadataDTO {
    public static class UsageStatisticsMetadataItem {
        public String key;
        public String name;
        public String description;
        public String datatype; // "string", "number", "object"

        public UsageStatisticsMetadataItem() {
        }

        public UsageStatisticsMetadataItem(String key, String name, String description, String datatype) {
            this.key = key;
            this.name = name;
            this.description = description;
            this.datatype = datatype;
        }
    }

    private List<UsageStatisticsMetadataItem> metadata = new ArrayList<>();

    public UsageStatisticsMetadataDTO() {
    }

    public List<UsageStatisticsMetadataItem> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<UsageStatisticsMetadataItem> list) {
        this.metadata = list;
    }
}
