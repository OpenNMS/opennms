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
package org.opennms.features.topology.plugins.topo.application.browsers;

import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.plugins.browsers.OnmsDaoContainerDatasource;
import org.opennms.features.topology.api.browsers.OnmsVaadinContainer;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.model.OnmsApplication;
import org.springframework.transaction.support.TransactionOperations;

public class ApplicationDaoContainer extends OnmsVaadinContainer<OnmsApplication, Integer> {
    private static final long serialVersionUID = 1L;

    public ApplicationDaoContainer(ApplicationDao applicationDao, TransactionOperations transactionTemplate) {
        super(OnmsApplication.class, new OnmsDaoContainerDatasource<>(applicationDao, transactionTemplate));
    }

    @Override
    protected Integer getId(OnmsApplication bean) {
        return bean == null ? null : bean.getId();
    }

    @Override
    protected ContentType getContentType() {
        return ContentType.Application;
    }
}
