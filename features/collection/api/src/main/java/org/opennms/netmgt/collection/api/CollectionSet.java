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
package org.opennms.netmgt.collection.api;

import java.util.Date;
import java.util.List;
import java.util.OptionalLong;


/**
 * {@link CollectionSet} represents the results of a collection and generally includes
 * a {@link List} of {@link CollectionResource} instances that were created during the
 * collection.
 */
public interface CollectionSet extends CollectionVisitable {

    CollectionStatus getStatus();

    /**
     * <p>ignorePersist</p>
     *
     * @return a boolean.
     */
    boolean ignorePersist();
    
    /**
     * Returns the timestamp of when this data collection was taken.
     * Used by thresholding.
     * @return
    */
    Date getCollectionTimestamp();

    /**
     * @return an optional containing the sequence number of the source this collection set was built from if
     * applicable, otherwise an empty optional
     */
    default OptionalLong getSequenceNumber() {
        return OptionalLong.empty();
    }
}
