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
-- availability of a (Node, IP Address, Service) tuple between two dates. 
-- The X date is the time closest to the current time. The Y date must
-- be older than the X date. The downtime between the two dates are
-- calculated based on all matching tuples using the already written 
-- getOutageTimeInWindow. These function return the percentage availability 
-- of the tuple during the time frame. Percentage Availability returned will 
-- be 100%, if the X < Y, since getOutageTimeInWindow returns 0.
--
-- @author Jacinta Remedios <jacinta@opennms.org>
--
-- Dependencies: getOutageTimeInWindow() that gets the outage time for the tuple.
-- 
-- $1	The node identifier
-- $2	The ipAddress
-- $3	The service identifier
-- $4	X time. This is the time that is closest to current
-- $5	Y time. This is the time that is furtherest from current.
--
-- In time:  Now >= X >= Y 
-- (X - Y) is the window for which outage is calculated
--
--
DROP FUNCTION IF EXISTS getPercentAvailabilityInWindow(integer,varchar(16),integer,float8,float8);
DROP FUNCTION IF EXISTS getPercentAvailabilityInWindow(integer,varchar(16),integer,text,text);
DROP FUNCTION IF EXISTS getPercentAvailabilityInWindow(integer,varchar(16),integer,timestamp without time zone,timestamp without time zone);

CREATE OR REPLACE FUNCTION getPercentAvailabilityInWindow(integer,timestamp without time zone,timestamp without time zone)
        RETURNS float8 AS '
   DECLARE
        ipifid ALIAS FOR $1;
        xtime ALIAS FOR $2;
        ytime ALIAS FOR $3;
        downtime float8;
   BEGIN
        downtime := getOutageTimeInWindow(ipifid, xtime, ytime);
        IF xtime > ytime THEN
                RETURN 100 * (1 - (downtime / (EXTRACT(EPOCH FROM (xtime - ytime))* 1000)));
        ELSE
                RETURN 100 * (1 - (downtime / (EXTRACT(EPOCH FROM (ytime - xtime))* 1000)));
        END IF;
   END;
' LANGUAGE 'plpgsql';
