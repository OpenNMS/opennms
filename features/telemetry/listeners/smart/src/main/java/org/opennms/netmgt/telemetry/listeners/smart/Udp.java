/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.listeners.smart;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.netmgt.telemetry.api.receiver.Listener;
import org.opennms.netmgt.telemetry.api.receiver.Parser;
import org.opennms.netmgt.telemetry.common.Beans;
import org.opennms.netmgt.telemetry.config.api.ParserDefinition;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

public class Udp implements Listener.Factory {

    @Override
    public Parser.Creator parser(final ParserDefinition parserDefinition) {
        return dispatcher -> Beans.buildParser(SmartUdpParser.Factory.class,
                SmartUdpParser.Factory::createUdpParser,
                parserDefinition, dispatcher);
    }

    @Override
    public Listener create(final String name,
                           final Map<String, String> parameters,
                           final Set<Parser> parsers) {
        final SmartUdpListener listener = new SmartUdpListener(name, parsers.stream()
                .map(p -> (SmartUdpParser) p)
                .collect(Collectors.toSet()));

        final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(listener);
        wrapper.setPropertyValues(parameters);

        return listener;
    }
}
