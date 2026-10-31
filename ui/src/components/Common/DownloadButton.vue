<!--
Licensed to The OpenNMS Group, Inc (TOG) under one or more
contributor license agreements.  See the LICENSE.md file
distributed with this work for additional information
regarding copyright ownership.

TOG licenses this file to You under the GNU Affero General
Public License Version 3 (the "License") or (at your option)
any later version.  You may not use this file except in
compliance with the License.  You may obtain a copy of the
License at:

     https://www.gnu.org/licenses/agpl-3.0.txt

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
either express or implied.  See the License for the specific
language governing permissions and limitations under the
License.
-->

<!--
  Outlined download button with the file format (PDF, CSV) centered under the
  download arrow, so pages offering more than one export are unambiguous.
  `title` names the button for assistive tech and is the native tooltip.
-->
<template>
  <OnmsButton
    variant="outlined"
    class="download-button"
    :title="title"
    :aria-label="title"
  >
    <span class="download-button__content">
      <OnmsIcon :icon="DownloadFile" class="download-button__icon" />
      <span class="download-button__format">{{ format }}</span>
    </span>
  </OnmsButton>
</template>

<script setup lang="ts">
import { OnmsButton, OnmsIcon } from '@opennms/onms-ui'
import DownloadFile from '@opennms/onms-ui/icons/action/DownloadFile.vue'

defineProps<{
  format: 'PDF' | 'CSV'
  title: string
}>()
</script>

<style scoped lang="scss">
.download-button {
  min-height: var(--onms-action-button-height, 3.5rem);
  padding-top: 0.25rem;
  padding-bottom: 0.25rem;
}

.download-button__content {
  display: flex;
  flex-direction: column;
  align-items: center;
  line-height: 1;
}

.download-button__icon {
  font-size: 1.5rem;
}

.download-button__format {
  margin-top: 0.125rem;
  font-size: 0.75rem;
  font-weight: 600;
  letter-spacing: 0.05em;
}
</style>
