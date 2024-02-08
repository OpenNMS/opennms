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
package org.opennms.features.topology.api.topo;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;

/**
 * Object which defines the defaults for a {@link GraphProvider}.
 * Usually the defaults are used to apply the default semantic zoom level, layout and default focus after the
 * graph provider was changed.
 *
 * @author Markus von Rüden
 */
public class Defaults {

    public static final int DEFAULT_SEMANTIC_ZOOM_LEVEL = 1;
    public static final String DEFAULT_PREFERRED_LAYOUT = "D3 Layout";

    private Supplier<Integer> szlSupplier = () -> DEFAULT_SEMANTIC_ZOOM_LEVEL;
    private Supplier<String> layoutSupplier = () -> DEFAULT_PREFERRED_LAYOUT;
    private Supplier<List<Criteria>> criteriaSupplier = Collections::emptyList;

    public Defaults withSemanticZoomLevel(Supplier<Integer> szlSupplier) {
        this.szlSupplier = Objects.requireNonNull(szlSupplier);
        return this;
    }

    public Defaults withPreferredLayout(Supplier<String> layoutSupplier) {
        this.layoutSupplier = Objects.requireNonNull(layoutSupplier);
        return this;
    }

    public Defaults withCriteria(Supplier<List<Criteria>> criteriaSupplier) {
        this.criteriaSupplier = Objects.requireNonNull(criteriaSupplier);
        return this;
    }

    public Defaults withSemanticZoomLevel(int szl) {
        Preconditions.checkArgument(szl >= 0, "The semantic zoom level must be greater or equal than 0");
        this.szlSupplier = () -> Integer.valueOf(szl);
        return this;
    }

    public Defaults withPreferredLayout(String layout) {
        this.layoutSupplier = () -> layout;
        return this;
    }

    public int getSemanticZoomLevel() {
        return szlSupplier.get();
    }

    public String getPreferredLayout() {
        return layoutSupplier.get();
    }

    public List<Criteria> getCriteria() {
        List<Criteria> criterias = criteriaSupplier.get();
        if (criterias != null) {
            return Collections.unmodifiableList(criterias);
        }
        return null;
    }
}
