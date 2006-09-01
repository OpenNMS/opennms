--First step to refactoring ipInterface, snmpInterface tables to have actual IDs
--This trigger puts IpInterface IDs in the IfServices table

DROP TRIGGER setSnmpInterfaceIdOnInsert ON ipInterface;
DROP TRIGGER setSnmpInterfaceIdOnUpdate ON ipInterface;

CREATE OR REPLACE FUNCTION syncIpInterfaceKeysOnInsert() RETURNS trigger AS '
BEGIN

  --
  -- (Insert with old style foreign key)
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
  -- (Insert with new style foreign key)
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

CREATE OR REPLACE FUNCTION syncIpInterfaceKeysOnUpdate() RETURNS trigger AS '
BEGIN

  --
  -- (Update with old style foreign key)
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
        RAISE EXCEPTION ''IpInterface Trigger Exception, Condition 3: No SnmpInterface found for... nodeid: %  ipaddr: %  ifindex: %'', NEW.nodeid, NEW.ipAddr, NEW.ifIndex;
     ELSE
        RAISE NOTICE ''IpInterface Trigger Notice, Condition 3: SnmpInterface found for... nodeid: %  ipaddr: %  serviceid: %'', NEW.nodeid, NEW.ipAddr, NEW.ifIndex;
     END IF;
     
  --
  -- (Update with new style foreign key)
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
         RAISE EXCEPTION ''IpInterface Trigger Exception, Condition 4: No SnmpInterface found for snmpInterfaceId: %'', NEW.snmpInterfaceId;
      ELSE
         RAISE NOTICE ''IpInterface Trigger Notice, Condition 4: SnmpInterface found for snmpInterfaceId: %'', NEW.snmpInterfaceId;
      END IF;
  END IF;

  RETURN NEW;
END;
' LANGUAGE 'plpgsql';


CREATE TRIGGER setSnmpInterfaceIdOnInsert
   BEFORE INSERT
   ON ipInterface FOR EACH ROW
   EXECUTE PROCEDURE syncIpInterfaceKeysOnInsert();

CREATE TRIGGER setSnmpInterfaceIdOnUpdate
   BEFORE UPDATE
   ON ipInterface FOR EACH ROW
   EXECUTE PROCEDURE syncIpInterfaceKeysOnUpdate();