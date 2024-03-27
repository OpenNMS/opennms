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
package org.opennms.netmgt.bsm.service.internal;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEntity;
import org.opennms.netmgt.bsm.service.BusinessServiceSearchCriteriaBuilder;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.graph.BusinessServiceGraph;
import org.opennms.netmgt.bsm.service.model.graph.internal.BusinessServiceGraphImpl;

import com.google.common.collect.ImmutableList;

public class BusinessServiceCriteriaTest {

    private List<BusinessService> businessServices = new ArrayList<>();

    private BusinessServiceManagerImpl businessServiceManager = new BusinessServiceManagerImpl() {
        @Override
        public List<BusinessService> getAllBusinessServices() {
            return businessServices;
        }

        @Override
        public Status getOperationalStatus(BusinessService service) {
            return service.getOperationalStatus();
        }

        @Override
        public BusinessServiceGraph getGraph() {
            // Does not set the status
            return new BusinessServiceGraphImpl(businessServices);
        }
    };

    private BusinessService bs1 = createDummyBusinessService(7, "BsA", "att1", "fooYes", Status.INDETERMINATE);
    private BusinessService bs2 = createDummyBusinessService(6, "BsB", "att1", "fooYes", Status.INDETERMINATE);
    private BusinessService bs3 = createDummyBusinessService(5, "BsC", "att1", "fooNo", Status.NORMAL);
    private BusinessService bs4 = createDummyBusinessService(4, "BsD", "att1", "fooYes", Status.WARNING);
    private BusinessService bs5 = createDummyBusinessService(3, "BsE", "att1", "fooYes", Status.MINOR);
    private BusinessService bs6 = createDummyBusinessService(2, "BsF", "att1", "fooNo", Status.MAJOR);
    private BusinessService bs7 = createDummyBusinessService(1, "BsG", "att1", "fooYes", Status.CRITICAL);

    public BusinessServiceCriteriaTest() {
        businessServices.add(bs1);
        businessServices.add(bs2);
        businessServices.add(bs3);
        businessServices.add(bs4);
        businessServices.add(bs5);
        businessServices.add(bs6);
        businessServices.add(bs7);
    }

    @Test
    public void testOrderByNameAscAndLimit() {
        BusinessServiceSearchCriteriaBuilder b = new BusinessServiceSearchCriteriaBuilder()
                .order(BusinessServiceSearchCriteriaBuilder.Order.Name)
                .asc()
                .limit(6);
        assertEquals(
                ImmutableList.<BusinessService>builder().add(bs1, bs2, bs3, bs4, bs5, bs6).build(),
                businessServiceManager.search(b));
    }

    @Test
    public void testOrderBySeverityDescAndLimit() {
        BusinessServiceSearchCriteriaBuilder b = new BusinessServiceSearchCriteriaBuilder()
                .order(BusinessServiceSearchCriteriaBuilder.Order.Severity)
                .desc()
                .limit(5);
        assertEquals(
                ImmutableList.<BusinessService>builder().add(bs7, bs6, bs5, bs4, bs3).build(),
                businessServiceManager.search(b));
    }

    @Test
    public void testFilterByAttribute() {
        BusinessServiceSearchCriteriaBuilder b = new BusinessServiceSearchCriteriaBuilder()
                .attribute("att1", ".*Yes")
                .order(BusinessServiceSearchCriteriaBuilder.Order.Name)
                .desc();
        assertEquals(
                ImmutableList.<BusinessService>builder().add(bs7, bs5, bs4, bs2, bs1).build(),
                businessServiceManager.search(b));
    }

    @Test
    public void testFilterByName() {
        BusinessServiceSearchCriteriaBuilder b = new BusinessServiceSearchCriteriaBuilder()
                .name(".*sG")
                .order(BusinessServiceSearchCriteriaBuilder.Order.Name)
                .asc();
        assertEquals(
                ImmutableList.<BusinessService>builder().add(bs7).build(),
                businessServiceManager.search(b));
    }

    @Test
    public void testFilterBySeverity() {
        BusinessServiceSearchCriteriaBuilder b = new BusinessServiceSearchCriteriaBuilder()
                .greaterOrEqualSeverity(Status.WARNING)
                .order(BusinessServiceSearchCriteriaBuilder.Order.Severity)
                .desc();
        assertEquals(
                ImmutableList.<BusinessService>builder().add(bs7, bs6, bs5, bs4).build(),
                businessServiceManager.search(b));
    }

    private BusinessService createDummyBusinessService(final long id, final String name, String attributeKey, String attributeValue, final Status status) {
        BusinessServiceEntity businessServiceEntity = new BusinessServiceEntity();
        businessServiceEntity.setId(id);
        businessServiceEntity.setAttribute(attributeKey, attributeValue);
        businessServiceEntity.setName(name);
        BusinessService businessService = new BusinessServiceImpl(businessServiceManager, businessServiceEntity) {
            @Override
            public Status getOperationalStatus() {
                return status;
            }
        };
        return businessService;
    }
}
