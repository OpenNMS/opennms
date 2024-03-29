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
package org.opennms.netmgt.dao.hibernate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.transform.Transformers;
import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.model.OnmsAssetRecord;

public class AssetRecordDaoHibernate extends AbstractDaoHibernate<OnmsAssetRecord, Integer> implements AssetRecordDao {

    /**
     * <p>
     * Constructor for AssetRecordDaoHibernate.
     * </p>
     */
    public AssetRecordDaoHibernate() {
        super(OnmsAssetRecord.class);
    }

    /**
     * <p>
     * findByNodeId
     * </p>
     *
     * @param id a {@link java.lang.Integer} object.
     * @return a {@link org.opennms.netmgt.model.OnmsAssetRecord} object.
     */
    @Override
    public OnmsAssetRecord findByNodeId(Integer id) {
        return (OnmsAssetRecord) findUnique("from OnmsAssetRecord rec where rec.node.id = ?", id);
    }

    /**
     * <p>
     * findImportedAssetNumbersToNodeIds
     * </p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @return a {@link java.util.Map} object.
     */
    @Override
    public Map<String, Integer> findImportedAssetNumbersToNodeIds(String foreignSource) {

        @SuppressWarnings("unchecked") List<Object[]> assetNumbers = (List<Object[]>)getHibernateTemplate().find("select a.node.id, a.assetNumber from OnmsAssetRecord a where a.assetNumber like '" + foreignSource + "%'");

        Map<String, Integer> assetNumberMap = new HashMap<String, Integer>();
        for (Object[] an : assetNumbers) {
            assetNumberMap.put((String) an[1], (Integer) an[0]);
        }
        return Collections.unmodifiableMap(assetNumberMap);
    }

    @Override
    public List<OnmsAssetRecord> getDistinctProperties() {
        DetachedCriteria criteria = DetachedCriteria.forClass(OnmsAssetRecord.class);
        ProjectionList projList = Projections.projectionList();

        // projList.add(Projections.alias(Projections.property("geolocation"), "geolocation"));
        projList.add(Projections.alias(Projections.property("additionalhardware"), "additionalhardware"));
        projList.add(Projections.alias(Projections.property("geolocation.address1"), "address1"));
        projList.add(Projections.alias(Projections.property("geolocation.address2"), "address2"));
        projList.add(Projections.alias(Projections.property("admin"), "admin"));
        projList.add(Projections.alias(Projections.property("assetNumber"), "assetNumber"));
        projList.add(Projections.alias(Projections.property("autoenable"), "autoenable"));
        projList.add(Projections.alias(Projections.property("building"), "building"));
        projList.add(Projections.alias(Projections.property("category"), "category"));
        projList.add(Projections.alias(Projections.property("circuitId"), "circuitId"));
        projList.add(Projections.alias(Projections.property("geolocation.city"), "city"));
        projList.add(Projections.alias(Projections.property("comment"), "comment"));
        projList.add(Projections.alias(Projections.property("connection"), "connection"));
        projList.add(Projections.alias(Projections.property("geolocation.longitude"), "longitude"));
        projList.add(Projections.alias(Projections.property("geolocation.latitude"), "latitude"));
        projList.add(Projections.alias(Projections.property("cpu"), "cpu"));
        projList.add(Projections.alias(Projections.property("department"), "department"));
        projList.add(Projections.alias(Projections.property("description"), "description"));
        projList.add(Projections.alias(Projections.property("displayCategory"), "displayCategory"));
        projList.add(Projections.alias(Projections.property("division"), "division"));
        projList.add(Projections.alias(Projections.property("enable"), "enable"));
        projList.add(Projections.alias(Projections.property("floor"), "floor"));
        projList.add(Projections.alias(Projections.property("hdd1"), "hdd1"));
        projList.add(Projections.alias(Projections.property("hdd2"), "hdd2"));
        projList.add(Projections.alias(Projections.property("hdd3"), "hdd3"));
        projList.add(Projections.alias(Projections.property("hdd4"), "hdd4"));
        projList.add(Projections.alias(Projections.property("hdd5"), "hdd5"));
        projList.add(Projections.alias(Projections.property("hdd6"), "hdd6"));
        projList.add(Projections.alias(Projections.property("inputpower"), "inputpower"));
        projList.add(Projections.alias(Projections.property("lease"), "lease"));
        projList.add(Projections.alias(Projections.property("maintcontract"), "maintcontract"));
        projList.add(Projections.alias(Projections.property("manufacturer"), "manufacturer"));
        projList.add(Projections.alias(Projections.property("modelNumber"), "modelNumber"));
        projList.add(Projections.alias(Projections.property("notifyCategory"), "notifyCategory"));
        projList.add(Projections.alias(Projections.property("numpowersupplies"), "numpowersupplies"));
        projList.add(Projections.alias(Projections.property("operatingSystem"), "operatingSystem"));
        projList.add(Projections.alias(Projections.property("pollerCategory"), "pollerCategory"));
        projList.add(Projections.alias(Projections.property("port"), "port"));
        projList.add(Projections.alias(Projections.property("rack"), "rack"));
        projList.add(Projections.alias(Projections.property("ram"), "ram"));
        projList.add(Projections.alias(Projections.property("region"), "region"));
        projList.add(Projections.alias(Projections.property("room"), "room"));
        projList.add(Projections.alias(Projections.property("serialNumber"), "serialNumber"));
        projList.add(Projections.alias(Projections.property("slot"), "slot"));
        projList.add(Projections.alias(Projections.property("snmpcommunity"), "snmpcommunity"));
        projList.add(Projections.alias(Projections.property("geolocation.state"), "state"));
        projList.add(Projections.alias(Projections.property("storagectrl"), "storagectrl"));
        projList.add(Projections.alias(Projections.property("supportPhone"), "supportPhone"));
        projList.add(Projections.alias(Projections.property("thresholdCategory"), "thresholdCategory"));
        projList.add(Projections.alias(Projections.property("username"), "username"));
        projList.add(Projections.alias(Projections.property("vendor"), "vendor"));
        projList.add(Projections.alias(Projections.property("vendorAssetNumber"), "vendorAssetNumber"));
        projList.add(Projections.alias(Projections.property("vendorFax"), "vendorFax"));
        projList.add(Projections.alias(Projections.property("vendorPhone"), "vendorPhone"));
        projList.add(Projections.alias(Projections.property("geolocation.zip"), "zip"));

        criteria.setProjection(Projections.distinct(projList));
        criteria.setResultTransformer(Transformers.aliasToBean(OnmsAssetRecord.class));

        @SuppressWarnings("unchecked") 
        List<OnmsAssetRecord> result = (List<OnmsAssetRecord>)getHibernateTemplate().findByCriteria(criteria);
        return result;
    }
}
