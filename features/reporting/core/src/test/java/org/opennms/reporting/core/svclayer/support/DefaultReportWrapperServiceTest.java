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