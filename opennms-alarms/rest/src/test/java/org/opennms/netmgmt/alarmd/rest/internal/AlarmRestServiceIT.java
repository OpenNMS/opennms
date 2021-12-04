/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2015 The OpenNMS Group, Inc.
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
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgmt.alarmd.rest.internal;

import org.junit.Test;

// cucumber?
import org.hamcrest.Matchers;
import io.restassured.RestAssured;

public class AlarmRestServiceIT {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AlarmRestServiceIT.class);

    @Test
    public void testAlarmList() throws Exception {
        log.info("Testing alarm list rest endpoint");

        String baseUrl = System.getProperty("alarm-service-base.url"); // set in pom's <groupId>org.apache.maven.plugins</groupId>

        System.out.println("Starting Testing alarm list rest endpoints off of "+baseUrl);

        RestAssured.get(baseUrl + "/cxf/alarmservice/alarms/list")
                .then()
                .assertThat()
                .statusCode(200)
                .body(Matchers.equalTo("")) // empty array
        ;

    }



}