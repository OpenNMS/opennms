//============================================================================
//
// Copyright (c) 2009+ desmax74
// Copyright (c) 2009+ The OpenNMS Group, Inc.
// All rights reserved everywhere.
//
// This program was developed and is maintained by Rocco RIONERO
// ("the author") and is subject to dual-copyright according to
// the terms set in "The OpenNMS Project Contributor Agreement".
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
// USA.
//
// The author can be contacted at the following email address:
//
//       Massimiliano Dess&igrave;
//       desmax74@yahoo.it
//
//
//-----------------------------------------------------------------------------
// OpenNMS Network Management System is Copyright by The OpenNMS Group, Inc.
//============================================================================
package org.opennms.acl.model;

/**
 * Map a OpenNms information
 * 
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 */
public class NodeONMSDTO {

    public String getDpname() {
        return dpname;
    }

    public void setDpname(String dpname) {
        this.dpname = dpname;
    }

    public String getNodelabel() {
        return nodelabel;
    }

    public void setNodelabel(String nodelabel) {
        this.nodelabel = nodelabel;
    }

    public Integer getNodeid() {
        return nodeid;
    }

    public void setNodeid(Integer nodeid) {
        this.nodeid = nodeid;
    }

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof NodeONMSDTO))
            return false;
        NodeONMSDTO node = (NodeONMSDTO) o;
        return (dpname.equalsIgnoreCase(node.getDpname()) && nodelabel.equalsIgnoreCase(node.getNodelabel()) && nodeid == node.getNodeid());
    }

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
