# Building Debian and RPM packages

OpenNMS ships three components — **opennms** (Horizon/Meridian core),
**minion**, and **sentinel** — as both `.deb` and `.rpm` packages. This
document covers how the build works and how to iterate on it locally.

## Quick start

```
make deb              # opennms .deb (full compile + assembly)
make deb-minion       # minion .deb
make deb-sentinel     # sentinel .deb
make rpm              # all three .rpms
make rpm-minion       # just the minion .rpm
make rpm-sentinel     # just the sentinel .rpm
make pkg-fast         # assembly-only rebuild of everything (see below)
make pkg-clean        # remove packaging build artifacts
```

These targets are thin wrappers around `makedeb.sh` / `makerpm.sh` (see
below) — run those scripts directly with `-h` for the full flag list
(signing, version overrides, branch/commit metadata, etc).

## How a build is put together

For each component, building a package means three steps:

1. **compile** (`compile.pl`, a thin wrapper around `mvn`) — builds and
   installs the Java modules into your local `~/.m2` repository.
2. **assemble** (`assemble.pl` / the `maven-assembly-plugin`) — lays out a
   full install tree (`etc/`, `lib/`, `bin/`, jetty webapps, etc.) from the
   already-built jars.
3. **package** — `dpkg-buildpackage` (driven by `debian/rules` +
   `debian/control`) or `rpmbuild -bb` (driven by
   `tools/packages/<component>/<component>.spec`) slices that install tree
   into the final set of `.deb`/`.rpm` files.

`opennms` core's compile+assemble step is embedded directly in
`debian/rules` and in `opennms.spec`. `minion` and `sentinel` instead share
one script each —
[`tools/packages/minion/create-minion-assembly.sh`](tools/packages/minion/create-minion-assembly.sh)
and
[`tools/packages/sentinel/create-sentinel-assembly.sh`](tools/packages/sentinel/create-sentinel-assembly.sh)
— which both the deb and rpm build paths call.

## Skipping recompiles

Both `makedeb.sh` and `makerpm.sh` accept:

- `-a` — assembly-only: skip the Java compile step and just re-run the
  assembly + packaging steps against whatever is already installed in your
  local `.m2` repository. Use this after a first full build when you're only
  changing packaging metadata (`debian/control`, `debian/rules`, a
  `.spec` file).
- `-d` — disable snapshot downloading during an assembly-only build.

`make pkg-fast` runs both scripts with `-a -d` for every component.

## Avoiding duplicate assembly work in CI

CI builds `.deb` and `.rpm` packages in two separate jobs
(`build-debian`, `build-rpm`). Historically, both jobs independently
re-ran the minion and sentinel compile+assembly step from scratch — the
same Maven reactor build, twice, per release.

A `build-package-assemblies` job now runs `create-minion-assembly.sh` and
`create-sentinel-assembly.sh` once, and both `build-debian` and
`build-rpm` reuse that output (set via `OPENNMS_REUSE_ASSEMBLY=1`) instead
of rebuilding it. `create-minion-assembly.sh` / `create-sentinel-assembly.sh`
honor this locally too: if `OPENNMS_REUSE_ASSEMBLY=1` is set and a
previously-built tarball is already sitting in
`opennms-assemblies/{minion,sentinel}/target/`, they skip straight past the
Maven invocation.

**Note:** the `opennms` core assembly is *not* shared between the deb and
rpm builds. `debian/rules` and `opennms.spec` pass a number of `-D`
install-path overrides to `assemble.pl`/`compile.pl` that have not been
verified to be identical between the two formats (e.g. `install.share.dir`
is explicitly overridden for the deb build but not for the rpm build) —
merging them without a build-verified diff of the resulting package
contents risked baking the wrong path into a shipped config file, so it was
left as future work rather than guessed at.

## Adding a new plugin package

Today, adding a new small plugin package (e.g. another provisioning adapter)
means editing three places by hand: a new stanza in `debian/control`, a new
install block in `debian/rules`, and a new `%package`/`%files` block in
`tools/packages/opennms/opennms.spec`. These are intentionally left as
separate, hand-maintained definitions for now — consolidating them into a
single shared manifest is a larger, riskier change (it touches the exact
file layout of every shipped package) and is being deferred to its own pass.
