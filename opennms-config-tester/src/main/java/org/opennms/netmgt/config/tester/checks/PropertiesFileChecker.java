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
