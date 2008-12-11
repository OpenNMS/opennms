create or replace function iplike(text, text) returns boolean as '
  declare
	i_ipaddress	alias for $1;
	i_rule		alias for $2;
	c_i1	integer;
	c_i2	integer;
	c_i3	integer;
	c_i4	integer;
	c_r1	text;
	c_r2	text;
	c_r3	text;
	c_r4	text;

	c_work text;

  begin
	if i_ipaddress is NULL or i_ipaddress is null then
		return ''f'';
	end if;

	if i_rule = ''*.*.*.*'' then
		return ''t'';
	end if;

	-- First, strip apart the IP address into octets, and
	-- verify that they are legitimate (0-255)
	if i_ipaddress ~ ''^[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+$'' then
		c_work := i_ipaddress;

		c_i1 := to_number(substr(c_work, 0, strpos(c_work, ''.'')), ''999'');
		if c_i1 > 255 then
			return ''f'';
		end if;
		c_work := ltrim(ltrim(c_work, ''0123456789''), ''.'');

		c_i2 := to_number(substr(c_work, 0, strpos(c_work, ''.'')), ''999'');
		if c_i2 > 255 then
			return ''f'';
		end if;
		c_work := ltrim(ltrim(c_work, ''0123456789''), ''.'');

		c_i3 := to_number(substr(c_work, 0, strpos(c_work, ''.'')), ''999'');
		if c_i3 > 255 then
			return ''f'';
		end if;
		c_work := ltrim(ltrim(c_work, ''0123456789''), ''.'');

		c_i4 := to_number(c_work, ''999'');
		if c_i4 > 255 then
			return ''f'';
		end if;
	else
		return ''f'';   -- IP address was not even nearly legit
	end if;

	-- Split apart the rule set
	if i_rule ~ ''^[0-9*,-]+\.[0-9*,-]+\.[0-9*,-]+\.[0-9*,-]+$'' then
		c_work := i_rule;
		c_r1 := substr(c_work, 0, strpos(c_work, ''.''));

		if not check_rule(c_i1, c_r1) then
			return ''f'';
		end if;

		c_work := ltrim(ltrim(c_work, ''0123456789,-*''), ''.'');

		c_r2 := substr(c_work, 0, strpos(c_work, ''.''));
	
		if not check_rule(c_i2, c_r2) then
			return ''f'';
		end if;

		c_work := ltrim(ltrim(c_work, ''0123456789,-*''), ''.'');
		c_r3 := substr(c_work, 0, strpos(c_work, ''.''));
	
		if not check_rule(c_i3, c_r3) then
			return ''f'';
		end if;

		c_work := ltrim(ltrim(c_work, ''0123456789,-*''), ''.'');
		c_r4 := c_work;

		if not check_rule(c_i4, c_r4) then
			return ''f'';
		end if;
	else
		return ''f'';
	end if;	

  return ''t'';
end;' language plpgsql;

create or replace function check_range (integer, text) returns boolean as '
declare
	i_octet	alias for $1;
	i_rule	alias for $2;
	c_r1 integer;
	c_r2 integer;
begin

	c_r1 := to_number(split_part(i_rule, ''-'', 1), ''999'');
	c_r2 := to_number(split_part(i_rule, ''-'', 2), ''999'');
	if i_octet between c_r1 and c_r2 then
		return ''t'';
	end if;
	return ''f'';
end;' language plpgsql;

create or replace function check_rule (integer, text) returns boolean as '
declare
	i_octet	alias for $1;
	i_rule	alias for $2;
	c_element text;
	c_work	text;

begin
	if i_rule = ''*'' then   -- * matches anything!
		return ''t'';
	end if;

	c_work := i_rule;
	while c_work <> '''' loop
		raise notice ''c_work = %'',c_work;
		if c_work ~ '','' then
			c_element := substr(c_work, 0, strpos(c_work, '',''));
			c_work := substr(c_work, strpos(c_work, '','')+1);
		else
			c_element := c_work;
			c_work := '''';
		end if;

		if c_element ~ ''-'' then
			if check_range(i_octet, c_element) then
				return ''t'';
			end if;
		else
			if i_octet = to_number(c_element, ''999'') then
				return ''t'';
			end if;
		end if;
	end loop;
	return ''f'';
end;' language plpgsql;
