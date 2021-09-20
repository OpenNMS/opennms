/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
