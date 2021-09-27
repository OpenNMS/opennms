/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.flows.classification.internal.value;

import org.opennms.netmgt.flows.classification.internal.decision.Bound;

public interface RuleValue<S extends Comparable<S>, T extends RuleValue<S, T>> {

    /**
     * Shrinks this rule value by removing those parts that are already covered by the given bound.
     * <p>
     * The given bounds result from thresholds along paths in the decision tree. During
     * classification those parts that are covered by these threshold need not to be checked again.
     *
     * @return Returns a shrunk rule value or {@code null} if this rule value is completely covered by the given bound.
     */
    T shrink(Bound<S> bound);
}
