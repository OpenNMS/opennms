create function public.getmanagepercentavailintfwindow(integer, character varying, timestamp without time zone, timestamp without time zone) returns double precision
    language plpgsql
as
$$
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
                RETURN  100 * (1 - (downtime / totalServiceTime));
ELSE
                IF totalServiceTime = 0 THEN
                        RETURN 100;
ELSE
                        RETURN -1;
END IF;
END IF;
END;
$$;

alter function public.getmanagepercentavailintfwindow(integer, varchar, timestamp, timestamp) owner to opennms;

create function public.getmanagepercentavailnodewindow(integer, timestamp without time zone, timestamp without time zone) returns double precision
    language plpgsql
as
$$
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
$$;

alter function public.getmanagepercentavailnodewindow(integer, timestamp, timestamp) owner to opennms;

create function public.getmanagedoutageforintfinwindow(integer, character varying, timestamp without time zone, timestamp without time zone) returns double precision
    language plpgsql
as
$$
DECLARE
nid ALIAS FOR $1;
        ipid ALIAS FOR $2;
        xtime ALIAS FOR $3;
        ytime ALIAS FOR $4;
        downtime float8 := 0.0;
        orec RECORD;
BEGIN
FOR orec IN SELECT DISTINCT ifservices.id AS ifServiceId
            FROM ifservices, ipinterface, node
            WHERE ifservices.ipInterfaceId = ipInterface.id
              AND ipinterface.nodeid = node.nodeid
              AND ifservices.status = 'A'
              AND ipinterface.ismanaged = 'M'
              AND ipinterface.ipaddr = ipid
              AND node.nodeid = nid
              AND node.nodetype = 'A'
                LOOP
BEGIN
                        downtime := downtime + getOutageTimeInWindow( orec.ifServiceId, xtime, ytime);
END;
END LOOP;
RETURN downtime;
END;
$$;

alter function public.getmanagedoutageforintfinwindow(integer, varchar, timestamp, timestamp) owner to opennms;

create function public.getmanagedoutagefornodeinwindow(integer, timestamp without time zone, timestamp without time zone) returns double precision
    language plpgsql
as
$$
DECLARE
nid ALIAS FOR $1;
        xtime ALIAS FOR $2;
        ytime ALIAS FOR $3;
        downtime float8 := 0.0;
        orec RECORD;
BEGIN
FOR orec IN SELECT DISTINCT ifservices.id AS ifServiceId
            FROM ifservices, ipinterface, node
            WHERE ifservices.ipInterfaceId = ipInterface.id
              AND ipinterface.nodeid = node.nodeid
              AND ifservices.status = 'A'
              AND ipinterface.ismanaged = 'M'
              AND node.nodeid = nid
              AND node.nodetype = 'A'
                LOOP
BEGIN
                        downtime := downtime + getOutageTimeInWindow( orec.ifServiceId, xtime, ytime);
END;
END LOOP;
RETURN downtime;
END;
$$;

alter function public.getmanagedoutagefornodeinwindow(integer, timestamp, timestamp) owner to opennms;

create function public.getmanagedservicecountforintf(integer, character varying) returns double precision
    language plpgsql
as
$$
DECLARE
nid ALIAS FOR $1;
        ipid ALIAS FOR $2;
        orec RECORD;
        counter float8;
BEGIN
        counter = 0;
FOR orec IN SELECT DISTINCT ifservices.ipInterfaceId, ifservices.serviceid
            FROM ifservices, ipinterface, node
            WHERE ifservices.ipInterfaceId = ipInterface.id
              AND ipinterface.nodeid = node.nodeid
              AND ifservices.status = 'A'
              AND ipinterface.ismanaged = 'M'
              AND ipinterface.ipaddr = ipid
              AND node.nodeid = nid
              AND node.nodetype = 'A'
                LOOP
BEGIN
                        counter := counter + 1;
END;
END LOOP;
RETURN counter;
END;
$$;

alter function public.getmanagedservicecountforintf(integer, varchar) owner to opennms;

create function public.getmanagedservicecountfornode(integer) returns double precision
    language plpgsql
as
$$
DECLARE
nid ALIAS FOR $1;
        orec RECORD;
        counter float8;
BEGIN
        counter = 0;
FOR orec IN SELECT DISTINCT ifservices.ipInterfaceId, ifservices.serviceid
            FROM ifservices, ipinterface, node
            WHERE ifservices.ipInterfaceId = ipInterface.id
              AND ipinterface.nodeid = node.nodeid
              AND ifservices.status = 'A'
              AND ipinterface.ismanaged = 'M'
              AND node.nodeid = nid
              AND node.nodetype = 'A'
                LOOP
BEGIN
                         counter := counter + 1;
END;
END LOOP;
RETURN counter;
END;
$$;

alter function public.getmanagedservicecountfornode(integer) owner to opennms;

create function public.getoutagetimeinwindow(integer, timestamp without time zone, timestamp without time zone) returns double precision
    language plpgsql
