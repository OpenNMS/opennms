/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.api;

import java.util.Date;
import java.util.List;


/**
 * {@link CollectionSet} represents the results of a collection and generally includes
 * a {@link List} of {@link CollectionResource} instances that were created during the
 * collection.
 */
public interface CollectionSet extends CollectionVisitable {

    CollectionStatus getStatus();

    /**
     * <p>ignorePersist</p>
     *
     * @return a boolean.
     */
    boolean ignorePersist();
    
    /**
     * Returns the timestamp of when this data collection was taken.
     * Used by thresholding.
     * @return
    */
    Date getCollectionTimestamp();
}
