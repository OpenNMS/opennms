/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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