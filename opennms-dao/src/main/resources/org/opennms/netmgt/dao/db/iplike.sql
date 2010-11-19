create or replace function iplike(i_ipaddress text, i_rule text) returns boolean as $$
  declare
    c_i integer;
    c_r text;

    c_addrwork text;
    c_addrtemp text;
    c_rulework text;
    c_ruletemp text;
    c_scopeid text;

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

            if not check_rule(c_i, c_r) then
                return 'f';
            end if;

            c_rulework := ltrim(ltrim(c_rulework, '0123456789,-*'), '.');

            i := i + 1;
        end loop;
    -- IPv6
    elsif i_ipaddress ~ E'^[0-9a-f]+:[0-9a-f]+:[0-9a-f]+:[0-9a-f]+:[0-9a-f]+:[0-9a-f]+:[0-9a-f]+:[0-9a-f]+(%[0-9]+)?$' and i_rule ~ E'^[0-9a-f*,-]+:[0-9a-f*,-]+:[0-9a-f*,-]+:[0-9a-f*,-]+:[0-9a-f*,-]+:[0-9a-f*,-]+:[0-9a-f*,-]+:[0-9a-f*,-]+(%[0-9*,-]+)?$' then
        c_addrwork := i_ipaddress;
        c_rulework := i_rule;

        -- TODO Add support for scope identifiers

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
            else
                c_r := c_rulework;
            end if;

            if not check_hex_rule(c_i, c_r) then
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
$$ language plpgsql;

create or replace function check_range (i_octet integer, i_rule text) returns boolean as $$
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
$$ language plpgsql;

create or replace function check_hex_range (i_octet integer, i_rule text) returns boolean as $$
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
$$ language plpgsql;

create or replace function check_rule (i_octet integer, i_rule text) returns boolean as $$
declare
    c_element text;
    c_work	text;
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
            if i_octet = to_number(c_element, '99999') then
                return 't';
            end if;
        end if;
    end loop;
    return 'f';
end;
$$ language plpgsql;

create or replace function check_hex_rule (i_octet integer, i_rule text) returns boolean as $$
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
$$ language plpgsql;
