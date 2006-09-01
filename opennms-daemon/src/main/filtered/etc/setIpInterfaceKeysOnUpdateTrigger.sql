--First step to refactoring ipInterface, snmpInterface tables to have actual IDs
--This trigger puts IpInterface IDs in the IfServices table

DROP TRIGGER setIpInterfaceKeysOnUpdateTrigger ON ifServices;

CREATE TRIGGER setIpInterfaceKeysOnUpdateTrigger
   BEFORE UPDATE
   ON ifServices FOR EACH ROW
   EXECUTE PROCEDURE setIpInterfaceKeysOnUpdate();
