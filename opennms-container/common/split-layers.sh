#!/usr/bin/env bash
#
# Licensed to The OpenNMS Group, Inc (TOG) under one or more
# contributor license agreements.  See the LICENSE.md file
# distributed with this work for additional information
# regarding copyright ownership.
#
# TOG licenses this file to You under the GNU Affero General
# Public License Version 3 (the "License") or (at your option)
# any later version.  You may not use this file except in
# compliance with the License.  You may obtain a copy of the
# License at:
#
#      https://www.gnu.org/licenses/agpl-3.0.txt
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
# either express or implied.  See the License for the specific
# language governing permissions and limitations under the
# License.
#

##
# Split an unpacked OpenNMS assembly into image layers ordered by how often
# their content changes, so that an upgrade only pulls what really changed.
#
#   split-layers.sh <source dir> <output dir> <install prefix> <version>
#
# Writes <output dir>/{deps,onms,app,src}/<install prefix>/..., each meant to
# be added to the image with its own COPY --link:
#
#   deps  third-party jars and the third-party part of the Karaf maven
#         repositories (system/, repositories/*/)
#   onms  OpenNMS-built jars and the org/opennms part of those repositories
#   app   everything else: scripts, config, web assets, empty directories
#   src   the bundled source archive (changes every build, so it goes last)
#
# Files with identical content, mode and owner are hardlinked to each other
# (e.g. the copies of the same jar in lib/ and system/), and every group of
# hardlinked files is kept inside one layer so the link survives in the image.
#
# deps and onms get a constant mtime so that an unchanged layer produces a
# byte-identical tar, and therefore the same digest, in every build. app keeps
# the real mtimes because Jetty derives Last-Modified/ETag for web assets from
# them.
##

set -o errexit -o nounset -o pipefail

SRC="${1:?source directory}"
OUT="${2:?output directory}"
PREFIX="${3:?install prefix, e.g. opennms}"
VERSION="${4:?version, e.g. 36.0.6}"
# 2000-01-01T00:00:00Z: any constant works, it only has to stay the same.
LAYER_MTIME="${LAYER_MTIME:-946684800}"

cd "${SRC}"

if find . -name $'*\n*' -o -name '*\\*' | grep -q .; then
  echo "split-layers: file names with newlines or backslashes are not supported" >&2
  exit 1
fi

WORK="$(mktemp -d)"
trap 'rm -rf "${WORK}"' EXIT

##
# 1. Hardlink duplicates. Key = content hash + mode + owner, so a link never
#    changes the permissions or ownership any path had before.
##
# Three line-aligned lists (xargs without -P keeps the input order).
find . -type f -size +0 -print0 | sort -z > "${WORK}/paths0"
xargs -0 -r sha256sum < "${WORK}/paths0" | cut -c1-64 > "${WORK}/hash"
xargs -0 -r stat -c '%a:%u:%g' < "${WORK}/paths0" > "${WORK}/meta"
tr '\0' '\n' < "${WORK}/paths0" > "${WORK}/paths"
# "<hash>:<mode>:<uid>:<gid> <0|1> <path>", grouped by key; system/ copies
# sort first so they stay the canonical file.
paste -d' ' "${WORK}/hash" "${WORK}/meta" "${WORK}/paths" \
  | awk '{ k = $1 ":" $2; p = substr($0, length($1) + length($2) + 3); print k, (p ~ /^\.\/system\// ? 0 : 1), p }' \
  | sort -k1,1 -k2,2n -k3 > "${WORK}/keyed"

linked=0
prev_key=""; canon=""
while read -r key _ path; do
  if [[ "${key}" == "${prev_key}" ]]; then
    ln -f "${canon}" "${path}"
    linked=$((linked + 1))
  else
    prev_key="${key}"; canon="${path}"
  fi
done < "${WORK}/keyed"

##
# 2. Classify every file. Volatility order: deps < app < onms < src; a group
#    of hardlinked files goes to the most volatile layer any member needs.
##
classify() {
  local p="${1#./}" b="${1##*/}"
  case "${p}" in
    jetty-webapps/*/source/*)          echo 3; return ;;
    system/org/opennms/*)              echo 2; return ;;
    repositories/*/org/opennms/*)      echo 2; return ;;
  esac
  case "${b}" in
    *.jar|*.war|*.kar)
      # Not *onms*: that also matches patched third-party builds such as
      # jetty-*-9.4.60.onms1.jar, which rarely change and belong in deps.
      if [[ "${b}" == *opennms* || "${b}" == *"${VERSION}"* ]]; then
        echo 2
      else
        echo 0
      fi
      return ;;
  esac
  # Karaf maven repositories: third-party poms, feature files and bundles
  case "${p}" in
    system/*|repositories/*) echo 0 ;;
    *)                       echo 1 ;;
  esac
}

LAYERS=(deps app onms src)
: > "${WORK}/files"
# inode -> most volatile layer of its members
declare -A inode_layer
while IFS=' ' read -r inode path; do
  l="$(classify "${path}")"
  if [[ -z "${inode_layer[${inode}]:-}" || "${l}" -gt "${inode_layer[${inode}]}" ]]; then
    inode_layer[${inode}]="${l}"
  fi
  printf '%s %s\n' "${inode}" "${path}" >> "${WORK}/files"
done < <(find . -type f -printf '%i %p\n')

for l in "${LAYERS[@]}"; do : > "${WORK}/list.${l}"; done
while IFS=' ' read -r inode path; do
  printf '%s\n' "${path#./}" >> "${WORK}/list.${LAYERS[${inode_layer[${inode}]}]}"
done < "${WORK}/files"
# Directories (including empty ones) and symlinks belong to the app layer.
find . -mindepth 1 \( -type d -o -type l \) -printf '%P\n' >> "${WORK}/list.app"

##
# 3. Materialize each layer. No -r: --files-from lists every entry, implied
#    parent directories are created with their source attributes.
##
for l in "${LAYERS[@]}"; do
  mkdir -p "${OUT}/${l}/${PREFIX}"
  sort -o "${WORK}/list.${l}" "${WORK}/list.${l}"
  rsync -lptgoDH --files-from="${WORK}/list.${l}" ./ "${OUT}/${l}/${PREFIX}/"
  # the layer root carries the attributes of the install directory itself
  chmod --reference=. "${OUT}/${l}/${PREFIX}"
  chown --reference=. "${OUT}/${l}/${PREFIX}"
done

for l in deps onms; do
  find "${OUT}/${l}" -exec touch -h -d "@${LAYER_MTIME}" {} +
done

echo "split-layers: hardlinked ${linked} duplicate files"
for l in "${LAYERS[@]}"; do
  printf 'split-layers: %-4s %6s files %8s\n' "${l}" "$(wc -l < "${WORK}/list.${l}")" "$(du -sh "${OUT}/${l}" | cut -f1)"
done
