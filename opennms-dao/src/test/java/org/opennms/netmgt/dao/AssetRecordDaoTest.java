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
// Modifications:
//
// 2008 Mar 25: Convert to use AbstractTransactionalDaoTestCase. - dj@opennms.org
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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.dao;

import java.util.Collection;

import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsNode;

public class AssetRecordDaoTest extends AbstractTransactionalDaoTestCase {
    
    public void testCreateAndGets() {
        OnmsNode onmsNode = new OnmsNode(getDistPollerDao().load("localhost"));
        onmsNode.setLabel("myNode");
        getNodeDao().save(onmsNode);
        OnmsAssetRecord assetRecord = onmsNode.getAssetRecord();
        assetRecord.setAssetNumber("imported-id: 7");
        getAssetRecordDao().update(assetRecord);
        getAssetRecordDao().flush();

        //Test findAll method
        Collection<OnmsAssetRecord> assetRecords = getAssetRecordDao().findAll();
        assertEquals(7, assetRecords.size());
        
        //Test countAll method
        assertEquals(7, getAssetRecordDao().countAll());

    }
    
}
