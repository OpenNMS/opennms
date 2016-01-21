/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 * http://www.gnu.org/licenses/
 * 
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.bsm.service.internal;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEntity;
import org.opennms.netmgt.bsm.service.BusinessServiceSearchCriteriaBuilder;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.model.OnmsSeverity;

import com.google.common.collect.ImmutableList;

import junit.framework.Assert;

public class BusinessServiceCriteriaTest {

    private List<BusinessService> businessServices = new ArrayList<>();

    private BusinessServiceManagerImpl businessServiceManager = new BusinessServiceManagerImpl() {
        @Override
        public List<BusinessService> getAllBusinessServices() {
            return businessServices;
        }

        @Override
        public OnmsSeverity getOperationalStatusForBusinessService(BusinessService service) {
            return service.getOperationalStatus();
        }
    };
    ;

    private BusinessService bs1 = createDummyBusinessService(7, "BsA", "att1", "fooYes", OnmsSeverity.INDETERMINATE);
    private BusinessService bs2 = createDummyBusinessService(6, "BsB", "att1", "fooYes", OnmsSeverity.CLEARED);
    private BusinessService bs3 = createDummyBusinessService(5, "BsC", "att1", "fooNo", OnmsSeverity.NORMAL);
    private BusinessService bs4 = createDummyBusinessService(4, "BsD", "att1", "fooYes", OnmsSeverity.WARNING);
    private BusinessService bs5 = createDummyBusinessService(3, "BsE", "att1", "fooYes", OnmsSeverity.MINOR);
    private BusinessService bs6 = createDummyBusinessService(2, "BsF", "att1", "fooNo", OnmsSeverity.MAJOR);
    private BusinessService bs7 = createDummyBusinessService(1, "BsG", "att1", "fooYes", OnmsSeverity.CRITICAL);

    {
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
        Assert.assertEquals(
                ImmutableList.<BusinessService>builder().add(bs1, bs2, bs3, bs4, bs5, bs6).build(),
                businessServiceManager.search(b));
    }

    @Test
    public void testOrderBySeverityDescAndLimit() {
        BusinessServiceSearchCriteriaBuilder b = new BusinessServiceSearchCriteriaBuilder()
                .order(BusinessServiceSearchCriteriaBuilder.Order.Severity)
                .desc()
                .limit(6);
        Assert.assertEquals(
                ImmutableList.<BusinessService>builder().add(bs7, bs6, bs5, bs4, bs3, bs2).build(),
                businessServiceManager.search(b));
    }

    @Test
    public void testFilterByAttribute() {
        BusinessServiceSearchCriteriaBuilder b = new BusinessServiceSearchCriteriaBuilder()
                .attribute("att1", ".*Yes")
                .order(BusinessServiceSearchCriteriaBuilder.Order.Name)
                .desc();
        Assert.assertEquals(
                ImmutableList.<BusinessService>builder().add(bs7, bs5, bs4, bs2, bs1).build(),
                businessServiceManager.search(b));
    }

    @Test
    public void testFilterByName() {
        BusinessServiceSearchCriteriaBuilder b = new BusinessServiceSearchCriteriaBuilder()
                .name(".*sG")
                .order(BusinessServiceSearchCriteriaBuilder.Order.Name)
                .asc();
        Assert.assertEquals(
                ImmutableList.<BusinessService>builder().add(bs7).build(),
                businessServiceManager.search(b));
    }

    @Test
    public void testFilterBySeverity() {
        BusinessServiceSearchCriteriaBuilder b = new BusinessServiceSearchCriteriaBuilder()
                .greaterOrEqualSeverity(OnmsSeverity.WARNING)
                .order(BusinessServiceSearchCriteriaBuilder.Order.Severity)
                .desc();
        Assert.assertEquals(
                ImmutableList.<BusinessService>builder().add(bs7, bs6, bs5, bs4).build(),
                businessServiceManager.search(b));
    }

    private BusinessService createDummyBusinessService(final long id, final String name, String attributeKey, String attributeValue, final OnmsSeverity onmsSeverity) {
        BusinessServiceEntity businessServiceEntity = new BusinessServiceEntity();
        businessServiceEntity.setId(id);
        businessServiceEntity.setAttribute(attributeKey, attributeValue);
        businessServiceEntity.setName(name);
        BusinessService businessService = new BusinessServiceImpl(businessServiceManager, businessServiceEntity) {
            @Override
            public OnmsSeverity getOperationalStatus() {
                return onmsSeverity;
            }
        };
        return businessService;
    }
}
