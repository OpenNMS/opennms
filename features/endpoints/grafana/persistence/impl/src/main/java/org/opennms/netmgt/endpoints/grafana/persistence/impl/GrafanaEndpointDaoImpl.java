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
package org.opennms.netmgt.endpoints.grafana.persistence.impl;

import java.util.List;

import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.endpoints.grafana.api.GrafanaEndpoint;
import org.opennms.netmgt.endpoints.grafana.persistence.api.GrafanaEndpointDao;
import org.springframework.stereotype.Component;

@Component
public class GrafanaEndpointDaoImpl extends AbstractDaoHibernate<GrafanaEndpoint, Long> implements GrafanaEndpointDao {

    public GrafanaEndpointDaoImpl() {
        super(GrafanaEndpoint.class);
    }

    @Override
    public GrafanaEndpoint getByUid(String endpointUid) {
        final List<GrafanaEndpoint> endpoints = (List<GrafanaEndpoint>) getHibernateTemplate().find("select g from GrafanaEndpoint g where g.uid = ?", endpointUid);
        if (endpoints.isEmpty()) {
            return null;
        }
        return endpoints.get(0);
    }
}
