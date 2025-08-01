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
import java.util.concurrent.TimeUnit;

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

    private volatile boolean m_stopped = true;

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
            while (!m_stopped) {
                if (m_thread.isInterrupted()) {
                    break;
                }
                WatchKey key;
                try {
                    LOG.trace("waiting for create event");
                    key = watcher.poll(1, TimeUnit.SECONDS);
                    if (key == null) {
                        continue;
                    }
                    LOG.trace("got an event, process it");
                } catch (InterruptedException ie) {
                    LOG.info("interrupted, must be time to shut down...");
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
    public synchronized void start() throws InterruptedException {
        if (m_thread != null) {
            stop();
        }
        LOG.trace("starting monitor");
        m_thread = new Thread(this, "DirectoryWatcher-" + m_directory.getParentFile().getName() + '-' + m_directory.getName());
        m_stopped = false;
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
    public synchronized void stop() throws InterruptedException {
        LOG.trace("stopping monitor");
        m_stopped = true;
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
