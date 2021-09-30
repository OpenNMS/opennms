/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
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

/**
 * Instantiate this class with fileName and callback implementing FileUpdateCallback
 * Whenever file updates, callback  reload() will be invoked.
 */

package org.opennms.core.fileutils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUpdateWatcher {

    private static final Logger LOG = LoggerFactory.getLogger(FileUpdateWatcher.class);

    private final FileUpdateCallback callback;

    private final WatchService watcher;

    private final String fileName;

    private final AtomicBoolean closed = new AtomicBoolean(false);

    public FileUpdateWatcher(String fileName, FileUpdateCallback fileUpdateCb) throws IOException {
        File file = new File(fileName);
        if (!file.exists() || file.isDirectory()) {
            throw new IllegalArgumentException(String.format("file %s doesn't exist", fileName));
        }
        this.fileName = fileName;
        Path path = Paths.get(file.getParent());
        this.callback = fileUpdateCb;
        watcher = path.getFileSystem().newWatchService();
        path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
        Thread thread = new Thread(new FileWatcherThread());
        thread.setName(String.format("fileUpdateWatcher-%s", file.getName()));
        thread.start();
        LOG.info("started watcher thread for file : {}", file.getName());

    }

    private class FileWatcherThread implements Runnable {

        @Override
        public void run() {
            while (!closed.get()) {
                WatchKey key = null;
                try {
                    key = watcher.take();
                } catch (Exception e) {
                    LOG.info("Watcher is either interruped or closed", e);
                    break;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    WatchEvent<Path> ev = cast(event);
                    Path dir = (Path) key.watchable();
                    Path updatedFile = dir.resolve(ev.context());
                    // Some editors create temporary buffers, watch for both create/modify
                    if (kind == StandardWatchEventKinds.ENTRY_MODIFY || kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        if (updatedFile.toString().equals(fileName)) {
                            LOG.info(" file {} got updated, send callback", updatedFile.getFileName());
                            callback.reload();
                        }
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
