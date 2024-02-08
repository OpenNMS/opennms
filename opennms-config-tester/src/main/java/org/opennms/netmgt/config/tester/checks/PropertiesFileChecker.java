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
package org.opennms.netmgt.config.tester.checks;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;


public class PropertiesFileChecker {

    private Path file;
    private Properties properties;

    private PropertiesFileChecker(Path file) {
        this.file = file;
    }

    public static PropertiesFileChecker checkFile(Path file) {
        return new PropertiesFileChecker(file);
    }

    public void forSyntax() {
        loadProperties();
    }

    private void loadProperties() {
        properties = new Properties();

        try {
            properties.load(new FileReader(file.toFile()));
        } catch (IOException | IllegalArgumentException e) {
            throw new ConfigCheckValidationException(e);
        }
    }
}
