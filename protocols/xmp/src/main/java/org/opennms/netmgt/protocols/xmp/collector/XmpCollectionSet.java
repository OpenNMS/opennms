/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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
 *******************************************************************************/

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
 *   Xmp CollectionSet class serves as a container for a collection of
 *   query results for the OpenNMS network management software suite.
 *   @author <a href="mailto:rdk@krupczak.org">Bobby Krupczak</a>
 *   @version $Id: XmpCollectionSet.java 38 2008-07-24 13:39:32Z rdk $
 */

package org.opennms.netmgt.protocols.xmp.collector;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.ServiceCollector;
import org.opennms.netmgt.config.collector.CollectionSet;
import org.opennms.netmgt.config.collector.CollectionSetVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class XmpCollectionSet implements CollectionSet {

    /* class variables and methods *********************** */
	private static final Logger LOG = LoggerFactory.getLogger(XmpCollectionSet.class);


    /* instance variables ******************************** */
    int status;
    boolean ignorePersistVar;
    CollectionAgent agent;
    XmpCollectionResource collectionResource;
    Set<XmpCollectionResource>listOfResources;
    private Date m_timestamp;

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
    

    /* public methods ************************************ */

    /**
     * <p>addResource</p>
     *
     * @param aResource a {@link org.opennms.netmgt.protocols.xmp.collector.XmpCollectionResource} object.
     */
    public void addResource(XmpCollectionResource aResource)
    {
        listOfResources.add(aResource);
    }

    /**
     * <p>getResources</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<XmpCollectionResource>getResources() 
    { 
        return listOfResources; 
    }

    // return a ServiceCollector status value 
    /**
     * <p>getCollectionAgent</p>
     *
     * @return a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     */
    public CollectionAgent getCollectionAgent() { return agent; }
    /**
     * <p>setCollectionAgent</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     */
    public void setCollectionAgent(CollectionAgent agent) { this.agent = agent; }

    /**
     * <p>Getter for the field <code>status</code>.</p>
     *
     * @return a int.
     */
    @Override
    public int getStatus() { return status; }
    /**
     * <p>Setter for the field <code>status</code>.</p>
     *
     * @param status a int.
     */
    public void setStatus(int status) { this.status = status; }

    /**
     * <p>setStatusSuccess</p>
     */
    public void setStatusSuccess() { this.status = ServiceCollector.COLLECTION_SUCCEEDED; }
    /**
     * <p>setStatusFailed</p>
     */
    public void setStatusFailed() { this.status = ServiceCollector.COLLECTION_FAILED; }

    // ignorePersist returns true if system has been restarted
    // that is, if sysUpTime has gone backwards, return true
    // if system has continued, return false

    /**
     * <p>ignorePersist</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean ignorePersist() { return ignorePersistVar; }

    /**
     * <p>ignorePersistTrue</p>
     */
    public void ignorePersistTrue() { ignorePersistVar = true; }
    /**
     * <p>ignorePersistFalse</p>
     */
    public void ignorePersistFalse() { ignorePersistVar = false; }

    // Visitor design pattern 

    // visit is called repeatedly with a vistor and I fill in values
    // into CollectionSetVisitor 

    //public XmpCollectionResource getResource() { return collectionResource; }

    /** {@inheritDoc} */
    @Override
    public void visit(CollectionSetVisitor visitor) 
    {
        LOG.debug("XmpCollectionSet: visit starting for set {}", agent);

        visitor.visitCollectionSet(this);

        // iterate over our collection set resources; only one right now
        // this will change
        // collectionResource.visit(visitor);

        for (XmpCollectionResource resource: getResources()) {
            resource.visit(visitor);
        }

        visitor.completeCollectionSet(this);
    }

    @Override
	public Date getCollectionTimestamp() {
		return m_timestamp;
	}
    public void setCollectionTimestamp(Date timestamp) {
    	this.m_timestamp = timestamp;
	}

} /* class XmpCollectionSet */
