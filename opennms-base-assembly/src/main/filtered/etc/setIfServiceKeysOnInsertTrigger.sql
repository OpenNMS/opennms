--First step to refactoring ipInterface, snmpInterface tables to have actual IDs
--This trigger puts ifService IDs in the outage table

DROP TRIGGER setIfServiceKeysOnInsertTrigger ON outages;

-- DB refactoring function to introduce IfService IDs

CREATE OR REPLACE FUNCTION setIfServiceKeysOnInsert() RETURNS trigger AS '
BEGIN

  --
  -- (Used with Trigger Insert with old style foreign key)
  -- This condition keeps the ifServiceID inSync with the composite foreign key of nodeid, ipaddr, serviceid
  -- This usually happens when a new record is written by our JDBC code (non-Hibernate DAO) for the old JDBC style
  -- code has no knowledge of the new keys
  --
  IF NEW.ifServiceId IS NULL 
  THEN
     SELECT ifsvc.id INTO NEW.ifserviceid
       FROM ifservices ifsvc
       WHERE (ifsvc.nodeid = NEW.nodeid AND ifsvc.ipAddr = NEW.ipAddr AND ifsvc.serviceid = NEW.serviceid);
       
     IF NOT FOUND 
     THEN
        RAISE EXCEPTION ''Outages Trigger Exception, Condition 1: No service found for... nodeid: %  ipaddr: %  serviceid: %'', NEW.nodeid, NEW.ipAddr, NEW.serviceid;
     END IF;
  
  --
  -- (Used with Trigger Insert with new style foreign key)
  -- This condition keeps the composite foreign key of nodeid, ipaddr, serviceid inSync with the ifserviceid
  -- This usually happens when a new record is written by our Hibernate DAOs... these DAOs have no knowledge of
  -- the composite key columns
  --
  ELSIF NEW.ifServiceId IS NOT NULL AND (NEW.nodeId IS NULL OR NEW.ipAddr IS NULL OR NEW.serviceId IS NULL)
  THEN
     SELECT ifsvc.nodeId, ifsvc.ipAddr, ifsvc.serviceId INTO NEW.nodeId, NEW.ipAddr, NEW.serviceId
       FROM ifservices ifsvc
      WHERE (ifsvc.id = NEW.ifServiceId);
      
      IF NOT FOUND THEN
         RAISE EXCEPTION ''Outages Trigger Exception, Condition 2: No service found for serviceID: %'', NEW.ifServiceId;
      END IF;

  END IF;
  
  RETURN NEW;
END;
' LANGUAGE 'plpgsql';


CREATE TRIGGER setIfServiceKeysOnInsertTrigger
   BEFORE INSERT
   ON outages FOR EACH ROW
   EXECUTE PROCEDURE setIfServiceKeysOnInsert();
