/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
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
   XmpCollectionAttributeType - Encapsulate a data type used in
   collection via management protocol.  E.g. counter, gauge, string, etc.
   @author <a href="mailto:rdk@krupczak.org">Bobby Krupczak</a>
   @version $Id: XmpCollectionAttributeType.java 38 2008-07-24 13:39:32Z rdk $
 **/

package org.opennms.netmgt.collectd;

import org.apache.log4j.Category;
import org.krupczak.Xmp.Xmp;
import org.krupczak.Xmp.XmpVar;
import org.opennms.core.utils.ThreadCategory;

class XmpCollectionAttributeType implements CollectionAttributeType 
{
    /* class variables and methods *********************** */

    /* instance variables ******************************** */
    //MibObj mibObj; // this might need to be MibObj
    XmpVar aVar;
    AttributeGroupType groupType;

    /* constructors  ************************************* */
    XmpCollectionAttributeType(XmpVar aVar, AttributeGroupType groupType)
    { 
        this.aVar = aVar;
        this.groupType = groupType;
    }

    /* private methods *********************************** */
    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    /* public methods ************************************ */
    public AttributeGroupType getGroupType() { return groupType; }

    public void storeAttribute(CollectionAttribute attrib, Persister persister)
    {
        log().debug("XmpCollectionAttributeType: store "+attrib);

        // persist as either string or numeric based on our
        // XMP type

        // extendedBoolean is mapped to string for true, false, unknown

        switch (aVar.getSyntax()) {

        case Xmp.SYNTAX_COUNTER:
        case Xmp.SYNTAX_GAUGE:
        case Xmp.SYNTAX_INTEGER:
        case Xmp.SYNTAX_UNSIGNEDINTEGER:
        case Xmp.SYNTAX_FLOATINGPOINT:
            persister.persistNumericAttribute(attrib);
            break;

        case Xmp.SYNTAX_IPV4ADDRESS:
        case Xmp.SYNTAX_IPV6ADDRESS:
        case Xmp.SYNTAX_DATETIME:
        case Xmp.SYNTAX_BOOLEAN:
        case Xmp.SYNTAX_MACADDRESS:
        case Xmp.SYNTAX_PHYSADDRESS:
        case Xmp.SYNTAX_DISPLAYSTRING:
        case Xmp.SYNTAX_BINARYSTRING:
        case Xmp.SYNTAX_EXTENDEDBOOLEAN:
        case Xmp.SYNTAX_UNSUPPORTEDVAR:
            persister.persistStringAttribute(attrib);
            break;

            // should not ever see these
        case Xmp.SYNTAX_NULLSYNTAX:
        case Xmp.SYNTAX_TABLE:
        default:
            persister.persistStringAttribute(attrib);
        break;

        } /* Xmp syntax/type */

    } /* storeAttribute() */

    public String getName() { return aVar.getObjName(); }
    public String getType() { return Xmp.syntaxToString(aVar.getSyntax()); }

    public String toString() { return "XmpCollectionAttributeType "+Xmp.syntaxToString(aVar.getSyntax()); }

} /* class XmpCollectionAttributeType */
