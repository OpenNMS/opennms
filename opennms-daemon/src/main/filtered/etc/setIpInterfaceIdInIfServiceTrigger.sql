--First step to refactoring ipInterface, snmpInterface tables to have actual IDs
--This trigger puts IpInterface IDs in the IfServices table

DROP TRIGGER setIpInterfaceIdInIfServiceTrigger ON ifServices;

CREATE OR REPLACE FUNCTION getIpInterfaceId() RETURNS trigger AS $IpInterfaceId$
BEGIN
  IF NEW.ipInterfaceId IS NULL THEN
     SELECT ipif.id INTO NEW.ipInterfaceId
       FROM ipinterface ipif
       WHERE (ipif.nodeid = NEW.nodeid AND ipif.ipAddr = NEW.ipAddr AND ipif.ifIndex = NEW.ifIndex);
       
       IF NOT FOUND THEN
          RAISE EXCEPTION 'Invalid Interface';
       END IF;
  END IF;
  RETURN NEW;
END;
$IpInterfaceId$ LANGUAGE 'plpgsql';

CREATE TRIGGER setIpInterfaceIdInIfServiceTrigger 
   BEFORE INSERT OR UPDATE
   ON ifServices FOR EACH ROW
   EXECUTE PROCEDURE getIpInterfaceId();
