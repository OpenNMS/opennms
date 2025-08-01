/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
        rec1.setNode(new OnmsNode());
        rec1.getNode().setId(1);
        
        Assert.assertTrue(rec1.equals(rec1));
    }

    @Test
    public void equalsDiffObject() {
        OnmsAssetRecord rec1 = new OnmsAssetRecord();
        rec1.setId(1);
        rec1.setNode(new OnmsNode());
        rec1.getNode().setId(1);
        
        OnmsAssetRecord rec2 = new OnmsAssetRecord();
        rec2.setId(null);
        rec2.setNode(new OnmsNode());
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
        rec1.setNode(new OnmsNode());
        rec1.getNode().setId(1);
        
        OnmsAssetRecord rec2 = new OnmsAssetRecord();
        rec2.setId(null);
        rec2.setNode(new OnmsNode());
        rec2.getNode().setId(2);
        
        Assert.assertFalse(rec1.equals(rec2));
    }
    
    @Test
    public void testMergeEqualRecord() {
        OnmsAssetRecord rec1 = new OnmsAssetRecord();
        rec1.setId(1);
        rec1.setNode(new OnmsNode());
        rec1.getNode().setId(1);
        rec1.setGeolocation(new OnmsGeolocation());
        rec1.getGeolocation().setAddress1("220 Chatham Business Drive");
        String building = "Club House";
        rec1.setBuilding(building);
        
        OnmsAssetRecord rec2 = new OnmsAssetRecord();
        rec2.setId(null);
        rec2.setNode(new OnmsNode());
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
        rec1.setNode(new OnmsNode());
        rec1.getNode().setId(1);
        String originalAddress1 = "220 Chatham Business Drive";
        rec1.getGeolocation().setAddress1(originalAddress1);
        String originalAddress2 = null;
        rec1.getGeolocation().setAddress2(originalAddress2);
        String building = "Club House";
        rec1.setBuilding(building);
        
        OnmsAssetRecord rec2 = new OnmsAssetRecord();
        rec2.setId(null);
        rec2.setNode(new OnmsNode());
        rec2.getNode().setId(2);
        rec2.getGeolocation().setAddress1("7025 Kit Creek Rd");
        rec2.getGeolocation().setAddress2("P.O. Box 14987");
        
        rec1.mergeRecord(rec2);
        Assert.assertEquals(originalAddress1, rec1.getGeolocation().getAddress1());
        Assert.assertEquals(originalAddress2, rec1.getGeolocation().getAddress2());
        Assert.assertEquals(building, rec1.getBuilding());
    }
}
