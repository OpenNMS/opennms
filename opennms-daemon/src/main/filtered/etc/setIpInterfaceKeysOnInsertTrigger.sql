--First step to refactoring ipInterface, snmpInterface tables to have actual IDs
--This trigger puts IpInterface IDs in the IfServices table

DROP TRIGGER setIpInterfaceKeysOnInsertTrigger ON ifServices;

CREATE TRIGGER setIpInterfaceKeysOnInsertTrigger
   BEFORE INSERT
   ON ifServices FOR EACH ROW
   EXECUTE PROCEDURE setIpInterfaceKeysOnInsert();
