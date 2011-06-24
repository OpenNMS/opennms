/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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


package org.opennms.netmgt.config.collector;

import java.util.Date;


/**
 * <p>CollectionSet interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface CollectionSet {
    
    /**
     * <p>getStatus</p>
     *
     * @return an int (one of the ServiceCollector.COLLECTION_<FOO> values)
     */
    public int getStatus();
    
    /**
     * Provide a way to visit all the values in the CollectionSet, for any appropriate purposes (persisting, thresholding, or others)
     * The expectation is that calling this method will ultimately call visitResource, visitGroup and visitAttribute (as appropriate)
     *
     * @param visitor a {@link org.opennms.netmgt.config.collector.CollectionSetVisitor} object.
     */
    public void visit(CollectionSetVisitor visitor);
    
    /**
     * <p>ignorePersist</p>
     *
     * @return a boolean.
     */
    public boolean ignorePersist();
    
    /**
     * Returns the timestamp of when this data collection was taken.
     * Used by thresholding
     * @return
    */
	public Date getCollectionTimestamp();
}
