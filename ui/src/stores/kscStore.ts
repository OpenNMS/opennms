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

import API from '@/services'
import useSnackbar from '@/composables/useSnackbar'
import { KscReport } from '@/types/ksc'
import { defineStore } from 'pinia'
import { ref } from 'vue'

// OnmsRestService errors come back as a plain-text body; fall back to the axios
// message so the user sees something actionable rather than a bare "failed".
const errorMessage = (err: unknown, fallback: string): string => {
  const e = err as { response?: { data?: unknown }, message?: string }
  const data = e?.response?.data
  if (typeof data === 'string' && data.trim()) {
    return data.trim()
  }
  return e?.message || fallback
}

export const useKscStore = defineStore('kscStore', () => {
  const reports = ref<KscReport[]>([])
  const loading = ref(false)
  const { showSnackBar } = useSnackbar()

  const load = async () => {
    loading.value = true
    try {
      reports.value = await API.getKscReports()
    } catch (err) {
      // Surface the failure rather than leaving an empty list that reads as
      // "no reports configured"; keep whatever was already loaded.
      showSnackBar({ msg: errorMessage(err, 'Could not load graph collections.'), error: true })
    } finally {
      loading.value = false
    }
  }

  const getReport = async (id: number): Promise<KscReport | null> => {
    return await API.getKscReport(id)
  }

  // Create when id is absent, otherwise replace the report in place. Returns
  // whether the save succeeded so the dialog can stay open on failure.
  const saveReport = async (report: KscReport): Promise<boolean> => {
    try {
      if (report.id === null || report.id === undefined) {
        await API.createKscReport(report)
      } else {
        await API.updateKscReport(report.id, report)
      }
      showSnackBar({ msg: `Report '${report.label}' saved.` })
      await load()
      return true
    } catch (err) {
      showSnackBar({ msg: errorMessage(err, 'Could not save the report.'), error: true })
      return false
    }
  }

  const removeReport = async (report: KscReport): Promise<boolean> => {
    if (report.id === null || report.id === undefined) {
      return false
    }
    try {
      await API.deleteKscReport(report.id)
      showSnackBar({ msg: `Report '${report.label}' deleted.` })
      await load()
      return true
    } catch (err) {
      showSnackBar({ msg: errorMessage(err, 'Could not delete the report.'), error: true })
      return false
    }
  }

  const reloadConfig = async (): Promise<void> => {
    try {
      await API.reloadKscConfig()
      showSnackBar({ msg: 'KSC configuration reloaded from disk.' })
      await load()
    } catch (err) {
      showSnackBar({ msg: errorMessage(err, 'Could not reload the configuration.'), error: true })
    }
  }

  return { reports, loading, load, getReport, saveReport, removeReport, reloadConfig }
})
