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

package org.opennms.features.config.convert;

import org.hamcrest.MatcherAssert;
import org.hamcrest.beans.SamePropertyValuesAs;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opennms.features.config.dao.api.ConfigConverter;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.impl.util.XsdHelper;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
abstract public class CmConfigTest<T> {
    private Class<?> objectClass;
    private T simpleObject;
    private String simpleXml;
    protected ConfigDefinition configDefinition;

    public CmConfigTest(final T sampleObject, final String sampleXml, final String schemaFile, String topLevelElement) {
        this.objectClass = sampleObject.getClass();
        this.simpleObject = sampleObject;
        this.simpleXml = sampleXml;
        this.configDefinition = XsdHelper.buildConfigDefinition(this.getClass().getName(), schemaFile, topLevelElement, "/cm", false);
    }

    private String getJsonStr() throws IOException {
        ConfigConverter converter = XsdHelper.getConverter(configDefinition);
        return converter.xmlToJson(simpleXml);
    }

    @Test
    public void xmlToObjectWithCompare() throws IOException {
        Object object = ConfigConvertUtil.jsonToObject(getJsonStr(), objectClass);
        // try match with equals if fail back to use SamePropertyValuesAs
        if(!simpleObject.equals(object)){
            MatcherAssert.assertThat(simpleObject, new SamePropertyValuesAs(object));
        }
    }

    @Test
    public void jsonValidation() throws IOException {
        // It should not throw exception ValidationException
        configDefinition.validate(getJsonStr());
    }

    @Test
    public void xmlToJsonWithCompare() throws IOException {
        Object object = ConfigConvertUtil.jsonToObject(getJsonStr(), objectClass);
        String jsonFromXml = ConfigConvertUtil.objectToJson(object);
        String jsonFromObject = ConfigConvertUtil.objectToJson(this.simpleObject);
        JSONAssert.assertEquals(jsonFromXml, jsonFromObject, true);
    }
}