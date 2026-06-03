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

<!-- HTML Content panel: loads a user-provided URL in an iframe (edit via gear). -->
<template>
  <div class="html-content">
    <iframe
      v-if="url"
      :src="url"
      class="html-content__frame"
      title="Panel content"
      sandbox="allow-scripts allow-same-origin allow-popups allow-forms"
    />
    <p
      v-else
      class="html-content__empty"
    >
      No URL set. In Edit mode, open this panel's options (⚙) to provide a URL.
    </p>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { PanelComponentProps } from '@/types/dashboard'

const props = defineProps<PanelComponentProps>()

const url = computed(() => String(props.options?.url ?? ''))
</script>

<style scoped lang="scss">
.html-content {
  height: 100%;

  &__frame {
    width: 100%;
    height: 100%;
    min-height: 200px;
    border: 0;
  }

  &__empty {
    color: var(--feather-secondary-text-on-surface, #666);
  }
}
</style>
