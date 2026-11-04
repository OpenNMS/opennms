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

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_DOUBLE;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.rrd.jrrd2.api.FetchResults;
import org.opennms.netmgt.rrd.jrrd2.api.JRrd2;
import org.opennms.netmgt.rrd.jrrd2.api.JRrd2Exception;

/**
 * {@link JRrd2} implementation that calls the system's stock {@code librrd}
 * through the Foreign Function &amp; Memory API (JEP 454) instead of the
 * custom {@code libjrrd2} JNI shim.
 *
 * <p>Behaviour mirrors {@code rrd_jinterface.c} from the jrrd2 project:
 * <ul>
 *   <li>each thread calls {@code rrd_get_context()} once before using librrd;</li>
 *   <li>errors are read back from {@code rrd_get_error()} and thrown as {@link JRrd2Exception};</li>
 *   <li>fetch/xport results start at {@code start + step} and have {@code (end - start) / step} rows;</li>
 *   <li>values are returned column-major ({@code values[column][row]});</li>
 *   <li>{@code rrd_xport} is not thread-safe in librrd, so calls are serialized.</li>
 * </ul>
 *
 * <p>Assumes an LP64 platform (Linux, macOS), where {@code long},
 * {@code unsigned long} and {@code time_t} are all 64 bits.
 *
 * <p>The library is located via the {@value #LIBRARY_PROPERTY} system
 * property (an absolute path), falling back to well-known names/locations.
 */
public final class JRrd2Ffm implements JRrd2 {

    /** System property holding an absolute path to librrd. */
    public static final String LIBRARY_PROPERTY = "opennms.library.rrd";

    private static final ThreadLocal<Boolean> CONTEXT = ThreadLocal.withInitial(() -> {
        try {
            final MemorySegment ctx = (MemorySegment) LibRrd.get().rrdGetContext.invokeExact();
            if (ctx.equals(MemorySegment.NULL)) {
                throw new IllegalStateException("rrd_get_context() returned NULL");
            }
        } catch (final Throwable t) {
            throw new IllegalStateException("rrd_get_context() failed", t);
        }
        return Boolean.TRUE;
    });

    private static final Object XPORT_LOCK = new Object();

    /**
     * @throws UnsatisfiedLinkError if librrd cannot be found or lacks a required symbol
     */
    public JRrd2Ffm() {
        LibRrd.get();
    }

    /** @return the path or name librrd was loaded from */
    public static String getLibrarySource() {
        return LibRrd.get().source;
    }

    @Override
    public void create(final String filename, final long pdpStep, final long lastUp, final String[] argv) throws JRrd2Exception {
        requireNonNull(filename, "filename");
        requireNonNull(argv, "argv");
        CONTEXT.get();
        final LibRrd lib = LibRrd.get();
        try (Arena arena = Arena.ofConfined()) {
            final MemorySegment nFilename = arena.allocateFrom(filename);
            final MemorySegment nArgv = toCStringArray(arena, argv);
            lib.clearError();
            final int result = (int) lib.rrdCreateR.invokeExact(nFilename, pdpStep, lastUp, argv.length, nArgv);
            if (result == -1) {
                throw lib.lastError("rrd_create_r");
            }
        } catch (final JRrd2Exception e) {
            throw e;
        } catch (final Throwable t) {
            throw wrap("rrd_create_r", t);
        }
    }

    @Override
    public void update(final String filename, final String template, final String[] argv) throws JRrd2Exception {
        requireNonNull(filename, "filename");
        requireNonNull(argv, "argv");
        CONTEXT.get();
        final LibRrd lib = LibRrd.get();
        try (Arena arena = Arena.ofConfined()) {
            final MemorySegment nFilename = arena.allocateFrom(filename);
            final MemorySegment nTemplate = template == null ? MemorySegment.NULL : arena.allocateFrom(template);
            final MemorySegment nArgv = toCStringArray(arena, argv);
            lib.clearError();
            final int result = (int) lib.rrdUpdateR.invokeExact(nFilename, nTemplate, argv.length, nArgv);
            if (result == -1) {
                throw lib.lastError("rrd_update_r");
            }
        } catch (final JRrd2Exception e) {
            throw e;
        } catch (final Throwable t) {
            throw wrap("rrd_update_r", t);
        }
    }

