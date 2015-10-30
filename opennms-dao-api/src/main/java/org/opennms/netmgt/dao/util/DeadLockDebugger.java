/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import org.opennms.core.db.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DeadlockLoserDataAccessException;

/**
 * Gathers additional system details when deadlock related exception are
 * detected.
 *
 * Initially developed to help isolate the cause of NMS-7899.
 *
 * @author jwhite
 */
public class DeadLockDebugger {

    private static final Logger LOG = LoggerFactory.getLogger(DeadLockDebugger.class);

    private static final long MIN_DEADLOCK_DETAILS_DELAY_MS = 5*60*1000L;

    private static volatile long lastDeadlockTimeMillis = 0L;

    public static void gatherDetails(Throwable e) {
        if (isDeadlockRelated(e)) {
            handleDeadlockRelatedException(e);
        }
    }

    /**
     * Recursively walks to trail to exceptions in order to determine if the
     * cause was related to a deadlock.
     */
    public static boolean isDeadlockRelated(Throwable causedBy) {
        if (causedBy == null) {
            return false;
        } else if (causedBy instanceof DeadlockLoserDataAccessException) {
            return true;
        } else if (causedBy.getMessage() != null && causedBy.getMessage().contains("deadlock detected")) {
            // Detected exception of type:
            // Caused by: org.postgresql.util.PSQLException: ERROR: deadlock
            // detected
            return true;
        } else {
            return isDeadlockRelated(causedBy.getCause());
        }
    }

    public static synchronized void handleDeadlockRelatedException(Throwable e) {
        LOG.error("The following exception appears to be related to a deadlock.", e);

        // Gathering additional details may take a lot of system resources, so we
        // perform some simple rate limiting
        long now = System.currentTimeMillis();
        if ((lastDeadlockTimeMillis + MIN_DEADLOCK_DETAILS_DELAY_MS) > now) {
            return;
        } else {
            lastDeadlockTimeMillis = now;
        }

        // Grab a thread dump
        Map<Thread, StackTraceElement[]> threadDump = Thread.getAllStackTraces();
        StringBuilder sb = new StringBuilder();
        for (Entry<Thread, StackTraceElement[]> stackTrace : threadDump.entrySet()) {
            Thread thread = stackTrace.getKey();

            sb.append(String.format("Thread[%d]: %s\n", thread.getId(), thread.getName()));
            for (StackTraceElement element :stackTrace.getValue()) {
                sb.append("\t at ");
                sb.append(element.toString());
                sb.append("\n");
            }
            sb.append("\n");
        }
        LOG.error("Thread dump near time of deadlock:\n {}", sb);

        // Gather PostgreSQL activity
        try {
            try (Connection conn = DataSourceFactory.getInstance().getConnection()) {
                PreparedStatement stmt = conn.prepareStatement("select * from pg_stat_activity");

                sb = new StringBuilder();
                printQuery(stmt, sb);

                LOG.error("Current database activity:\n{}", sb);
            }
        } catch (Throwable ex) {
            LOG.error("Failed to gather PostgreSQL activity for deadlock diagnostics.", e);
        }
    }

    // Adapted from activemq-core/src/test/java/org/apache/activemq/store/jdbc/JDBCNegativeQueueTest.java
    private static void printQuery(PreparedStatement s, StringBuilder sb) throws SQLException {
        ResultSet set = null;
        try {
            set = s.executeQuery();
            ResultSetMetaData metaData = set.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                if (i == 1)
                    sb.append("||");
                sb.append(metaData.getColumnName(i) + "||");
            }
            sb.append("\n");
            while (set.next()) {
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    if (i == 1)
                        sb.append("|");
                    sb.append(set.getString(i) + "|");
                }
                sb.append("\n");
            }
        } finally {
            try {
                set.close();
            } catch (Throwable ignore) {
            }
            try {
                s.close();
            } catch (Throwable ignore) {
            }
        }
    }
}
