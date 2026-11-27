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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.junit.Test;
import org.opennms.core.test.Level;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.icmp.NullPinger;
import org.opennms.netmgt.icmp.Pinger;
import org.opennms.netmgt.icmp.TestPinger;
import org.opennms.netmgt.icmp.best.BestMatchPingerFactory.Attempt;
import org.opennms.netmgt.icmp.best.BestMatchPingerFactory.Selection;
import org.opennms.netmgt.icmp.ffm.FfmPinger;
import org.opennms.netmgt.icmp.jna.JnaPinger;
import org.opennms.netmgt.icmp.jni.JniPinger;
import org.opennms.netmgt.icmp.jni6.Jni6Pinger;

@SuppressWarnings("removal")
public class BestMatchPingerFactoryTest {
    private static final Attempt V46 = new Attempt(PingerMatch.IPv46, null);
    private static final Attempt V4 = new Attempt(PingerMatch.IPv4, null);
    private static final Attempt NONE = new Attempt(PingerMatch.NONE, "socket: Operation not permitted");
    private static final String PROPERTY = "org.opennms.netmgt.icmp.pingerClass";

    private final List<Class<? extends Pinger>> m_probed = new ArrayList<>();

    private Function<Class<? extends Pinger>, Attempt> probe(final Attempt ffm, final Attempt jni6, final Attempt jni, final Attempt jna) {
        final Map<Class<? extends Pinger>, Attempt> results =
                Map.of(FfmPinger.class, ffm, Jni6Pinger.class, jni6, JniPinger.class, jni, JnaPinger.class, jna);
        return c -> {
            m_probed.add(c);
            return results.get(c);
        };
    }

    private Selection scan(final Attempt ffm, final Attempt jni6, final Attempt jni, final Attempt jna) {
        return BestMatchPingerFactory.scan(BestMatchPingerFactory.CANDIDATES, probe(ffm, jni6, jni, jna));
    }

    private static long warnCount(final String warning) {
        return Arrays.stream(MockLogAppender.getEvents())
                .filter(e -> e.getLevel() == Level.WARN && warning.equals(e.getMessage()))
                .count();
    }

    @Test
    public void candidatesAreInFallbackOrder() {
        assertEquals(List.of(FfmPinger.class, Jni6Pinger.class, JniPinger.class, JnaPinger.class),
                BestMatchPingerFactory.CANDIDATES);
    }

    @Test
    public void ffmWinsAndStopsTheScan() {
        assertSame(FfmPinger.class, scan(V46, V46, V4, V46).pinger());
        assertEquals(List.of(FfmPinger.class), m_probed);
    }

    @Test
    public void jicmp6IsTheFirstFallback() {
        assertSame(Jni6Pinger.class, scan(NONE, V46, V4, V46).pinger());
        assertEquals(List.of(FfmPinger.class, Jni6Pinger.class), m_probed);
    }

    @Test
    public void jicmpComesBeforeJna() {
        assertSame(JniPinger.class, scan(NONE, NONE, V4, V4).pinger());
    }

    @Test
    public void jnaWinsOnlyWithMoreAddressFamilies() {
        assertSame(JnaPinger.class, scan(NONE, NONE, V4, V46).pinger());
    }

    @Test
    public void jnaIsTheLastResort() {
        assertSame(JnaPinger.class, scan(NONE, NONE, NONE, V46).pinger());
    }

    @Test
    public void ipv4OnlyHostKeepsFfm() {
        assertSame(FfmPinger.class, scan(V4, V4, V4, V4).pinger());
        assertEquals(BestMatchPingerFactory.CANDIDATES, m_probed);
    }

    @Test
    public void nothingWorksGivesTheNullPinger() {
        assertSame(NullPinger.class, scan(NONE, NONE, NONE, NONE).pinger());
    }

    @Test
    public void selectionCarriesTheFfmFailure() {
        assertEquals("socket: Operation not permitted", scan(NONE, V46, V4, V46).preferred().failure());
    }

    @Test
    public void noWarningForFfm() {
        assertNull(BestMatchPingerFactory.deprecationWarning(FfmPinger.class, V46, false));
    }

