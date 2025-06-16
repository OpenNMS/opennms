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
package liquibase.ext2.cm.change;

import liquibase.change.AbstractChange;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.ext2.cm.database.CmDatabase;

public abstract class AbstractCmChange extends AbstractChange {

    @Override
    public boolean supports(Database database) {
        return database instanceof CmDatabase;
    }

    @Override
    public boolean generateStatementsVolatile(final Database database) {
        return false;
    }

    @Override
    public boolean generateRollbackStatementsVolatile(final Database database) {
        return false;
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = super.validate(database);
        return validate((CmDatabase) database, validationErrors);
    }

    protected abstract ValidationErrors validate(CmDatabase database, ValidationErrors validationErrors);

    protected void checkRequiredField(ValidationErrors validationErrors, String name, String value) {
        if (value == null || value.isBlank()) {
            validationErrors.addError(String.format("Attribute %s is missing", name));
        }
    }

    @FunctionalInterface
    protected interface RunnableWithException {
        void doRun() throws Exception;
    }
}