as
$$
DECLARE
ifsrvid ALIAS FOR $1;
        xtime ALIAS FOR $2;
        ytime ALIAS FOR $3;
        orec RECORD;
        lostTime timestamp without time zone;
        gainTime timestamp without time zone;
        downtime float8;
        zero CONSTANT float8 := 0.0;
        epochTime CONSTANT timestamp without time zone := to_timestamp('01 Jan 1970 00:00:00', 'DD Mon YYYY HH24:MI:SS');
BEGIN
        downtime = zero;
FOR orec IN SELECT ifLostService,ifRegainedService
            FROM outages
            WHERE ifServiceId = ifsrvid AND perspective IS NULL
              AND (
                (ifRegainedService IS NULL AND ifLostService <= xtime)
                    OR (ifRegainedService > ytime)
                )
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
                   -- regain time between x and y
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
$$;

alter function public.getoutagetimeinwindow(integer, timestamp, timestamp) owner to opennms;

create function public.getpercentavailabilityinwindow(integer, timestamp without time zone, timestamp without time zone) returns double precision
    language plpgsql
as
$$
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
$$;

alter function public.getpercentavailabilityinwindow(integer, timestamp, timestamp) owner to opennms;

create function public.drop_trigger_if_exists(character varying, character varying) returns void
    language plpgsql
as
$$
DECLARE
in_triggername ALIAS FOR $1;
                in_tablename ALIAS FOR $2;

BEGIN
                PERFORM tgname FROM pg_catalog.pg_trigger, pg_catalog.pg_class WHERE pg_catalog.pg_class.oid = pg_catalog.pg_trigger.tgrelid AND tgname = in_triggername AND pg_catalog.pg_class.relname = in_tablename;
                IF FOUND THEN
                        EXECUTE 'DROP TRIGGER ' || in_triggername || ' ON ' || in_tablename;
END IF;
                RETURN;
END;
$$;

alter function public.drop_trigger_if_exists(varchar, varchar) owner to opennms;

create function public.generate_daily_series(start timestamp without time zone, days integer) returns SETOF daily_series
    language sql
as
$$ select $1 + CAST(n || ' days' as interval) as ds, $1 + CAST((n+1)||' days' as interval) as de, n as dow from generate_series(0,$2) as n $$;

alter function public.generate_daily_series(timestamp, integer) owner to opennms;

create function public.iplike(i_ipaddress text, i_rule text) returns boolean
    language plpgsql
as
$$
declare
c_i integer;
    c_r text;

    c_addrwork text;
    c_addrtemp text;
    c_rulework text;
    c_ruletemp text;
    c_scopeid text;
    c_rulescope text;

    i integer;

begin
    if i_ipaddress is NULL or i_ipaddress is null then
        return 'f';
end if;

    if i_rule = '*.*.*.*' or i_rule = '*:*:*:*:*:*:*:*' then
        return 't';
end if;

    -- First, strip apart the IP address into octets, and
    -- verify that they are legitimate (0-255)
    --
    -- IPv4
    if i_ipaddress ~ E'^[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+$' and i_rule ~ E'^[0-9*,-]+\.[0-9*,-]+\.[0-9*,-]+\.[0-9*,-]+$' then
        c_addrwork := i_ipaddress;
        c_rulework := i_rule;

        i := 0;
        while i < 4 loop
            if (strpos(c_addrwork, '.') > 0) then
                c_i := to_number(substr(c_addrwork, 0, strpos(c_addrwork, '.')), '999');
else
                c_i := to_number(c_addrwork, '999');
end if;

            if c_i > 255 then
                return 'f';
end if;
            c_addrwork := ltrim(ltrim(c_addrwork, '0123456789'), '.');

            if (strpos(c_rulework, '.') > 0) then
                c_r := substr(c_rulework, 0, strpos(c_rulework, '.'));
else
                c_r := c_rulework;
end if;

            if check_rule(c_i, c_r) is not true then
                return 'f';
end if;

            c_rulework := ltrim(ltrim(c_rulework, '0123456789,-*'), '.');

            i := i + 1;
end loop;
    -- IPv6
    elsif i_ipaddress ~ E'^[0-9a-f]+:[0-9a-f]+:[0-9a-f]+:[0-9a-f]+:[0-9a-f]+:[0-9a-f]+:[0-9a-f]+:[0-9a-f]+(%.+)?$' and i_rule ~ E'^[0-9a-f*,-]+:[0-9a-f*,-]+:[0-9a-f*,-]+:[0-9a-f*,-]+:[0-9a-f*,-]+:[0-9a-f*,-]+:[0-9a-f*,-]+:[0-9a-f*,-]+(%.+)?$' then
        c_addrwork := i_ipaddress;
        c_rulework := i_rule;

        i := 0;
        while i < 8 loop
            if (strpos(c_addrwork, ':') > 0) then
                c_addrtemp = substr(c_addrwork, 0, strpos(c_addrwork, ':'));
