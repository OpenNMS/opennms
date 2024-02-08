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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import org.junit.Test;

import com.google.common.collect.Lists;

public class PropertiesFileCheckerTest {


    @Test
    public void shouldSucceedOnFileWithCorrectSyntax() throws IOException {
        Properties properties = new Properties();
        properties.setProperty("key", "abc");
        testProperty(properties);
    }

    @Test(expected = ConfigCheckValidationException.class)
    public void shouldFailOnFileWithIncorrectSyntax() throws IOException {
        // we need to create the properties file manually to be able to make a corrupt one:
        String properties="key=\\u005";
        File file = createPropertiesFile();
        FileWriter writer = new FileWriter(file);
        writer.write(properties);
        writer.close();
        PropertiesFileChecker.checkFile(file.toPath()).forSyntax();
    }

    private void testProperty(Properties properties) throws IOException {
        File file = savePropertiesToFile(properties);
        PropertiesFileChecker.checkFile(file.toPath()).forSyntax();
    }

    private File savePropertiesToFile(Properties properties) throws IOException{
        File file = createPropertiesFile();
        properties.store(new FileOutputStream(file), "");
        return file;
    }

    private File createPropertiesFile() throws IOException{
        return File.createTempFile(this.getClass().getSimpleName(), ".properties");
    }
}