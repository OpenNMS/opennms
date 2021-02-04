/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2003-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.vacuumd;

import junit.framework.TestCase;
import org.junit.Assert;
import org.opennms.netmgt.config.vacuumd.Action;
import org.opennms.netmgt.config.vacuumd.Statement;

public class AutomationProcessorTest extends TestCase {

    public void testGetActionSQL() {
        String emptyString = "";
        //test null Action
        AutomationProcessor.ActionProcessor ap1 = new AutomationProcessor.ActionProcessor("", null);
        Assert.assertEquals(emptyString, ap1.getActionSQL());

        //test Action with null statement
        Action actionNullStatement = new Action();
        AutomationProcessor.ActionProcessor ap2 = new AutomationProcessor.ActionProcessor("", actionNullStatement);
        Assert.assertEquals(emptyString, ap2.getActionSQL());

        //test statement with null content
        Statement statementNullContent = new Statement();
        Action actionNullContent = new Action("name","dataSource",statementNullContent);
        AutomationProcessor.ActionProcessor ap3 = new AutomationProcessor.ActionProcessor("", actionNullContent);
        Assert.assertEquals(emptyString, ap3.getActionSQL());

        //test everything is well
        Statement statement = new Statement("value", true);
        Action action = new Action("name","dataSource",statement);
        AutomationProcessor.ActionProcessor ap4 = new AutomationProcessor.ActionProcessor("", action);
        Assert.assertEquals("value", ap4.getActionSQL());
    }
}