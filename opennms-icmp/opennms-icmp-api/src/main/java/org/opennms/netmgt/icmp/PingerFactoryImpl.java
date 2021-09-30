/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.icmp;

public class PingerFactoryImpl extends AbstractPingerFactory {
    @Override
    public Class<? extends Pinger> getPingerClass() {
        final String pingerClassName = System.getProperty("org.opennms.netmgt.icmp.pingerClass", "org.opennms.netmgt.icmp.best.BestMatchPinger");

        // If the default (0) DSCP pinger has already been initialized, use the
        // same class in case it's been manually overridden with a setInstance()
        // call (ie, in the Remote Poller)
        final Pinger defaultPinger = m_pingers.getIfPresent(1);
        if (defaultPinger != null) {
            return defaultPinger.getClass();
        }

        try {
            return Class.forName(pingerClassName).asSubclass(Pinger.class);
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException("Unable to locate pinger class " + pingerClassName);
        }
    }

}
