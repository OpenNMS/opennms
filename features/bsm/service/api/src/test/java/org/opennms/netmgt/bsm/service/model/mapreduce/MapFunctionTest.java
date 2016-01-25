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
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.bsm.service.model.mapreduce;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.functions.map.Decrease;
import org.opennms.netmgt.bsm.service.model.functions.map.Identity;
import org.opennms.netmgt.bsm.service.model.functions.map.Ignore;
import org.opennms.netmgt.bsm.service.model.functions.map.Increase;
import org.opennms.netmgt.bsm.service.model.functions.map.SetTo;

public class MapFunctionTest {

    @Test
    public void verifySetTo() {
        SetTo setTo = new SetTo();
        setTo.setStatus(Status.WARNING);
        for (Status eachStatus : Status.values()) {
            Assert.assertEquals(Status.WARNING, setTo.map(eachStatus).get());
        }
    }

    @Test
    public void verifyIgnore() {
        Ignore ignore = new Ignore();
        for (Status eachStatus : Status.values()) {
            Assert.assertFalse(ignore.map(eachStatus).isPresent());
        }
    }

    @Test
    public void verifyIdentity() {
        Identity ignore = new Identity();
        for (Status eachStatus : Status.values()) {
            Assert.assertEquals(eachStatus, ignore.map(eachStatus).get());
        }
    }

    @Test
    public void verifyDecrease() {
        Decrease decrease = new Decrease();
        Assert.assertEquals(Status.MAJOR, decrease.map(Status.CRITICAL).get());
        Assert.assertEquals(Status.MINOR, decrease.map(Status.MAJOR).get());
        Assert.assertEquals(Status.WARNING, decrease.map(Status.MINOR).get());
        Assert.assertEquals(Status.NORMAL, decrease.map(Status.WARNING).get());
        Assert.assertEquals(Status.INDETERMINATE, decrease.map(Status.INDETERMINATE).get());
    }

    @Test
    public void verifyIncrease() {
        Increase increase = new Increase();
        Assert.assertEquals(Status.WARNING, increase.map(Status.NORMAL).get());
        Assert.assertEquals(Status.MINOR, increase.map(Status.WARNING).get());
        Assert.assertEquals(Status.MAJOR, increase.map(Status.MINOR).get());
        Assert.assertEquals(Status.CRITICAL, increase.map(Status.MAJOR).get());
        Assert.assertEquals(Status.CRITICAL, increase.map(Status.CRITICAL).get());
    }
}
