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
package org.opennms.netmgt.dao.support;

import org.opennms.netmgt.dao.api.OnmsDao;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Template for creating a row if and only if one doesn't already exists.  This suffers
 * from some of the same concurrency issues as described in the {@link UpsertTemplate}.  See the
 * detailed javadoc there for a description.
 *
 * @author brozow
 */
public abstract class CreateIfNecessaryTemplate<T, D extends OnmsDao<T, ?>> extends UpsertTemplate<T, D> {

    /**
     * Create a CreateIfNecessaryTemplate using the given transactionManager to create transactions.
     */
    public CreateIfNecessaryTemplate(PlatformTransactionManager transactionManager, D dao) {
        super(transactionManager, dao);
    }

    @Override
    protected abstract T query();

    /**
     * There is no need to update the object for this case as we just return the object found.
     */
    @Override
    protected T doUpdate(T dbObj) {
        return dbObj;
    }

    @Override
    protected abstract T doInsert();
    

}
