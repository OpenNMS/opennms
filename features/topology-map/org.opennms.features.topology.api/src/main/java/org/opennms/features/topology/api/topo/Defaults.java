/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
