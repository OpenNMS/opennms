/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *
 * From the original copyright headers:
 *
 * Copyright (c) 2009+ desmax74
 * Copyright (c) 2009+ The OpenNMS Group, Inc.
 *
 * This program was developed and is maintained by Rocco RIONERO
 * ("the author") and is subject to dual-copyright according to
 * the terms set in "The OpenNMS Project Contributor Agreement".
 *
 * The author can be contacted at the following email address:
 *
 *     Massimiliano Dess&igrave;
 *     desmax74@yahoo.it
 *******************************************************************************/

package org.opennms.acl.model;

/**
 * Map a OpenNms information
 *
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 * @version $Id: $
 */
public class NodeONMSDTO {

    /**
     * <p>Getter for the field <code>dpname</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDpname() {
        return dpname;
    }

    /**
     * <p>Setter for the field <code>dpname</code>.</p>
     *
     * @param dpname a {@link java.lang.String} object.
     */
    public void setDpname(String dpname) {
        this.dpname = dpname;
    }

    /**
     * <p>Getter for the field <code>nodelabel</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNodelabel() {
        return nodelabel;
    }

    /**
     * <p>Setter for the field <code>nodelabel</code>.</p>
     *
     * @param nodelabel a {@link java.lang.String} object.
     */
    public void setNodelabel(String nodelabel) {
        this.nodelabel = nodelabel;
    }

    /**
     * <p>Getter for the field <code>nodeid</code>.</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getNodeid() {
        return nodeid;
    }

    /**
     * <p>Setter for the field <code>nodeid</code>.</p>
     *
     * @param nodeid a {@link java.lang.Integer} object.
     */
    public void setNodeid(Integer nodeid) {
        this.nodeid = nodeid;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {

        if (!(o instanceof NodeONMSDTO))
            return false;
        NodeONMSDTO node = (NodeONMSDTO) o;
        return (dpname.equalsIgnoreCase(node.getDpname()) && nodelabel.equalsIgnoreCase(node.getNodelabel()) && nodeid == node.getNodeid());
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int result = hashCode;
        if (result == 0) {
            result = 17;
            result = 31 * result + dpname.hashCode();
            result = 31 * result + nodelabel.hashCode();
            result = 31 * result + nodeid.hashCode();
            hashCode = result;
        }
        return result;
    }

    private String dpname, nodelabel;
    private Integer nodeid;
    private volatile int hashCode;
}
