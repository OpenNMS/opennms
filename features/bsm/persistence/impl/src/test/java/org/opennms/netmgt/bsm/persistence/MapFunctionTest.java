/*******************************************************************************
 * This file is part of OpenNMS(R).
 * <p>
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 * http://www.gnu.org/licenses/
 * <p>
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.bsm.persistence;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.bsm.persistence.api.Decrease;
import org.opennms.netmgt.bsm.persistence.api.Identity;
import org.opennms.netmgt.bsm.persistence.api.Ignore;
import org.opennms.netmgt.bsm.persistence.api.Increase;
import org.opennms.netmgt.bsm.persistence.api.SetTo;
import org.opennms.netmgt.model.OnmsSeverity;

public class MapFunctionTest {

    @Test
    public void verifySetTo() {
        SetTo setTo = new SetTo();
        setTo.setSeverity(OnmsSeverity.WARNING);

        for (OnmsSeverity eachSeverity : OnmsSeverity.values()) {
            Assert.assertEquals(OnmsSeverity.WARNING, setTo.map(eachSeverity).get());
        }
    }

    @Test
    public void verifyIgnore() {
        Ignore ignore = new Ignore();
        for (OnmsSeverity eachSeverity : OnmsSeverity.values()) {
            Assert.assertFalse(ignore.map(eachSeverity).isPresent());
        }
    }

    @Test
    public void verifyIdentity() {
        Identity ignore = new Identity();
        for (OnmsSeverity eachSeverity : OnmsSeverity.values()) {
            Assert.assertEquals(eachSeverity, ignore.map(eachSeverity).get());
        }
    }

    @Test
    public void verifyDecrease() {
        Decrease decrease = new Decrease();
        Assert.assertEquals(OnmsSeverity.MAJOR, decrease.map(OnmsSeverity.CRITICAL).get());
        Assert.assertEquals(OnmsSeverity.MINOR, decrease.map(OnmsSeverity.MAJOR).get());
        Assert.assertEquals(OnmsSeverity.WARNING, decrease.map(OnmsSeverity.MINOR).get());
        Assert.assertEquals(OnmsSeverity.NORMAL, decrease.map(OnmsSeverity.WARNING).get());
        Assert.assertEquals(OnmsSeverity.CLEARED, decrease.map(OnmsSeverity.NORMAL).get());
        Assert.assertEquals(OnmsSeverity.INDETERMINATE, decrease.map(OnmsSeverity.CLEARED).get());
        Assert.assertEquals(OnmsSeverity.INDETERMINATE, decrease.map(OnmsSeverity.INDETERMINATE).get());
    }

    @Test
    public void verifyIncrease() {
        Increase increase = new Increase();
        Assert.assertEquals(OnmsSeverity.CLEARED, increase.map(OnmsSeverity.INDETERMINATE).get());
        Assert.assertEquals(OnmsSeverity.NORMAL, increase.map(OnmsSeverity.CLEARED).get());
        Assert.assertEquals(OnmsSeverity.WARNING, increase.map(OnmsSeverity.NORMAL).get());
        Assert.assertEquals(OnmsSeverity.MINOR, increase.map(OnmsSeverity.WARNING).get());
        Assert.assertEquals(OnmsSeverity.MAJOR, increase.map(OnmsSeverity.MINOR).get());
        Assert.assertEquals(OnmsSeverity.CRITICAL, increase.map(OnmsSeverity.MAJOR).get());
        Assert.assertEquals(OnmsSeverity.CRITICAL, increase.map(OnmsSeverity.CRITICAL).get());
    }
}
