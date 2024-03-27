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
