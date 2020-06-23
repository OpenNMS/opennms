#!/bin/bash -e
echo "##### Starting Postgres"
docker run --rm --name postgres-onms-itest -p 5432:5432 -d postgres:10.7-alpine \
	-c 'shared_buffers=256MB' -c 'max_connections=200' -c 'fsync=off'

echo "##### Waiting for Postgres to start..."
WAIT=0
while ! docker exec postgres-onms-itest psql -U postgres -c 'select 1;'; do
  sleep 1
  WAIT=$((WAIT + 1))
  if [ "$WAIT" -gt 15 ]; then
    echo "Error: Timeout waiting for Postgres to start"
    exit 1
  fi
done

# Postgres gets restarted in the container, let's wait long enough for this to happen
sleep 5
WAIT=0
while ! docker exec postgres-onms-itest psql -U postgres -c 'select 1;'; do
  sleep 1
  WAIT=$((WAIT + 1))
  if [ "$WAIT" -gt 15 ]; then
    echo "Error: Timeout waiting for Postgres to start"
    exit 1
  fi
done

echo "##### Adding opennms user to Postgres"
docker exec postgres-onms-itest psql -U postgres -c "CREATE USER opennms; \
ALTER USER opennms WITH SUPERUSER; \
ALTER USER opennms WITH PASSWORD 'opennms';";
# Break this out, otherwise we run into: ERROR:  CREATE DATABASE cannot be executed from a function or multi-command string
docker exec postgres-onms-itest psql -U postgres -c "CREATE DATABASE opennms;";
docker exec postgres-onms-itest psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE opennms TO opennms;"
