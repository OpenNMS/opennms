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
package org.opennms.netmgt.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.destinationPaths.DestinationPaths;
import org.opennms.netmgt.config.destinationPaths.Header;
import org.opennms.netmgt.config.destinationPaths.Path;
import org.opennms.netmgt.config.destinationPaths.Target;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventDatetimeFormatter;

/**
 * <p>Abstract DestinationPathManager class.</p>
 *
 * @author David Hustace <david@opennms.org>
 */
public abstract class DestinationPathManager {
    private static final EventDatetimeFormatter FORMATTER = EventConstants.getEventDatetimeFormatter();

    private DestinationPaths allPaths;

    // replaced wholesale on reload and read without synchronization from
    // getPath/getPaths, so the swap must be safely published
    private volatile Map<String, Path> m_destinationPaths = new TreeMap<String, Path>();

    private Header oldHeader;

    /**
     * <p>parseXML</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @throws IOException 
     */
    protected synchronized void parseXML(final InputStream stream) throws IOException {
        try (final InputStreamReader isr = new InputStreamReader(stream)) {
            allPaths = JaxbUtils.unmarshal(DestinationPaths.class, isr);
            oldHeader = allPaths.getHeader();
            initializeDestinationPaths();
        }
    }

    private void initializeDestinationPaths() {
        // Build the replacement map and swap the field reference in one
        // assignment rather than clearing in place: a reload replaces memory
        // with the file (instead of merging over it), while a reader holding an
        // earlier unmodifiableMap keeps a consistent snapshot and saveCurrent()'s
        // iteration is never emptied mid-flight.
        final Map<String, Path> paths = new TreeMap<>();
        for (Path curPath : allPaths.getPaths()) {
            paths.put(curPath.getName(), curPath);
        }
        m_destinationPaths = paths;
    }

    /**
     * <p>getPath</p>
     *
     * @param pathName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.destinationPaths.Path} object.
     * @throws java.io.IOException if any.
     */
    public Path getPath(String pathName) throws IOException {
        update();
    
        return m_destinationPaths.get(pathName);
    }

    /**
     * <p>getPaths</p>
     *
     * @return a {@link java.util.Map} object.
     * @throws java.io.IOException if any.
     */
    public Map<String, Path> getPaths() throws IOException {
        update();
    
        return Collections.unmodifiableMap(m_destinationPaths);
    }

    /**
     * <p>getTargetCommands</p>
     *
     * @param path a {@link org.opennms.netmgt.config.destinationPaths.Path} object.
     * @param index a int.
     * @param target a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     * @throws java.io.IOException if any.
     */
    public Collection<String> getTargetCommands(Path path, int index, String target) throws IOException {
        update();
    
        Target[] targets = getTargetList(index, path);
    
        for (int i = 0; i < targets.length; i++) {
            if (targets[i].getName().equals(target))
                return targets[i].getCommands();
        }
    
        // default null value if target isn't found in Path
        return null;
    }

    /**
     * <p>getTargetList</p>
     *
     * @param index a int.
     * @param path a {@link org.opennms.netmgt.config.destinationPaths.Path} object.
     * @return an array of {@link org.opennms.netmgt.config.destinationPaths.Target} objects.
     * @throws java.io.IOException if any.
     */
    public Target[] getTargetList(int index, Path path) throws IOException {
        update();
    
        Target[] targets = null;
        // index of -1 indicates the initial targets, any other index means to
        // get
        // the targets from the Escalate object at that index
        if (index == -1) {
            targets = path.getTargets().toArray(new Target[0]);
        } else {
            targets = path.getEscalates().get(index).getTargets().toArray(new Target[0]);
        }
    
        return targets;
    }

    /**
     * <p>pathHasTarget</p>
     *
     * @param path a {@link org.opennms.netmgt.config.destinationPaths.Path} object.
     * @param target a {@link java.lang.String} object.
     * @return a boolean.
     * @throws java.io.IOException if any.
     */
    public boolean pathHasTarget(Path path, String target) throws IOException {
        update();

        for (Target curTarget : path.getTargets()) {
            if (curTarget.getName().equals(target))
                return true;
        }
    
        // default false value if target isn't found
        return false;
    }

