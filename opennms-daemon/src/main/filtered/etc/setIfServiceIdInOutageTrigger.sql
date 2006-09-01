--First step to refactoring ipInterface, snmpInterface tables to have actual IDs
--This trigger puts ifService IDs in the outage table

DROP TRIGGER setIfServiceIdOnInsert ON outages;
DROP TRIGGER setIfServiceIdOnUpdate ON outages;

CREATE OR REPLACE FUNCTION syncIpIntefaceKeysOnInsert() RETURNS trigger AS '
BEGIN

  --
  -- (Insert with old style foreign key)
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
     ELSE
        RAISE NOTICE ''Outages Trigger Success, Condition 1: Service found for... nodeid: %  ipaddr: %  serviceid: %'', NEW.nodeid, NEW.ipAddr, NEW.serviceid;
     END IF;
  
  --
  -- (Insert with new style foreign key)
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
      ELSE
         RAISE NOTICE ''Outages Trigger Success, Condition 2: Service found for serviceID: %'', NEW.ifServiceId;
      END IF;

  END IF;
  
  RETURN NEW;
END;
' LANGUAGE 'plpgsql';



CREATE OR REPLACE FUNCTION syncIpIntefaceKeysOnUpdate() RETURNS trigger AS '
BEGIN

  --
  -- (Update with old style foreign key)
  -- This condition keeps ifserviceid inSync with the composite foreign key of nodeid, ipaddr, serviceid
  -- This usually happens when a record is being updated by old JDBC code (non-Hibernate DAOs) and has changed
  -- one or more of the composite key values, the ifServiceId needs to be updated
  --
  IF (NEW.ifserviceID = OLD.ifServiceId) AND (NEW.nodeId != OLD.nodeId OR NEW.ipAddr != OLD.ipAddr OR NEW.serviceId != OLD.serviceID) 
  THEN
     SELECT ifsvc.id INTO NEW.ifserviceid
       FROM ifservices ifsvc
       WHERE (ifsvc.nodeid = NEW.nodeid AND ifsvc.ipAddr = NEW.ipAddr AND ifsvc.serviceid = NEW.serviceid);
       
     IF NOT FOUND THEN
        RAISE EXCEPTION ''Outages Trigger Exception, Condition 3: No service found for... nodeid: %  ipaddr: %  serviceid: %'', NEW.nodeid, NEW.ipAddr, NEW.serviceid;
     ELSE
        RAISE NOTICE ''Outages Trigger Success, Condition 3: Service found for... nodeid: %  ipaddr: %  serviceid: %'', NEW.nodeid, NEW.ipAddr, NEW.serviceid;
     END IF;
  --
  -- (Update with new style foreign key)
  -- This condition keeps the composite foreign key of nodeid, ipaddr, serviceid inSync with the ifserviceid
  -- This usually happens with the Hibernate DAOs decide to change the ifserviceid (MonitoredService) represented
  -- by the outage.
  --
  ELSIF NEW.ifServiceId != OLD.ifServiceId
  THEN
     SELECT ifsvc.nodeId, ifsvc.ipAddr, ifsvc.serviceId INTO NEW.nodeId, NEW.ipAddr, NEW.serviceId
       FROM ifservices ifsvc
      WHERE (ifsvc.id = NEW.ifServiceId);
      
      IF NOT FOUND THEN
         RAISE EXCEPTION ''Outages Trigger Exception, Condition 4: No service found for serviceID: %'', NEW.ifServiceId;
      ELSE
         RAISE NOTICE ''Outages Trigger Success, Condition 4: Service found for serviceID: %'', NEW.ifServiceId;
      END IF;
  END IF;

  RETURN NEW;
END;
' LANGUAGE 'plpgsql';


CREATE TRIGGER setIfServiceIdOnInsert
   BEFORE INSERT
   ON outages FOR EACH ROW
   EXECUTE PROCEDURE syncIpIntefaceKeysOnInsert();

CREATE TRIGGER setIfServiceIdOnUpdate
   BEFORE UPDATE
   ON outages FOR EACH ROW
   EXECUTE PROCEDURE syncIpIntefaceKeysOnUpdate();
