--
--    Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
--
--    This library is free software; you can redistribute it and/or
--    modify it under the terms of the GNU General Public
--    License as published by the Free Software Foundation; either
--    version 2.1 of the License, or (at your option) any later version.
--
--    This library is distributed in the hope that it will be useful,
--    but WITHOUT ANY WARRANTY; without even the implied warranty of
--    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
--    General Public License for more details.
--
--    You should have received a copy of the GNU General Public
--    License along with this library; if not, write to the Free Software
--    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
--
-- --------------------------------------------------------------------------
--
-- The following PL/PgSQL function is used to calculate the percentage
-- availability for all managed services on an interface between two dates. 
-- This function looks for all managed services tuples. The X date is the time 
-- closest to the current time. The Y date must be older than the X date. 
-- The downtime between the two dates are calculated based on all matching tuples.
--
-- @author Lawrence Karnowski <larry@opennms.org>
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
DROP FUNCTION getManagePercentAvailIntfWindow(integer,varchar(16),float8,float8);
DROP FUNCTION getManagePercentAvailIntfWindow(integer,varchar(16),text,text);
DROP FUNCTION getManagePercentAvailIntfWindow(integer,varchar(16),timestamp without time zone,timestamp without time zone);

CREATE FUNCTION getManagePercentAvailIntfWindow(integer,varchar(16),timestamp without time zone,timestamp without time zone)
	RETURNS float8 AS '
   DECLARE
	nid ALIAS FOR $1;
	ipid ALIAS FOR $2;
	xtime ALIAS FOR $3;
	ytime ALIAS FOR $4;
	downtime float8 := 0.0;
	count integer := 0;
	rollingWindow float := 0;
	totalServiceTime float := 0;
   BEGIN
	IF xtime < ytime THEN
		rollingWindow := EXTRACT (EPOCH FROM (ytime - xtime));
		downtime := getManagedOutageForIntfInWindow(nid, ipid, ytime, xtime)/1000;
	ELSE
		rollingWindow := EXTRACT (EPOCH FROM (xtime - ytime));
		downtime := getManagedOutageForIntfInWindow(nid, ipid, xtime, ytime)/1000;
	END IF;
	count := getManagedServiceCountForIntf(nid, ipid);
	totalServiceTime := count * rollingWindow;

	IF totalServiceTime > 0 THEN
		RETURN	100 * (1 - (downtime / totalServiceTime));
	ELSE
                IF totalServiceTime = 0 THEN
                        RETURN 100;
                ELSE
                        RETURN -1;
                END IF;
	END IF;    
   END;
' LANGUAGE 'plpgsql';
