/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2006, 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


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

    public void testAddUserName() {
        OnmsNode onmsNode = new OnmsNode(getDistPollerDao().load("localhost"));
        onmsNode.setLabel("myNode");
        getNodeDao().save(onmsNode);
        OnmsAssetRecord assetRecord = onmsNode.getAssetRecord();
        assetRecord.setAssetNumber("imported-id: 7");
        assetRecord.setUsername("antonio");
        assetRecord.setPassword("password");
        assetRecord.setEnable("cisco");
        assetRecord.setConnection(OnmsAssetRecord.TELNET_CONNECTION);
        getAssetRecordDao().update(assetRecord);
        getAssetRecordDao().flush();

        //Test findAll method
        int id = assetRecord.getId();
        OnmsAssetRecord assetRecordFromDb = getAssetRecordDao().get(id);
        assertEquals(assetRecord.getUsername(), assetRecordFromDb.getUsername());
        assertEquals(assetRecord.getPassword(), assetRecordFromDb.getPassword());
        assertEquals(assetRecord.getEnable(), assetRecordFromDb.getEnable());
        assertEquals(assetRecord.getConnection(), assetRecordFromDb.getConnection());

    }
    
    public void testAddAutoenable() {
        OnmsNode onmsNode = new OnmsNode(getDistPollerDao().load("localhost"));
        onmsNode.setLabel("myNode");
        getNodeDao().save(onmsNode);
        OnmsAssetRecord assetRecord = onmsNode.getAssetRecord();
        assetRecord.setAssetNumber("imported-id: 7");
        assetRecord.setUsername("antonio");
        assetRecord.setPassword("password");
        assetRecord.setAutoenable(OnmsAssetRecord.AUTOENABLED);
        assetRecord.setConnection(OnmsAssetRecord.TELNET_CONNECTION);
        getAssetRecordDao().update(assetRecord);
        getAssetRecordDao().flush();

        //Test findAll method
        int id = assetRecord.getId();
        OnmsAssetRecord assetRecordFromDb = getAssetRecordDao().get(id);
        assertEquals(assetRecord.getUsername(), assetRecordFromDb.getUsername());
        assertEquals(assetRecord.getPassword(), assetRecordFromDb.getPassword());
        assertEquals(assetRecord.getAutoenable(), assetRecordFromDb.getAutoenable());
        assertEquals(assetRecord.getConnection(), assetRecordFromDb.getConnection());

    }
    

}
