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
package org.opennms.web.rest.v2.model;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ChartData", description = "A configured bar chart with its series queries evaluated.")
public class ChartDataDto extends ChartSummaryDto {

    @Schema(name = "ChartCategory", description = "One domain-axis category: the query's key and the label to show for it.")
    public static class Category {
        private String key;
        private String label;

        public Category() {
        }

        public Category(final String key, final String label) {
            this.key = key;
            this.label = label;
        }

        public String getKey() {
            return key;
        }

        public void setKey(final String key) {
            this.key = key;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(final String label) {
            this.label = label;
        }
    }

    @Schema(name = "ChartSeries", description = "One series: a value per category, in category order; null where the query returned no row.")
    public static class Series {
        private String name;
        private String color;
        private List<Number> values = new ArrayList<>();

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public String getColor() {
            return color;
        }

        public void setColor(final String color) {
            this.color = color;
        }

        public List<Number> getValues() {
            return values;
        }

        public void setValues(final List<Number> values) {
            this.values = values;
        }
    }

    private List<Category> categories = new ArrayList<>();
    private List<Series> series = new ArrayList<>();

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(final List<Category> categories) {
        this.categories = categories;
    }

    public List<Series> getSeries() {
        return series;
    }

    public void setSeries(final List<Series> series) {
        this.series = series;
    }
}
