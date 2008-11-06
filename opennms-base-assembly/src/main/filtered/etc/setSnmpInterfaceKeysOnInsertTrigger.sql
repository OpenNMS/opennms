--First step to refactoring ipInterface, snmpInterface tables to have actual IDs
--This trigger puts IpInterface IDs in the IfServices table

DROP TRIGGER setSnmpInterfaceKeysOnInsertTrigger ON ipInterface;

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
     IF NEW.ifIndex IS NOT NULL
     THEN
       SELECT snmpif.id INTO NEW.snmpInterfaceId
         FROM snmpinterface snmpif
         WHERE (snmpif.nodeid = NEW.nodeid AND snmpif.snmpIfIndex = NEW.ifIndex);
       
       IF NOT FOUND 
       THEN
         RAISE EXCEPTION ''IpInterface Trigger Notice, Condition 1: No SnmpInterface found for... nodeid: % ifindex: %'', NEW.nodeid, NEW.ifIndex;
       END IF;
     END IF;
       
  --
  -- (Used for Insert with new style foreign key)
  -- This condition keeps the composite foreign key of nodeid, ipaddr, ifindex inSync with the SnmpInterfaceId
  -- This usually happens when a new record is written by our Hibernate DAOs... these DAOs have no knowledge of
  -- the composite key columns
  --

  ELSIF NEW.snmpInterfaceId IS NOT NULL AND (NEW.nodeId IS NULL OR NEW.ifIndex IS NULL)
  THEN
     SELECT snmpif.nodeid, snmpif.snmpIfIndex INTO NEW.nodeid, NEW.ifIndex
       FROM snmpinterface snmpif
      WHERE (snmpif.id = NEW.snmpInterfaceId);
      
      IF NOT FOUND
      THEN
         RAISE EXCEPTION ''IpInterface Trigger Notice: No SnmpInterface found for snmpInterfaceId: %'', NEW.snmpInterfaceId;
      END IF;
  END IF;
  RETURN NEW;
END;
' LANGUAGE 'plpgsql';

CREATE TRIGGER setSnmpInterfaceKeysOnInsertTrigger
   BEFORE INSERT
   ON ipInterface FOR EACH ROW
   EXECUTE PROCEDURE setSnmpInterfaceKeysOnInsert();