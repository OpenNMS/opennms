/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

/**
 *
 * @author dmitri
 */
public class JsonPathHelperTest {


    private static final int OBJNUMBER1 = 123;
    private static final int OBJNUMBER2 = 456;
    private static final int SOMENUMBER = 789;
    private static final int EVIL = 666;
    private static final int LUCKY = 777;
    private static final String SOMETEXT1 ="someText1";
    private static final String SOME_TEXT_ELEMENT_WITH_QUOTE ="\"some text in array\"";
    private static final String EVIL_TEXT = "evil text";
    private static final String LUCKY_TEXT = "lucky text";

    private static final String OBJECT1 = "{\"objNumber\":" + OBJNUMBER1 + ",\"objText\":\""+SOMETEXT1+"\"}";
    private static final String OBJECT2 = "{\"objNumber\":" + OBJNUMBER2 + ",\"objText\":\"someText2\"}";
    private static final String OBJECT3 = "{\"objNumber\":" + EVIL + ",\"objText\":\"" + EVIL_TEXT + "\"}";
    private static final String ARRAY = "[123456," + OBJECT1 + "," + OBJECT2 + "," + SOME_TEXT_ELEMENT_WITH_QUOTE + "]";
    private static final String DATA = "{\"numProperty\":" + SOMENUMBER + ",\"textProperty\":\"barFoo\",\"array\":"
            + ARRAY + ",\"nestedObject\":" + OBJECT3 + ",\"anotherText\":\"someText1\"}";
    private static final String MULTIPLE_PATH = "$..*";


    @Test
    public void testGet_Ok() {
        assertEquals(Integer.toString(OBJNUMBER1), JsonPathHelper.get(DATA, "$.array[1].objNumber"));
        assertEquals("[" + Integer.toString(OBJNUMBER1) + "]", JsonPathHelper.get(DATA, "$.array[?(@.objText=='" + SOMETEXT1 + "')].objNumber"));
        assertEquals(ARRAY, JsonPathHelper.get(DATA, "$.array"));
    }

    @Test
    public void testGet_Multiple() {
        //No exception on reading multiple nodes - not bug but feature
        String modified = JsonPathHelper.get(DATA, MULTIPLE_PATH);
        assertNotEquals("must be modified", DATA, modified);
        assertNotEquals("must be longer", DATA.length(), modified.length());
        assertTrue("must sontain old data", modified.contains(ARRAY));
        assertTrue("must contain old data", modified.contains(OBJECT3));
        assertTrue("must contain several time", StringUtils.countMatches(modified,EVIL_TEXT) > 1);
    }

    @Test(expected = Exception.class)
    public void testGet_wrongJson() {
        JsonPathHelper.get("some eerors" + DATA, "$.numProperty");
    }

    @Test(expected = Exception.class)
    public void testGet_notExistingPath() {
        JsonPathHelper.get(DATA, "$.noSuchProperty");
    }

    @Test
    public void testUpdate_ok() {
        //replace text
        assertEquals(
                DATA.replace(EVIL_TEXT,LUCKY_TEXT),
                JsonPathHelper.update(DATA,"$.nestedObject.objText","\"" + LUCKY_TEXT + "\"")
        );

        //replace number
        assertEquals(
                DATA.replace(Integer.toString(EVIL),Integer.toString(LUCKY)),
                JsonPathHelper.update(DATA,"$.nestedObject.objNumber",Integer.toString(LUCKY)));

        //replace Objects
        String placeholder = "PlaceHolder";
        String expected = DATA.replace(OBJECT1, placeholder).replace(OBJECT2, OBJECT1).replace(placeholder, OBJECT2);
        final String step1 = JsonPathHelper.update(DATA, "$.array[1]", OBJECT2);
        final String step2 = JsonPathHelper.update(step1, "$.array[2]", OBJECT1);

        assertEquals(expected,step2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdate_Multiple() {
        JsonPathHelper.update(DATA, "$.array[1,2]", OBJECT2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppend_Multiple() {
        JsonPathHelper.append(DATA, "$.array[1,2]", OBJECT2);
    }

    @Test
    public void testAppend_ok() {
        //append text
        assertEquals(
                DATA.replace(SOME_TEXT_ELEMENT_WITH_QUOTE, SOME_TEXT_ELEMENT_WITH_QUOTE + "," + SOME_TEXT_ELEMENT_WITH_QUOTE),
                JsonPathHelper.append(DATA,"$.array",SOME_TEXT_ELEMENT_WITH_QUOTE)
        );

        //append number
        assertEquals(
                DATA.replace(SOME_TEXT_ELEMENT_WITH_QUOTE, SOME_TEXT_ELEMENT_WITH_QUOTE + "," + LUCKY),
                JsonPathHelper.append(DATA,"$.array",Long.toString(LUCKY))
        );

        //append object
        assertEquals(
                DATA.replace(SOME_TEXT_ELEMENT_WITH_QUOTE, SOME_TEXT_ELEMENT_WITH_QUOTE + "," + OBJECT3),
                JsonPathHelper.append(DATA,"$.array",OBJECT3)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDelete_Multiple() {
        JsonPathHelper.delete(DATA, "$.array[1,2]");
    }

    @Test
    public void testDelete_ok() {
        String expected = DATA.replace("," + OBJECT1, "");
        String result = JsonPathHelper.delete(DATA, "$.array[1]");
        assertEquals(expected,result);
    }

}
