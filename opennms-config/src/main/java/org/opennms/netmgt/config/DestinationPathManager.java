/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

/**
 * <p>Abstract DestinationPathManager class.</p>
 *
 * @author David Hustace <david@opennms.org>
 * @version $Id: $
 */
public abstract class DestinationPathManager {

    /**
     * 
     */
    private DestinationPaths allPaths;

    /**
     * 
     */
    private Map<String, Path> m_destinationPaths;

    /**
     * 
     */
    private Header oldHeader;

    /**
     * <p>parseXML</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @throws IOException 
     */
    protected void parseXML(final InputStream stream) throws IOException {
        try (final InputStreamReader isr = new InputStreamReader(stream)) {
            allPaths = JaxbUtils.unmarshal(DestinationPaths.class, isr);
            oldHeader = allPaths.getHeader();
            initializeDestinationPaths();
        }
    }

    private void initializeDestinationPaths() {
        m_destinationPaths = new TreeMap<String, Path>();
        for (Path curPath : allPaths.getPaths()) {
            m_destinationPaths.put(curPath.getName(), curPath);
        }
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
        m_destinationPaths.put(newPath.getName(), newPath);
    
        saveCurrent();
    }

    /**
     * <p>replacePath</p>
     *
     * @param oldName a {@link java.lang.String} object.
     * @param newPath a {@link org.opennms.netmgt.config.destinationPaths.Path} object.
     * @throws java.io.IOException if any.
     */
    public synchronized void replacePath(String oldName, Path newPath) throws IOException {
        if (m_destinationPaths.containsKey(oldName)) {
            m_destinationPaths.remove(oldName);
        }
    
        addPath(newPath);
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
        m_destinationPaths.remove(path.getName());
        saveCurrent();
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
        m_destinationPaths.remove(name);
        saveCurrent();
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
    
        header.setCreated(EventConstants.formatToString(new Date()));
    
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
