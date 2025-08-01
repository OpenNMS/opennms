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
package org.opennms.netmgt.enlinkd.service.impl;

import java.util.List;

import org.opennms.netmgt.enlinkd.model.UserDefinedLink;
import org.opennms.netmgt.enlinkd.persistence.api.UserDefinedLinkDao;
import org.opennms.netmgt.enlinkd.service.api.UserDefinedLinkTopologyService;
import org.springframework.beans.factory.annotation.Autowired;

public class UserDefinedLinkTopologyServiceImpl extends TopologyServiceImpl implements UserDefinedLinkTopologyService {

    @Autowired
    private UserDefinedLinkDao userDefinedLinkDao;

    @Override
    public List<UserDefinedLink> findAllUserDefinedLinks() {
        return userDefinedLinkDao.findAll();
    }

    @Override
    public void saveOrUpdate(UserDefinedLink udl) {
        userDefinedLinkDao.save(udl);
        userDefinedLinkDao.flush();
        updatesAvailable();
    }

    @Override
    public void delete(UserDefinedLink udl) {
        userDefinedLinkDao.delete(udl);
        userDefinedLinkDao.flush();
        updatesAvailable();
    }

    @Override
    public void delete(Integer udlLinkId) {
        userDefinedLinkDao.delete(udlLinkId);
        userDefinedLinkDao.flush();
        updatesAvailable();
    }

}