    @Override
    public FetchResults fetch(final String filename, final String cf, final long start, final long end, final long step) throws JRrd2Exception {
        requireNonNull(filename, "filename");
        requireNonNull(cf, "cf");
        CONTEXT.get();
        final LibRrd lib = LibRrd.get();
        try (Arena arena = Arena.ofConfined()) {
            final MemorySegment pStart = arena.allocateFrom(JAVA_LONG, start);
            final MemorySegment pEnd = arena.allocateFrom(JAVA_LONG, end);
            final MemorySegment pStep = arena.allocateFrom(JAVA_LONG, step);
            final MemorySegment pDsCnt = arena.allocate(JAVA_LONG);
            final MemorySegment pDsNamv = arena.allocate(ADDRESS);
            final MemorySegment pData = arena.allocate(ADDRESS);
            lib.clearError();
            final int result = (int) lib.rrdFetchR.invokeExact(arena.allocateFrom(filename), arena.allocateFrom(cf),
                    pStart, pEnd, pStep, pDsCnt, pDsNamv, pData);
            if (result == -1) {
                throw lib.lastError("rrd_fetch_r");
            }
            return lib.toFetchResults(pStart.get(JAVA_LONG, 0), pEnd.get(JAVA_LONG, 0), pStep.get(JAVA_LONG, 0),
                    pDsCnt.get(JAVA_LONG, 0), pDsNamv.get(ADDRESS, 0), pData.get(ADDRESS, 0));
        } catch (final JRrd2Exception e) {
            throw e;
        } catch (final Throwable t) {
            throw wrap("rrd_fetch_r", t);
        }
    }

    @Override
    public FetchResults xport(final long start, final long end, final long step, final long maxrows, final String[] argv) throws JRrd2Exception {
        requireNonNull(argv, "argv");
        final List<String> args = new ArrayList<>(argv.length + 9);
        args.add("xport");
        args.add("--start");
        args.add(Long.toString(start));
        args.add("--end");
        args.add(Long.toString(end));
        args.add("--step");
        args.add(Long.toString(step));
        if (maxrows > 0) {
            args.add("--maxrows");
            args.add(Long.toString(maxrows));
        }
        args.addAll(List.of(argv));

        CONTEXT.get();
        final LibRrd lib = LibRrd.get();
        synchronized (XPORT_LOCK) {
            try (Arena arena = Arena.ofConfined()) {
                final MemorySegment nArgv = toCStringArray(arena, args.toArray(new String[0]));
                final MemorySegment pXsize = arena.allocate(JAVA_INT);
                final MemorySegment pStart = arena.allocate(JAVA_LONG);
                final MemorySegment pEnd = arena.allocate(JAVA_LONG);
                final MemorySegment pStep = arena.allocate(JAVA_LONG);
                final MemorySegment pColCnt = arena.allocate(JAVA_LONG);
                final MemorySegment pLegendV = arena.allocate(ADDRESS);
                final MemorySegment pData = arena.allocate(ADDRESS);
                lib.clearError();
                final int result = (int) lib.rrdXport.invokeExact(args.size(), nArgv, pXsize, pStart, pEnd, pStep,
                        pColCnt, pLegendV, pData);
                if (result == -1) {
                    throw lib.lastError("rrd_xport");
                }
                return lib.toFetchResults(pStart.get(JAVA_LONG, 0), pEnd.get(JAVA_LONG, 0), pStep.get(JAVA_LONG, 0),
                        pColCnt.get(JAVA_LONG, 0), pLegendV.get(ADDRESS, 0), pData.get(ADDRESS, 0));
            } catch (final JRrd2Exception e) {
                throw e;
            } catch (final Throwable t) {
                throw wrap("rrd_xport", t);
            }
        }
    }

    private static void requireNonNull(final Object value, final String name) throws JRrd2Exception {
        if (value == null) {
            throw new JRrd2Exception(name + " cannot be null.");
        }
    }

