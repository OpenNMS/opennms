//
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
 * Definition of error values for System.exit()
 * @author John Rodriguez
 * @version 1.0
 * @since 8/23/2003 <br><br>
 *
 * snmpfactotum@yahoo.com
 */
 
public class Errors {
    // these int values are to be used by System.exit on fatal parser errors.
    // register new errors at any time. this is just to supply meaning to exit
    
    static public final int UNKNOWN_FATAL = 1;
    static public final int INCLUDE_MIB_MISSING = 2;
    static public final int FILE_NOT_FOUND = 3;
}
