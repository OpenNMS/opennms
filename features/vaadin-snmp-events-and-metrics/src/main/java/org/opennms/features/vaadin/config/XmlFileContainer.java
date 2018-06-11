/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.vaadin.data.util.FilesystemContainer;

/**
 * The Class XmlFileContainer.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class XmlFileContainer extends FilesystemContainer {

    /** The exclude list. */
    protected List<String> excludeList = new ArrayList<>();

    /**
     * Instantiates a new XML file container.
     *
     * @param root the root
     * @param recursive the recursive flag
     */
    public XmlFileContainer(File root, boolean recursive) {
        super(root, "xml", recursive);
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.util.FilesystemContainer#getItemIds()
     */
    @Override
    public Collection<File> getItemIds() {
        Collection<File> files = new ArrayList<File>(super.getItemIds());
        Iterator<File> it = files.iterator();
        while (it.hasNext()) {
            File f = it.next();
            if (f.isDirectory() || excludeList.contains(f.getName())) {
                it.remove();
            }
        }
        return Collections.unmodifiableCollection(files);
    }

    /**
     * Adds the exclude file.
     *
     * @param excludeFile the exclude file
     */
    public void addExcludeFile(String excludeFile) {
        excludeList.add(excludeFile);
    }

    /**
     * Removes the exclude file.
     *
     * @param excludeFile the exclude file
     */
    public void removeExcludeFile(String excludeFile) {
        excludeList.remove(excludeFile);
    }

}
