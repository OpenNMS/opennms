--DB Refactoring function to introduce SnmpInterface IDs

CREATE OR REPLACE FUNCTION setSnmpInterfaceKeysOnUpdate() RETURNS trigger AS '
BEGIN

  --
  -- (Used for Trigger update with old style foreign key)
  -- This condition keeps snmpinterfaceid inSync with the composite foreign key of nodeid, ipaddr, ifindex
  -- This usually happens when a record is being updated by old JDBC code (non-Hibernate DAOs) and has changed
  -- one or more of the composite key values, the snmpInterfaceId needs to be updated
  --
  IF (NEW.snmpInterfaceId = OLD.snmpInterfaceId) AND (NEW.nodeId != OLD.nodeId OR NEW.ipAddr != OLD.ipAddr OR NEW.ifIndex != OLD.ifIndex) 
  THEN
     SELECT snmpif.id INTO NEW.snmpInterfaceId
       FROM snmpinterface snmpif
       WHERE (snmpif.nodeid = NEW.nodeid AND snmpif.ipAddr = NEW.ipAddr AND snmpif.ifIndex = NEW.ifIndex);
       
     IF NOT FOUND THEN
        RAISE NOTICE ''IpInterface Trigger Notice, Condition 3: No SnmpInterface found for... nodeid: %  ipaddr: %  ifindex: %'', NEW.nodeid, NEW.ipAddr, NEW.ifIndex;
     ELSE
        RAISE NOTICE ''IpInterface Trigger Notice, Condition 3: SnmpInterface found for... nodeid: %  ipaddr: %  serviceid: %'', NEW.nodeid, NEW.ipAddr, NEW.ifIndex;
     END IF;
     
  --
  -- (Used for Trigger update with new style foreign key)
  -- This condition keeps the composite foreign key of nodeid, ipaddr, ifindex inSync with the snmpinterfaceid
  -- This usually happens with the Hibernate DAOs decide to change the snmpinterfaceid represented
  -- by the ipinterface.
  --
  ELSIF NEW.ipInterfaceId != OLD.ipInterfaceId
  THEN
     SELECT snmpif.nodeId, snmpif.ipAddr, snmpif.ifIndex INTO NEW.nodeId, NEW.ipAddr, NEW.ifIndex
       FROM snmpinterface snmpif
      WHERE (snmpif.id = NEW.snmpInterfaceId);
      
      IF NOT FOUND THEN
         RAISE NOTICE ''IpInterface Trigger Notice, Condition 4: No SnmpInterface found for snmpInterfaceId: %'', NEW.snmpInterfaceId;
      ELSE
         RAISE NOTICE ''IpInterface Trigger Notice, Condition 4: SnmpInterface found for snmpInterfaceId: %'', NEW.snmpInterfaceId;
      END IF;
  END IF;

  RETURN NEW;
END;
' LANGUAGE 'plpgsql';
