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
package org.opennms.core.fileutils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.atomic.AtomicBoolean;

public class DotDUpdateWatcher {
    private static final Logger LOG = LoggerFactory.getLogger(DotDUpdateWatcher.class);

    private final FileUpdateCallback callback;

    private final WatchService watcher;

    private final String directory;

    private final FilenameFilter filenameFilter;

    private final AtomicBoolean closed = new AtomicBoolean(false);

    public DotDUpdateWatcher(String directory, FilenameFilter filenameFilter, FileUpdateCallback fileUpdateCb) throws IOException {
        this.directory = directory;
        this.filenameFilter = filenameFilter;
        this.callback = fileUpdateCb;

        final File file = new File(directory);
        final Path path = file.toPath();

        if (!file.exists() || !file.isDirectory()) {
            throw new IllegalArgumentException(String.format("directory %s doesn't exist", directory));
        }

        watcher = path.getFileSystem().newWatchService();
        path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

        final Thread thread = new Thread(new DotDUpdateWatcher.FileWatcherThread());
        thread.setName(String.format("dotDUpdateWatcher-%s", file.getName()));
        thread.start();

        LOG.info("started watcher thread for directory : {}", file.getName());
    }

    private class FileWatcherThread implements Runnable {
        @Override
        public void run() {
            while (!closed.get()) {
                WatchKey key = null;
                try {
                    key = watcher.take();
                } catch (Exception e) {
                    LOG.info("Watcher is either interrupted or closed", e);
                    break;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    WatchEvent<Path> ev = cast(event);
                    Path dir = (Path) key.watchable();
                    Path updatedFile = dir.resolve(ev.context());

                    // Some editors create temporary buffers, watch for both create/modify
                    if (filenameFilter.accept(dir.toFile(), updatedFile.getFileName().toString())) {
                        LOG.info(" file {} in directory {} got updated, send callback", updatedFile.getFileName(), directory);
                        callback.reload();
                    }
                    if (!key.reset()) {
                        break;
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    public void destroy() {
        try {
            if (watcher != null) {
                watcher.close();
            }
        } catch (Exception e) {
            LOG.info("Exception while closing watcher", e);
        }
        closed.set(true);
    }
}
