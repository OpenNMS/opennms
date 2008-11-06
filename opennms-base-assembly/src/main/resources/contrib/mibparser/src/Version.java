// This file is part of the OpenNMS(R) MIB Parser.
//
// Copyright (C) 2002-2003 John Rodriguez
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
// See: http://www.fsf.org/copyleft/lesser.html
//

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
