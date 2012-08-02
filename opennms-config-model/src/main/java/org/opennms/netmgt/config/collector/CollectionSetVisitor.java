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

/**
 * <p>CollectionSetVisitor interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface CollectionSetVisitor {

    /**
     * <p>visitCollectionSet</p>
     *
     * @param set a {@link org.opennms.netmgt.collectd.CollectionSet} object.
     */
    void visitCollectionSet(CollectionSet set);

    /**
     * <p>visitResource</p>
     *
     * @param resource a {@link org.opennms.netmgt.config.collector.CollectionResource} object.
     */
    void visitResource(CollectionResource resource);

    /**
     * <p>visitGroup</p>
     *
     * @param group a {@link org.opennms.netmgt.collectd.AttributeGroup} object.
     */
    void visitGroup(AttributeGroup group);

    /**
     * <p>visitAttribute</p>
     *
     * @param attribute a {@link org.opennms.netmgt.collectd.CollectionAttribute} object.
     */
    void visitAttribute(CollectionAttribute attribute);

    /**
     * <p>completeAttribute</p>
     *
     * @param attribute a {@link org.opennms.netmgt.collectd.CollectionAttribute} object.
     */
    void completeAttribute(CollectionAttribute attribute);

    /**
     * <p>completeGroup</p>
     *
     * @param group a {@link org.opennms.netmgt.collectd.AttributeGroup} object.
     */
    void completeGroup(AttributeGroup group);

    /**
     * <p>completeResource</p>
     *
     * @param resource a {@link org.opennms.netmgt.config.collector.CollectionResource} object.
     */
    void completeResource(CollectionResource resource);

    /**
     * <p>completeCollectionSet</p>
     *
     * @param set a {@link org.opennms.netmgt.collectd.CollectionSet} object.
     */
    void completeCollectionSet(CollectionSet set);

}
