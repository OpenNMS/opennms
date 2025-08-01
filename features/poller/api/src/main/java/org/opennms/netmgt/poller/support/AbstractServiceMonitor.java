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
package org.opennms.netmgt.poller.support;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.ServiceMonitor;

/**
 * <p>
 * This class provides a basic implementation for most of the interface methods
 * of the <code>ServiceMonitor</code> class. Since most pollers do not do any
 * special initialization, and only require that the interface is an
 * <code>InetAddress</code> object this class provides everything by the
 * <code>poll<code> interface.
 *
 * @author <A HREF="mike@opennms.org">Mike</A>
 * @author <A HREF="weave@oculan.com">Weave</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public abstract class AbstractServiceMonitor implements ServiceMonitor {

    @Override
    public Map<String, Object> getRuntimeAttributes(MonitoredService svc, Map<String, Object> parameters) {
        return Collections.emptyMap();
    }

    @Override
    public String getEffectiveLocation(String location) {
        return location;
    }

    public static Object getKeyedObject(final Map<String, Object> parameterMap, final String key, final Object defaultValue) {
        if (key == null) return defaultValue;

        final Object value = parameterMap.get(key);
        if (value == null) return defaultValue;

        return value;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getKeyedInstance(final Map<String, Object> parameterMap, final String key, final Supplier<T> defaultValue) {
        if (key == null) return defaultValue.get();

        final Object value = parameterMap.get(key);
        if (value == null) return defaultValue.get();

        return (T)value;
    }

    public static Boolean getKeyedBoolean(final Map<String, Object> parameterMap, final String key, final Boolean defaultValue) {
        final Object value = getKeyedObject(parameterMap, key, defaultValue);
        if (value == null) return defaultValue;

        if (value instanceof String) {
            return "true".equalsIgnoreCase((String)value) ? Boolean.TRUE : Boolean.FALSE;
        } else if (value instanceof Boolean) {
            return (Boolean)value;
        }

        return defaultValue;
    }

    public static String getKeyedString(final Map<String, Object> parameterMap, final String key, final String defaultValue) {
        final Object value = getKeyedObject(parameterMap, key, defaultValue);
        if (value == null) return defaultValue;

        if (value instanceof String) {
            return (String)value;
        }

        return value.toString();
    }

    public static Integer getKeyedInteger(final Map<String, Object> parameterMap, final String key, final Integer defaultValue) {
        final Object value = getKeyedObject(parameterMap, key, defaultValue);
        if (value == null) return defaultValue;

        if (value instanceof String) {
            try {
                return Integer.valueOf((String)value);
            } catch (final NumberFormatException e) {
                return defaultValue;
            }
        } else if (value instanceof Integer) {
            return (Integer)value;
        } else if (value instanceof Number) {
            return Integer.valueOf(((Number)value).intValue());
        }

        return defaultValue;
    }

    public static Long getKeyedLong(final Map<String, Object> parameterMap, final String key, final Long defaultValue) {
        final Object value = getKeyedObject(parameterMap, key, defaultValue);
        if (value == null) return defaultValue;

        if (value instanceof String) {
            try {
                return Long.valueOf((String)value);
            } catch (final NumberFormatException e) {
                return defaultValue;
            }
        } else if (value instanceof Long) {
            return (Long)value;
        } else if (value instanceof Number) {
            return Long.valueOf(((Number)value).longValue());
        }

        return defaultValue;
    }

    public static Properties getServiceProperties(final MonitoredService svc) {
        final InetAddress addr = InetAddressUtils.addr(svc.getIpAddr());
        final boolean requireBrackets = addr != null && addr instanceof Inet6Address && !svc.getIpAddr().startsWith("[");
        final Properties properties = new Properties();
        properties.put("ipaddr", requireBrackets ? "[" + svc.getIpAddr() + "]" : svc.getIpAddr());
        properties.put("nodeid", svc.getNodeId());
        properties.put("nodelabel", svc.getNodeLabel());
        properties.put("svcname", svc.getSvcName());
        return properties;
    }
}
