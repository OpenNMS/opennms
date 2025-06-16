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
package org.opennms.netmgt.dao.api;

import java.io.Serializable;
import java.util.List;

import org.opennms.core.criteria.Criteria;
import org.opennms.netmgt.model.OnmsCriteria;

/**
 * @deprecated DAO interfaces that inherit from this should be refactored to no longer
 * use the {@link OnmsCriteria} functions.
 * 
 * @param <T> The type of the Entity this DAO is intended to manage.
 * @param <K> The key of the Entity.
 */
public interface LegacyOnmsDao<T, K extends Serializable> extends OnmsDao<T,K> {
    
    /**
     * @deprecated use {@link #findMatching(Criteria)} instead.
     */
    List<T> findMatching(OnmsCriteria criteria);

    /**
     * @deprecated use {@link #countMatching(Criteria)} instead.
     */
    int countMatching(final OnmsCriteria onmsCrit);
}
