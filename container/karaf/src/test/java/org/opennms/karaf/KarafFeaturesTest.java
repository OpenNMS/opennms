/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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
