/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.support;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.poller.ComplexPollerParameter;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollerParameter;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.SimplePollerParameter;

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
    public Map<String, PollerParameter> getRuntimeAttributes(MonitoredService svc, Map<String, PollerParameter> parameters) {
        return Collections.emptyMap();
    }

    @Override
    public String getEffectiveLocation(String location) {
        return location;
    }

    public static Optional<String> getKeyedSimple(final Map<String, PollerParameter> parameterMap, final String key) {
        if (key == null) return Optional.empty();

        final PollerParameter value = parameterMap.get(key);
        if (value == null) return Optional.empty();

        return value.asSimple().map(SimplePollerParameter::getValue);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getKeyedInstance(final Map<String, PollerParameter> parameterMap, final String key, Class<T> clazz, final Supplier<T> defaultValue) {
        if (key == null) return defaultValue.get();

        final PollerParameter value = parameterMap.get(key);
        if (value == null) return defaultValue.get();

        return value.asComplex()
                .map(complex -> {
                    try {
                        return complex.getInstance(clazz);
                    } catch (final JAXBException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElseGet(defaultValue);
    }

    public static Boolean getKeyedBoolean(final Map<String, PollerParameter> parameterMap, final String key, final Boolean defaultValue) {
        return getKeyedSimple(parameterMap, key)
                .map(value -> "true".equalsIgnoreCase(value) ? Boolean.TRUE : Boolean.FALSE)
                .orElse(defaultValue);
    }

    public static String getKeyedString(final Map<String, PollerParameter> parameterMap, final String key, final String defaultValue) {
        return getKeyedSimple(parameterMap, key)
                .orElse(defaultValue);
    }

    public static Integer getKeyedInteger(final Map<String, PollerParameter> parameterMap, final String key, final Integer defaultValue) {
        return getKeyedSimple(parameterMap, key)
                .map(value -> {
                    try {
                        return Integer.valueOf(value);
                    } catch (final NumberFormatException e) {
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }

    public static Integer getKeyedDecodedInteger(final Map<String, PollerParameter> parameterMap, final String key, final Integer defaultValue) {
        return getKeyedSimple(parameterMap, key)
                .map(value -> {
                    try {
                        return Integer.decode(value);
                    } catch (final NumberFormatException e) {
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }

    public int[] getKeyedIntegerArray(Map<String, PollerParameter> parameters, String key, int[] defaultValues) {
        return getKeyedSimple(parameters, key)
                .map(simple -> ParameterMap.getIntegerArrayValue(key, simple))
                .orElse(defaultValues);
    }

    public static Long getKeyedLong(final Map<String, PollerParameter> parameterMap, final String key, final Long defaultValue) {
        return getKeyedSimple(parameterMap, key)
                .map(value -> {
                    try {
                        return Long.valueOf(value);
                    } catch (final NumberFormatException e) {
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
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
