//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

package org.opennms.web.svclayer.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.web.svclayer.dao.SurveillanceViewConfigDao;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;


import junit.framework.TestCase;

public class DefaultSurveillanceServiceTest extends TestCase {
    
    private NodeDao m_nodeDao;
    private CategoryDao m_categoryDao;
    private SurveillanceViewConfigDao m_surveillanceViewConfigDao;
    
    @Override
    public void setUp() throws Exception {
        m_nodeDao = createMock(NodeDao.class);
        m_categoryDao = createMock(CategoryDao.class);
        m_surveillanceViewConfigDao = createMock(SurveillanceViewConfigDao.class);
    }
    
    public void testCreateSurveillanceTable() {
        
        DefaultSurveillanceService surveillanceSvc = new DefaultSurveillanceService();
        surveillanceSvc.setNodeDao(m_nodeDao);
        surveillanceSvc.setCategoryDao(m_categoryDao);
        surveillanceSvc.setSurveillanceConfigDao(m_surveillanceViewConfigDao);

        surveillanceSvc.createSurveillanceTable();
        
    }

    public Collection<OnmsCategory> createCategories(List<String> catNames) {
        Collection<OnmsCategory> categories = createCategoryNameCollection(catNames);
        return categories;
    }

    private Collection<OnmsCategory> createCategoryNameCollection(List<String> categoryNames) {
        
        Collection<OnmsCategory> categories = new ArrayList<OnmsCategory>();
        for (String catName : categoryNames) {
            categories.add(m_categoryDao.findByName(catName));
        }
        return categories;
    }

}
