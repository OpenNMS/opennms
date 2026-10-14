///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements.  See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version.  You may not use this file except in
/// compliance with the License.  You may obtain a copy of the
/// License at:
///
///      https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied.  See the License for the specific
/// language governing permissions and limitations under the
/// License.
///

// The subset of the FileSystemEntry API this traversal relies on, typed here because the
// entries come from the non-standardized webkitGetAsEntry() and lib.dom leaves them loose.
export type DroppedEntry = {
  isFile: boolean
  isDirectory: boolean
  file?: (resolve: (file: File) => void, reject: (error: unknown) => void) => void
  createReader?: () => {
    readEntries: (resolve: (entries: DroppedEntry[]) => void, reject: (error: unknown) => void) => void
  }
}

const fileOf = (entry: DroppedEntry): Promise<File | null> =>
  new Promise((resolve) => {
    if (!entry.file) {
      resolve(null)
      return
    }
    entry.file(resolve, () => resolve(null))
  })

const readAllEntries = async (entry: DroppedEntry): Promise<DroppedEntry[]> => {
  const reader = entry.createReader?.()
  if (!reader) {
    return []
  }
  const all: DroppedEntry[] = []
  // readEntries() hands out at most ~100 entries per call; it must be called again
  // until an empty batch, or a large folder silently loses everything past the first batch
  for (;;) {
    const batch = await new Promise<DroppedEntry[]>((resolve) => {
      reader.readEntries(resolve, () => resolve([]))
    })
    if (batch.length === 0) {
      return all
    }
    all.push(...batch)
  }
}

/** Every file under the given entries, folders walked recursively. */
export const filesFromEntries = async (entries: Array<DroppedEntry | null>): Promise<File[]> => {
  const files: File[] = []
  for (const entry of entries) {
    if (!entry) {
      continue
    }
    if (entry.isFile) {
      const file = await fileOf(entry)
      if (file) {
        files.push(file)
      }
    } else if (entry.isDirectory) {
      files.push(...await filesFromEntries(await readAllEntries(entry)))
    }
  }
  return files
}

/**
 * The files of a drop, folders included. The entries are captured synchronously because the
 * DataTransferItemList is cleared once the drop handler yields; browsers without
 * webkitGetAsEntry() fall back to the plain file list (no folder support there).
 */
export const collectDroppedFiles = async (dataTransfer: DataTransfer): Promise<File[]> => {
  const items = Array.from(dataTransfer.items ?? [])
  const entries = items.map(item =>
    (typeof item.webkitGetAsEntry === 'function' ? item.webkitGetAsEntry() as DroppedEntry | null : null))
  if (entries.some(entry => entry !== null)) {
    return filesFromEntries(entries)
  }
  return Array.from(dataTransfer.files ?? [])
}
