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

package org.opennms.features.geocoder.mapquest;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.features.geocoder.GeocoderConfigurationException;

public class MapquestConfigurationTest {

    @Test(expected = GeocoderConfigurationException.class)
    public void expectEmptyConfigurationToFail() {
        final MapquestConfiguration config = new MapquestConfiguration();
        config.validate(); // should fail
    }

    @Test(expected = GeocoderConfigurationException.class)
    public void expectMissingUrlTemplateToFail() {
        final MapquestConfiguration config = new MapquestConfiguration();
        config.setApiKey("XXX");
        config.validate(); // should fail
    }

    @Test(expected = GeocoderConfigurationException.class)
    public void expectErrorOnInvalidUrl() {
        final MapquestConfiguration config = new MapquestConfiguration();
        config.setApiKey("XXX");
        config.setUrlTemplate("XXX");
        config.validate(); // should fail
    }

    @Test
    public void expectFullyConfiguredToPass() {
        final MapquestConfiguration config = new MapquestConfiguration();
        config.setApiKey("XXX");
        config.setUrlTemplate("http://mapquest.opennms.org");
        config.validate(); // should pass
    }

    @Test
    public void verifyConfigurationCreation() {
        final MapquestConfiguration configuration = new MapquestConfiguration();
        configuration.setUseSystemProxy(true);
        configuration.setUrlTemplate("http://mapquest.opennms.org");
        configuration.setApiKey("apiKey");

        final Map<String, Object> properties = configuration.asMap();
        Assert.assertEquals(true, properties.get(MapquestConfiguration.USE_SYSTEM_PROXY_KEY));
        Assert.assertEquals("http://mapquest.opennms.org", properties.get(MapquestConfiguration.URL_KEY));
        Assert.assertEquals("apiKey", properties.get(MapquestConfiguration.API_KEY_KEY));

        final MapquestConfiguration clonedConfiguration = MapquestConfiguration.fromMap(properties);
        Assert.assertEquals(configuration, clonedConfiguration);
    }
}
