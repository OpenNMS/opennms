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
package org.opennms.features.ifttt.helper;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.model.OnmsSeverity;

public class DefaultVariableNameExpansionTest {

    @Test
    public void replaceTest() {
        final VariableNameExpansion variableNameExpansion = new DefaultVariableNameExpansion(OnmsSeverity.CRITICAL, OnmsSeverity.MAJOR, 10,20);
        final String string1 = "foo%os%bar%ns%abc%oc%def%nc%xyz";
        Assert.assertEquals("fooCRITICALbarMAJORabc10def20xyz", variableNameExpansion.replace(string1));
        final String string2 = "foo%oldSeverity%bar%newSeverity%abc%oldCount%def%newCount%xyz";
        Assert.assertEquals("fooCRITICALbarMAJORabc10def20xyz", variableNameExpansion.replace(string2));
    }
}
