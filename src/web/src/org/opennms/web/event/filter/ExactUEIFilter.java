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


/** Encapsulates filtering on exact unique event identifiers. */
public class ExactUEIFilter extends Object implements Filter {
    public static final String TYPE = "exactUei";
    protected String uei;

    public ExactUEIFilter( String uei ) {
        if( uei == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        this.uei = uei;
    }

    public String getSql() {
        return( " EVENTUEI='" + this.uei + "'" );
    }

    public String getDescription() {
        return( TYPE + "=" + this.uei );
    }
    
    public String getTextDescription() {
        return this.getDescription();
    }

    public String toString() {
        return( "<EventFactory.ExactUEIFilter: " + this.getDescription() + ">" );
    }

    public String getUEI() {
        return( this.uei );
    }

    public boolean equals( Object obj ) {
        return( this.toString().equals( obj.toString() ));
    }
}


