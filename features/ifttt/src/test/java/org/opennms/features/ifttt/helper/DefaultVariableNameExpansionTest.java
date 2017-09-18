/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
