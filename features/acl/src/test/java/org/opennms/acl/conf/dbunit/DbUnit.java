/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *
 * From the original copyright headers:
 *
 * Copyright (c) 2009+ desmax74
 * Copyright (c) 2009+ The OpenNMS Group, Inc.
 *
 * This program was developed and is maintained by Rocco RIONERO
 * ("the author") and is subject to dual-copyright according to
 * the terms set in "The OpenNMS Project Contributor Agreement".
 *
 * The author can be contacted at the following email address:
 *
 *     Massimiliano Dess&igrave;
 *     desmax74@yahoo.it
 *******************************************************************************/

package org.opennms.acl.conf.dbunit;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.dbunit.DBTestCase;
import org.dbunit.DatabaseUnitException;
import org.dbunit.DefaultDatabaseTester;
import org.dbunit.IDatabaseTester;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatDtdDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.opennms.acl.conf.Config;

public abstract class DbUnit extends DBTestCase {

    public DbUnit() {
    }

    public static IDatabaseConnection setUpConnection() {
        System.out.println("DbUnit setUpConnection");
        Config config = new Config();
        if (dbConn == null) {
            Connection jdbcConnection = null;

            try {
                Class.forName(config.getDbDriver());
                jdbcConnection = DriverManager.getConnection(config.getDbUrl(), config.getDbUser(), config.getDbPass());
                dbConn = new DatabaseConnection(jdbcConnection);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return dbConn;

    }

    @Override
    public IDatabaseTester newDatabaseTester() throws Exception {
    	return new DefaultDatabaseTester(setUpConnection());
    }

    public static void main(String[] args) throws Exception {
        Config config = new Config();
        Class.forName(config.getDbDriver());
        Connection jdbcConnection = DriverManager.getConnection(config.getDbUrl(), config.getDbUser(), config.getDbPass());
        IDatabaseConnection connection = new DatabaseConnection(jdbcConnection, "public");

        // dtd
        FlatDtdDataSet.write(connection.createDataSet(), new FileOutputStream("src/test/java/it/pronetics/acl/conf/dbunit/database-schema.dtd"));

        // export
        IDataSet fullDataSet = connection.createDataSet();
        FlatXmlDataSet.write(fullDataSet, new FileOutputStream("src/test/java/opennms/opennms/acl/conf/dbunit/full.xml"));
    }

    public void prepareDb() {
        try {
            try {
                DatabaseOperation.CLEAN_INSERT.execute(getConnection(), getDataSet());
            } finally {
                // connection.close();
            }
        } catch (DatabaseUnitException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        ++operation;
    }

    public void cleanDb() {
        try {

            try {
                DatabaseOperation.DELETE_ALL.execute(getConnection(), getDataSet());
            } finally {
                // connection.close();
            }
        } catch (Throwable e) {
            /* do we care?
            System.err.print("An error occurred deleting dataset: ");
            e.printStackTrace();
            */
        }

        ++operation;

    }

    public static void closeConnection() {
        System.out.println("DbUnit closeConnection");
        if (dbConn != null) {
            try {
                dbConn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                dbConn = null;
                System.gc();
            }
        }
        System.out.println("Total DbUnit Operation:" + operation);
    }

    private static IDatabaseConnection dbConn = setUpConnection();
    private static int operation;
}