    private static JRrd2Exception wrap(final String function, final Throwable t) {
        final JRrd2Exception e = new JRrd2Exception(function + "() failed: " + t);
        e.initCause(t);
        return e;
    }

    /** Allocates a NULL-terminated {@code const char *[]}. */
    private static MemorySegment toCStringArray(final Arena arena, final String[] strings) {
        final MemorySegment array = arena.allocate(ADDRESS, strings.length + 1L);
        for (int i = 0; i < strings.length; i++) {
            if (strings[i] == null) {
                throw new IllegalArgumentException("argv[" + i + "] is null");
            }
            array.setAtIndex(ADDRESS, i, arena.allocateFrom(strings[i]));
        }
        array.setAtIndex(ADDRESS, strings.length, MemorySegment.NULL);
        return array;
    }

    /** Lazily-loaded librrd bindings, shared by all instances. */
    private static final class LibRrd {
        private static volatile LibRrd instance;
        private static volatile UnsatisfiedLinkError failure;

        final String source;
        final MethodHandle rrdGetContext;
        final MethodHandle rrdClearError;
        final MethodHandle rrdTestError;
        final MethodHandle rrdGetError;
        final MethodHandle rrdFreemem;
        final MethodHandle rrdCreateR;
        final MethodHandle rrdUpdateR;
        final MethodHandle rrdFetchR;
        final MethodHandle rrdXport;

        static LibRrd get() {
            LibRrd lib = instance;
            if (lib != null) {
                return lib;
            }
            synchronized (LibRrd.class) {
                if (instance == null && failure == null) {
                    try {
                        instance = new LibRrd();
                    } catch (final UnsatisfiedLinkError e) {
                        failure = e;
                    }
                }
                if (failure != null) {
                    throw failure;
                }
                return instance;
            }
        }

