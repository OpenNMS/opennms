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
-- The following PL/PgSQL function is used to calculate the downtime
-- of all managed services on node between two dates. This function
-- looks for all managed services tuples. The X date is the time 
-- closest to the current time. The Y date must be older than the X date. 
-- The downtime between the two dates are calculated based on all matching tuples.
--
-- @author Jacinta Remedios <jacinta@opennms.org>
--
-- Dependencies: ipinterface and ifservices table from etc/create.sql
-- 		 getOutageTimeInWindow to compute the outage time for a tuple.
-- 
-- $1	The node identifier
-- $2	X time. This is the time that is closest to current
-- $3	Y time. This is the time that is furtherest from current.
--
-- In time:  Now >= X >= Y 
-- (X - Y) is the window for which outage is calculated
--
--
DROP FUNCTION getManagedOutageForIntfInWindow(integer,varchar(16),float8,float8);
DROP FUNCTION getManagedOutageForIntfInWindow(integer,varchar(16),text,text);
DROP FUNCTION getManagedOutageForIntfInWindow(integer,varchar(16),timestamp without time zone,timestamp without time zone);

CREATE FUNCTION getManagedOutageForIntfInWindow(integer,varchar(16),timestamp without time zone,timestamp without time zone)
	RETURNS float8 AS '
   DECLARE
	nid ALIAS FOR $1;
	ipid ALIAS FOR $2;
	xtime ALIAS FOR $3;
	ytime ALIAS FOR $4;
	downtime float8 := 0.0;
	orec RECORD;
   BEGIN
	FOR orec IN SELECT distinct ifservices.nodeid, ifservices.ipaddr, ifservices.serviceid FROM ipinterface, ifservices where ifservices.nodeid = nid AND ifservices.ipaddr = ipid AND ipinterface.nodeid = nid AND ipinterface.ipaddr = ipid AND ipinterface.ismanaged = ''M'' AND ifservices.status = ''A''
	LOOP
		BEGIN
			downtime := downtime + getOutageTimeInWindow( orec.nodeid, orec.ipaddr, orec.serviceid, xtime, ytime);
		END;
	END LOOP;
	RETURN downtime;
   END;
' LANGUAGE 'plpgsql';
