/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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


package org.opennms.web.rest.v1;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;


@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
public class AssetRecordResourceIT extends AbstractSpringJerseyRestTestCase {


    @Autowired
    private TransactionTemplate template;

    @Autowired
    private DatabasePopulator databasePopulator;

    private Date yesterday = new Date(System.currentTimeMillis()-(1000*60*24));

    @Override
    protected void afterServletStart() {
        MockLogAppender.setupLogging(true, "INFO");
        template.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                databasePopulator.populateDatabase();
                databasePopulator.getNode1().getAssetRecord().setLastModifiedBy("oldUser");
                databasePopulator.getNode1().getAssetRecord().setLastModifiedDate(yesterday);
                databasePopulator.getNodeDao().flush();
            }
        });
    }


    @Test
    @JUnitTemporaryDatabase
    public void shouldUpdateLastModifiedWithCurrentUser() throws Exception {

        String username="admin";
        Map<String, String> params = new HashMap<>();
        params.put("operatingSystem", "Linux");
        params.put("category", "Unspecified");
        params.put("password", "admin");
        params.put("description", "");
        params.put("username", "admin");
        params.put("manufacturer", "");
        params.put("autoenable", "");
        params.put("comment", "");
        setUser(username, new String[]{});
        sendRequest(PUT, "/nodes/1/assetRecord", params, 204);

        assertEquals(username, databasePopulator.getNode1().getAssetRecord().getLastModifiedBy());
        assertTrue("lastModifiedDate should be within the last second"
                , databasePopulator.getNode1().getAssetRecord().getLastModifiedDate().after(new Date(System.currentTimeMillis()-1000)));
    }

}

