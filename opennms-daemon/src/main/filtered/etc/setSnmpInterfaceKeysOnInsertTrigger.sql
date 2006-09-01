--First step to refactoring ipInterface, snmpInterface tables to have actual IDs
--This trigger puts IpInterface IDs in the IfServices table

DROP TRIGGER setSnmpInterfaceKeysOnInsertTrigger ON ipInterface;

CREATE TRIGGER setSnmpInterfaceKeysOnInsertTrigger
   BEFORE INSERT
   ON ipInterface FOR EACH ROW
   EXECUTE PROCEDURE setSnmpInterfaceKeysOnInsert();