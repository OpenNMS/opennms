/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opennms.netmgt.config.service;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

public class ServiceCatalogParityTest {
    private static final Path REPO_ROOT = Path.of("..").toAbsolutePath().normalize();
    private static final Path DEFAULT_CATALOG = REPO_ROOT.resolve(
            "opennms-config-model/src/main/resources/defaults/service-configuration.xml");
    private static final Pattern SERVICE_NAME = Pattern.compile(
            "<service(?:\\s+[^>]*)?>\\s*<name>([^<]+)</name>");

    private static final List<Path> SHIPPED_CONFIGURATIONS = List.of(
            REPO_ROOT.resolve("opennms-base-assembly/src/main/filtered/etc/service-configuration.xml"),
            REPO_ROOT.resolve("opennms-base-assembly/src/main/filtered/etc/examples/service-configuration.xml.default"),
            REPO_ROOT.resolve("opennms-container/core/container-fs/confd/templates/service-configuration.xml.tmpl"),
            REPO_ROOT.resolve("smoke-test/src/main/resources/opennms-overlay/etc/service-configuration.xml"));

    @Test
    public void shippedConfigurationsMatchDefaultServiceNamesAndOrder() throws IOException {
        final List<String> defaultServiceNames = serviceNames(DEFAULT_CATALOG);

        for (Path shippedConfiguration : SHIPPED_CONFIGURATIONS) {
            assertEquals("Service catalog differs in " + shippedConfiguration,
                    defaultServiceNames, serviceNames(shippedConfiguration));
        }
    }

    private static List<String> serviceNames(final Path configuration) throws IOException {
        final var matcher = SERVICE_NAME.matcher(Files.readString(configuration));
        final List<String> names = new ArrayList<>();
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names;
    }
}
