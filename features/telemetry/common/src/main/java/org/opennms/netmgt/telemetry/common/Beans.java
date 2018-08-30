/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.common;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.netmgt.telemetry.api.receiver.Listener;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.api.receiver.Parser;
import org.opennms.netmgt.telemetry.config.api.ListenerDefinition;
import org.opennms.netmgt.telemetry.config.api.ParserDefinition;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

public class Beans {

    public static <F> F createFactory(final Class<F> factoryClass, final String className) {
        final Constructor<? extends F> ctor;
        try {
            ctor = Class.forName(className)
                    .asSubclass(factoryClass)
                    .getConstructor();
        } catch (final ClassNotFoundException | NoSuchMethodException e) {
            throw new IllegalArgumentException(String.format("%s not found", className));
        } catch (final ClassCastException e) {
            throw new IllegalArgumentException(String.format("%s must implement %s", className, factoryClass.getCanonicalName()));
        }

        try {
            return ctor.newInstance();
        } catch (final Exception e) {
            throw new RuntimeException(String.format("Failed to instantiate class '%s'.", className), e);
        }
    }

    @FunctionalInterface
    public interface ParserCreator<F, P> {
        P create(final F factory,
                 final String name,
                 final Map<String, String> paramters,
                 final AsyncDispatcher<TelemetryMessage> dispatcher);
    }

    public static <P extends Parser, F> P buildParser(final Class<F> factoryClass,
                                                      final ParserCreator<F, P> creator,
                                                      final ParserDefinition parserDefinition,
                                                      final AsyncDispatcher<TelemetryMessage> dispatcher) {
        final F factory = createFactory(factoryClass, parserDefinition.getClassName());
        final P parser = creator.create(factory, parserDefinition.getName(), parserDefinition.getParameterMap(), dispatcher);

        return parser;
    }
}
