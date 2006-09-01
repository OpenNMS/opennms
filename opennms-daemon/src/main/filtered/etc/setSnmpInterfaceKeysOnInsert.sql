--DB Refactoring Function to introduce SnmpInterface IDs

CREATE OR REPLACE FUNCTION setSnmpInterfaceKeysOnInsert() RETURNS trigger AS '
BEGIN

  --
  -- (Used for Trigger insert with old style foreign key)
  -- This condition keeps the snmpInterfaceId inSync with the composite foreign key of nodeid, ipaddr, ifindex
  -- This usually happens when a new record is written by our JDBC code (non-Hibernate DAO) for the old JDBC style
  -- code has no knowledge of the new keys
  --
  IF NEW.snmpInterfaceId IS NULL 
  THEN
     SELECT snmpif.id INTO NEW.snmpInterfaceId
       FROM snmpinterface snmpif
       WHERE (snmpif.nodeid = NEW.nodeid AND snmpif.ipAddr = NEW.ipAddr AND (snmpif.snmpIfIndex = NEW.ifIndex OR (snmpif.snmpIfIndex IS NULL AND NEW.ifIndex IS NULL)));
       
       IF NOT FOUND 
       THEN
          RAISE EXCEPTION ''IpInterface Trigger Exception, Condition 1: No SnmpInterface found for... nodeid: %  ipaddr: %  ifindex: %'', NEW.nodeid, NEW.ipAddr, NEW.ifIndex;
       ELSE
          RAISE NOTICE ''IpInterface Trigger Notice, Condition 1: SnmpInterface found for... nodeid: %  ipaddr: %  ifindex: %'', NEW.nodeid, NEW.ipAddr, NEW.ifIndex;
       END IF;
       
  --
  -- (Used for Insert with new style foreign key)
  -- This condition keeps the composite foreign key of nodeid, ipaddr, ifindex inSync with the SnmpInterfaceId
  -- This usually happens when a new record is written by our Hibernate DAOs... these DAOs have no knowledge of
  -- the composite key columns
  --

  ELSIF NEW.snmpInterfaceId IS NOT NULL AND (NEW.nodeId IS NULL OR NEW.ipAddr IS NULL OR NEW.ifIndex IS NULL)
  THEN
     SELECT snmpif.nodeid, snmpif.ipAddr, snmpif.ifIndex INTO NEW.nodeid, NEW.ipAddr, NEW.ifIndex
       FROM snmpinterface snmpif
      WHERE (snmpif.id = NEW.snmpInterfaceId);
      
      IF NOT FOUND
      THEN
         RAISE EXCEPTION ''IpInterface Trigger Exception: No SnmpInterface found for snmpInterfaceId: %'', NEW.snmpInterfaceId;
      ELSE
         RAISE EXCEPTION ''IpInterface Trigger Notice: SnmpInterface found for snmpInterfaceId: %'', NEW.snmpInterfaceId;
      END IF;
  END IF;
  RETURN NEW;
END;
' LANGUAGE 'plpgsql';
