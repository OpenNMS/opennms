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
-- The following PL/PgSQL function is used to calculate the number 
-- of all managed services on node. This function looks for all managed services 
-- tuples and returns their count. 
--
-- @author Lawrence Karnowski <larry@opennms.org>
-- @author Jacinta Remedios <jacinta@opennms.org>
--
-- Dependencies: ipinterface and ifservices table from etc/create.sql
-- 
-- $1	The node identifier
-- $2	X time. This is the time that is closest to current
-- $3	Y time. This is the time that is furtherest from current.
--
DROP FUNCTION getManagedServiceCountForIntf(integer,varchar(16));

CREATE FUNCTION getManagedServiceCountForIntf(integer,varchar(16))
	RETURNS float8 AS '
   DECLARE
	nid ALIAS FOR $1;
	ipid ALIAS FOR $2;
	orec RECORD;
	counter float8;
   BEGIN
	counter = 0;
	FOR orec IN SELECT DISTINCT ifservices.nodeid, ifservices.ipaddr, ifservices.serviceid 
		FROM ipinterface, ifservices 
		WHERE ifservices.nodeid = nid 
			AND ifservices.ipaddr = ipid 
			AND ipinterface.nodeid = nid 
			AND ipinterface.ipaddr = ipid 
			AND ipinterface.ismanaged = ''M'' 
			AND ifservices.status = ''A''
	LOOP
		BEGIN
			counter := counter + 1;
		END;
	END LOOP;
	RETURN counter;
   END;
' LANGUAGE 'plpgsql';
