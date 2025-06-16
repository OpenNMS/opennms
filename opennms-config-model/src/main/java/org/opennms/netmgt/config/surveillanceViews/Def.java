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
package org.opennms.netmgt.config.surveillanceViews;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Helper interface to handle similar column-def/row-def stuff.
 *
 * @author Christian Pape
 */
public interface Def {
    /**
     * Returns the label of this column/row def.
     *
     * @return the label
     */
    String getLabel();

    /**
     * Returns the report-category attribute of this column/row def.
     *
     * @return the report-category attribute
     */
    Optional<String> getReportCategory();

    /**
     * Returns the list of categories used by this column/row def.
     *
     * @return the list of categories
     */
    List<Category> getCategories();

    /**
     * Returns a set of category names used by this column/row def.
     *
     * @return the set of category names
     */
    Set<String> getCategoryNames();

    /**
     * Sets the label of this column/row def
     *
     * @param label the label to be used
     */
    void setLabel(String label);

    /**
     * Sets the report-category attribute of this column/row def
     *
     * @param reportCategory the report-category attribute to be used
     */
    void setReportCategory(String reportCategory);

    /**
     * Checks whether this column/row def uses the given category.
     *
     * @param name the nam eof the category to be checked
     * @return true, if used by this column/row def, false otherwise
     */
    boolean containsCategory(String name);
}
