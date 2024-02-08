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
package org.opennms.features.vaadin.events;

import com.vaadin.v7.data.util.converter.Converter;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Locale;

/**
 * The CSV List Converter.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class CsvListConverter implements Converter<String, ArrayList<String>> {

    /* (non-Javadoc)
     * @see com.vaadin.data.util.converter.Converter#convertToModel(java.lang.Object, java.lang.Class, java.util.Locale)
     */
    @Override
    public ArrayList<String> convertToModel(String fieldValue, Class<? extends ArrayList<String>> targetType, Locale locale) throws ConversionException {
        if (fieldValue == null) {
            return null;
        }
        ArrayList<String> list = new ArrayList<>();
        if (fieldValue != null) {
            for (String s : fieldValue.split(",")) {
                if (s == null || "".equals(s.trim())) {
                    // Blank value, skip it
                } else {
                    list.add(s.trim());
                }
            }
        }
        return list;
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.util.converter.Converter#convertToPresentation(java.lang.Object, java.lang.Class, java.util.Locale)
     */
    @Override
    public String convertToPresentation(ArrayList<String> propertyValue, Class<? extends String> targetType, Locale locale) throws ConversionException {
        return propertyValue == null ? null : StringUtils.join(propertyValue, ',');
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.util.converter.Converter#getModelType()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Class<ArrayList<String>> getModelType() {
        return (Class<ArrayList<String>>) new ArrayList<String>().getClass();
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.util.converter.Converter#getPresentationType()
     */
    @Override
    public Class<String> getPresentationType() {
        return String.class;
    }
}
