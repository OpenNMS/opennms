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
package org.opennms.features.vaadin.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.vaadin.v7.data.util.FilesystemContainer;

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