else
                c_addrtemp = c_addrwork;
end if;
            if (strpos(c_addrtemp, '%') > 0) then
                -- Strip off the scope ID for now
                c_scopeid = substr(c_addrtemp, strpos(c_addrtemp, '%') + 1);
                c_addrtemp = substr(c_addrtemp, 0, strpos(c_addrtemp, '%'));
end if;
            while length(c_addrtemp) < 4 loop
                c_addrtemp := '0' || c_addrtemp;
end loop;
            c_i := cast(cast('x' || cast(c_addrtemp as text) as bit(16)) as integer);

            -- Max 16-bit integer value
            if c_i > 65535 then
                return 'f';
end if;
            c_addrwork := ltrim(ltrim(c_addrwork, '0123456789abcdef'), ':');

            if (strpos(c_rulework, ':') > 0) then
                c_r := substr(c_rulework, 0, strpos(c_rulework, ':'));
            elsif (strpos(c_rulework, '%') > 0) then
                c_rulescope := substr(c_rulework, strpos(c_rulework, '%') + 1);
                c_r := substr(c_rulework, 0, strpos(c_rulework, '%'));
else
                c_r := c_rulework;
end if;

            if check_hex_rule(c_i, c_r) is not true then
                return 'f';
end if;
            if (c_rulescope is not null) and ((c_scopeid = c_rulescope) is not true) then
                return 'f';
end if;

            c_rulework := ltrim(ltrim(c_rulework, '0123456789abcdef,-*'), ':');

            i := i + 1;
end loop;
else
        return 'f';
end if;

return 't';
end;
$$;

alter function public.iplike(text, text) owner to opennms;

create function public.check_range(i_octet integer, i_rule text) returns boolean
    language plpgsql
as
$$
declare
c_r1 integer;
    c_r2 integer;
begin

    c_r1 := to_number(split_part(i_rule, '-', 1), '999');
    c_r2 := to_number(split_part(i_rule, '-', 2), '999');
    if i_octet between c_r1 and c_r2 then
        return 't';
end if;
return 'f';
end;
$$;

alter function public.check_range(integer, text) owner to opennms;

create function public.check_hex_range(i_octet integer, i_rule text) returns boolean
    language plpgsql
as
$$
declare
c_temp text;
    c_r1 integer;
    c_r2 integer;
begin

    c_temp := split_part(i_rule, '-', 1);
    while length(c_temp) < 4 loop
        c_temp := '0' || c_temp;
end loop;
    c_r1 := cast(cast('x' || cast(c_temp as text) as bit(16)) as integer);
    c_temp := split_part(i_rule, '-', 2);
    while length(c_temp) < 4 loop
        c_temp := '0' || c_temp;
end loop;
    c_r2 := cast(cast('x' || cast(c_temp as text) as bit(16)) as integer);
    if i_octet between c_r1 and c_r2 then
        return 't';
end if;
return 'f';
end;
$$;

alter function public.check_hex_range(integer, text) owner to opennms;

create function public.check_rule(i_octet integer, i_rule text) returns boolean
    language plpgsql
as
$$
declare
c_element text;
    c_work    text;
begin
    if i_rule = '*' then   -- * matches anything!
        return 't';
end if;

    c_work := i_rule;
    while c_work <> '' loop
        -- raise notice 'c_work = %',c_work;
        if c_work ~ ',' then
            c_element := substr(c_work, 0, strpos(c_work, ','));
            c_work := substr(c_work, strpos(c_work, ',')+1);
else
            c_element := c_work;
            c_work := '';
end if;

        if c_element ~ '-' then
            if check_range(i_octet, c_element) then
                return 't';
end if;
else
            if c_element = '*' then
                return 't';
            elsif i_octet = to_number(c_element, '99999') then
                return 't';
end if;
end if;
end loop;
return 'f';
end;
$$;

alter function public.check_rule(integer, text) owner to opennms;

create function public.check_hex_rule(i_octet integer, i_rule text) returns boolean
    language plpgsql
as
$$
declare
c_element text;
    c_work  text;
begin
    if i_rule = '*' then   -- * matches anything!
        return 't';
end if;

    c_work := i_rule;
    while c_work <> '' loop
        -- raise notice 'c_work = %',c_work;
        if c_work ~ ',' then
            c_element := substr(c_work, 0, strpos(c_work, ','));
            c_work := substr(c_work, strpos(c_work, ',')+1);
else
            c_element := c_work;
            c_work := '';
end if;

        if c_element ~ '-' then
            if check_hex_range(i_octet, c_element) then
                return 't';
end if;
else
            while length(c_element) < 4 loop
                c_element := '0' || c_element;
end loop;
            if i_octet = cast(cast('x' || cast(c_element as text) as bit(16)) as integer) then
                return 't';
end if;
end if;
end loop;
return 'f';
end;
$$;

alter function public.check_hex_rule(integer, text) owner to opennms;