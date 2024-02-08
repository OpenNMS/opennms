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
package org.opennms.features.config.service.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.opennms.features.config.exception.ConfigConversionException;

public class ConfigConvertUtil {
    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new Jdk8Module())
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .setPropertyNamingStrategy(new PropertyNamingStrategies.KebabCaseStrategy());

    static {
        ConfigConvertUtil.mapper.setVisibility(
                ConfigConvertUtil.mapper.getSerializationConfig().getDefaultVisibilityChecker()
                        .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                        .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
    }

    private ConfigConvertUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Convert config json to entity class
     *
     * @param jsonStr
     * @param entityClass
     * @param <E>         entity class
     * @return
     */
    public static <E> E jsonToObject(String jsonStr, Class<E> entityClass) {
        try {
            return mapper.readValue(jsonStr, entityClass);
        } catch (JsonProcessingException e) {
            throw new ConfigConversionException("Fail to convert json to object. ", e);
        }
    }

    public static String objectToJson(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new ConfigConversionException("Fail to convert object to json. ", e);
        }
    }
}