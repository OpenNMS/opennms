/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.rest;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.preemptive;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.dao.hibernate.AssetRecordDaoHibernate;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;
import org.opennms.smoketest.utils.HibernateDaoFactory;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

public abstract class AbstractNodeRestServiceTest extends OpenNMSSeleniumTestCase {

    private final String endpoint;

    @Before
    public void setUp() {
        RestAssured.baseURI = getBaseUrl();
        RestAssured.port = getServerHttpPort();
        RestAssured.basePath = this.endpoint;
        RestAssured.authentication = preemptive().basic(BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD);
    }

    @After
    public void tearDown() {
        given().delete("SmokeTests:TestMachine1");

        RestAssured.reset();
    }

    public AbstractNodeRestServiceTest(String endpoint) {
        this.endpoint = endpoint;
    }

    // See NMS-9855
    @Test
    public void verifyCreationWithAssetRecord() {
        final String node = "<node type=\"A\" label=\"TestMachine1\" foreignSource=\"SmokeTests\" foreignId=\"TestMachine1\">" +
                "<assetRecord>" +
                "<description>Right here, right now</description>" +
                "</assetRecord>" +
                "<labelSource>H</labelSource>" +
                "<sysContact>The Owner</sysContact>" +
                "<sysDescription>" +
                "Darwin TestMachine 9.4.0 Darwin Kernel Version 9.4.0: Mon Jun  9 19:30:53 PDT 2008; root:xnu-1228.5.20~1/RELEASE_I386 i386" +
                "</sysDescription>" +
                "<sysLocation>DevJam</sysLocation>" +
                "<sysName>TestMachine1</sysName>" +
                "<sysObjectId>.1.3.6.1.4.1.8072.3.2.255</sysObjectId>" +
                "<createTime>2011-09-24T07:12:46.421-04:00</createTime>" +
                "<lastCapsdPoll>2011-09-24T07:12:46.421-04:00</lastCapsdPoll>" +
                "</node>";
        given().body(node).contentType(ContentType.XML).post()
                .then().assertThat()
                .statusCode(201);

        // Verify that only one asset record has been created
        final HibernateDaoFactory daoFactory = new HibernateDaoFactory(getPostgresService());
        final AssetRecordDao dao = daoFactory.getDao(AssetRecordDaoHibernate.class);
        assertThat(dao.countAll(), is(1));

        // Ensure we can get nodes with asset records attached
        given().get()
                .then()
                .log().all()
                .and().assertThat()
                .statusCode(200);
    }
}
