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
 * Created: February 21, 2007
 * 
 * October 22, 2009: added getCategoriesList method jonathan@opennms.org
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
package org.opennms.web.svclayer;

import java.util.Collection;
import java.util.List;

import org.opennms.netmgt.config.categories.Category;

/**
 * <p>CategoryConfigService interface.</p>
 *
 * @author <a href="mailto:sartin@opennms.org">Jonathan Sartin</a>
 * @version $Id: $
 * @since 1.8.1
 */
public interface CategoryConfigService {

    /**
     * <p>getCategories</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    Collection<Category> getCategories();
    
    /**
     * <p>getCategoriesList</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<String> getCategoriesList();

}
