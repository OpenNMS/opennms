/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2009 Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * Copyright (C) 2009 The OpenNMS Group, Inc.
 * All rights reserved.
 *
 * This program was developed and is maintained by Rocco RIONERO
 * ("the author") and is subject to dual-copyright according to
 * the terms set in "The OpenNMS Project Contributor Agreement".
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.acl.conf.dbunit;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.dbunit.DBTestCase;
import org.dbunit.DatabaseUnitException;
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
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return dbConn;

    }

    public IDatabaseConnection getConnection() throws Exception {

        if (null == dbConn) {
            dbConn = setUpConnection();
        }
        return dbConn;
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
        } catch (Exception e) {
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
        } catch (Exception e) {
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
