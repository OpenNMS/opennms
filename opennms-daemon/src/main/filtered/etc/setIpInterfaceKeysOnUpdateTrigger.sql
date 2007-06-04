--First step to refactoring ipInterface, snmpInterface tables to have actual IDs
--This trigger puts IpInterface IDs in the IfServices table

DROP TRIGGER setIpInterfaceKeysOnUpdateTrigger ON ifServices;

-- Refactoring DB with IpInterface IDs

CREATE OR REPLACE FUNCTION setIpInterfaceKeysOnUpdate() RETURNS trigger AS '
BEGIN

  -- ifServices must have an IP address that is *not* 0.0.0.0
  IF NEW.ipAddr IS NOT NULL AND NEW.ipAddr = ''0.0.0.0''
  THEN
    RAISE EXCEPTION ''IfServices Trigger Exception, Condition 0: ipAddr of 0.0.0.0 is not allowed in ifServices table'';
  END IF;

  --
  -- (Used with Trigger Update with old style foreign key)
  -- This condition keeps ipinterfaceid inSync with the composite foreign key of nodeid, ipaddr, ifindex
  -- This usually happens when a record is being updated by old JDBC code (non-Hibernate DAOs) and has changed
  -- one or more of the composite key values, the ipInterfaceId needs to be updated
  --
  IF (NEW.ipInterfaceId = OLD.ipInterfaceId) AND (NEW.nodeId != OLD.nodeId OR NEW.ipAddr != OLD.ipAddr) 
  THEN
     SELECT ipif.id INTO NEW.ipInterfaceId
       FROM ipinterface ipif
       WHERE (ipif.nodeid = NEW.nodeid AND ipif.ipAddr = NEW.ipAddr AND ipif.ipAddr != ''0.0.0.0'');
       
     IF NOT FOUND THEN
        RAISE EXCEPTION ''IfServices Trigger Exception, Condition 3: No IpInterface found for... nodeid: %  ipaddr: % '', NEW.nodeid, NEW.ipAddr;
     END IF;
     
  --
  -- (Used with Trigger Update with new style foreign key)
  -- This condition keeps the composite foreign key of nodeid, ipaddr, ifindex inSync with the ipinterfaceid
  -- This usually happens with the Hibernate DAOs decide to change the ipinterfaceid represented
  -- by the ifservices.
  --
  ELSIF NEW.ipInterfaceId != OLD.ipInterfaceId
  THEN
     SELECT ipif.nodeId, ipif.ipAddr, ipif.ifIndex INTO NEW.nodeId, NEW.ipAddr, NEW.ifIndex
       FROM ipinterface ipif
      WHERE (ipif.id = NEW.ipInterfaceId);
      
      IF NOT FOUND THEN
         RAISE EXCEPTION ''IfServices Trigger Exception, Condition 4: No IpInterface found for ipInterfaceId: %'', NEW.ipInterfaceId;
      END IF;
      
      IF NEW.ipAddr = ''0.0.0.0''
      THEN
         RAISE EXCEPTION ''IfServices Trigger Exception, Condition 5: IpInterface found for ipInterfaceId: % has 0.0.0.0 ipAddr'', NEW.ipInterfaceId;
      END IF;
  END IF;

  RETURN NEW;
END;
' LANGUAGE 'plpgsql';

CREATE TRIGGER setIpInterfaceKeysOnUpdateTrigger
   BEFORE UPDATE
   ON ifServices FOR EACH ROW
   EXECUTE PROCEDURE setIpInterfaceKeysOnUpdate();
