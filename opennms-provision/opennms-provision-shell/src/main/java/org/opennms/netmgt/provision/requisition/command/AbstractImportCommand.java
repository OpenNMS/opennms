/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.requisition.command;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.felix.gogo.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.springframework.transaction.support.TransactionOperations;

import com.google.common.base.Strings;

public abstract class AbstractImportCommand<T> extends OsgiCommandSupport {

    @Option(name = "-f", aliases = "--file", description = "The file to import.", required = false, multiValued = false)
    protected String file;

    @Option(name = "-d", aliases = "--dir", description = "The directory to import. Does not read the directory recursively.", required = false, multiValued = false)
    protected String directory;

    @Option(name = "-o", aliases = "--overwrite-existing", description = "Overwrite existing")
    protected boolean overwriteExisting = false;

    protected TransactionOperations transactionOperations;

    @Override
    protected Object doExecute() throws Exception {
        if (Strings.isNullOrEmpty(file) && Strings.isNullOrEmpty(directory)) {
            System.out.println("You must either define a file or directory to import");
            return null;
        }
        if (!Strings.isNullOrEmpty(file)) {
            importFile(file);
        }
        if (!Strings.isNullOrEmpty(directory)) {
            importDirectory(directory);
        }
        return null;
    }

    private void importFile(String file) {
        File f = new File(file);
        if (!f.exists()) {
            System.out.println("File '" + file + "' does not exist. Skipping.");
        } else if (!f.canRead()) {
            System.out.println("Cannot read from file '" + file + "'. Skipping.");
        } else if (!f.isFile()) {
            System.out.println("File '" + file + "' is not a file. Skipping.");
        } else {
            importFile(f);
        }
    }

    private void importFile(File file) {
        ImportItem itemToImport = getItemToImport(file);
        if (itemToImport.alreadyExists() && !overwriteExisting) {
            System.out.println(itemToImport.getType() + " with name '" + itemToImport.getName() + "' already exists. Skipping.");
        } else {
            transactionOperations.execute(status -> {
                try {
                    doImport(itemToImport);
                    System.out.println(itemToImport.getType() + " '" + itemToImport.getName() + " from '" + file + "' imported successfully");
                } catch (Exception ex) {
                    System.out.println(itemToImport.getType() + " '" + itemToImport.getName() + " from '" + file + "' could not be imported: " + ex.getMessage());
                }
                return null;
            });
        }
    }

    private void importDirectory(String directory) {
        File file = new File(directory);
        if (!file.exists()) {
            System.out.println("Directory '" + file + "' does not exist. Skipping.");
        } else if (!file.canRead()) {
            System.out.println("Cannot read from directory '" + file + "'. Skipping.");
        } else if (!file.isDirectory()) {
            System.out.println("'" + file + "' is not a directory. Skipping.");
        } else {
            importDirectory(file);
        }
    }

    private void importDirectory(File file) {
        final File pendingDirectory = new File(file, "pending");
        boolean usePendingDirectory = pendingDirectory.exists() && pendingDirectory.isDirectory();
        if (usePendingDirectory) {
            System.out.println("Detected pending directory. Will favor files from here.");
        }
        List<File> files = listFiles(file);
        for (File eachFile : files) {
            File fileToImport = getFileToImport(eachFile, usePendingDirectory);
            if (fileToImport != eachFile) {
                System.out.println("Importing pending file '" + fileToImport + "' instead of '" + eachFile + "'");
            }
            importFile(fileToImport);
        }
    }

    private File getFileToImport(File file, boolean usePendingDirectory) {
        if (usePendingDirectory) {
            File pendingFile = Paths.get(file.getParent(), "pending", file.getName()).toFile();
            if (pendingFile.isFile() && pendingFile.exists()) {
                return pendingFile;
            }
        }
        return file;
    }

    protected List<File> listFiles(File file) {
        return Arrays.asList(file.listFiles(pathname -> pathname.isFile() && pathname.getName().endsWith(".xml")));
    }

    public void setTransactionOperations(TransactionOperations transactionOperations) {
        this.transactionOperations = transactionOperations;
    }

    protected abstract void doImport(ImportItem<T> itemToImport);

    protected abstract ImportItem<T> getItemToImport(File file);

}
