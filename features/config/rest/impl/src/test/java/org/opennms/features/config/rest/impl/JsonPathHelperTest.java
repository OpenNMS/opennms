/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.features.config.rest.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.jayway.jsonpath.JsonPath;

/**
 *
 * @author dmitri
 */
public class JsonPathHelperTest {


    private static final int OBJ_NUMBER1 = 123;
    private static final int OBJ_NUMBER2 = 456;
    private static final int SOME_NUMBER = 789;
    private static final int EVIL = 666;
    private static final int LUCKY = 777;
    private static final String SOME_TEXT1 ="someText1";
    private static final String SOME_TEXT_ELEMENT_WITH_QUOTE ="\"some text in array\"";
    private static final String EVIL_TEXT = "evil text";
    private static final String LUCKY_TEXT = "lucky text";

    private static final String OBJECT1 = "{\"objNumber\":" + OBJ_NUMBER1 + ",\"objText\":\""+SOME_TEXT1+"\"}";
    private static final String OBJECT2 = "{\"objNumber\":" + OBJ_NUMBER2 + ",\"objText\":\"someText2\"}";
    private static final String OBJECT3 = "{\"objNumber\":" + EVIL + ",\"objText\":\"" + EVIL_TEXT + "\"}";
    private static final String ARRAY = "[123456," + OBJECT1 + "," + OBJECT2 + "," + SOME_TEXT_ELEMENT_WITH_QUOTE + "]";
    private static final String DATA = "{\"numProperty\":" + SOME_NUMBER + ",\"textProperty\":\"barFoo\",\"array\":"
            + ARRAY + ",\"nestedObject\":" + OBJECT3 + ",\"anotherText\":\"anotherText\"}";
    private static final String MULTIPLE_PATH = "$..*";


    @Test
    public void testGetWhenOk() {
        assertEquals(Integer.toString(OBJ_NUMBER1), JsonPathHelper.get(DATA, "$.array[1].objNumber"));
        assertEquals("[" + Integer.toString(OBJ_NUMBER1) + "]", JsonPathHelper.get(DATA, "$.array[?(@.objText=='" + SOME_TEXT1 + "')].objNumber"));
        assertEquals(ARRAY, JsonPathHelper.get(DATA, "$.array"));
    }

    @Test
    public void testGetWhenMultiple() {
        //No exception on reading multiple nodes - not bug but feature
        String modified = JsonPathHelper.get(DATA, MULTIPLE_PATH);
        assertNotEquals("must be modified", DATA, modified);
        assertNotEquals("must be longer", DATA.length(), modified.length());
        assertTrue("must contain old data", modified.contains(ARRAY));
        assertTrue("must contain old data", modified.contains(OBJECT3));
        assertTrue("must contain several time", StringUtils.countMatches(modified,EVIL_TEXT) > 1);
    }

    @Test(expected = Exception.class)
    public void testGetWhenWrongJson() {
        JsonPathHelper.get("some eerors" + DATA, "$.numProperty");
    }

    @Test(expected = Exception.class)
    public void testGetWhenNotExistingPath() {
        JsonPathHelper.get(DATA, "$.noSuchProperty");
    }

    @Test
    public void testUpdateWhenOk() {
        //replace text
        assertEquals(
                DATA.replace(EVIL_TEXT, LUCKY_TEXT),
                JsonPathHelper.update(DATA, "$.nestedObject.objText", "\"" + LUCKY_TEXT + "\"")
        );

        //replace number
        assertEquals(
                DATA.replace(Integer.toString(EVIL),Integer.toString(LUCKY)),
                JsonPathHelper.update(DATA, "$.nestedObject.objNumber",Integer.toString(LUCKY)));

        //replace Objects
        String placeholder = "PlaceHolder";
        String expected = DATA.replace(OBJECT1, placeholder).replace(OBJECT2, OBJECT1).replace(placeholder, OBJECT2);
        final String step1 = JsonPathHelper.update(DATA, "$.array[1]", OBJECT2);
        final String step2 = JsonPathHelper.update(step1, "$.array[2]", OBJECT1);

        assertEquals(expected,step2);
    }

    @Test
    public void testInsertOrUpdateWhenOkAndPathExists() {
        assertEquals(
                DATA.replace(EVIL_TEXT, LUCKY_TEXT),
                JsonPathHelper.insertOrUpdateNode(DATA, "$.nestedObject", "objText", "\"" + LUCKY_TEXT + "\"")
       );
    }

    @Test
    public void testInsertOrUpdateWhenOkAndPathDoesNotExists() {
        String wiredKey = "a'd\\c\\d\"e[f]g";
        String wiredPath = "$.nestedObject.['" + wiredKey.replace("\\", "\\\\").replace("'", "\\'") + "']";

        String jsonWithNewElement = JsonPathHelper.insertOrUpdateNode(DATA, "$.nestedObject", wiredKey, "\"" + LUCKY_TEXT + "\"");

        assertEquals(
                "Content stored successfully",
                LUCKY_TEXT,
                JsonPath.read(jsonWithNewElement, wiredPath)
        );

        assertEquals(
                "No errors while deleting and we get the original JSON",
                JsonPath.parse(jsonWithNewElement).delete(wiredPath).jsonString(),
                DATA
        );
    }

    @Test(expected = Exception.class)
    public void testInsertOrUpdateWhenWrongJson() {
        JsonPathHelper.get("some eerors" + DATA, "$.numProperty");
    }

    @Test(expected = Exception.class)
    public void testInsertOrUpdateWhenNotExistingPath() {
        JsonPathHelper.insertOrUpdateNode(DATA, "$.noSuchProperty", "someName", "someContent");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateWhenMultiple() {
        JsonPathHelper.insertOrUpdateNode(DATA, "$.array[1,2]", "someName", OBJECT2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendWhenMultiple() {
        JsonPathHelper.append(DATA, "$.array[1,2]", OBJECT2);
    }

    @Test
    public void testAppendWhenOk() {
        //append text
        assertEquals(
                DATA.replace(SOME_TEXT_ELEMENT_WITH_QUOTE, SOME_TEXT_ELEMENT_WITH_QUOTE + "," + SOME_TEXT_ELEMENT_WITH_QUOTE),
                JsonPathHelper.append(DATA, "$.array", SOME_TEXT_ELEMENT_WITH_QUOTE)
        );

        //append number
        assertEquals(
                DATA.replace(SOME_TEXT_ELEMENT_WITH_QUOTE, SOME_TEXT_ELEMENT_WITH_QUOTE + "," + LUCKY),
                JsonPathHelper.append(DATA, "$.array", Long.toString(LUCKY))
        );

        //append object
        assertEquals(
                DATA.replace(SOME_TEXT_ELEMENT_WITH_QUOTE, SOME_TEXT_ELEMENT_WITH_QUOTE + "," + OBJECT3),
                JsonPathHelper.append(DATA, "$.array", OBJECT3)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteWhenMultiple() {
        JsonPathHelper.delete(DATA, "$.array[1,2]");
    }

    @Test
    public void testDeleteWhenOk() {
        String expected = DATA.replace("," + OBJECT1, "");
        String result = JsonPathHelper.delete(DATA, "$.array[1]");
        assertEquals(expected, result);
    }

}
