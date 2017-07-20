/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.junit;

import org.junit.Before;
import org.junit.ClassRule;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;
import org.opennms.smoketest.TestEnvironmentSetup;
import org.opennms.test.system.api.NewTestEnvironment;

import io.restassured.RestAssured;

public class RestTestCase {

    @ClassRule
    public static TestEnvironmentSetup testEnvironmentSetup = TestEnvironmentSetup.DEFAULTS;

    public String getBaseUrl() {
        return "http://" + this.getServerAddress();
    }

    private String getServerAddress() {
        return testEnvironmentSetup.getServiceAddress(NewTestEnvironment.ContainerAlias.OPENNMS, 8980).getAddress().getHostAddress();
    }

    private int getServerHttpPort() {
        return testEnvironmentSetup.getServiceAddress(NewTestEnvironment.ContainerAlias.OPENNMS,8980).getPort();
    }

    @Before
    public void before() {
        System.out.println("before");
        RestAssured.baseURI = getBaseUrl();
        RestAssured.port = getServerHttpPort();
        RestAssured.basePath = "/opennms";
        RestAssured.authentication = RestAssured.preemptive().basic(OpenNMSSeleniumTestCase.BASIC_AUTH_USERNAME, OpenNMSSeleniumTestCase.BASIC_AUTH_PASSWORD);
    }
}
