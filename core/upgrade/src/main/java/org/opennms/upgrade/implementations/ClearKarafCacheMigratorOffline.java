/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

package org.opennms.upgrade.implementations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.upgrade.api.AbstractOnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;

public class ClearKarafCacheMigratorOffline  extends AbstractOnmsUpgrade {

    private final Path opennmsDataPath;

    public ClearKarafCacheMigratorOffline() throws OnmsUpgradeException {
         this.opennmsDataPath = Path.of(ConfigFileConstants.getHome()).resolve("data");
    }

    @Override
    public int getOrder() {
        return 16;
    }

    @Override
    public String getDescription() {
        return String.format("Clears the Karaf cache in '%s', see NMS-16226", this.opennmsDataPath);
    }

    @Override
    public void preExecute() throws OnmsUpgradeException {
    }

    @Override
    public void postExecute() throws OnmsUpgradeException {
    }

    @Override
    public void rollback() throws OnmsUpgradeException {
    }

    @Override
    public void execute() throws OnmsUpgradeException {
        final Path historyFilePath = this.opennmsDataPath.resolve("history.txt");
        try {
            Files.walk(this.opennmsDataPath)
                    .filter((path) -> !path.equals(historyFilePath) && !path.equals(this.opennmsDataPath))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            throw new OnmsUpgradeException(String.format("Error pruning Karaf's data directory '%s'.", this.opennmsDataPath), e);
        }
    }

    @Override
    public boolean requiresOnmsRunning() {
        return false;
    }

    @Override
    public boolean runOnlyOnce() {
        return false;
    }
}
