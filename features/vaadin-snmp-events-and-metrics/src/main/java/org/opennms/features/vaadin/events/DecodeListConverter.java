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
import org.opennms.netmgt.xml.eventconf.Decode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The Varbind's Decode List Converter.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class DecodeListConverter implements Converter<String, ArrayList<Decode>> {

    /* (non-Javadoc)
     * @see com.vaadin.data.util.converter.Converter#convertToModel(java.lang.Object, java.lang.Class, java.util.Locale)
     */
    @Override
    public ArrayList<Decode> convertToModel(String fieldValue, Class<? extends ArrayList<Decode>> targetType, Locale locale) throws ConversionException {
        if (fieldValue == null) {
            return null;
        }
        ArrayList<Decode> list = new ArrayList<>();
        for (String s : fieldValue.split(",")) {
            String[] parts = s.split("=");
            if (parts.length == 2) {
                Decode d = new Decode();
                d.setVarbindvalue(parts[0].trim());
                d.setVarbinddecodedstring(parts[1].trim());
                list.add(d);
            }
        }
        return list;
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.util.converter.Converter#convertToPresentation(java.lang.Object, java.lang.Class, java.util.Locale)
     */
    @Override
    public String convertToPresentation(ArrayList<Decode> propertyValue, Class<? extends String> targetType, Locale locale) throws ConversionException {
        if (propertyValue == null) {
            return null;
        }
        final List<String> values = new ArrayList<>();
        for (Decode d : propertyValue) {
            values.add(d.getVarbindvalue() + '=' + d.getVarbinddecodedstring());
        }
        return StringUtils.join(values, ',');
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.util.converter.Converter#getModelType()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Class<ArrayList<Decode>> getModelType() {
        return (Class<ArrayList<Decode>>) new ArrayList<Decode>().getClass();

    }

    /* (non-Javadoc)
     * @see com.vaadin.data.util.converter.Converter#getPresentationType()
     */
    @Override
    public Class<String> getPresentationType() {
        return String.class;
    }

}
