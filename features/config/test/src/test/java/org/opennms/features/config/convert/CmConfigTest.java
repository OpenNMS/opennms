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