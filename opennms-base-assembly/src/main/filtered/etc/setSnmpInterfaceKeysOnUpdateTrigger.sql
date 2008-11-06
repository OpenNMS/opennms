--First step to refactoring ipInterface, snmpInterface tables to have actual IDs
--This trigger puts IpInterface IDs in the IfServices table

DROP TRIGGER setSnmpInterfaceKeysOnUpdateTrigger ON ipInterface;

--DB Refactoring function to introduce SnmpInterface IDs

CREATE OR REPLACE FUNCTION setSnmpInterfaceKeysOnUpdate() RETURNS trigger AS '
BEGIN

  --
  -- (Used for Trigger update with old style foreign key)
  -- This condition keeps snmpinterfaceid inSync with the composite foreign key of nodeid, ipaddr, ifindex
  -- This usually happens when a record is being updated by old JDBC code (non-Hibernate DAOs) and has changed
  -- one or more of the composite key values, the snmpInterfaceId needs to be updated
  --
  IF ((NEW.snmpInterfaceId = OLD.snmpInterfaceId OR (NEW.snmpInterfaceId IS NULL AND OLD.snmpInterfaceId IS NULL)) AND 
      (NEW.nodeId != OLD.nodeId OR NEW.ifIndex != OLD.ifIndex OR (NEW.ifIndex IS NULL AND OLD.ifIndex IS NOT NULL) OR (NEW.ifIndex IS NOT NULL AND OLD.ifIndex IS NULL)))
  THEN
    IF NEW.ifIndex IS NULL AND NEW.snmpInterfaceId IS NOT NULL
    THEN
       SELECT NULL INTO NEW.snmpInterfaceId;
    ELSIF NEW.ifIndex IS NOT NULL
    THEN
     SELECT snmpif.id INTO NEW.snmpInterfaceId
       FROM snmpinterface snmpif
       WHERE (snmpif.nodeid = NEW.nodeid AND snmpif.snmpIfIndex = NEW.ifIndex);
       
     IF NOT FOUND THEN
       RAISE EXCEPTION ''IpInterface Trigger Notice, Condition 3: No SnmpInterface found for... nodeid: % ifindex: %'', NEW.nodeid, NEW.ifIndex;
     END IF;
    END IF;
     
  --
  -- (Used for Trigger update with new style foreign key)
  -- This condition keeps the composite foreign key of nodeid, ipaddr, ifindex inSync with the snmpinterfaceid
  -- This usually happens with the Hibernate DAOs decide to change the snmpinterfaceid represented
  -- by the ipinterface.
  --
  -- We don't match on the case where NEW.snmpInterfaceId IS NULL, because we use it in the WHERE clause.
  --
  ELSIF (NEW.snmpInterfaceId != OLD.snmpInterfaceId OR (NEW.snmpInterfaceId IS NOT NULL AND OLD.snmpInterfaceId IS NULL))
  THEN
     SELECT snmpif.nodeId, snmpif.snmpIfIndex INTO NEW.nodeId, NEW.ifIndex
       FROM snmpinterface snmpif
      WHERE (snmpif.id = NEW.snmpInterfaceId);
      
      IF NOT FOUND THEN
         RAISE EXCEPTION ''IpInterface Trigger Notice, Condition 4: No SnmpInterface found for snmpInterfaceId: %'', NEW.snmpInterfaceId;
      END IF;
  END IF;

  RETURN NEW;
END;
' LANGUAGE 'plpgsql';

CREATE TRIGGER setSnmpInterfaceKeysOnUpdateTrigger
   BEFORE UPDATE
   ON ipInterface FOR EACH ROW
   EXECUTE PROCEDURE setSnmpInterfaceKeysOnUpdate();