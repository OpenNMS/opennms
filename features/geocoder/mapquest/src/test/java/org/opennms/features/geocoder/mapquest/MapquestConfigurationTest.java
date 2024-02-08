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
