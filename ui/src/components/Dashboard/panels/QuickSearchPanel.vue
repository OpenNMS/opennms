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
  Dashboard panel replicating the legacy Quick Search box: simple forms that GET
  to element/nodeList.htm (node id / label / IP), matching quicksearch-box.jsp.
-->
<template>
  <div class="quick-search">
    <form
      v-for="field in fields"
      :key="field.name"
      class="quick-search__row"
      action="/opennms/element/nodeList.htm"
      method="get"
    >
      <label class="quick-search__label">{{ field.label }}</label>
      <input
        type="hidden"
        name="listInterfaces"
        :value="field.listInterfaces"
      >
      <div class="quick-search__input">
        <input
          class="quick-search__text"
          :name="field.name"
          :placeholder="field.placeholder"
        >
        <button
          class="quick-search__btn"
          type="submit"
          :title="`Search by ${field.label}`"
        >
          <i class="pi pi-search" />
        </button>
      </div>
    </form>

    <form
      v-if="services.length"
      class="quick-search__row"
      action="/opennms/element/nodeList.htm"
      method="get"
    >
      <label class="quick-search__label">Providing service</label>
      <input
        type="hidden"
        name="listInterfaces"
        value="false"
      >
      <div class="quick-search__input">
        <select
          class="quick-search__text"
          name="service"
        >
          <option
            v-for="svc in services"
            :key="svc.id"
            :value="svc.id"
          >{{ svc.name }}</option>
        </select>
        <button
          class="quick-search__btn"
          type="submit"
          title="Search by providing service"
        >
          <i class="pi pi-search" />
        </button>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import type { PanelComponentProps } from '@/types/dashboard'
import { getServiceTypes, type ServiceType } from '@/services/dashboardService'

defineProps<PanelComponentProps>()

const fields = [
  { label: 'Node ID', name: 'nodeId', placeholder: 'Node ID', listInterfaces: 'false' },
  { label: 'Node label', name: 'nodename', placeholder: 'localhost', listInterfaces: 'true' },
  { label: 'TCP/IP Address', name: 'iplike', placeholder: '*.*.*.* or *:*:*:*:*:*:*:*', listInterfaces: 'false' }
]

const services = ref<ServiceType[]>([])
onMounted(async () => {
  services.value = await getServiceTypes()
})
</script>

<style scoped lang="scss">
.quick-search {
  font-size: 0.875rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;

  &__label {
    display: block;
    font-weight: 600;
    margin-bottom: 0.15rem;
  }

  &__input {
    display: flex;
    gap: 0.25rem;
  }

  &__text {
    flex: 1 1 auto;
    min-width: 0;
    padding: 0.3rem 0.5rem;
    border: 1px solid var(--p-content-border-color, #ccc);
    border-radius: 4px;
  }

  &__btn {
    flex: 0 0 auto;
    padding: 0.3rem 0.6rem;
    border: 1px solid var(--p-content-border-color, #ccc);
    border-radius: 4px;
    background: var(--p-content-background, #fff);
    cursor: pointer;
  }
}
</style>
