/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
// TODO : Patrick: this is the wrong copyright header but I copied it in here so our checks will work...

package liquibase.sqlgenerator;

import java.util.TreeSet;

// TODO: Patrick: find a better solution:
//  I copied this class from the liquibase source since it is not available as jar in the Maven repo:
// in 3.6.3 we had: https://repo1.maven.org/maven2/org/liquibase/liquibase-core/3.6.3/liquibase-core-3.6.3-tests.jar
// in 4.4.3 it is missing: https://repo1.maven.org/maven2/org/liquibase/liquibase-core/4.4.3/liquibase-core-4.4.3-tests.jar doesnt exist
public class MockSqlGeneratorChain extends SqlGeneratorChain {
    public MockSqlGeneratorChain() {
        super(new TreeSet<SqlGenerator>());
    }
}
