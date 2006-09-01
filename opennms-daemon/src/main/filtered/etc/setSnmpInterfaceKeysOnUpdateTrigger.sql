--First step to refactoring ipInterface, snmpInterface tables to have actual IDs
--This trigger puts IpInterface IDs in the IfServices table

DROP TRIGGER setSnmpInterfaceKeysOnUpdateTrigger ON ipInterface;

CREATE TRIGGER setSnmpInterfaceKeysOnUpdateTrigger
   BEFORE UPDATE
   ON ipInterface FOR EACH ROW
   EXECUTE PROCEDURE setSnmpInterfaceKeysOnUpdate();