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

package org.opennms.web.notification;

import java.util.ArrayList;


/**
 * Convenience data structure for holding the arguments to an notice query. 
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class NoticeQueryParms extends Object 
{
    public NoticeFactory.SortStyle sortStyle;
    public NoticeFactory.AcknowledgeType ackType;
    public ArrayList filters;
    public int limit;
    public int multiple;


    /**
     * Convert the internal (and useful) ArrayList filters object
     * into an array of EventFactory.Filter instances.
     */ 
    public NoticeFactory.Filter[] getFilters() {
        NoticeFactory.Filter[] filters = new NoticeFactory.Filter[this.filters.size()];                        
        filters = (NoticeFactory.Filter[])this.filters.toArray( filters );
        return( filters );
    }
}
