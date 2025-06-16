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
package org.opennms.features.topology.plugins.browsers;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import org.opennms.core.criteria.Criteria;
import org.opennms.features.topology.api.browsers.OnmsContainerDatasource;
import org.opennms.netmgt.dao.api.OnmsDao;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

public class OnmsDaoContainerDatasource<T, K extends Serializable> implements OnmsContainerDatasource<T, K> {

    private final OnmsDao<T, K> dao;
    private final TransactionOperations transactionTemplate;

    public OnmsDaoContainerDatasource(OnmsDao<T, K> dao, TransactionOperations transactionTemplate) {
        this.dao = Objects.requireNonNull(dao);
        this.transactionTemplate = Objects.requireNonNull(transactionTemplate);
    }

    @Override
    public void clear() {
        dao.clear();
    }

    @Override
    public void delete(K itemId) {
        dao.delete(itemId);
    }

    @Override
    public List<T> findMatching(Criteria criteria) {
        // Wrap the find and callbacks in a single transaction
        return transactionTemplate.execute(new TransactionCallback<List<T>>() {
            @Override
            public List<T> doInTransaction(TransactionStatus arg0) {
                final List<T> matchingItems = dao.findMatching(criteria);
                matchingItems.forEach(t -> findMatchingCallback(t));
                return matchingItems;
            }
        });
    }

    public void findMatchingCallback(T item) {
        // pass
    }

    @Override
    public int countMatching(Criteria criteria) {
        return dao.countMatching(criteria);
    }

    @Override
    public T createInstance(Class<T> itemClass) throws IllegalAccessException, InstantiationException {
        return itemClass.newInstance();
    }
}
