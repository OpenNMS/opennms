/*******************************************************************************
 * This file is part of OpenNMS(R).
 * <p>
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 * http://www.gnu.org/licenses/
 * <p>
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest.v2.bsm.test;

import java.util.HashSet;
import java.util.Set;

import org.opennms.netmgt.bsm.persistence.api.BusinessServiceDao;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEntity;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.web.rest.v2.bsm.test.BusinessServiceEntityBuilder;
import org.springframework.beans.factory.annotation.Autowired;

// TODO MVR this should live in its own module
public class BsmDatabasePopulator {

    @Autowired
    private DatabasePopulator databasePopulator;

    @Autowired
    private BusinessServiceDao businessServiceDao;

    public void populateDatabase() {
        databasePopulator.setPopulateInSeparateTransaction(false);
        databasePopulator.populateDatabase();
        BusinessServiceEntity businessService1 = new BusinessServiceEntityBuilder()
                .name("Business Service 1")
                .addAttribute("location", "Pittsboro")
                .addAttribute("email", "a@b.com")
                .toEntity();

        BusinessServiceEntity businessService2 = new BusinessServiceEntityBuilder()
                .name("Business Service 2")
                .addParent(businessService1)
                .toEntity();

        BusinessServiceEntity businessService3 = new BusinessServiceEntityBuilder()
                .name("Business Service 3")
                .addParent(businessService1)
                .toEntity();

        BusinessServiceEntity businessService4 = new BusinessServiceEntityBuilder()
                .name("Business Service 4")
                .toEntity();

        Set<BusinessServiceEntity> children = new HashSet<>();
        children.add(businessService2);
        children.add(businessService3);
        businessService1.setChildServices(children);

        businessServiceDao.save(businessService1);
        businessServiceDao.save(businessService2);
        businessServiceDao.save(businessService3);
        businessServiceDao.save(businessService4);
    }

    public void resetDatabase() {
        for (BusinessServiceEntity eachEntity : businessServiceDao.findAll()) {
            businessServiceDao.delete(eachEntity);
        }
        databasePopulator.resetDatabase();
    }

    // Reeturns the number of expected business services. The total is <initially created ones> + <createdCount>
    public int expectedBsCount(int createdCount) {
        return 4 + createdCount;
    }
}
