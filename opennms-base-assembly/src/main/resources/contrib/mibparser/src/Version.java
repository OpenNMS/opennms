/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2004-2014 The OpenNMS Group, Inc.
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

/**
 * Definition of versionString<br>
 * This is also to be a collection of comments that describe the changes.
 *
 * @author John Rodriguez
 * @version 1.0
 * @since 8/23/2003 <br><br>
 *
 * snmpfactotum@yahoo.com
 */
 
public class Version {
    // the version naming convention is "major.minor"
    
    /**
     * This version has the base functionality implemented to parse
     * MIBS and output XML.
     * There are some shortcuts taken, such as not handling MACROS.
     * This is because the MACROS do not have any information to be
     * output in the XML.
     */
    static public final String versionString = "1.0";
    
    /**
     * This version has (describe your changes)+
     */
    // static public final String versionString = ...
}
