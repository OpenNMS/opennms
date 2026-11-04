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
package org.opennms.netmgt.rrd.rrdtool.ffm;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.netmgt.rrd.jrrd2.api.FetchResults;
import org.opennms.netmgt.rrd.jrrd2.api.JRrd2Exception;

public class JRrd2FfmTest {

    private static final long STEP = 300;
    /** Step-aligned base time. */
    private static final long T0 = 1_699_999_800L;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private JRrd2Ffm jrrd2;

    @Before
    public void setUp() {
        try {
            jrrd2 = new JRrd2Ffm();
        } catch (final UnsatisfiedLinkError e) {
            Assume.assumeNoException("librrd not available: skipping", e);
        }
        System.err.println("librrd loaded from: " + JRrd2Ffm.getLibrarySource());
    }

    private String createAndPopulate(final String name, final double... values) throws Exception {
        final String file = new File(tempFolder.getRoot(), name).getAbsolutePath();
        jrrd2.create(file, STEP, T0 - 1, new String[] {
                "DS:x:GAUGE:600:U:U",
                "DS:y:GAUGE:600:U:U",
                "RRA:AVERAGE:0.5:1:100"
        });
        final String[] updates = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            updates[i] = (T0 + (i + 1) * STEP) + ":" + values[i] + ":" + (values[i] * 2);
        }
        jrrd2.update(file, null, updates);
        return file;
    }

    private static Map<Long, Double> series(final FetchResults results, final int column) {
        final long[] ts = results.getTimestamps();
        final double[] vals = results.getValues()[column];
        assertEquals(ts.length, vals.length);
        final Map<Long, Double> map = new HashMap<>();
        for (int i = 0; i < ts.length; i++) {
            map.put(ts[i], vals[i]);
        }
        return map;
    }

    @Test
    public void canCreateUpdateAndFetch() throws Exception {
        final String file = createAndPopulate("fetch.rrd", 10, 20, 30, 40);
        assertTrue(new File(file).exists());

        final FetchResults results = jrrd2.fetch(file, "AVERAGE", T0, T0 + 4 * STEP, 1);
        assertNotNull(results);
        assertEquals(STEP, results.getStep());
        assertArrayEquals(new String[] { "x", "y" }, results.getColumns());
        assertEquals(2, results.getValues().length);

        final Map<Long, Double> x = series(results, 0);
        final Map<Long, Double> y = series(results, 1);
        assertEquals(10.0, x.get(T0 + STEP), 0.0001);
        assertEquals(20.0, x.get(T0 + 2 * STEP), 0.0001);
        assertEquals(30.0, x.get(T0 + 3 * STEP), 0.0001);
        assertEquals(20.0, y.get(T0 + STEP), 0.0001);
        assertEquals(60.0, y.get(T0 + 3 * STEP), 0.0001);
    }

    @Test
    public void canUpdateWithTemplate() throws Exception {
        final String file = new File(tempFolder.getRoot(), "template.rrd").getAbsolutePath();
        jrrd2.create(file, STEP, T0 - 1, new String[] { "DS:x:GAUGE:600:U:U", "DS:y:GAUGE:600:U:U", "RRA:AVERAGE:0.5:1:100" });
        jrrd2.update(file, "y:x", new String[] { (T0 + STEP) + ":5:7", (T0 + 2 * STEP) + ":5:7" });

        final FetchResults results = jrrd2.fetch(file, "AVERAGE", T0, T0 + 2 * STEP, 1);
        assertEquals(7.0, series(results, 0).get(T0 + STEP), 0.0001);
        assertEquals(5.0, series(results, 1).get(T0 + STEP), 0.0001);
    }

    @Test
    public void canXport() throws Exception {
        final String file = createAndPopulate("xport.rrd", 1, 2, 3, 4, 5);

        final FetchResults results = jrrd2.xport(T0, T0 + 5 * STEP, STEP, 0, new String[] {
                "DEF:a=" + file + ":x:AVERAGE",
                "DEF:b=" + file + ":y:AVERAGE",
                "CDEF:c=a,b,+",
                "XPORT:a:alpha",
                "XPORT:c:sum"
        });
        assertArrayEquals(new String[] { "alpha", "sum" }, results.getColumns());
        assertEquals(STEP, results.getStep());

        final Map<Long, Double> alpha = series(results, 0);
        final Map<Long, Double> sum = series(results, 1);
        assertEquals(2.0, alpha.get(T0 + 2 * STEP), 0.0001);
        assertEquals(6.0, sum.get(T0 + 2 * STEP), 0.0001);
        assertEquals(12.0, sum.get(T0 + 4 * STEP), 0.0001);
    }

    @Test
    public void errorsAreReportedAsJRrd2Exception() throws Exception {
        final String missing = new File(tempFolder.getRoot(), "does-not-exist.rrd").getAbsolutePath();
        try {
            jrrd2.update(missing, null, new String[] { "N:1:2" });
            fail("expected JRrd2Exception");
        } catch (final JRrd2Exception e) {
            assertTrue("unexpected message: " + e.getMessage(), e.getMessage().contains("does-not-exist.rrd"));
        }

        // Error state must not leak into the next successful call
        createAndPopulate("after-error.rrd", 1);

        try {
            jrrd2.create(new File(tempFolder.getRoot(), "bad.rrd").getAbsolutePath(), STEP, T0, new String[] { "DS:bogus" });
            fail("expected JRrd2Exception");
        } catch (final JRrd2Exception e) {
            assertNotNull(e.getMessage());
        }

        try {
            jrrd2.xport(T0, T0 + STEP, STEP, 0, new String[] { "XPORT:undefined:nope" });
            fail("expected JRrd2Exception");
        } catch (final JRrd2Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void nullArgumentsAreRejected() {
        try {
            jrrd2.update(null, null, new String[0]);
            fail("expected JRrd2Exception");
        } catch (final JRrd2Exception e) {
            assertEquals("filename cannot be null.", e.getMessage());
        }
        try {
            jrrd2.fetch("x.rrd", null, 0, 1, 1);
            fail("expected JRrd2Exception");
        } catch (final JRrd2Exception e) {
            assertEquals("cf cannot be null.", e.getMessage());
        }
    }

    @Test
    public void isSafeToUseFromManyThreads() throws Exception {
        final int threads = 8;
        final int filesPerThread = 25;
        final ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            final List<Future<?>> futures = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                final int thread = t;
                futures.add(pool.submit((Callable<Void>) () -> {
                    for (int f = 0; f < filesPerThread; f++) {
                        final double v = thread * 1000 + f;
                        final String file = createAndPopulate("mt-" + thread + "-" + f + ".rrd", v, v, v);
                        final FetchResults r = jrrd2.fetch(file, "AVERAGE", T0, T0 + 3 * STEP, 1);
                        assertEquals(v, series(r, 0).get(T0 + 2 * STEP), 0.0001);
                        // interleave failures to check per-thread error isolation
                        try {
                            jrrd2.update(file + ".missing", null, new String[] { "N:1:1" });
                            fail("expected JRrd2Exception");
                        } catch (final JRrd2Exception expected) {
                            assertTrue(expected.getMessage(), expected.getMessage().contains(".missing"));
                        }
                    }
                    return null;
                }));
            }
            for (final Future<?> future : futures) {
                future.get();
            }
        } finally {
            pool.shutdownNow();
        }
    }
}
