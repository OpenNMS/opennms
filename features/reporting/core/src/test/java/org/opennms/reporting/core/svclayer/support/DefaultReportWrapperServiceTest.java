/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
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

package org.opennms.reporting.core.svclayer.support;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.opennms.reporting.core.svclayer.support.DefaultReportWrapperService.substituteUrl;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.opennms.api.reporting.ReportFormat;
import org.opennms.reporting.core.DeliveryOptions;

import com.google.common.collect.Maps;

public class DefaultReportWrapperServiceTest {

    @Test
    public void verifySubstituteUrl() {
        assertThat(substituteUrl("http://localhost:8000", Maps.newHashMap()), is("http://localhost:8000"));

        final DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.setFormat(ReportFormat.CSV);
        deliveryOptions.setInstanceId("opennms-report-1");

        final String inputUrl = "http://localhost:8000/:instanceId/:format";
        assertThat(substituteUrl(inputUrl, deliveryOptions, Maps.newHashMap()), is("http://localhost:8000/opennms-report-1/CSV"));

        deliveryOptions.setInstanceId("opennms report 1");
        assertThat(substituteUrl(inputUrl, deliveryOptions, Maps.newHashMap()), is("http://localhost:8000/opennms+report+1/CSV"));
    }

    @Test
    public void verifySubstituteUrlWithParamters() {
        final String inputUrl = "http://localhost:8000/endpoint/:instanceId/:format?author=:parameter_author&option=:parameter_option&format=:format";
        final DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.setFormat(ReportFormat.PDF);
        deliveryOptions.setInstanceId("report");

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("author", "ulf");
        parameters.put("option", true);

        assertThat(substituteUrl(inputUrl, deliveryOptions, parameters), Matchers.is("http://localhost:8000/endpoint/report/PDF?author=ulf&option=true&format=PDF"));
    }

}