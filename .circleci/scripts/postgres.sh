#!/bin/bash

set -e
set -o pipefail

echo "##### Configuring system defaults"
psql -U postgres -c "ALTER SYSTEM SET fsync=off;"
psql -U postgres -c "ALTER SYSTEM SET max_connections=200;"
psql -U postgres -c "ALTER SYSTEM SET shared_buffers='256MB';"

echo "##### Adding opennms user to Postgres"
psql -U postgres -c "CREATE USER opennms; \
  ALTER USER opennms WITH SUPERUSER; \
  ALTER USER opennms WITH PASSWORD 'opennms';";

echo "##### Creating base OpenNMS database"
psql -U postgres -c "CREATE DATABASE opennms;";
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE opennms TO opennms;"
