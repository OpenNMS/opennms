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
package org.opennms.core.password;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.opennms.core.db.DataSourceFactory;

/** Sets a web user's password in the database. */
public class Password {
    public static void main(String[] args) {
        final String OPENNMS_HOME = System.getProperty("opennms.home");
        final Logger log = System.getLogger(Password.class.getName());

        if (args.length <2){
            log.log(Level.WARNING, "usage: password.jar <username> <password>");
            System.exit(1);
        }

        final String userId = args[0];
        final String newPassword = args[1];

        if (OPENNMS_HOME == null || OPENNMS_HOME.isEmpty() || userId.isEmpty() || newPassword.isEmpty()){
            log.log(Level.ERROR, "Unable to determine OpenNMS home, or no username or password was provided.");
            System.exit(1);
        }

        System.exit(setPasswordDb(log, userId, newPassword));
    }

    private static int setPasswordDb(Logger log, String userId, String newPassword) {
        try {
            DatabasePasswords.setPassword(DataSourceFactory.getInstance(), userId, newPassword);
            log.log(Level.INFO, "The password of user " + userId + " was changed in the database.");
            return 0;
        } catch (final IllegalArgumentException e) {
            log.log(Level.ERROR, e.getMessage());
        } catch (final Exception e) {
            log.log(Level.ERROR, "Cannot change the password in the database: " + e.getMessage(), e);
        }

        return 1;
    }
}
