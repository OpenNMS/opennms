-- ------------------------------------------------------------------------------
-- This file is part of OpenNMS(R).
--
-- Copyright (C) 1999-2014 The OpenNMS Group, Inc.
-- OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
--
-- OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
--
-- OpenNMS(R) is free software: you can redistribute it and/or modify
-- it under the terms of the GNU Affero General Public License as published
-- by the Free Software Foundation, either version 3 of the License,
-- or (at your option) any later version.
--
-- OpenNMS(R) is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU Affero General Public License for more details.
--
-- You should have received a copy of the GNU Affero General Public License
-- along with OpenNMS(R).  If not, see:
--      http://www.gnu.org/licenses/
--
-- For more information contact:
--     OpenNMS(R) Licensing <license@opennms.org>
--     http://www.opennms.org/
--     http://www.opennms.com/
-- ------------------------------------------------------------------------------
--
-- The following PL/PgSQL function is used to calculate the percentage
-- availability for all managed services on node between two dates. This function
-- looks for all managed services tuples. The X date is the time 
-- closest to the current time. The Y date must be older than the X date. 
-- The downtime between the two dates are calculated based on all matching tuples.
--
-- @author Jacinta Remedios <jacinta@opennms.org>
--
-- Dependencies: getManagedOutageForNodeInWindow to compute the outage time for a node.
-- 		 getManagedServiceCountForNode to compute the number of managed services on a node.
-- 
-- $1	The node identifier
-- $2	X time. This is the time that is closest to current
-- $3	Y time. This is the time that is furtherest from current.
--
-- In time:  Now >= X >= Y 
-- (X - Y) is the window for which outage is calculated
--
--
DROP FUNCTION IF EXISTS getManagePercentAvailNodeWindow(integer,float8,float8);
DROP FUNCTION IF EXISTS getManagePercentAvailNodeWindow(integer,text,text);

CREATE OR REPLACE FUNCTION getManagePercentAvailNodeWindow(integer,timestamp without time zone,timestamp without time zone)
        RETURNS float8 AS '
   DECLARE
        nid ALIAS FOR $1;
        xtime ALIAS FOR $2;
        ytime ALIAS FOR $3;
        downtime float8 := 0.0;
        count integer := 0;
        rollingWindow float := 0;
        totalServiceTime float := 0;
   BEGIN
        IF xtime < ytime THEN
                rollingWindow := EXTRACT (EPOCH FROM (ytime - xtime));
                downtime := getManagedOutageForNodeInWindow(nid, ytime, xtime)/1000;
        ELSE
                rollingWindow := EXTRACT (EPOCH FROM (xtime - ytime));
                downtime := getManagedOutageForNodeInWindow(nid, xtime, ytime)/1000;
        END IF;
        count := getManagedServiceCountForNode(nid);
        totalServiceTime := count * rollingWindow;

        IF totalServiceTime > 0 THEN
                RETURN 100 * (1 - (downtime / totalServiceTime));
        ELSE
                IF totalServiceTime = 0 THEN
                        RETURN 100;
                ELSE
                        RETURN -1;
                END IF;
        END IF;
   END;
' LANGUAGE 'plpgsql';
