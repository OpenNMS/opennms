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
package org.opennms.features.scv.cli.commands;

import java.io.File;
import java.util.Map;
import java.util.function.Function;

import org.kohsuke.args4j.Option;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.opennms.features.scv.cli.ScvCli;
import org.opennms.features.scv.jceks.JCEKSSecureCredentialsVault;
import org.opennms.features.scv.utils.ScvUtils;

public class RotateKeyCommand implements Function<ScvCli, Integer> {

    @Option(name = "--new-password",
            aliases = {"-np"},
            required = true,
            metaVar = "NEW_PASSWORD",
            usage = "the new keystore password")
    private String newPassword;

    @Override
    public Integer apply(ScvCli scvCli) {
        try {
            System.out.println("WARNING: Key rotation must be performed while OpenNMS is stopped.");
            System.out.println("If OpenNMS is running, stop it first and re-run this command.");
            System.out.println();

            // Read all credentials with the current key + salt
            SecureCredentialsVault currentVault = scvCli.getSecureCredentialsVault();
            Map<String, Credentials> allCredentials = currentVault.getAllCredentials();

            String opennmsHome = ScvUtils.resolveOpennmsHome();

            // Generate new random salt
            byte[] newSalt;
            if (opennmsHome != null && !opennmsHome.isEmpty()) {
                newSalt = ScvUtils.generateAndSaveSalt(opennmsHome);
            } else {
                System.err.println("WARNING: OPENNMS_HOME not set, cannot save salt file. Using default salt.");
                newSalt = ScvUtils.DEFAULT_SALT.clone();
            }

            // Delete the old keystore file (resolve actual filename for PKCS12)
            String keystorePath = scvCli.getKeystorePath();
            String keyStoreType = scvCli.getKeyStoreType();
            String resolvedPath = keystorePath;
            if ("PKCS12".equalsIgnoreCase(keyStoreType)) {
                resolvedPath = keystorePath.replaceAll(".jce", ".pk12");
            }
            File keystoreFile = new File(resolvedPath);
            if (keystoreFile.isFile()) {
                keystoreFile.delete();
            }

            // Create new vault with new password + new salt, preserving keystore type
            JCEKSSecureCredentialsVault newVault = new JCEKSSecureCredentialsVault(
                    keystorePath, newPassword, false, newSalt, keyStoreType);

            // Re-encrypt all credentials with the new key + salt
            for (Map.Entry<String, Credentials> entry : allCredentials.entrySet()) {
                newVault.setCredentials(entry.getKey(), entry.getValue());
            }

            // Write new password to scv.key file
            if (opennmsHome != null && !opennmsHome.isEmpty()) {
                ScvUtils.writeKeyToFile(opennmsHome, newPassword);
                System.out.println("Key rotation completed successfully.");
                System.out.println("Updated files (permissions set to 600):");
                System.out.println("  - " + resolvedPath);
                System.out.println("  - " + opennmsHome + "/etc/" + ScvUtils.SCV_KEY_FILENAME);
                System.out.println("  - " + opennmsHome + "/etc/" + ScvUtils.SCV_SALT_FILENAME);
            } else {
                System.out.println("Key rotation completed. Keystore re-encrypted with new password.");
                System.out.println("WARNING: Could not write scv.key file (OPENNMS_HOME not set).");
            }

            System.out.println(allCredentials.size() + " credential(s) migrated.");
            return 0;

        } catch (Exception e) {
            System.err.println("Key rotation failed: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }
}
