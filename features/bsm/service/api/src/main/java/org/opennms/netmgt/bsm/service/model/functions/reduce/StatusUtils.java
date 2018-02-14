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

package org.opennms.netmgt.bsm.service.model.functions.reduce;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.StatusWithIndex;
import org.opennms.netmgt.bsm.service.model.StatusWithIndices;

/**
 * Utility methods for manipulating {@link StatusWithIndex} and {@link StatusWithIndices} objects.
 *
 * @author jwhite
 */
public class StatusUtils {

    /**
     * Retrieves the indices with a status >= the given threshold.
     *
     * @param statuses
     * @param threshold
     * @return
     */
    protected static List<Integer> getIndicesWithStatusGe(List<StatusWithIndex> statuses, Status threshold) {
        return statuses.stream()
            .filter(si -> si.getStatus().isGreaterThanOrEqual(threshold))
            .map(StatusWithIndex::getIndex)
            .collect(Collectors.toList());
    }

    /**
     * Converts a list of {@link Status} to a list of {@link StatusWithIndex}, using
     * the position in the array as the index.
     *
     * @param statuses
     * @return
     */
    protected static List<StatusWithIndex> toListWithIndices(List<Status> statuses) {
        final List<StatusWithIndex> indexedStatuses = new ArrayList<>();
        for (int i = 0; i < statuses.size(); i++) {
            indexedStatuses.add(new StatusWithIndex(statuses.get(i), i));
        }
        return indexedStatuses;
    }

    /**
     * Retrieves the {@link Status} from a {@link StatusWithIndices}.
     *
     * @param si
     * @return
     */
    protected static Optional<Status> getStatus(Optional<StatusWithIndices> si) {
        if (!si.isPresent()) {
            return Optional.empty();
        } else {
            return Optional.of(si.get().getStatus());
        }
    }

}
