/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.namecutter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.*;

/**
 *
 * @author Markus Neumann <markus@opennms.com>
 */
public class NameCutterTest {

    private static Map<String, String> dictionary = new HashMap<String, String>();

    private NameCutter nameCutter = new NameCutter();

    @BeforeClass
    public static void setUpClass() throws Exception {
        Properties properties = new Properties();
        BufferedInputStream stream = new BufferedInputStream(new FileInputStream(new File("src/test/resources/dictionary.properties")));
        properties.load(stream);
        stream.close();
        for (Object key : properties.keySet()) {
            dictionary.put(key.toString(), properties.get(key).toString());
            System.out.println(key.toString() + "\t" + properties.get(key).toString());
        }
    }

    @Before
    public void setUp() {
        nameCutter = new NameCutter();
        nameCutter.setDictionary(dictionary);
    }

    @Test
    public void testTrimByDictionary() {
        Assert.assertEquals("Blo", nameCutter.trimByDictionary("Bloom"));
        Assert.assertEquals("Tok", nameCutter.trimByDictionary("Token"));

        Assert.assertEquals("CommitVirtMemSize", nameCutter.trimByDictionary("CommittedVirtualMemorySize"));
        Assert.assertEquals("AvgCompRatio" , nameCutter.trimByDictionary("AverageCompressionRatio"));
        Assert.assertEquals("AllIdntToknzCnt" , nameCutter.trimByDictionary("AllIdentityTokenizedCount"));
    }

    @Test
    public void testTrimByCamelCase() {
        Assert.assertEquals("CommitteVirtMemSize", nameCutter.trimByCamelCase("CommittedVirtMemSize", 19));
        Assert.assertEquals("CommiVirtuMemorSize", nameCutter.trimByCamelCase("CommittedVirtualMemorySize", 19));
        Assert.assertEquals("AllIdentTokeniCount", nameCutter.trimByCamelCase("AllIdentityTokenizedCount", 19));
    }
}
