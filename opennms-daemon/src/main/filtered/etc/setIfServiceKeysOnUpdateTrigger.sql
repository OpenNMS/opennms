--First step to refactoring ipInterface, snmpInterface tables to have actual IDs
--This trigger puts ifService IDs in the outage table

DROP TRIGGER setIfServiceKeysOnUpdateTrigger ON outages;

--DB Refactoring function to introduce IfService IDs

CREATE OR REPLACE FUNCTION setIfServiceKeysOnUpdate() RETURNS trigger AS '
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
      END IF;
  END IF;

  RETURN NEW;
END;
' LANGUAGE 'plpgsql';

CREATE TRIGGER setIfServiceKeysOnUpdateTrigger
   BEFORE UPDATE
   ON outages FOR EACH ROW
   EXECUTE PROCEDURE setIfServiceKeysOnUpdate();
