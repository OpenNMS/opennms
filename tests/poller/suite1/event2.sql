DELETE FROM ifServices WHERE nodeID = 2;
DELETE FROM ipInterface WHERE nodeID = 2;
DELETE FROM node WHERE nodeID = 2;

INSERT INTO node (nodeID, dpName, nodeCreateTime, nodeType) VALUES (2, 'localhost', '30-Aug-2001 12:45:00', 'A');
INSERT INTO ipInterface (nodeID, ipAddr, ipHostname, isManaged) VALUES (2, '192.168.100.31', '192.168.100.31', 'A');
-- INSERT INTO service (serviceID, serviceName) VALUES (1, 'HTTP');
INSERT INTO ifServices (nodeID, ipAddr, serviceID, status, source, notify) VALUES (2, '192.168.100.31', 1, 'A', 'P', 'N');

