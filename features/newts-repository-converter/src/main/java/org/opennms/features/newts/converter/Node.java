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
package org.opennms.features.newts.converter;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The Class Node.
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class Node {

    /** The id. */
    private int id;
    
    /** The label. */
    private String label;
    
    /** The foreign id. */
    private String foreignId;
    
    /** The foreign source. */
    private String foreignSource;

    /**
     * Instantiates a new node.
     *
     * @param id the id
     * @param label the label
     * @param foreignId the foreign id
     * @param foreignSource the foreign source
     */
    public Node(int id, String label, String foreignId, String foreignSource) {
        super();
        this.id = id;
        this.label = label;
        this.foreignId = foreignId;
        this.foreignSource = foreignSource;
    }

    /**
     * Instantiates a new node.
     *
     * @param rs the rs
     * @throws SQLException the SQL exception
     */
    public Node(ResultSet rs) throws SQLException {
        this(rs.getInt("nodeid"), rs.getString("nodelabel"), rs.getString("foreignsource"), rs.getString("foreignid"));
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the label.
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label.
     *
     * @param label the new label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Gets the foreign id.
     *
     * @return the foreign id
     */
    public String getForeignId() {
        return foreignId;
    }

    /**
     * Sets the foreign id.
     *
     * @param foreignId the new foreign id
     */
    public void setForeignId(String foreignId) {
        this.foreignId = foreignId;
    }

    /**
     * Gets the foreign source.
     *
     * @return the foreign source
     */
    public String getForeignSource() {
        return foreignSource;
    }

    /**
     * Sets the foreign source.
     *
     * @param foreignSource the new foreign source
     */
    public void setForeignSource(String foreignSource) {
        this.foreignSource = foreignSource;
    }

    /**
     * Gets the dir.
     *
     * @param baseDir the base dir
     * @param storeByFS the store by fs
     * @return the dir
     */
    public File getDir(File baseDir, boolean storeByFS) {
        if (foreignSource == null || storeByFS == false)
            return new File(baseDir, Integer.toString(id));
        return new File(baseDir, foreignSource + File.separator + foreignId);
    }

    /**
     * Gets the FS id.
     *
     * @return the FS id
     */
    public String getFSId() {
        return foreignSource + ':' + foreignId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Node[id=" + id + ", label=" + label + ", foreignSource=" + foreignSource + ", foreignId=" + foreignId + "]";
    }


}
