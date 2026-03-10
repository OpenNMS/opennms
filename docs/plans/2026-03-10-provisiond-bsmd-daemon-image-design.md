# Provisiond & BSMd Daemon Image Design

## Problem

The opennms/daemon image (Sentinel Karaf assembly) includes 13 daemon features but not `opennms-daemon-provisiond` or `opennms-daemon-bsmd`. Their transitive dependency JARs (core.tasks, provision-persistence, bsm-service-impl, etc.) are missing from the image's `system/` directory, causing ClassNotFoundException at runtime.

Workaround: Provisiond runs in webapp (`CORE_SERVICE_PROVISIOND_ENABLED: "true"`). BSMd is non-functional.

## Solution

Add `opennms-daemon-provisiond` and `opennms-daemon-bsmd` to the `<installedFeatures>` section of `features/container/sentinel/pom.xml`. Remove `opennms-daemon-passivestatusd` (eliminated daemon).

Rebuild the assembly chain: `container/features` -> `features/container/sentinel` -> `opennms-assemblies/daemon` -> docker build.

Revert `CORE_SERVICE_PROVISIOND_ENABLED` to `"false"` in docker-compose.yml (standalone container takes over).

## Success Criteria

- Provisiond container reaches healthy status
- BSMd container reaches healthy status
- Webapp no longer runs Provisiond (disabled)
