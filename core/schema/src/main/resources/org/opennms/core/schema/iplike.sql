--
-- Licensed to The OpenNMS Group, Inc (TOG) under one or more
-- contributor license agreements.  See the LICENSE.md file
-- distributed with this work for additional information
-- regarding copyright ownership.
--
-- TOG licenses this file to You under the GNU Affero General
-- Public License Version 3 (the "License") or (at your option)
-- any later version.  You may not use this file except in
-- compliance with the License.  You may obtain a copy of the
-- License at:
--
--      https://www.gnu.org/licenses/agpl-3.0.txt
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
-- either express or implied.  See the License for the specific
-- language governing permissions and limitations under the
-- License.
--

-- PL/pgSQL implementation of iplike, revision 2 (see the COMMENT ON at the
-- end; the Migrator uses it to upgrade older PL/pgSQL revisions in place).
--
-- Profiling-driven changes relative to revision 1, semantics unchanged
-- (verified against the iplike reference corpus by IPLikeCoverageIT):
--   * value classification via family(::inet) instead of regex matching
--     (the zone id is stripped with split_part first: ::inet cannot parse
--     it, and an 8-field LIKE guard keeps compressed IPv6 out of the
--     field-parsing loop, which assumes exactly 8 groups)
--   * to_number(x, '999') replaced by ::integer casts
--   * regex operators in check_rule replaced by strpos()
--   * parsing wrapped in a nested block whose EXCEPTION handler turns any
--     cast/parse error into 'f' (this is what tolerates garbage input); the
--     null check and the match-all fast paths stay OUTSIDE that block so the
--     most common calls never pay the handler's subtransaction setup

create or replace function iplike(i_ipaddress text, i_rule text) returns boolean as $$
  declare
    c_i integer;
    c_r text;

    c_addrwork text;
    c_addrtemp text;
    c_rulework text;
    c_scopeid text;
    c_rulescope text;

    i integer;

  begin
    if i_ipaddress is null then
        return 'f';
    end if;

    if i_rule = '*.*.*.*' or i_rule = '*:*:*:*:*:*:*:*' then
        return 't';
    end if;

    begin
        -- IPv4
        if family(split_part(i_ipaddress, '%', 1)::inet) = 4 and i_rule ~ E'^[0-9*,-]+\\.[0-9*,-]+\\.[0-9*,-]+\\.[0-9*,-]+$' then
            c_addrwork := i_ipaddress;
            c_rulework := i_rule;

            i := 0;
            while i < 4 loop
                if (strpos(c_addrwork, '.') > 0) then
                    c_i := (substr(c_addrwork, 0, strpos(c_addrwork, '.')))::integer;
                else
                    c_i := c_addrwork::integer;
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
        -- IPv6 (the LIKE guard requires the 8 fully-expanded groups this
        -- parsing loop assumes, as the previous regex did)
        elsif i_ipaddress like '%:%:%:%:%:%:%:%'
              and family(split_part(i_ipaddress, '%', 1)::inet) = 6
              and i_rule ~ E'^[0-9a-f*,-]+:[0-9a-f*,-]+:[0-9a-f*,-]+:[0-9a-f*,-]+:[0-9a-f*,-]+:[0-9a-f*,-]+:[0-9a-f*,-]+:[0-9a-f*,-]+(%.+)?$' then
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
    exception when others then
        return 'f';
    end;
end;
$$ language plpgsql;

create or replace function check_range (i_octet integer, i_rule text) returns boolean as $$
declare
    c_r1 integer;
    c_r2 integer;
begin
    c_r1 := split_part(i_rule, '-', 1)::integer;
    c_r2 := split_part(i_rule, '-', 2)::integer;
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
    c_work    text;
begin
    if i_rule = '*' then   -- * matches anything!
        return 't';
    end if;

    c_work := i_rule;
    while c_work <> '' loop
        if (strpos(c_work, ',') > 0) then
            c_element := substr(c_work, 0, strpos(c_work, ','));
            c_work := substr(c_work, strpos(c_work, ',')+1);
        else
            c_element := c_work;
            c_work := '';
        end if;

        if (strpos(c_element, '-') > 0) then
            if check_range(i_octet, c_element) then
                return 't';
            end if;
        else
            if c_element = '*' then
                return 't';
            elsif i_octet = c_element::integer then
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
        if (strpos(c_work, ',') > 0) then
            c_element := substr(c_work, 0, strpos(c_work, ','));
            c_work := substr(c_work, strpos(c_work, ',')+1);
        else
            c_element := c_work;
            c_work := '';
        end if;

        if (strpos(c_element, '-') > 0) then
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

comment on function iplike(text, text) is 'opennms-iplike-plpgsql-2';
