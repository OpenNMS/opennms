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
package org.opennms.smoketest.rest;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.preemptive;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper.BASIC_AUTH_PASSWORD;
import static org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper.BASIC_AUTH_USERNAME;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.dao.hibernate.AssetRecordDaoHibernate;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.utils.HibernateDaoFactory;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

public abstract class AbstractNodeRestServiceTest {

    private final String endpoint;

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.MINIMAL;

    @Before
    public void setUp() {
        RestAssured.baseURI = stack.opennms().getBaseUrlExternal().toString();
        RestAssured.port = stack.opennms().getWebPort();
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
        final HibernateDaoFactory daoFactory = stack.postgres().getDaoFactory();
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
