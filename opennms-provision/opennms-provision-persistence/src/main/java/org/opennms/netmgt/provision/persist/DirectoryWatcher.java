/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.persist;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.opennms.core.spring.FileReloadCallback;
import org.opennms.core.spring.FileReloadContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class DirectoryWatcher.
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 * @param <T> the object representation of the file
 */
public class DirectoryWatcher<T> implements Runnable, Closeable {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DirectoryWatcher.class);

    /** The path object (associated with the directory object). */
    private Path m_path;

    /** The internal thread for monitoring the file system. */
    private Thread m_thread;

    /** The source directory. */
    private File m_directory;

    /** The loader callback. */
    private FileReloadCallback<T> m_loader;

    /** The internal cache of container objects. */
    private ConcurrentHashMap<String, FileReloadContainer<T>> m_contents = new ConcurrentHashMap<String, FileReloadContainer<T>>();

    /**
     * Instantiates a new directory watcher.
     *
     * @param directory the directory
     * @param loader the loader
     * @throws InterruptedException the interrupted exception
     */
    public DirectoryWatcher(File directory, FileReloadCallback<T> loader) throws InterruptedException {
        m_directory = directory;
        if (!m_directory.exists()) {
            if(!m_directory.mkdirs()) {
                LOG.warn("Could not make directory: {}", m_directory.getPath());
            }
        }
        m_loader = loader;
        if (m_directory != null) {
            m_path = Paths.get(m_directory.getAbsolutePath());
            start();
        }
    }

    /* (non-Javadoc)
     * @see java.io.Closeable#close()
     */
    @Override
    public void close() throws IOException {
        try {
            stop();
        }
        catch (InterruptedException e) {
            LOG.warn( "request to stop failed, guess its time to stop being polite!" );
        }
    }

    /**
     * Join.
     *
     * @throws InterruptedException the interrupted exception
     */
    public void join() throws InterruptedException {
        m_thread.join();
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        LOG.info("starting run loop");
        try (WatchService watcher = m_path.getFileSystem().newWatchService()) {
            LOG.debug("registering create watcher on {}", m_path.toAbsolutePath().toString());
            m_path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
            LOG.debug("watcher registration complete for {}", m_path.toAbsolutePath().toString());
            synchronized (this) {
                this.notifyAll();
            }
            while (true) {
                if (m_thread.isInterrupted()) {
                    break;
                }
                WatchKey key = null;
                try {
                    LOG.debug("waiting for create event");
                    key = watcher.take();
                    LOG.debug("got an event, process it");
                } catch (InterruptedException ie) {
                    LOG.info("interruped, must be time to shut down...");
                    break;
                }

                for (WatchEvent<?> watchEvent : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = watchEvent.kind();
                    Path pathChanged = ((WatchEvent<Path>) watchEvent).context();
                    final String fileName = pathChanged.toString();
                    final File file = new File(m_directory, fileName);
                    if (file.isDirectory()) { // Ignoring changes on directories.
                        LOG.debug("{} is a directory, ignoring.", file);
                        continue;
                    }
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        LOG.debug("overflow receiving, ignoring changes.");
                        continue;
                    } else if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        LOG.info("file '{}' created. Ignoring...", fileName);
                    } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                        LOG.info("file '{}' modified. Removing entry from cache.", fileName);
                        m_contents.remove(fileName);
                    } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                        LOG.info("file '{}' deleted. Removing entry from cache.", fileName);
                        m_contents.remove(fileName);
                    }
                    // IMPORTANT: The key must be reset after processed
                    if (! key.reset()) {
                        break;
                    }
                }
            }
        } catch (IOException ioe) {
            LOG.error(ioe.getMessage(), ioe);
        }
        LOG.info("existing run loop");
    }

    /**
     * Start.
     *
     * @throws InterruptedException the interrupted exception
     */
    public void start() throws InterruptedException {
        LOG.trace("starting monitor");
        m_thread = new Thread(this, "DirectoryWatcher-" + m_directory.getParentFile().getName() + '-' + m_directory.getName());
        m_thread.start();
        synchronized (this) {
            this.wait();
        }
        LOG.trace("monitor started");
    }

    /**
     * Stop.
     *
     * @throws InterruptedException the interrupted exception
     */
    public void stop() throws InterruptedException {
        LOG.trace("stopping monitor");
        m_thread.interrupt();
        m_thread.join();
        m_thread = null;
        LOG.trace("monitor stopped");
    }

    /**
     * Gets the file names.
     *
     * @return the file names
     */
    public Set<String> getFileNames() {
        final String[] list = m_directory.list();
        return new LinkedHashSet<String>(Arrays.asList(list));
    }

    /**
     * Gets the base names with extension.
     *
     * @param extension the extension
     * @return the base names with extension
     */
    public Set<String> getBaseNamesWithExtension(String extension) {
        Set<String> fileNames = getFileNames();
        Set<String> basenames = new LinkedHashSet<String>(); 
        for(String fileName : fileNames) {
            if (fileName.endsWith(extension)) {
                String basename = fileName.substring(0, fileName.length()-extension.length());
                basenames.add(basename);
            }
        }
        return basenames;
    }

    /**
     * Gets the contents.
     *
     * @param fileName the file name
     * @return the contents
     * @throws FileNotFoundException the file not found exception
     */
    public T getContents(String fileName) throws FileNotFoundException {
        File file = new File(m_directory, fileName);
        if (file.exists() && !file.isDirectory()) {
            FileReloadContainer<T> newContainer = new FileReloadContainer<T>(file, m_loader);
            newContainer.setReloadCheckInterval(0);
            FileReloadContainer<T> container = m_contents.putIfAbsent(fileName, newContainer);
            if (container == null) {
                LOG.debug("getting content of {}", file);
                container = newContainer;
            }
            return container.getObject();
        }
        m_contents.remove(fileName);
        throw new FileNotFoundException("there is no file called '" + fileName + "' in directory " + m_directory);
    }

}
