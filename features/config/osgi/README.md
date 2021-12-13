# Osgi Integration for CM
This module makes the configuration management available for the "osgi world".

It consists of two parts.
They are executed at different times in the startup order:

1. Core OSGI
2. Part One: Delegating facade
3. Felix ConfigurationAdmin
4. Classes for CM
5. Part 2: CM Integration
6. Rest of OpenNMS bundles

## Part One: Delegating facade
Called very early in the startup process.
It needs to be available for Felix ConfigurationAdmin.
It has very limited dependencies and can thus start very early.

It acts as a delegater:
- OpenNMS plugins => CM
- Native Osgi / Felix bundles => FilePersistenceManager

## Part 2: CM Integration
Called later in the startup process, after all Felix "native" bundles are registered but before OpenNMS bundles.
It will be registered only after CM relevant classed are available.

Acts as a bridge to CM.
