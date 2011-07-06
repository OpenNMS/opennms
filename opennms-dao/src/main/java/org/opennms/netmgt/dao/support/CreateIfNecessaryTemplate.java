/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2011 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.dao.support;

import org.opennms.netmgt.dao.OnmsDao;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Template for creating a row if and only if one doesn't already exists.  This suffers
 * from some of the same concurrency issues as described in the {@link UpsertTemplate}.  See the
 * detailed javadoc there for a description.
 *
 * @author brozow
 */
public abstract class CreateIfNecessaryTemplate<T, D extends OnmsDao<T, ?>> extends UpsertTemplate<T, D> {

    /**
     * Create a CreateIfNecessaryTemplate using the given transactionManager to create transactions.
     */
    public CreateIfNecessaryTemplate(PlatformTransactionManager transactionManager, D dao) {
        super(transactionManager, dao);
    }

    @Override
    abstract protected T query();

    /**
     * There is no need to update the object for this case as we just return the object found.
     */
    @Override
    protected T doUpdate(T dbObj) {
        return dbObj;
    }

    @Override
    abstract protected T doInsert();
    

}
