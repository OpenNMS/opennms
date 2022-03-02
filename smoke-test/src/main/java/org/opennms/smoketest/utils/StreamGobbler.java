/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Starts a thread that reads lines from an input stream, logs them, and stores them in an array list. The list of
 * stored lines is used to search for expected output.
 * <p>
 * Used for consuming {@code stdout} and {@code stderr} of processes. Processes get blocked when their output buffers
 * are full. Consuming their output prevents them from getting blocked.
 */
public class StreamGobbler extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(StreamGobbler.class);
    private static final AtomicInteger counter = new AtomicInteger();

    private final InputStream stream;
    private final String kind;
    private final List<String> lines = new ArrayList<>();


    public StreamGobbler(InputStream stream, String kind) {
        super("StreamGobbler-" + counter.incrementAndGet());
        this.stream = stream;
        this.kind = kind;
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        try (var inputStreamReader = new InputStreamReader(stream)) {
            try (var bufferedReader = new BufferedReader(inputStreamReader)) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    LOG.debug(kind + ": " + line);
                    synchronized (lines) {
                        lines.add(line);
                    }
                }
            }
        } catch (InterruptedIOException e) {
            // ignore
        } catch (Throwable e) {
            LOG.error("error reading stream", e);
        }
        LOG.debug(kind + "<EOF>");
    }

    public List<String> getLines() {
        synchronized (lines) {
            return new ArrayList(lines);
        }
    }
}
