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

package org.opennms.netmgt.bsm.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opennms.netmgt.bsm.persistence.api.BusinessServiceDao;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.springframework.beans.factory.annotation.Autowired;

public class BsmDatabasePopulator {

    @Autowired
    private DatabasePopulator databasePopulator;

    @Autowired
    private BusinessServiceDao businessServiceDao;
    private List<BsmTestData> testDatas;

    /**
     * Optional set of test data which is pushed to the database in addition to the default
     * database population.
     * @param testDatas optional test data.
     */
    public void populateDatabase(BsmTestData... testDatas) {
        this.testDatas = testDatas == null ? new ArrayList<>() : Arrays.asList(testDatas);

        databasePopulator.setPopulateInSeparateTransaction(false);
        databasePopulator.populateDatabase();

        this.testDatas.forEach(
            eachSet -> eachSet.getServices().forEach(
                    eachService -> businessServiceDao.save(eachService)
            )
        );
    }

    public void resetDatabase(boolean cleanUpNotInitializedBusinessServicesAsWell) {
        databasePopulator.resetDatabase();
        testDatas.forEach(eachSet -> eachSet.getServices().forEach(
                eachService -> businessServiceDao.delete(eachService)
        ));
        if (cleanUpNotInitializedBusinessServicesAsWell) {
            businessServiceDao.findAll().forEach(eachBs -> businessServiceDao.delete(eachBs));
        }
    }

    // Returns the number of expected business services. The total is <initially created ones> + <createdCount>
    public int expectedBsCount(int createdCount) {
        int sum = testDatas.stream()
                .mapToInt(BsmTestData::getServiceCount)
                .sum();
        return sum + createdCount;
    }
}
