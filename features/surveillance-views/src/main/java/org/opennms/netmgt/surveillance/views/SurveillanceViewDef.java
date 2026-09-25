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
package org.opennms.netmgt.surveillance.views;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.MoreObjects;

/**
 * One row or column of a {@link SurveillanceView}: a label plus the node
 * category names it aggregates (the legacy {@code row-def}/{@code column-def}).
 * {@code reportCategory} is carried for round-trip fidelity with the legacy
 * XML model; nothing in the new stack consumes it.
 */
public class SurveillanceViewDef {

    private String label;
    private List<String> categories = new ArrayList<>();
    private String reportCategory;

    public SurveillanceViewDef() {
    }

    public SurveillanceViewDef(String label, String... categories) {
        this.label = label;
        for (String category : categories) {
            this.categories.add(category);
        }
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories == null ? new ArrayList<>() : categories;
    }

    public String getReportCategory() {
        return reportCategory;
    }

    public void setReportCategory(String reportCategory) {
        this.reportCategory = reportCategory;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("label", label)
                .add("categories", categories)
                .add("reportCategory", reportCategory)
                .toString();
    }
}
