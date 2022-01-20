/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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
 ******************************************************************************/

package org.opennms.core.schema.migrator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Verify operation of the Migrator.
 *
 * Note this is incomplete.
 */
@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
public class MigratorTest {

    private Migrator target;

    private MigratorResourceProvider mockResourceProvider;
    private MigratorLiquibaseExecutor mockLiquibaseExecutor;

    private DataSource mockDataSource;
    private Connection mockConnection;
    private Statement mockStatement1;
    private ResultSet mockResultSet1;
    private ResultSet mockEncodingResultSet;
    private ResultSet mockTimeResultSet;
    private ResultSet mockTableNamesResultSet;

    private Timestamp mockTimestamp;
    private DatabaseMetaData mockDatabaseMetaData;

    @Before
    public void setUp() throws Exception {
        this.mockResourceProvider = Mockito.mock(MigratorResourceProvider.class);
        this.mockLiquibaseExecutor = Mockito.mock(MigratorLiquibaseExecutor.class);

        this.mockDataSource = Mockito.mock(DataSource.class);
        this.mockConnection = Mockito.mock(Connection.class);
        this.mockStatement1 = Mockito.mock(Statement.class);
        this.mockResultSet1 = Mockito.mock(ResultSet.class);
        this.mockEncodingResultSet   = Mockito.mock(ResultSet.class);
        this.mockTimeResultSet       = Mockito.mock(ResultSet.class);
        this.mockTableNamesResultSet = Mockito.mock(ResultSet.class);

        this.mockTimestamp = Mockito.mock(Timestamp.class);
        this.mockDatabaseMetaData = Mockito.mock(DatabaseMetaData.class);

        this.target = new Migrator(this.mockResourceProvider, this.mockLiquibaseExecutor);

        Mockito.when(this.mockDataSource.getConnection()).thenReturn(this.mockConnection);

        Mockito.when(this.mockConnection.createStatement()).thenReturn(this.mockStatement1);
        Mockito.when(this.mockConnection.getMetaData()).thenReturn(this.mockDatabaseMetaData);

        Mockito
                .when(this.mockStatement1.executeQuery("SELECT oid FROM pg_proc WHERE proname='plpgsql_call_handler' AND proargtypes = ''"))
                .thenReturn(this.mockResultSet1);

        Mockito
                .when(this.mockStatement1.executeQuery("SELECT pg_language.oid "
                        + "FROM pg_language, pg_proc WHERE "
                        + "pg_proc.proname='plpgsql_call_handler' AND "
                        + "pg_proc.proargtypes = '' AND "
                        + "pg_proc.oid = pg_language.lanplcallfoid AND "
                        + "pg_language.lanname = 'plpgsql'"))
                .thenReturn(this.mockResultSet1)
                ;

        Mockito.when(this.mockEncodingResultSet.next()).thenReturn(true);
        Mockito.when(this.mockEncodingResultSet.getInt(1)).thenReturn(6);

        Mockito
                .when(this.mockStatement1.executeQuery(
                        "SELECT encoding FROM pg_database WHERE LOWER(datname)='x-dbname-x'"
                        ))
                .thenReturn(this.mockEncodingResultSet)
                ;

        Mockito.when(this.mockTimestamp.getTime()).thenReturn(System.currentTimeMillis()); // WARNING: potential for false positives on slow systems
        Mockito.when(this.mockTimeResultSet.next()).thenReturn(true);
        Mockito.when(this.mockTimeResultSet.getTimestamp(1)).thenReturn(this.mockTimestamp);

        Mockito
                .when(this.mockStatement1.executeQuery("SELECT NOW()"))
                .thenReturn(this.mockTimeResultSet)
                ;

        Mockito
                .when(this.mockTableNamesResultSet.next())
                .thenReturn(false)
                ;

        Mockito
                .when(this.mockDatabaseMetaData.getTables(Mockito.eq(null), Mockito.eq("public"), Mockito.eq("%"), Mockito.any()))
                .thenReturn(this.mockTableNamesResultSet)
                ;
    }

    /**
     * Test the common use-case of setupDatabase.
     */
    @Test
    public void testCommonSetupDatabase() throws Exception {
        //
        // Setup test data and interactions
        //
        Mockito.when(this.mockResultSet1.next()).thenReturn(false);

        this.target.setDataSource(this.mockDataSource);
        this.target.setAdminDataSource(this.mockDataSource);
        this.target.setDatabaseName("x-dbname-x");

        //
        // Execute
        //
        this.target.setupDatabase(true, false, false, true, false);


        //
        // Validate
        //
        Mockito
                .verify(this.mockStatement1)
                .execute(Mockito.matches(Pattern.quote("CREATE FUNCTION plpgsql_call_handler () RETURNS OPAQUE AS '$libdir/plpgsql.") + ".*" + Pattern.quote("' LANGUAGE 'c'")))
                ;

        Mockito
                .verify(this.mockStatement1)
                .execute("CREATE TRUSTED PROCEDURAL LANGUAGE 'plpgsql' "
                        + "HANDLER plpgsql_call_handler LANCOMPILER 'PL/pgSQL'")
                ;
    }
}