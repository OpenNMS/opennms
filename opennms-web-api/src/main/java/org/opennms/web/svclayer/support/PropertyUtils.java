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
package org.opennms.web.svclayer.support;

import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.LinkedList;

import org.opennms.netmgt.model.InetAddressTypeEditor;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.OnmsSeverityEditor;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.PrimaryTypeEditor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessorFactory;

public interface PropertyUtils {

    /**
     * <p>getProperties</p>
     *
     * @param bean a {@link java.lang.Object} object.
     * @return a {@link java.util.Collection} object.
     */
    public static Collection<String> getProperties(Object bean) {
        Collection<String> props = new LinkedList<>();

        BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(bean);
        for (PropertyDescriptor pd : wrapper.getPropertyDescriptors()) {
            props.add(pd.getName());
        }
        return props;
    }
    
    /**
     * <p>getPathValue</p>
     *
     * @param bean a {@link java.lang.Object} object.
     * @param path a {@link java.lang.String} object.
     * @param expectedClass a {@link java.lang.Class} object.
     * @param <T> a T object.
     * @return a T object.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getPathValue(final Object bean, final String path, final Class<T> expectedClass) {
        final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(bean);
        wrapper.registerCustomEditor(java.net.InetAddress.class, new InetAddressTypeEditor());
        wrapper.registerCustomEditor(OnmsSeverity.class, new OnmsSeverityEditor());
        wrapper.registerCustomEditor(PrimaryType.class, new PrimaryTypeEditor());
        try {
            final Class<?> propType = wrapper.getPropertyType(path);
            if (!expectedClass.isAssignableFrom(propType)) {
                throw new IllegalArgumentException("Could not retrieve property of type "+propType+" as type "+expectedClass);
            }
        } catch (final BeansException e) {
            LoggerFactory.getLogger(PropertyUtils.class).warn("propType in BeanUtils is null for path: {}", path);
            return null;
        }
        return (T) wrapper.getPropertyValue(path);
    }

}
