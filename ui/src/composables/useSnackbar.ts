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

import { MessageSeverity, SnackbarProps } from '@/types'
import { useOnmsToast, OnmsToastSeverity } from '@opennms/onms-ui'

// Legacy snackbar API, now a thin adapter over the @opennms/onms-ui toast seam
// (NMS-20029). New code should call useOnmsToast() directly; this adapter exists
// so the many existing showSnackBar call sites keep working.

// Total map: adding a MessageSeverity member without a seam equivalent (or
// renaming a seam severity) fails compilation here instead of at runtime.
const SEVERITY_MAP: Record<MessageSeverity, OnmsToastSeverity> = {
  [MessageSeverity.Error]: 'error',
  [MessageSeverity.Info]: 'info',
  [MessageSeverity.Success]: 'success',
  [MessageSeverity.Warn]: 'warn'
}

const useSnackbar = () => {
  const { showToast, hideAllToasts } = useOnmsToast()

  const showSnackBar = (snackbarProps: SnackbarProps) => {
    const { center, error, msg, severity: severityProp, timeout } = snackbarProps

    // Prefer an explicit severity; fall back to the legacy error boolean.
    const severity = SEVERITY_MAP[severityProp ?? (error ? MessageSeverity.Error : MessageSeverity.Success)]

    showToast({
      message: msg,
      severity,
      center,
      timeout
    })
  }

  return {
    showSnackBar,
    hideSnackbar: hideAllToasts
  }
}

export default useSnackbar
