//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.sortova.com/
//
package org.opennms.web.event.filter;


/** Encapsulates all interface filtering functionality. */
public class NegativeInterfaceFilter extends Object implements Filter {
    public static final String TYPE = "interfacenot";
    protected String ipAddress;

    public NegativeInterfaceFilter( String ipAddress ) {
        if( ipAddress == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        this.ipAddress = ipAddress;
    }

    public String getSql() {
        return( " (IPADDR<>'" + this.ipAddress + "' OR IPADDR IS NULL)" );
    }

    public String getDescription() {
        return( TYPE + "=" + this.ipAddress );
    }
    
    public String getTextDescription() {
        return( "interface is not " + this.ipAddress );
    }

    public String toString() {
        return( "<EventFactory.NegativeInterfaceFilter: " + this.getDescription() + ">" );
    }

    public String getIpAddress() {
        return( this.ipAddress );
    }

    public boolean equals( Object obj ) {
        return( this.toString().equals( obj.toString() ));
    }
}
