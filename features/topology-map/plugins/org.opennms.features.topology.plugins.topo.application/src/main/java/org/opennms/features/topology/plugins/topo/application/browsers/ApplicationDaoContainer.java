/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

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
