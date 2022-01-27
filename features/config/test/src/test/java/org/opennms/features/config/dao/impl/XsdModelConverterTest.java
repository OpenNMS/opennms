/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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
