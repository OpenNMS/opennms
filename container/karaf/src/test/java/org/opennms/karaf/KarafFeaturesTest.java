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
package org.opennms.karaf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import org.junit.Test;

public class KarafFeaturesTest {

    @Test
    public void featuresBootOpennmsKarafHealthLast() throws IOException {
        var karafFeaturesCfgFile = Path.of("src/main/filtered-resources/etc/org.apache.karaf.features.cfg");
        assertTrue("karaf features cfg file should exist: " + karafFeaturesCfgFile, karafFeaturesCfgFile.toFile().exists());

        var properties = new Properties();
        try (var cfg = new FileInputStream(karafFeaturesCfgFile.toFile());) {
            properties.load(cfg);
        }

        var featuresBoot = properties.getProperty("featuresBoot");
        assertNotNull("featuresBoot property should exist in " + karafFeaturesCfgFile, featuresBoot);

        var featuresBootSplit = featuresBoot.split("\\s*,\\s*");
        var lastFeature = featuresBootSplit[featuresBootSplit.length - 1];
        assertEquals("last feature should be opennms-karaf-health so we can properly detect if all of the "
                + "features have started and are healthy",
                "opennms-karaf-health",
                lastFeature);
    }
}
