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
package org.opennms.features.config.dao.impl;

import com.google.common.io.Resources;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.features.config.dao.api.ConfigItem;
import org.opennms.features.config.dao.impl.util.ConfigSwaggerConverter;
import org.opennms.features.config.dao.impl.util.XsdHelper;
import org.opennms.features.config.dao.impl.util.XsdModelConverter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class XsdModelConverterTest {

    @Test
    public void testData() throws IOException {
        String xsdStr = Resources.toString(XsdHelper.getSchemaPath("provisiond-configuration.xsd"), StandardCharsets.UTF_8);
        XsdModelConverter xsdConverter = new XsdModelConverter(xsdStr);
        ConfigItem item = xsdConverter.convert("provisiond-configuration");

        Assert.assertEquals("Should have enough children items", 7, item.getChildren().size());
        Assert.assertEquals("Should have correct schema ref.",
                "{http://xmlns.opennms.org/xsd/config/provisiond-configuration}provisiond-configuration", item.getSchemaRef());
        Assert.assertNotNull("Should have documentation", item.getDocumentation());
    }

    @Test
    public void testEnum() throws IOException {
        String xsdStr = Resources.toString(XsdHelper.getSchemaPath("service-configuration.xsd"), StandardCharsets.UTF_8);
        XsdModelConverter xsdConverter = new XsdModelConverter(xsdStr);
        ConfigItem item = xsdConverter.convert("service-configuration");

        ConfigItem serviceItem = item.getChildren().get(0).getChildren().get(0);
        Assert.assertEquals("Should have enough children items", 5, serviceItem.getChildren().size());
        Optional<ConfigItem> invokeItem = serviceItem.getChild("invoke");
        Assert.assertArrayEquals(new String[]{"start", "stop", "status"},
                invokeItem.get().getChildren().get(0).getChild("at").get().getEnumValues().toArray());
    }

    @Test
    public void testExclusive() throws IOException {
        String xsdStr = Resources.toString(XsdHelper.getSchemaPath("fake-vacuumd-configuration.xsd"), StandardCharsets.UTF_8);
        XsdModelConverter xsdConverter = new XsdModelConverter(xsdStr);
        ConfigItem rootItem = xsdConverter.convert("VacuumdConfiguration");

        List<ConfigItem> itemList = rootItem.getChildren().stream().filter(item -> "period-exc".equals(item.getName()))
                .collect(Collectors.toList());
        Assert.assertEquals("Should have one period-exc", 1, itemList.size());
        ConfigItem excludeItem = itemList.get(0);
        Assert.assertEquals("Should have correct min", 1L, (long) excludeItem.getMin());
        Assert.assertTrue("Should exclude min", excludeItem.isMinExclusive());
        Assert.assertEquals("Should have correct max", 100L, (long) excludeItem.getMax());
        Assert.assertTrue("Should exclude max", excludeItem.isMaxExclusive());
    }
}
