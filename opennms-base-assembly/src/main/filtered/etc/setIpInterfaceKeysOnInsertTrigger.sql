--First step to refactoring ipInterface, snmpInterface tables to have actual IDs
--This trigger puts IpInterface IDs in the IfServices table

DROP TRIGGER setIpInterfaceKeysOnInsertTrigger ON ifServices;

-- Refactoring DB with IpInterface IDs

CREATE OR REPLACE FUNCTION setIpInterfaceKeysOnInsert() RETURNS trigger AS '
BEGIN

  -- ifServices must have an IP address that is *not* 0.0.0.0
  IF NEW.ipAddr IS NOT NULL AND NEW.ipAddr = ''0.0.0.0''
  THEN
    RAISE EXCEPTION ''IfServices Trigger Exception, Condition 0: ipAddr of 0.0.0.0 is not allowed in ifServices table'';
  END IF;
  
  --
  -- (Insert with old style foreign key)
  -- This condition keeps the ipInterfaceId inSync with the composite foreign key of nodeid, ipaddr, ifindex
  -- This usually happens when a new record is written by our JDBC code (non-Hibernate DAO) for the old JDBC style
  -- code has no knowledge of the new keys
  --
  IF NEW.ipInterfaceId IS NULL 
  THEN
     SELECT ipif.id INTO NEW.ipInterfaceId
       FROM ipinterface ipif
       WHERE (ipif.nodeid = NEW.nodeid AND ipif.ipAddr = NEW.ipAddr AND ipif.ipAddr != ''0.0.0.0'');
       
       IF NOT FOUND 
       THEN
          RAISE EXCEPTION ''IfServices Trigger Exception, Condition 1: No IpInterface found for... nodeid: %  ipaddr: %'', NEW.nodeid, NEW.ipAddr;
       END IF;
       
  --
  -- (Insert with new style foreign key)
  -- This condition keeps the composite foreign key of nodeid, ipaddr, ifindex inSync with the ipInterfaceId
  -- This usually happens when a new record is written by our Hibernate DAOs... these DAOs have no knowledge of
  -- the composite key columns
  --

  ELSIF NEW.ipInterfaceId IS NOT NULL AND (NEW.nodeId IS NULL OR NEW.ipAddr IS NULL)
  THEN
     SELECT ipif.nodeid, ipif.ipAddr, ipif.ifIndex INTO NEW.nodeid, NEW.ipAddr, NEW.ifIndex
       FROM ipinterface ipif
      WHERE (ipif.id = NEW.ipInterfaceId);
      
      IF NOT FOUND
      THEN
         RAISE EXCEPTION ''IfServices Trigger Exception: No ipinterface found for ipInterfaceId: %'', NEW.ipInterfaceId;
      END IF;
      
      IF NEW.ipAddr = ''0.0.0.0''
      THEN
         RAISE EXCEPTION ''IfServices Trigger Exception, Condition 5: IpInterface found for ipInterfaceId: % has 0.0.0.0 ipAddr'', NEW.ipInterfaceId;
      END IF;
  END IF;
  RETURN NEW;
END;
' LANGUAGE 'plpgsql';

CREATE TRIGGER setIpInterfaceKeysOnInsertTrigger
   BEFORE INSERT
   ON ifServices FOR EACH ROW
   EXECUTE PROCEDURE setIpInterfaceKeysOnInsert();
