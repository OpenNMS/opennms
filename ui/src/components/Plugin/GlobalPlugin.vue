<script setup lang="ts">
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

import { computed, onMounted } from 'vue'
import Container from '@/components/Plugin/Container.vue'
import { addStylesheet, getCSSPath, getJSPath } from '@/components/Plugin/utils'
import type { Plugin } from '@/types'

const props = defineProps<{ plugin: Plugin }>()

const baseRestUrl = import.meta.env.VITE_BASE_REST_URL

// globalModuleFileName is guaranteed non-null by the v-for filter in App.vue,
// but we guard anyway so the type system stays happy.
const scriptUrl = computed(() =>
  props.plugin.globalModuleFileName
    ? getJSPath(baseRestUrl, props.plugin.extensionId, props.plugin.resourceRootPath, props.plugin.globalModuleFileName)
    : null
)

onMounted(() => {
  addStylesheet(getCSSPath(baseRestUrl, props.plugin.extensionId))
})
</script>

<template>
  <Container v-if="scriptUrl" :script="scriptUrl" />
</template>
