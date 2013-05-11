/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

/************************************************************************
 * Change history
 *
 * 2013-04-18 Updated package names to match new XMP JAR (jeffg@opennms.org)
 *
 ************************************************************************/

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
 *   XmpCollectionAttribute is an actual data point collected via XMP;
 *   what this means in English is that we've finally arrived at an
 *   actual Xmp variable -- something that has a MIB object name, type,
 *   and value.
 *   @author <a href="mailto:rdk@krupczak.org">Bobby Krupczak</a>
 *   @version $Id: XmpCollectionAttribute.java 38 2008-07-24 13:39:32Z rdk $
 */

package org.opennms.netmgt.protocols.xmp.collector;

import org.krupczak.xmp.Xmp;
import org.krupczak.xmp.XmpVar;
import org.opennms.netmgt.collectd.AbstractCollectionAttribute;
import org.opennms.netmgt.config.collector.CollectionAttribute;
import org.opennms.netmgt.config.collector.CollectionAttributeType;
import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.config.collector.ServiceParameters;
public class XmpCollectionAttribute extends AbstractCollectionAttribute 
implements CollectionAttribute 
{
    /* class variables and methods *********************** */

    /* instance variables ******************************** */
    private XmpVar aVar;
    private String alias;
    private XmpCollectionResource resource;
    private CollectionAttributeType attribType;

    /* constructors  ************************************* */
    XmpCollectionAttribute() { aVar = null; }

    XmpCollectionAttribute(XmpVar aVar, String alias, XmpCollectionResource res)
    {
        this.aVar = aVar;
        this.alias = alias;
        this.resource = res;
    }

    XmpCollectionAttribute(XmpCollectionResource res, 
                           CollectionAttributeType attribType, 
                           String alias, XmpVar aVar) 
                           {
        this(aVar,alias,res);
        this.attribType = attribType;
                           }

    /* private methods *********************************** */

    /* public methods ************************************ */

    /**
     * <p>getAttributeType</p>
     *
     * @return a {@link org.opennms.netmgt.config.collector.CollectionAttributeType} object.
     */
    @Override
    public CollectionAttributeType getAttributeType() { return attribType; }

    /**
     * <p>setAttributeType</p>
     *
     * @param attribType a {@link org.opennms.netmgt.config.collector.CollectionAttributeType} object.
     */
    public void setAttributeType(CollectionAttributeType attribType)
    {
        this.attribType = attribType;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() 
    { 
        return new String(alias);
    }

    /**
     * <p>Getter for the field <code>resource</code>.</p>
     *
     * @return a {@link org.opennms.netmgt.config.collector.CollectionResource} object.
     */
    @Override
    public CollectionResource getResource() { return resource; }

    /**
     * <p>getNumericValue</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getNumericValue() { return aVar.getValue(); }

    /**
     * <p>getType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getType() { return Xmp.syntaxToString(aVar.xmpSyntax); }

    /** {@inheritDoc} */
    @Override
    public boolean shouldPersist(ServiceParameters params) { return true; }

    //public void visit(CollectionSetVisitor visitor) { super(visitor); }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() 
    { 
        return "XmpCollectionAttribute "+alias+"="+aVar.getValue()+" attribType="+attribType; 
    }

    /**
     * <p>getStringValue</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getStringValue() { return aVar.getValue(); }

    @Override
    public String getMetricIdentifier() {
        return "Not supported yet._" + "XMP_" + getName();
    }

}
