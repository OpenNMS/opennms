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
package liquibase.ext2.cm.executor;

import java.util.List;

import liquibase.exception.DatabaseException;
import liquibase.executor.jvm.JdbcExecutor;
import liquibase.ext2.cm.database.CmDatabase;
import liquibase.ext2.cm.statement.AbstractCmStatement;
import liquibase.servicelocator.LiquibaseService;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.SqlStatement;

/**
 * We are using a hybrid approach here:
 * - All liquibase specific operations are carried out with the JdbcExecutor.
 * - All NotConfd operations are carried out with this subclass.
 */
@LiquibaseService
public class CmExecutor extends JdbcExecutor {
    @Override
    public void execute(final SqlStatement sql, final List<SqlVisitor> sqlVisitors) throws DatabaseException {
        if (sql instanceof AbstractCmStatement && this.database instanceof CmDatabase) {
            ((AbstractCmStatement) sql).execute((CmDatabase) this.database);
            return;
        }

        // not our statement => delegate to parent
        super.execute(sql, sqlVisitors);
    }
}
