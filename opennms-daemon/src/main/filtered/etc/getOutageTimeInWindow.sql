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
-- of a (Node, IP Address, Service) tuple between two dates. The
-- X date is the time closest to the current time. The Y date must
-- be older than the X date. The downtime between the two dates are
-- calculated based on all matching tuples.
--
-- @author Brian Weave <weave@oculan.com>
-- @author Sowmya Nataraj <sowmya@opennms.org>
--
-- Dependencies: outages table from etc/create.sql
-- 
-- $1	The node identifier
-- $2	The ipAddress
-- $3	The service identifier
-- $4	X time. This is the time that is closest to current
-- $5	Y time. This is the time that is furtherest from current.
--
-- In time:  Now >= X >= Y 
-- (X -Y) is the window for which outage is calculated
--
--
DROP FUNCTION getOutageTimeInWindow(integer,varchar(16),integer,float8,float8);
DROP FUNCTION getOutageTimeInWindow(integer,varchar(16),integer,text,text);
DROP FUNCTION getOutageTimeInWindow(integer,varchar(16),integer,timestamp without time zone,timestamp without time zone);

CREATE FUNCTION getOutageTimeInWindow(integer,varchar(16),integer,timestamp without time zone,timestamp without time zone)
	RETURNS float8 AS '
   DECLARE
	nid ALIAS FOR $1;
	ipid ALIAS FOR $2;
	sid ALIAS FOR $3;
	xtime ALIAS FOR $4;
	ytime ALIAS FOR $5;
	orec RECORD;
	lostTime timestamp without time zone;
	gainTime timestamp without time zone;
	downtime float8;
	zero CONSTANT float8 := 0.0;
	epochTime CONSTANT timestamp without time zone := to_timestamp(''01 Jan 1970 00:00:00'', ''DD Mon YYYY HH24:MI:SS'');
   BEGIN
   	downtime = zero;
   	FOR orec IN SELECT ifLostService,ifRegainedService
		FROM outages WHERE (nodeID = nid AND ipAddr = ipid AND serviceID = sid AND
		                   ((ifRegainedService IS NULL AND ifLostService <= xtime)  OR
				    (ifRegainedService > ytime)))
	LOOP
	 BEGIN
		gainTime := epochTime;
		lostTime := orec.ifLostService;
		IF orec.ifRegainedService IS NOT NULL THEN
			gainTime := orec.ifRegainedService;
		END IF;
		--
		-- Find the appropriate records
		--
		IF xtime > lostTime THEN
		 --
		 -- for any outage to be in window of 
		 -- opportunity the lost time must ALWAYS be
		 -- less that the x time.
		 --
		 IF gainTime = epochTime THEN
		  --
		  -- if the gain time is epochTime then the outage
		  -- does not have an uptime.
		  --
		   IF ytime > lostTime THEN
		    downtime := downtime + EXTRACT(EPOCH FROM (xtime - ytime));
		   ELSE
		    downtime := downtime + EXTRACT(EPOCH FROM (xtime - lostTime));
		   END IF;
		 ELSE
		  IF xtime > gainTime AND gainTime > ytime THEN
		   --
		   -- regain time between x & y
		   --
		    IF ytime > lostTime THEN
		     downtime := downtime + EXTRACT (EPOCH FROM (gainTime - ytime));
		    ELSE
		     downtime := downtime + EXTRACT (EPOCH FROM (gainTime - lostTime));
		    END IF; 
		  ELSE
		   IF gainTime > xtime THEN
		   --
		   -- regain time greater than x, lost less that x
		   --
		    IF ytime > lostTime THEN
		     downtime := downtime + EXTRACT (EPOCH FROM (xtime - ytime));
		    ELSE
		     downtime := downtime + EXTRACT (EPOCH FROM (xtime - lostTime));
		    END IF;
		   -- end gainTime > xtime
		   END IF;
		  -- end xtime > gainTime AND gainTime > ytime
		  END IF;
		 -- end gaintime == epochTime
		 END IF;
		-- end xtime > lostTime
		END IF;
	 END;
	END LOOP;
	RETURN downtime*1000.0;
   END;
' LANGUAGE 'plpgsql';



