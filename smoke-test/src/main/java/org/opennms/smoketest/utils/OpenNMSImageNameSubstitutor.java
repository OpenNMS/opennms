/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.utils;

import org.junit.Assert;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.ImageNameSubstitutor;

public class OpenNMSImageNameSubstitutor extends ImageNameSubstitutor {
    @Override
    public DockerImageName apply(DockerImageName original) {
        // Use the multi-arch (ARM64, ARM/v7, and AMD64) Seleniarm images for
        // Selenium 4.x and higher.
        // Ref: https://github.com/seleniumhq-community/docker-seleniarm/releases
        // Note: "-debug" images are used to get VNC recording for 3.x selenium so
        // use a startsWith match on the name.
        if (original.getUnversionedPart().startsWith("selenium/standalone-firefox")
                 && original.getVersionPart().startsWith("4.")) {
            return DockerImageName.parse("seleniarm/standalone-firefox:" + getSeleniarmVersion());
        } else if (original.getUnversionedPart().startsWith("selenium/standalone-chrome")
                 && original.getVersionPart().startsWith("4.")) {
            return DockerImageName.parse("seleniarm/standalone-chromium:" +  getSeleniarmVersion());
        } else {
            return original;
        }
    }

    private String getSeleniarmVersion() {
        var version = System.getProperty("seleniarm.version");
        Assert.assertNotNull("The system property 'seleniarm.version' needs to be set to do browser testing. Make sure it is set in pom.xml.", version);
        return version;
    }

    @Override
    protected String getDescription() {
        return "OpenNMS image name substitutor";
    }
}
