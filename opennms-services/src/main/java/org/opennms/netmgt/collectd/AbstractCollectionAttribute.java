/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 *
 */

package org.opennms.netmgt.collectd;

import org.opennms.core.utils.ThreadCategory;

/**
 * <p>Abstract AbstractCollectionAttribute class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class AbstractCollectionAttribute implements  CollectionAttribute {
    /**
     * <p>log</p>
     *
     * @return a {@link org.opennms.core.utils.ThreadCategory} object.
     */
    protected ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }
    
    /**
     * <p>getAttributeType</p>
     *
     * @return a {@link org.opennms.netmgt.collectd.CollectionAttributeType} object.
     */
    public abstract CollectionAttributeType getAttributeType();

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getName();

    /**
     * <p>getNumericValue</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getNumericValue() ;

    /**
     * <p>getResource</p>
     *
     * @return a {@link org.opennms.netmgt.collectd.CollectionResource} object.
     */
    public abstract CollectionResource getResource();

    /**
     * <p>getStringValue</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getStringValue() ;

    /** {@inheritDoc} */
    public abstract boolean shouldPersist(ServiceParameters params);

    /** {@inheritDoc} */
    public void storeAttribute(Persister persister) {
        getAttributeType().storeAttribute(this, persister);
    }

    /** {@inheritDoc} */
    public void visit(CollectionSetVisitor visitor) {
        log().debug("Visiting attribute "+this);
        visitor.visitAttribute(this);
        visitor.completeAttribute(this);
    }   

}
