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
package org.opennms.features.topology.api.browsers;

import java.io.Serializable;
import java.util.List;

import org.opennms.core.criteria.Criteria;

/**
 * Abstraction to allow the {@link OnmsVaadinContainer} to use different kinds of data sources, not only DAOs.
 *
 * @param <T> The entity type (e.g. OnmsAlarm).
 * @param <K> The key type of the entity (e.g. Integer)
 */
public interface OnmsContainerDatasource<T, K extends Serializable> {
    void clear();

    void delete(K itemId);

    List<T> findMatching(Criteria criteria);

    int countMatching(Criteria criteria);

    T createInstance(Class<T> itemClass) throws IllegalAccessException, InstantiationException;
}
