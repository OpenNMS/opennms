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

package org.opennms.jmxconfiggenerator.helper;

import org.junit.*;

/**
 *
 * @author Markus Neumann <markus@opennms.com>
 */
public class NameToolsTest {

    public NameToolsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        //The loaded extermal dictionary will be the internal dictionary if the application is packaged as a jar file
        NameTools.loadExtermalDictionary("src/main/resources/dictionary.properties");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testTrimByDictionary() {
        Assert.assertEquals("Blo", NameTools.trimByDictionary("Bloom"));
        Assert.assertEquals("Tok", NameTools.trimByDictionary("Token"));

        Assert.assertEquals("CommitVirtMemSize", NameTools.trimByDictionary("CommittedVirtualMemorySize"));
        Assert.assertEquals("AvgCompRatio" , NameTools.trimByDictionary("AverageCompressionRatio"));
        Assert.assertEquals("AllIdntToknzCnt" , NameTools.trimByDictionary("AllIdentityTokenizedCount"));
    }

    @Test
    public void testTrimByCamelCase() {
        Assert.assertEquals("CommitteVirtMemSize", NameTools.trimByCamelCase("CommittedVirtMemSize", 19));
        Assert.assertEquals("CommiVirtuMemorSize", NameTools.trimByCamelCase("CommittedVirtualMemorySize", 19));
        Assert.assertEquals("AllIdentTokeniCount", NameTools.trimByCamelCase("AllIdentityTokenizedCount", 19));
    }
}
