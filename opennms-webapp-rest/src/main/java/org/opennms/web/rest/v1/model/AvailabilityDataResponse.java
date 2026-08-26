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
package org.opennms.web.rest.v1.model;

import java.util.List;

import org.opennms.web.category.Category;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Documentation-only description of the body returned by {@code GET /availability}. The handler
 * returns a package-private JAXB type whose properties swagger cannot introspect.
 */
@Schema(name = "AvailabilityData", description = "Category groups as defined by categories.xml, each holding its categories.")
public class AvailabilityDataResponse {

    @Schema(description = "One entry per category group in categories.xml.", required = true)
    private List<AvailabilitySection> section;

    public List<AvailabilitySection> getSection() {
        return section;
    }

    @Schema(name = "AvailabilitySection", description = "A named group of categories.")
    public static class AvailabilitySection {

        @Schema(description = "Group name from categories.xml.", example = "Categories", required = true)
        private String name;

        @Schema(description = "Categories in this group, wrapped in the usual count/offset envelope.", required = true)
        private AvailabilityCategoryList categories;

        public String getName() {
            return name;
        }

        public AvailabilityCategoryList getCategories() {
            return categories;
        }
    }

    @Schema(name = "AvailabilityCategoryList", description = "Envelope around the category list.")
    public static class AvailabilityCategoryList {

        @Schema(description = "Number of categories in the group.", example = "8")
        private Integer totalCount;

        @Schema(description = "Number of categories in this response.", example = "8")
        private Integer count;

        @Schema(description = "Always 0; this list is not paged.", example = "0")
        private Integer offset;

        @Schema(description = "The categories. `last-updated` is emitted as epoch milliseconds, not as a date-time string.")
        private List<Category> category;

        public Integer getTotalCount() {
            return totalCount;
        }

        public Integer getCount() {
            return count;
        }

        public Integer getOffset() {
            return offset;
        }

        public List<Category> getCategory() {
            return category;
        }
    }
}