    @Test
    public void fallbackWarningNamesCauseAndRemoval() {
        final String warning = BestMatchPingerFactory.deprecationWarning(Jni6Pinger.class, NONE, false);
        assertTrue(warning, warning.contains("socket: Operation not permitted"));
        assertTrue(warning, warning.contains(Jni6Pinger.class.getName()));
        assertTrue(warning, warning.contains("a future release"));
        assertFalse(warning, warning.contains("latency"));
    }

    @Test
    public void fallbackWarningForPartialFfm() {
        final String warning = BestMatchPingerFactory.deprecationWarning(Jni6Pinger.class, V4, false);
        assertTrue(warning, warning.contains("it supports IPv4 only"));
    }

    @Test
    public void jnaWarningNamesTheLatency() {
        final String warning = BestMatchPingerFactory.deprecationWarning(JnaPinger.class, NONE, false);
        assertTrue(warning, warning.contains("adds latency"));
        assertTrue(warning, warning.contains("less accurate"));
    }

    @Test
    public void pinnedWarningTellsToRemoveTheSetting() {
        final String warning = BestMatchPingerFactory.deprecationWarning(JniPinger.class, null, true);
        assertTrue(warning, warning.contains(PROPERTY));
        assertTrue(warning, warning.contains("a future release"));
        assertTrue(warning, warning.contains("Remove the setting"));
    }

    @Test
    public void pinnedJnaWarningNamesTheLatency() {
        final String warning = BestMatchPingerFactory.deprecationWarning(JnaPinger.class, null, true);
        assertTrue(warning, warning.contains("adds latency"));
    }

    @Test
    public void noPingerWarningNamesTheFfmFailure() {
        final String warning = BestMatchPingerFactory.noPingerWarning(scan(NONE, NONE, NONE, NONE));
        assertTrue(warning, warning.contains("socket: Operation not permitted"));
    }

    @Test
    public void noPingerWarningOnlyForTheNullPinger() {
        assertNull(BestMatchPingerFactory.noPingerWarning(scan(V46, V46, V4, V46)));
        assertNull(BestMatchPingerFactory.noPingerWarning(scan(NONE, NONE, NONE, V46)));
    }

    @Test
    public void warnOnceReturnsTrueOnlyTheFirstTime() {
        final String message = "warn-once-" + UUID.randomUUID();
        assertTrue(BestMatchPingerFactory.warnOnce(message));
        assertFalse(BestMatchPingerFactory.warnOnce(message));
    }

    @Test
    public void pinnedClassSkipsTheScan() {
        final String saved = System.getProperty(PROPERTY);
        try {
            System.setProperty(PROPERTY, "org.opennms.netmgt.icmp.TestPinger");
            assertSame(TestPinger.class, BestMatchPingerFactory.findPinger());
        } finally {
            restore(saved);
        }
    }

    @Test
    public void pinnedDeprecatedPingerWarnsOncePerJvm() {
        final String saved = System.getProperty(PROPERTY);
        MockLogAppender.setupLogging();
        try {
            System.setProperty(PROPERTY, JniPinger.class.getName());
            assertSame(JniPinger.class, BestMatchPingerFactory.findPinger());
            assertSame(JniPinger.class, BestMatchPingerFactory.findPinger());
            assertEquals(1, warnCount(BestMatchPingerFactory.deprecationWarning(JniPinger.class, null, true)));
        } finally {
            restore(saved);
        }
    }

    @Test
    public void fallbackWarnsOncePerJvm() {
        MockLogAppender.setupLogging();
        // a failure no other test uses, because the warning is logged once per JVM
        final Attempt ffm = new Attempt(PingerMatch.NONE, "fallback-" + UUID.randomUUID());
        final Function<Class<? extends Pinger>, Attempt> probe = probe(ffm, V46, V4, V46);
        assertSame(Jni6Pinger.class, BestMatchPingerFactory.select(BestMatchPingerFactory.CANDIDATES, probe));
        assertSame(Jni6Pinger.class, BestMatchPingerFactory.select(BestMatchPingerFactory.CANDIDATES, probe));
        assertEquals(1, warnCount(BestMatchPingerFactory.deprecationWarning(Jni6Pinger.class, ffm, false)));
    }

