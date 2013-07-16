/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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
 *******************************************************************************/

package org.opennms.netmgt.poller.monitors;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.model.PollStatus;

/**
 * This class implements a basic JDBC monitoring framework; The idea is than
 * these tests doesn't take too long (or too much resources to run) and provide
 * the basic healt information about the polled server. See
 * <code>src/services/org/opennms/netmgt/poller</code> OpenNMS plugin
 * information at <a
 * href="http://www.opennms.org/users/docs/docs/html/devref.html">OpenNMS
 * developer site </a>
 *
 * @author Jose Vicente Nunez Zuleta (josevnz@users.sourceforge.net) - RHCE,
 *         SJCD, SJCP version 0.1 - 07/23/2002 * version 0.2 - 08/05/2002 --
 *         Added retry logic, input validations to poller.
 * @since 0.1
 * @version $Id: $
 */
final public class JDBCStoredProcedureMonitor extends JDBCMonitor
{
   /**
    * Class constructor.
    *
    * @throws java.lang.ClassNotFoundException if any.
    * @throws java.lang.InstantiationException if any.
    * @throws java.lang.IllegalAccessException if any.
    */
   public JDBCStoredProcedureMonitor() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
   }

   /** {@inheritDoc} */
   @Override
   public PollStatus checkDatabaseStatus(Connection con, Map<String, Object> parameters)
   {
	   
      PollStatus status = PollStatus.unavailable();
      CallableStatement cs = null;
      try
      {
         boolean bPass = false;
         String storedProcedure = ParameterMap.getKeyedString(parameters, "stored-procedure", null);
         if ( storedProcedure == null )
            return status;

         String schemaName = ParameterMap.getKeyedString(parameters, "schema", "test");

         String procedureCall = "{ ? = call " + schemaName + "." + storedProcedure + "()}";
         cs = con.prepareCall( procedureCall );
         
         LOG.debug("Calling stored procedure: {}", procedureCall);
         
         cs.registerOutParameter(1, java.sql.Types.BIT );
         cs.executeUpdate();
         bPass = cs.getBoolean( 1 );

         LOG.debug("Stored procedure returned: {}", bPass);

         // If the query worked, assume than the server is ok
         if (bPass)
         {
            status = PollStatus.available();
         }
      }
      catch (SQLException sqlEx)
      {
            String reason = "JDBC stored procedure call not functional: " + sqlEx.getSQLState() + ", " + sqlEx.toString();
        LOG.debug(reason, sqlEx);
            status = PollStatus.unavailable(reason);
      }
      finally
      {
    	  closeStmt(cs);
      }
      return status;
   }
} // End of class


