/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

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
        rec1.setGeolocation(new OnmsGeolocation());
        rec1.getGeolocation().setAddress1("220 Chatham Business Drive");
        String building = "Club House";
        rec1.setBuilding(building);
        
        OnmsAssetRecord rec2 = new OnmsAssetRecord();
        rec2.setId(null);
        rec2.setNode(new OnmsNode(distPoller));
        rec2.getNode().setId(1);
        String newAddress1 = "7025 Kit Creek Rd";
        rec2.getGeolocation().setAddress1(newAddress1);
        String newAddress2 = "P.O. Box 14987";
        rec2.getGeolocation().setAddress2(newAddress2);
        
        rec1.mergeRecord(rec2);
        Assert.assertEquals(newAddress1, rec1.getGeolocation().getAddress1());
        Assert.assertEquals(newAddress2, rec1.getGeolocation().getAddress2());
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
        rec1.getGeolocation().setAddress1(originalAddress1);
        String originalAddress2 = null;
        rec1.getGeolocation().setAddress2(originalAddress2);
        String building = "Club House";
        rec1.setBuilding(building);
        
        OnmsAssetRecord rec2 = new OnmsAssetRecord();
        rec2.setId(null);
        rec2.setNode(new OnmsNode(distPoller));
        rec2.getNode().setId(2);
        rec2.getGeolocation().setAddress1("7025 Kit Creek Rd");
        rec2.getGeolocation().setAddress2("P.O. Box 14987");
        
        rec1.mergeRecord(rec2);
        Assert.assertEquals(originalAddress1, rec1.getGeolocation().getAddress1());
        Assert.assertEquals(originalAddress2, rec1.getGeolocation().getAddress2());
        Assert.assertEquals(building, rec1.getBuilding());
    }
}
