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
