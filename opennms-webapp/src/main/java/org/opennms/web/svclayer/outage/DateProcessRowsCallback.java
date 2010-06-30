/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: August 16, 2006
 *
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
package org.opennms.web.svclayer.outage;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.extremecomponents.table.callback.ProcessRowsCallback;
import org.extremecomponents.table.core.TableModel;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * Overwrite Default ProcessRowsCallback to enable date filtering.
 * Example code from Nathan MA
 *
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @version $Id: $
 * @since 1.6.12
 */
public class DateProcessRowsCallback extends ProcessRowsCallback
{
    /** {@inheritDoc} */
    public Collection filterRows(TableModel model, Collection rows)
        throws Exception
    {
        boolean filtered = model.getLimit().isFiltered();
        boolean cleared = model.getLimit().isCleared();

        if (!filtered || cleared)
        {
             return rows;
        }

        if (filtered)
        {
            Collection collection = new ArrayList();
            Predicate filterPredicate = new DateFilterPredicate(model);
            CollectionUtils.select(rows, filterPredicate, collection);

            return collection;
        }

        return rows;
    }
}
