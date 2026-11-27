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
package org.opennms.netmgt.icmp.best;

import java.net.InetAddress;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static org.opennms.core.utils.InetAddressUtils.addr;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.icmp.AbstractPingerFactory;
import org.opennms.netmgt.icmp.NullPinger;
import org.opennms.netmgt.icmp.Pinger;
import org.opennms.netmgt.icmp.ffm.FfmPinger;
import org.opennms.netmgt.icmp.jna.JnaPinger;
import org.opennms.netmgt.icmp.jni.JniPinger;
import org.opennms.netmgt.icmp.jni6.Jni6Pinger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BestMatchPingerFactory extends AbstractPingerFactory {
    private static Logger LOG = LoggerFactory.getLogger(BestMatchPingerFactory.class);
    private static InetAddress LOOPBACK = InetAddressUtils.getLocalLoopbackAddress().orElse(addr("127.0.0.1"));

    Class<? extends Pinger> m_pingerClass = null;

    @Override
    public Class<? extends Pinger> getPingerClass() {
        initialize();
        return m_pingerClass;
    }

    /** The message of t, plus the root cause (type and message) if that is another throwable. */
    private static String describe(final Throwable t) {
        Throwable root = t;
        final Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        while (root.getCause() != null && seen.add(root)) {
            root = root.getCause();
        }
        final String message = message(t);
        if (root == t) {
            return message;
        }
        // drop a trailing period so the result does not read ".: "
        return (message.endsWith(".") ? message.substring(0, message.length() - 1) : message) + ": " + root;
    }

    private static String message(final Throwable t) {
        return t.getMessage() != null ? t.getMessage() : t.getClass().getName();
    }

    static Attempt tryPinger(final Class<? extends Pinger> pingerClass) {
        boolean v4 = false;
        boolean v6 = false;
        String failure = null;

        final Pinger pinger;
        try {
            pinger = pingerClass.newInstance();
        } catch (final Throwable t) {
            LOG.info("Failed to get instance of {}: {}", pingerClass, t.getMessage());
            LOG.trace("Failed to get instance of {}.", pingerClass, t);
            return new Attempt(PingerMatch.NONE, describe(t));
        }

        try {
            if (pinger.isV4Available()) {
                pinger.initialize4();
                v4 = true;
            }
        } catch (final Throwable t) {
            LOG.info("Failed to initialize {} for IPv4: {}", pingerClass, t.getMessage());
            LOG.trace("Failed to initialize {} for IPv4.", pingerClass, t);
            failure = describe(t);
        }

        try {
            if (pinger.isV6Available()) {
                pinger.initialize6();
                v6 = true;
            }
        } catch (final Throwable t) {
            LOG.info("Failed to initialize {} for IPv6: {}", pingerClass, t.getMessage());
            LOG.trace("Failed to initialize {} for IPv6.", pingerClass, t);
            failure = describe(t);
        }

        final long timeout = Long.valueOf(System.getProperty("org.opennms.netmgt.icmp.best.timeout", "500"), 10);

        // try the found loopback, fall back to 127.0.0.1 or ::1 as a last resort
        for (final InetAddress tryme : new InetAddress[] { LOOPBACK, addr("127.0.0.1"), addr("::1") }) {
            try {
                final Number result = pinger.ping(tryme, timeout, 0);
                if (result == null) {
                    throw new IllegalStateException("No result pinging localhost.");
                }

                // as long as we have v4 and/or v6, return success if pinger.ping() passes
                if (v4 && v6) {
                    return new Attempt(PingerMatch.IPv46, failure);
                } else if (v6) {
                    return new Attempt(PingerMatch.IPv6, failure);
                } else if (v4) {
                    return new Attempt(PingerMatch.IPv4, failure);
                }
            } catch (final Throwable t) {
                LOG.info("Found pinger {}, but it was unable to ping localhost: {}", pingerClass, t.getMessage());
                LOG.trace("Pinger failure:", t);
                failure = describe(t);
            }
        }

        // if none of the loopback addresses works, give up
        return new Attempt(PingerMatch.NONE, failure);
    }

    /**
     * Candidate pingers, in order of preference. A later candidate is chosen
     * only if its {@link PingerMatch} ranks higher than that of an earlier one.
     * The ranks are NONE, IPv4, IPv6, IPv46, so an IPv6-only candidate beats an
     * IPv4-only one. The FFM pinger comes first. The JICMP6, JICMP and JNA
     * pingers are deprecated fallbacks. JNA is last because it adds latency.
     */
    @SuppressWarnings("removal")
    static final List<Class<? extends Pinger>> CANDIDATES =
            List.of(FfmPinger.class, Jni6Pinger.class, JniPinger.class, JnaPinger.class);

    /** failure: message of the last Throwable seen while creating, initialising or pinging. Null if none. */
    record Attempt(PingerMatch match, String failure) {}

    /** preferred: the Attempt of the first candidate. */
    record Selection(Class<? extends Pinger> pinger, Attempt preferred) {}

    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    static Selection scan(final List<Class<? extends Pinger>> candidates,
                          final Function<Class<? extends Pinger>, Attempt> probe) {
        Attempt preferred = null;
        PingerMatch match = PingerMatch.NONE;
        Class<? extends Pinger> pinger = NullPinger.class;

        for (final Class<? extends Pinger> pingerClass : candidates) {
            final Attempt tried = probe.apply(pingerClass);
            if (preferred == null) {
                preferred = tried;
            }
            if (tried.match().compareTo(match) > 0) {
                match = tried.match();
                pinger = pingerClass;
            }
            if (match == PingerMatch.IPv46) {
                // nothing later in the list can do better
                break;
            }
        }
        return new Selection(pinger, preferred);
    }

    @SuppressWarnings("removal")
    private static boolean isJna(final Class<? extends Pinger> pinger) {
        return JnaPinger.class.isAssignableFrom(pinger);
    }

    /** True for the JICMP6, JICMP and JNA pingers. */
    @SuppressWarnings("removal")
    private static boolean isDeprecated(final Class<? extends Pinger> pinger) {
        return Jni6Pinger.class.isAssignableFrom(pinger) || JniPinger.class.isAssignableFrom(pinger) || isJna(pinger);
    }

    /** Null when pinger is not a deprecated pinger. preferred is ignored when pinned is true. */
    static String deprecationWarning(final Class<? extends Pinger> pinger, final Attempt preferred, final boolean pinned) {
        if (!isDeprecated(pinger)) {
            return null;
        }
        final String message;
        if (pinned) {
            message = "org.opennms.netmgt.icmp.pingerClass is set to the deprecated " + pinger.getName()
                    + ". The JICMP, JICMP6 and JNA pingers will be removed in a future release."
                    + " Remove the setting to use the FFM pinger.";
        } else {
            final String reason = preferred.match() == PingerMatch.NONE
                    ? preferred.failure()
                    : "it supports " + preferred.match() + " only";
            message = "The FFM pinger cannot be used here (" + reason + "). Using the deprecated "
                    + pinger.getName() + " instead. The JICMP, JICMP6 and JNA pingers will be removed in a future release.";
        }
        return isJna(pinger)
                ? message + " The JNA pinger adds latency, so ICMP response times are less accurate."
                : message;
    }

    /** Null unless no candidate works, so the scan chose the NullPinger. */
    static String noPingerWarning(final Selection selection) {
        if (selection.pinger() != NullPinger.class) {
            return null;
        }
        return "No ICMP pinger works here, so ICMP is not available. The FFM pinger failed: " + selection.preferred().failure();
    }

    /** True the first time a message is seen in this JVM. */
    static boolean warnOnce(final String message) {
        return WARNED.add(message);
    }

    /** Logs a warning once per JVM. Null is ignored. */
    private static void warn(final String warning) {
        if (warning != null && warnOnce(warning)) {
            LOG.warn(warning);
        }
    }

    static Class<? extends Pinger> findPinger() {
        final String pingerClassStr = System.getProperty("org.opennms.netmgt.icmp.pingerClass");
        if (pingerClassStr != null) {
            try {
                final Class<? extends Pinger> pingerClass = Class.forName(pingerClassStr).asSubclass(Pinger.class);
                LOG.warn("Not scanning for best pinger because explicit pinger class has been set: {}", pingerClassStr);
                warn(deprecationWarning(pingerClass, null, true));
                return pingerClass;
            } catch (final Throwable t) {
                LOG.error("org.opennms.netmgt.icmp.pingerClass is set ({}), but it failed to initialize! Erroring out.", pingerClassStr, t);
                throw new IllegalStateException("Unable to initialize pinger class set in org.opennms.netmgt.icmp.pingerClass", t);
            }
        }

        LOG.info("Searching for best available pinger...");
        return select(CANDIDATES, BestMatchPingerFactory::tryPinger);
    }

    /** Scans the candidates and logs the result. Warns once per JVM about a fallback and about no working pinger. */
    static Class<? extends Pinger> select(final List<Class<? extends Pinger>> candidates,
                                          final Function<Class<? extends Pinger>, Attempt> probe) {
        final Selection selection = scan(candidates, probe);
        LOG.info("Best available pinger is: {}", selection.pinger());
        warn(deprecationWarning(selection.pinger(), selection.preferred(), false));
        warn(noPingerWarning(selection));
        return selection.pinger();
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