    @Test
    public void noWorkingPingerWarnsOncePerJvm() {
        MockLogAppender.setupLogging();
        final Attempt ffm = new Attempt(PingerMatch.NONE, "no-pinger-" + UUID.randomUUID());
        final Function<Class<? extends Pinger>, Attempt> probe = probe(ffm, NONE, NONE, NONE);
        assertSame(NullPinger.class, BestMatchPingerFactory.select(BestMatchPingerFactory.CANDIDATES, probe));
        assertSame(NullPinger.class, BestMatchPingerFactory.select(BestMatchPingerFactory.CANDIDATES, probe));
        assertEquals(1, warnCount(BestMatchPingerFactory.noPingerWarning(new Selection(NullPinger.class, ffm))));
    }

    @Test
    public void pinnedUnknownClassFails() {
        final String saved = System.getProperty(PROPERTY);
        try {
            System.setProperty(PROPERTY, "no.such.Pinger");
            assertThrows(IllegalStateException.class, BestMatchPingerFactory::findPinger);
        } finally {
            restore(saved);
        }
    }

    public static class IPv4Pinger extends NullPinger {
        @Override
        public boolean isV4Available() {
            return true;
        }

        @Override
        public boolean isV6Available() {
            return false;
        }
    }

    public static class WrappedFailurePinger extends IPv4Pinger {
        @Override
        public Number ping(final InetAddress host, final long timeout, final int retries) throws Exception {
            throw new IOException("IPv4 and IPv6 are not available.", new IllegalStateException("socket: Operation not permitted"));
        }
    }

    public static class PlainFailurePinger extends IPv4Pinger {
        @Override
        public Number ping(final InetAddress host, final long timeout, final int retries) throws Exception {
            throw new IllegalStateException("boom");
        }
    }

    public static class NoMessagePinger extends IPv4Pinger {
        @Override
        public Number ping(final InetAddress host, final long timeout, final int retries) throws Exception {
            throw new UnsupportedOperationException();
        }
    }

    public static class ClassNotFoundPinger extends IPv4Pinger {
        @Override
        public Number ping(final InetAddress host, final long timeout, final int retries) throws Exception {
            throw new IOException("IPv4 and IPv6 are not available.",
                    new ClassNotFoundException("org.opennms.jicmp.ffm.FfmNativeDatagramSocket"));
        }
    }

    public static class WorkingPinger extends IPv4Pinger {
        @Override
        public Number ping(final InetAddress host, final long timeout, final int retries) throws Exception {
            return 1.0;
        }
    }

    @Test
    public void tryPingerReportsTheRootCause() {
        final Attempt attempt = BestMatchPingerFactory.tryPinger(WrappedFailurePinger.class);
        assertEquals(PingerMatch.NONE, attempt.match());
        assertEquals("IPv4 and IPv6 are not available: java.lang.IllegalStateException: socket: Operation not permitted", attempt.failure());
    }

    @Test
    public void tryPingerNamesTheTypeOfARootCauseWithoutUsefulMessage() {
        assertEquals("IPv4 and IPv6 are not available: java.lang.ClassNotFoundException: org.opennms.jicmp.ffm.FfmNativeDatagramSocket",
                BestMatchPingerFactory.tryPinger(ClassNotFoundPinger.class).failure());
    }

    @Test
    public void tryPingerReportsAPlainFailure() {
        assertEquals("boom", BestMatchPingerFactory.tryPinger(PlainFailurePinger.class).failure());
    }

    @Test
    public void tryPingerNamesTheClassWhenThereIsNoMessage() {
        assertEquals("java.lang.UnsupportedOperationException",
                BestMatchPingerFactory.tryPinger(NoMessagePinger.class).failure());
    }

    @Test
    public void tryPingerSucceedsWithoutAFailure() {
        final Attempt attempt = BestMatchPingerFactory.tryPinger(WorkingPinger.class);
        assertEquals(PingerMatch.IPv4, attempt.match());
        assertNull(attempt.failure());
    }

    private static void restore(final String saved) {
        if (saved == null) {
            System.clearProperty(PROPERTY);
        } else {
            System.setProperty(PROPERTY, saved);
        }
    }
}
