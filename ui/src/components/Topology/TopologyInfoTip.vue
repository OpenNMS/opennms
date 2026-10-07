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
  A small (i) beside a heading that shows its explanation on hover or focus,
  so panels can keep explanations out of their body text.
-->
<template>
  <OnmsIconButton
    class="topology-info-tip"
    :icon="Info"
    icon-size="1rem"
    :title="title"
    :tooltip="text"
    :tooltip-position="position"
  />
</template>

<script setup lang="ts">
import { OnmsIconButton } from '@opennms/onms-ui'
import Info from '@opennms/onms-ui/icons/action/Info.vue'

withDefaults(defineProps<{
  text: string
  title?: string
  position?: 'top' | 'bottom' | 'left' | 'right'
}>(), {
  title: 'More information',
  position: 'right'
})
</script>

<style scoped>
.topology-info-tip {
  width: 1.5rem;
  height: 1.5rem;
  padding: 0;
}
</style>
