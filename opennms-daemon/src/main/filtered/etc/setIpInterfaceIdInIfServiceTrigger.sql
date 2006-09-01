--First step to refactoring ipInterface, snmpInterface tables to have actual IDs
--This trigger puts IpInterface IDs in the IfServices table

DROP TRIGGER setIpInterfaceIdOnInsert ON ifServices;
DROP TRIGGER setIpInterfaceIdOnUpdate ON ifServices;

CREATE OR REPLACE FUNCTION syncIfServiceKeysOnInsert() RETURNS trigger AS '
BEGIN

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
       WHERE (ipif.nodeid = NEW.nodeid AND ipif.ipAddr = NEW.ipAddr AND (ipif.ifIndex = NEW.ifIndex OR (ipif.ifIndex IS NULL AND NEW.ifIndex IS NULL)));
       
       IF NOT FOUND 
       THEN
          RAISE EXCEPTION ''IfServices Trigger Exception, Condition 1: No IpInterface found for... nodeid: %  ipaddr: %  ifindex: %'', NEW.nodeid, NEW.ipAddr, NEW.ifIndex;
       ELSE
          RAISE NOTICE ''IfServices Trigger Success, Condition 1: IpInterface found for... nodeid: %  ipaddr: %  ifindex: %'', NEW.nodeid, NEW.ipAddr, NEW.ifIndex;
       END IF;
       
  --
  -- (Insert with new style foreign key)
  -- This condition keeps the composite foreign key of nodeid, ipaddr, ifindex inSync with the ipInterfaceId
  -- This usually happens when a new record is written by our Hibernate DAOs... these DAOs have no knowledge of
  -- the composite key columns
  --

  ELSIF NEW.ipInterfaceId IS NOT NULL AND (NEW.nodeId IS NULL OR NEW.ipAddr IS NULL OR NEW.ifIndex IS NULL)
  THEN
     SELECT ipif.nodeid, ipif.ipAddr, ipif.ifIndex INTO NEW.nodeid, NEW.ipAddr, NEW.ifIndex
       FROM ipinterface ipif
      WHERE (ipif.id = NEW.ipInterfaceId);
      
      IF NOT FOUND
      THEN
         RAISE EXCEPTION ''IfServices Trigger Exception: No ipinterface found for ipInterfaceId: %'', NEW.ipInterfaceId;
      ELSE
         RAISE EXCEPTION ''IfServices Trigger Notice: IpInterface found for ipInterfaceId: %'', NEW.ipInterfaceId;
      END IF;
  END IF;
  RETURN NEW;
END;
' LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION syncIfServiceKeysOnUpdate() RETURNS trigger AS '
BEGIN

  --
  -- (Update with old style foreign key)
  -- This condition keeps ipinterfaceid inSync with the composite foreign key of nodeid, ipaddr, ifindex
  -- This usually happens when a record is being updated by old JDBC code (non-Hibernate DAOs) and has changed
  -- one or more of the composite key values, the ipInterfaceId needs to be updated
  --
  IF (NEW.ipInterfaceId = OLD.ipInterfaceId) AND (NEW.nodeId != OLD.nodeId OR NEW.ipAddr != OLD.ipAddr OR NEW.ifIndex != OLD.ifIndex) 
  THEN
     SELECT ipif.id INTO NEW.ipInterfaceId
       FROM ipinterface ifsvc
       WHERE (ipif.nodeid = NEW.nodeid AND ipif.ipAddr = NEW.ipAddr AND ipif.ifIndex = NEW.ifIndex);
       
     IF NOT FOUND THEN
        RAISE EXCEPTION ''IfServices Trigger Exception, Condition 3: No IpInterface found for... nodeid: %  ipaddr: %  ifindex: %'', NEW.nodeid, NEW.ipAddr, NEW.ifIndex;
     ELSE
        RAISE NOTICE ''IfServices Trigger Success, Condition 3: IpInterface found for... nodeid: %  ipaddr: %  serviceid: %'', NEW.nodeid, NEW.ipAddr, NEW.ifIndex;
     END IF;
     
  --
  -- (Update with new style foreign key)
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
      ELSE
         RAISE NOTICE ''IfServices Trigger Success, Condition 4: IpInterface found for ipIntefaceId: %'', NEW.ipInterfaceId;
      END IF;
  END IF;

  RETURN NEW;
END;
' LANGUAGE 'plpgsql';


CREATE TRIGGER setIpInterfaceIdOnInsert
   BEFORE INSERT
   ON ifServices FOR EACH ROW
   EXECUTE PROCEDURE syncIfServiceKeysOnInsert();

CREATE TRIGGER setIpInterfaceIdOnUpdate
   BEFORE UPDATE
   ON ifServices FOR EACH ROW
   EXECUTE PROCEDURE syncIfServiceKeysOnUpdate();