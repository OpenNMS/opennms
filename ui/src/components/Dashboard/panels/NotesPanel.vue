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

<!-- Free-text Notes panel. Text is stored in the panel options (edit via gear). -->
<template>
  <div class="notes">
    <p
      v-if="text"
      class="notes__text"
    >
      {{ text }}
    </p>
    <p
      v-else
      class="notes__empty"
    >
      No notes yet. In Edit mode, open this panel's options (⚙) to add text.
    </p>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { PanelComponentProps } from '@/types/dashboard'

const props = defineProps<PanelComponentProps>()

const text = computed(() => String(props.options?.text ?? ''))
</script>

<style scoped lang="scss">
.notes {
  font-size: 0.875rem;

  &__text {
    margin: 0;
    white-space: pre-wrap; // preserve line breaks / spacing from the textarea
    word-break: break-word;
  }

  &__empty {
    margin: 0;
    color: var(--feather-secondary-text-on-surface, #666);
  }
}
</style>
