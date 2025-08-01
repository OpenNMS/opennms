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
package org.opennms.netmgt.alarmd.drools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.opennms.core.utils.ConfigFileConstants;

public class AlarmdTestUtil {
    public static File enableDisabledRules() throws IOException {
        final File srcFile = Paths.get(ConfigFileConstants.getHome(), "etc", "alarmd", "drools-rules.d", "alarmd.drl").toFile();
        final Path dstDirectory = Paths.get("target","test", "drools-rules.d");

        dstDirectory.toFile().mkdirs();

        final File dstFile = Paths.get(dstDirectory.toString(), "alarmd.drl").toFile();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(srcFile));
             BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(dstFile))) {
            while (bufferedReader.ready()) {
                String line = bufferedReader.readLine();
                if ("enabled false".equals(line.trim())) {
                    line = line.replace("enabled false", "enabled true");
                }
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }
        }
        return dstDirectory.toFile();
    }
}
