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
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.junit.Test;

public class XMLFileCheckerTest {

    @Test
    public void shouldSucceedOnFileWithCorrectSyntax() throws IOException {
        File file = saveXmltoFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?><element></element>");
        XMLFileChecker.checkFile(file.toPath()).forSyntax();
    }

    @Test(expected = ConfigCheckValidationException.class)
    public void shouldFailOnFileWithIncorrectSyntax() throws IOException {
        File file = saveXmltoFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?><elementThatIsNotClosed>");
        XMLFileChecker.checkFile(file.toPath()).forSyntax();
    }


    private File saveXmltoFile(String xml) throws IOException{
        File file = File.createTempFile(this.getClass().getSimpleName(), ".xml");
        Writer writer = new FileWriter(file);
        writer.write(xml);
        writer.close();
        return file;
    }

}