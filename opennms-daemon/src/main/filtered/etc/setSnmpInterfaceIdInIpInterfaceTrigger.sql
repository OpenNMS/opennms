--First step to refactoring ipInterface, snmpInterface tables to have actual IDs
--This trigger puts IpInterface IDs in the IfServices table

DROP TRIGGER setSnmpInterfaceIdInIpInterfaceTrigger ON ipInterface;

CREATE OR REPLACE FUNCTION getSnmpInterfaceId() RETURNS trigger AS $SnmpIpInterfaceId$
BEGIN
  IF NEW.snmpInterfaceId IS NULL AND NEW.ifIndex IS NOT NULL THEN
     SELECT snmpif.id INTO NEW.snmpInterfaceId
       FROM snmpinterface snmpif
       WHERE (snmpif.nodeid = NEW.nodeid AND snmpif.ipAddr = NEW.ipAddr AND snmpif.snmpIfIndex = NEW.ifIndex);
       
       IF NOT FOUND THEN
          RAISE EXCEPTION 'Invalid SNMP Interface';
       END IF;
  END IF;
  RETURN NEW;
END;
$SnmpIpInterfaceId$ LANGUAGE 'plpgsql';

CREATE TRIGGER setSnmpInterfaceIdInIpInterfaceTrigger 
   BEFORE INSERT OR UPDATE
   ON ipInterface FOR EACH ROW
   EXECUTE PROCEDURE getSnmpInterfaceId();
