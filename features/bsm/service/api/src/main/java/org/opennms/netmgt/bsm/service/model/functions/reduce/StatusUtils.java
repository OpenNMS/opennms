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
