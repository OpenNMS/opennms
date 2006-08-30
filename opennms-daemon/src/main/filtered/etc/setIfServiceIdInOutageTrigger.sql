--First step to refactoring ipInterface, snmpInterface tables to have actual IDs
--This trigger puts ifService IDs in the outage table

DROP TRIGGER setIfServiceIdInOutage ON outages;

CREATE OR REPLACE FUNCTION getIfServiceId() RETURNS trigger AS '
BEGIN
  IF NEW.ifServiceId IS NULL THEN
     SELECT ifsvc.id INTO NEW.ifserviceid
       FROM ifservices ifsvc
       WHERE (ifsvc.nodeid = NEW.nodeid AND ifsvc.ipAddr = NEW.ipAddr AND ifsvc.serviceid = NEW.serviceid);
       
       IF NOT FOUND THEN
          RAISE EXCEPTION \'Invalid Service.\';
       END IF;
  END IF;
  RETURN NEW;
END;
' LANGUAGE 'plpgsql';

CREATE TRIGGER setIfServiceIdInOutage
   BEFORE INSERT OR UPDATE
   ON outages FOR EACH ROW
   EXECUTE PROCEDURE getIfServiceId();