        private LibRrd() {
            final Linker linker = Linker.nativeLinker();
            final List<String> tried = new ArrayList<>();
            SymbolLookup lookup = null;
            String loadedFrom = null;
            for (final String candidate : candidates()) {
                tried.add(candidate);
                try {
                    lookup = candidate.startsWith("/")
                            ? SymbolLookup.libraryLookup(Path.of(candidate), Arena.global())
                            : SymbolLookup.libraryLookup(candidate, Arena.global());
                    loadedFrom = candidate;
                    break;
                } catch (final IllegalArgumentException e) {
                    // not found; try next
                }
            }
            if (lookup == null) {
                throw new UnsatisfiedLinkError("Unable to load librrd (tried " + tried + "). Set -D" + LIBRARY_PROPERTY
                        + "=/path/to/librrd to override.");
            }
            this.source = loadedFrom;

            final SymbolLookup lib = lookup;
            rrdGetContext = bind(linker, lib, "rrd_get_context", FunctionDescriptor.of(ADDRESS));
            rrdClearError = bind(linker, lib, "rrd_clear_error", FunctionDescriptor.ofVoid());
            rrdTestError = bind(linker, lib, "rrd_test_error", FunctionDescriptor.of(JAVA_INT));
            rrdGetError = bind(linker, lib, "rrd_get_error", FunctionDescriptor.of(ADDRESS));
            rrdFreemem = bind(linker, lib, "rrd_freemem", FunctionDescriptor.ofVoid(ADDRESS));
            // int rrd_create_r(const char *filename, unsigned long pdp_step, time_t last_up, int argc, const char **argv)
            rrdCreateR = bind(linker, lib, "rrd_create_r",
                    FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_LONG, JAVA_LONG, JAVA_INT, ADDRESS));
            // int rrd_update_r(const char *filename, const char *template, int argc, const char **argv)
            rrdUpdateR = bind(linker, lib, "rrd_update_r",
                    FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, JAVA_INT, ADDRESS));
            // int rrd_fetch_r(const char *filename, const char *cf, time_t *start, time_t *end,
            //                 unsigned long *step, unsigned long *ds_cnt, char ***ds_namv, rrd_value_t **data)
            rrdFetchR = bind(linker, lib, "rrd_fetch_r",
                    FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS));
            // int rrd_xport(int argc, const char **argv, int *xsize, time_t *start, time_t *end,
            //               unsigned long *step, unsigned long *col_cnt, char ***legend_v, rrd_value_t **data)
            rrdXport = bind(linker, lib, "rrd_xport",
                    FunctionDescriptor.of(JAVA_INT, JAVA_INT, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS));
        }

        private static List<String> candidates() {
            final List<String> candidates = new ArrayList<>();
            final String override = System.getProperty(LIBRARY_PROPERTY);
            if (override != null && !override.isBlank() && !override.startsWith("${")) {
                candidates.add(override.trim());
                return candidates;
            }
            final boolean mac = System.getProperty("os.name", "").toLowerCase().contains("mac");
            if (mac) {
                for (final String dir : List.of("/opt/homebrew/lib", "/opt/homebrew/opt/rrdtool/lib", "/usr/local/lib", "/opt/local/lib")) {
                    for (final String name : List.of("librrd.8.dylib", "librrd.dylib")) {
                        final Path p = Path.of(dir, name);
                        if (Files.exists(p)) {
                            candidates.add(p.toString());
                        }
                    }
                }
                candidates.add("librrd.8.dylib");
                candidates.add("librrd.dylib");
            } else {
                // dlopen() searches the standard linker paths (ld.so.cache, LD_LIBRARY_PATH)
                candidates.add("librrd.so.8");
                candidates.add("librrd.so");
            }
            return candidates;
        }

        private static MethodHandle bind(final Linker linker, final SymbolLookup lib, final String name, final FunctionDescriptor fd) {
            final MemorySegment symbol = lib.find(name)
                    .orElseThrow(() -> new UnsatisfiedLinkError("librrd is missing symbol " + name));
            return linker.downcallHandle(symbol, fd);
        }

        void clearError() throws Throwable {
            rrdClearError.invokeExact();
        }

        JRrd2Exception lastError(final String function) throws Throwable {
            final int hasError = (int) rrdTestError.invokeExact();
            String message = null;
            if (hasError != 0) {
                final MemorySegment err = (MemorySegment) rrdGetError.invokeExact();
                if (!err.equals(MemorySegment.NULL)) {
                    message = err.reinterpret(Long.MAX_VALUE).getString(0);
                }
                rrdClearError.invokeExact();
            }
            return new JRrd2Exception(message != null ? message : function + "() failed, but no error code was set.");
        }

        /**
         * Converts librrd's row-major result into {@link FetchResults} and
         * releases the librrd-allocated legend strings and data.
         */
        FetchResults toFetchResults(final long start, final long end, final long step, final long colCount,
                                    final MemorySegment namesPtr, final MemorySegment dataPtr) throws Throwable {
            final int cols = Math.toIntExact(colCount);
            final int rows = step > 0 ? Math.toIntExact((end - start) / step) : 0;
            try {
                final MemorySegment names = namesPtr.reinterpret(ADDRESS.byteSize() * cols);
                final String[] columns = new String[cols];
                for (int c = 0; c < cols; c++) {
                    columns[c] = names.getAtIndex(ADDRESS, c).reinterpret(Long.MAX_VALUE).getString(0);
                }
                final MemorySegment data = dataPtr.reinterpret(JAVA_DOUBLE.byteSize() * (long) cols * rows);
                final double[][] values = new double[cols][rows];
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < cols; c++) {
                        values[c][r] = data.getAtIndex(JAVA_DOUBLE, (long) r * cols + c);
                    }
                }
                return new FetchResults(start + step, end, step, columns, values);
            } finally {
                if (!namesPtr.equals(MemorySegment.NULL)) {
                    final MemorySegment names = namesPtr.reinterpret(ADDRESS.byteSize() * cols);
                    for (int c = 0; c < cols; c++) {
                        rrdFreemem.invokeExact(names.getAtIndex(ADDRESS, c));
                    }
                    rrdFreemem.invokeExact(namesPtr);
                }
                if (!dataPtr.equals(MemorySegment.NULL)) {
                    rrdFreemem.invokeExact(dataPtr);
                }
            }
        }
    }
}
