/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