    /**
     * <p>addPath</p>
     *
     * @param newPath a {@link org.opennms.netmgt.config.destinationPaths.Path} object.
     * @throws java.io.IOException if any.
     */
    public synchronized void addPath(Path newPath) throws IOException {
        saveWithRollback(() -> m_destinationPaths.put(newPath.getName(), newPath));
    }

    @FunctionalInterface
    private interface ConfigChange {
        void apply() throws IOException;
    }

    /**
     * Applies an in-memory change and persists it, restoring the previous paths if
     * persistence fails. saveCurrent() marshals the model only after the caller has
     * already changed the map, so a change that leaves the config unmarshallable —
     * e.g. removing the last path, which violates destinationPaths.xsd — would
     * otherwise leave memory diverged from the on-disk file until a restart.
     *
     * Shallow snapshot: safe only while every mutator replaces whole Path objects. An
     * in-place edit like NotificationManager.replaceNotification would need a deep copy.
     */
    private synchronized void saveWithRollback(final ConfigChange change) throws IOException {
        final Map<String, Path> snapshot = new TreeMap<>(m_destinationPaths);
        try {
            change.apply();
            saveCurrent();
        } catch (final RuntimeException | IOException e) {
            // Restore by swapping the reference: clear()+putAll() would expose a
            // transient empty map to readers holding the unmodifiable view, which
            // is exactly what initializeDestinationPaths() swaps references to avoid.
            m_destinationPaths = snapshot;
            throw e;
        }
    }

    /**
     * <p>replacePath</p>
     *
     * @param oldName a {@link java.lang.String} object.
     * @param newPath a {@link org.opennms.netmgt.config.destinationPaths.Path} object.
     * @throws java.io.IOException if any.
     */
    public synchronized void replacePath(String oldName, Path newPath) throws IOException {
        // one atomic change so a failed save rolls back both the remove and the add
        saveWithRollback(() -> {
            m_destinationPaths.remove(oldName);
            m_destinationPaths.put(newPath.getName(), newPath);
        });
    }

    /**
     * Removes a Path from the xml file.
     *
     * @param path
     *            the path to remove
     * @exception IOException
     * @throws java.io.IOException if any.
     */
    public synchronized void removePath(Path path) throws IOException {
        saveWithRollback(() -> m_destinationPaths.remove(path.getName()));
    }

    /**
     * Removes a Path form the xml file based on its name
     *
     * @param name
     *            the name of the path to remove
     * @exception IOException
     * @throws java.io.IOException if any.
     */
    public synchronized void removePath(String name) throws IOException {
        saveWithRollback(() -> m_destinationPaths.remove(name));
    }

    /**
     * <p>saveCurrent</p>
     *
     * @throws java.io.IOException if any.
     */
    public synchronized void saveCurrent() throws IOException {
        allPaths.clearPaths();
        for (Path path : m_destinationPaths.values()) {
            allPaths.addPath(path);
        }
    
        allPaths.setHeader(rebuildHeader());
    
        // Marshal to a string first, then write the string to the file. This
        // way the original config
        // isn't lost if the XML from the marshal is hosed.
        StringWriter stringWriter = new StringWriter();
        JaxbUtils.marshal(allPaths, stringWriter);
        String writerString = stringWriter.toString();
        saveXML(writerString);
    
        /*
         * TODO: what do do about this?  Should this be here?
         * Appears that everything is handled through the update
         * method when a member of field is requested.
         * 
         * Delete after all Notifd tests are passing.
         */
        //reload();
    }

    /**
     * <p>saveXML</p>
     *
     * @param writerString a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     */
    protected abstract void saveXML(String writerString) throws IOException;
    
    /**
     * 
     */
    private Header rebuildHeader() {
        Header header = oldHeader;
    
        header.setCreated(FORMATTER.format(new Date()));
    
        return header;
    }

    /**
     * <p>update</p>
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     */
    public abstract void update() throws IOException, FileNotFoundException;

}
