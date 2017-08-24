/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.icmp.best;

import java.util.Arrays;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.icmp.AbstractPingerFactory;
import org.opennms.netmgt.icmp.NullPinger;
import org.opennms.netmgt.icmp.Pinger;
import org.opennms.netmgt.icmp.jna.JnaPinger;
import org.opennms.netmgt.icmp.jni.JniPinger;
import org.opennms.netmgt.icmp.jni6.Jni6Pinger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BestMatchPingerFactory extends AbstractPingerFactory {
    private static Logger LOG = LoggerFactory.getLogger(BestMatchPingerFactory.class);
    Class<? extends Pinger> m_pingerClass = null;

    @Override
    public Class<? extends Pinger> getPingerClass() {
        initialize();
        return m_pingerClass;
    }

    private static PingerMatch tryPinger(final Class<? extends Pinger> pingerClass) {
        boolean v4 = false;
        boolean v6 = false;

        final Pinger pinger;
        try {
            pinger = pingerClass.newInstance();
        } catch (final Throwable t) {
            LOG.info("Failed to get instance of {}: {}", pingerClass, t.getMessage());
            LOG.trace("Failed to get instance of {}.", pingerClass, t);
            return PingerMatch.NONE;
        }

        try {
            if (pinger.isV4Available()) {
                pinger.initialize4();
                v4 = true;
            }
        } catch (final Throwable t) {
            LOG.info("Failed to initialize {} for IPv4: ", pingerClass, t.getMessage());
            LOG.trace("Failed to initialize {} for IPv4.", pingerClass, t);
        }

        try {
            if (pinger.isV6Available()) {
                pinger.initialize6();
                v6 = true;
            }
        } catch (final Throwable t) {
            LOG.info("Failed to initialize {} for IPv4: {}", pingerClass, t.getMessage());
            LOG.trace("Failed to initialize {} for IPv4.", pingerClass, t);
        }

        try {
            final long timeout = Long.valueOf(System.getProperty("org.opennms.netmgt.icmp.best.timeout", "500"), 10);
            final Number result = pinger.ping(InetAddressUtils.getLocalHostAddress(), timeout, 0);
            if (result == null) {
                throw new IllegalStateException("No result pinging localhost.");
            }
        } catch (final Throwable t) {
            LOG.info("Found pinger {}, but it was unable to ping localhost: {}", pingerClass, t.getMessage());
            LOG.trace("Found pinger {}, but it was unable to ping localhost.", pingerClass, t);
            return PingerMatch.NONE;
        }

        if (v4 && v6) {
            return PingerMatch.IPv46;
        } else if (v6) {
            return PingerMatch.IPv6;
        } else if (v4) {
            return PingerMatch.IPv4;
        } else {
            return PingerMatch.NONE;
        }
    }

    static Class<? extends Pinger> findPinger() {
        final String pingerClassStr = System.getProperty("org.opennms.netmgt.icmp.pingerClass");
        if (pingerClassStr != null) {
            try {
                final Class<? extends Pinger> pingerClass = Class.forName(pingerClassStr).asSubclass(Pinger.class);
                LOG.warn("Not scanning for best pinger because explicit pinger class has been set: {}", pingerClassStr);
                return pingerClass;
            } catch (final Throwable t) {
                LOG.error("org.opennms.netmgt.icmp.pingerClass is set ({}), but it failed to initialize! Erroring out.", pingerClassStr, t);
                throw new IllegalStateException("Unable to initialize pinger class set in org.opennms.netmgt.icmp.pingerClass", t);
            }
        }

        PingerMatch match = PingerMatch.NONE;
        Class<? extends Pinger> pinger = NullPinger.class;

        LOG.info("Searching for best available pinger...");
        for (final Class<? extends Pinger> pingerClass : Arrays.asList(JniPinger.class, Jni6Pinger.class, JnaPinger.class)) {
            final PingerMatch tried = tryPinger(pingerClass);
            if (tried.compareTo(match) > 0) {
                match = tried;
                pinger = pingerClass;
            }
        }

        LOG.info("Best available pinger is: {}", pinger);
        return pinger;
    }

    private void initialize() {
        if (m_pingerClass == null) {
            // If the default (0) DSCP pinger has already been initialized, use the
            // same class in case it's been manually overridden with a setInstance()
            // call (ie, in the Remote Poller)
            final Pinger defaultPinger = m_pingers.getIfPresent(1);
            if (defaultPinger != null) {
                m_pingerClass = defaultPinger.getClass();
            } else {
                m_pingerClass = findPinger();
            }
        }
    }
}
