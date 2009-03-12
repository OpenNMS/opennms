/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: March 12, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.model;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;


/**
 * Model Object behavioral testing
 * 
 * @author <a href="mailto:david@openms.org">David Hustace</a>
 */
public class OnmsAssetRecordTest {

    @Test
    public void equalsSameObject() {
        OnmsAssetRecord rec1 = new OnmsAssetRecord();
        rec1.setId(1);
        OnmsDistPoller distPoller = new OnmsDistPoller("localhost", "127.0.0.1");
        rec1.setNode(new OnmsNode(distPoller));
        rec1.getNode().setId(1);
        
        Assert.assertTrue(rec1.equals(rec1));
    }

    @Test
    public void equalsDiffObject() {
        OnmsAssetRecord rec1 = new OnmsAssetRecord();
        rec1.setId(1);
        OnmsDistPoller distPoller = new OnmsDistPoller("localhost", "127.0.0.1");
        rec1.setNode(new OnmsNode(distPoller));
        rec1.getNode().setId(1);
        
        OnmsAssetRecord rec2 = new OnmsAssetRecord();
        rec2.setId(null);
        rec2.setNode(new OnmsNode(distPoller));
        rec2.getNode().setId(1);
        
        Assert.assertTrue(rec1.equals(rec2));
    }
    
    /**
     * Test to make sure that asset records with same id are different
     * if the node id is different
     * TODO: Determine what is the best behavior here.
     */
    @Test
    public void notEquals() {
        OnmsAssetRecord rec1 = new OnmsAssetRecord();
        rec1.setId(1);
        OnmsDistPoller distPoller = new OnmsDistPoller("localhost", "127.0.0.1");
        rec1.setNode(new OnmsNode(distPoller));
        rec1.getNode().setId(1);
        
        OnmsAssetRecord rec2 = new OnmsAssetRecord();
        rec2.setId(null);
        rec2.setNode(new OnmsNode(distPoller));
        rec2.getNode().setId(2);
        
        Assert.assertFalse(rec1.equals(rec2));
    }
    
    @Test
    public void testMergeEqualRecord() {
        OnmsAssetRecord rec1 = new OnmsAssetRecord();
        rec1.setId(1);
        OnmsDistPoller distPoller = new OnmsDistPoller("localhost", "127.0.0.1");
        rec1.setNode(new OnmsNode(distPoller));
        rec1.getNode().setId(1);
        rec1.setAddress1("220 Chatham Business Drive");
        rec1.setAddress2(null);
        String building = "Club House";
        rec1.setBuilding(building);
        
        OnmsAssetRecord rec2 = new OnmsAssetRecord();
        rec2.setId(null);
        rec2.setNode(new OnmsNode(distPoller));
        rec2.getNode().setId(1);
        String newAddress1 = "7025 Kit Creek Rd";
        rec2.setAddress1(newAddress1);
        String newAddress2 = "P.O. Box 14987";
        rec2.setAddress2(newAddress2);
        
        rec1.mergeRecord(rec2);
        Assert.assertEquals(newAddress1, rec1.getAddress1());
        Assert.assertEquals(newAddress2, rec1.getAddress2());
        Assert.assertEquals(building, rec1.getBuilding());
    }

    @Test
    public void testMergeNotEqualRecord() {
        OnmsAssetRecord rec1 = new OnmsAssetRecord();
        rec1.setId(1);
        OnmsDistPoller distPoller = new OnmsDistPoller("localhost", "127.0.0.1");
        rec1.setNode(new OnmsNode(distPoller));
        rec1.getNode().setId(1);
        String originalAddress1 = "220 Chatham Business Drive";
        rec1.setAddress1(originalAddress1);
        String originalAddress2 = null;
        rec1.setAddress2(originalAddress2);
        String building = "Club House";
        rec1.setBuilding(building);
        
        OnmsAssetRecord rec2 = new OnmsAssetRecord();
        rec2.setId(null);
        rec2.setNode(new OnmsNode(distPoller));
        rec2.getNode().setId(2);
        rec2.setAddress1("7025 Kit Creek Rd");
        rec2.setAddress2("P.O. Box 14987");
        
        rec1.mergeRecord(rec2);
        Assert.assertEquals(originalAddress1, rec1.getAddress1());
        Assert.assertEquals(originalAddress2, rec1.getAddress2());
        Assert.assertEquals(building, rec1.getBuilding());
    }
}
