/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 24, 2008  - rdk@krupczak.org
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

/*
* OCA CONTRIBUTION ACKNOWLEDGEMENT - NOT PART OF LEGAL BOILERPLATE
* DO NOT DUPLICATE THIS COMMENT BLOCK WHEN CREATING NEW FILES!
*
* This file was contributed to the OpenNMS(R) project under the
* terms of the OpenNMS Contributor Agreement (OCA).  For details on
* the OCA, see http://www.opennms.org/index.php/Contributor_Agreement
*
* Contributed under the terms of the OCA by:
*
* Bobby Krupczak <rdk@krupczak.org>
* THE KRUPCZAK ORGANIZATION, LLC
* http://www.krupczak.org/
*/

/** 
   Xmp CollectionSet class serves as a container for a collection of
   query results for the OpenNMS network management software suite.
   @author <a href="mailto:rdk@krupczak.org">Bobby Krupczak</a>
   @version $Id: XmpCollectionSet.java 38 2008-07-24 13:39:32Z rdk $
 **/

package org.opennms.netmgt.collectd;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.opennms.core.utils.ThreadCategory;

public class XmpCollectionSet implements CollectionSet {

    /* class variables and methods *********************** */

    /* instance variables ******************************** */
    int status;
    boolean ignorePersistVar;
    CollectionAgent agent;
    XmpCollectionResource collectionResource;
    Set<XmpCollectionResource>listOfResources;

    /* constructors  ************************************* */
    XmpCollectionSet(CollectionAgent agent) 
    {  
        // default status
        status = ServiceCollector.COLLECTION_SUCCEEDED;
        ignorePersistVar = false;
        this.agent = agent;

        // this is going to change
        //collectionResource = new XmpCollectionResource(agent,"node",null);

        listOfResources = new HashSet<XmpCollectionResource>();

        return; 
    }

    /* private methods *********************************** */
    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    /* public methods ************************************ */

    public void addResource(XmpCollectionResource aResource)
    {
        listOfResources.add(aResource);
    }

    public Collection<XmpCollectionResource>getResources() 
    { 
        return listOfResources; 
    }

    // return a ServiceCollector status value 
    public CollectionAgent getCollectionAgent() { return agent; }
    public void setCollectionAgent(CollectionAgent agent) { this.agent = agent; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public void setStatusSuccess() { this.status = ServiceCollector.COLLECTION_SUCCEEDED; }
    public void setStatusFailed() { this.status = ServiceCollector.COLLECTION_FAILED; }

    // ignorePersist returns true if system has been restarted
    // that is, if sysUpTime has gone backwards, return true
    // if system has continued, return false

    public boolean ignorePersist() { return ignorePersistVar; }

    public void ignorePersistTrue() { ignorePersistVar = true; }
    public void ignorePersistFalse() { ignorePersistVar = false; }

    // Visitor design pattern 

    // visit is called repeatedly with a vistor and I fill in values
    // into CollectionSetVisitor 

    //public XmpCollectionResource getResource() { return collectionResource; }

    public void visit(CollectionSetVisitor visitor) 
    {
        log().debug("XmpCollectionSet: visit starting for set "+agent);

        visitor.visitCollectionSet(this);

        // iterate over our collection set resources; only one right now
        // this will change
        // collectionResource.visit(visitor);

        for (XmpCollectionResource resource: getResources()) {
            resource.visit(visitor);
        }

        visitor.completeCollectionSet(this);
    }

} /* class XmpCollectionSet */
