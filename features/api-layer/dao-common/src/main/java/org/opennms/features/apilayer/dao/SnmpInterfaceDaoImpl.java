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
package org.opennms.features.apilayer.dao;

import java.util.Objects;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.features.apilayer.utils.ModelMappers;
import org.opennms.integration.api.v1.dao.SnmpInterfaceDao;
import org.opennms.integration.api.v1.model.SnmpInterface;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsSnmpInterface;

public class SnmpInterfaceDaoImpl implements SnmpInterfaceDao {

    private final org.opennms.netmgt.dao.api.SnmpInterfaceDao snmpInterfaceDao;
    private final SessionUtils sessionUtils;

    public SnmpInterfaceDaoImpl(org.opennms.netmgt.dao.api.SnmpInterfaceDao snmpInterfaceDao, SessionUtils sessionUtils) {
        this.snmpInterfaceDao = Objects.requireNonNull(snmpInterfaceDao);
        this.sessionUtils = Objects.requireNonNull(sessionUtils);
    }

    @Override
    public Long getSnmpInterfaceCount() {
        final CriteriaBuilder criteriaBuilder = new CriteriaBuilder(OnmsSnmpInterface.class);
        return sessionUtils.withReadOnlyTransaction(() -> (long)snmpInterfaceDao.countMatching(criteriaBuilder.toCriteria()));
    }

    @Override
    public SnmpInterface findByNodeIdAndDescrOrName(Integer nodeId, String descrOrName) {
        // Note that the SnmpInterfaceDaoHibernate#findByNodeIdAndDescription method actually
        // searches by either the ifName or the ifDescr
        return sessionUtils.withReadOnlyTransaction(() -> ModelMappers.toSnmpInterface(snmpInterfaceDao.findByNodeIdAndDescription(nodeId, descrOrName)));
    }

}
