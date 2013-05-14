/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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
 * @since 1.8.1
 */
@SuppressWarnings("unchecked")
public class DateProcessRowsCallback extends ProcessRowsCallback
{
    /** {@inheritDoc} */
    @Override
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
