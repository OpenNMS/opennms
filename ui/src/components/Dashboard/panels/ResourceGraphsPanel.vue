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
  Dashboard panel for the legacy "Resource Graphs" box. The legacy target
  (graph/chooseresource.jsp) no longer exists, and a node search that lands on
  the node list contradicts the panel title — so this is an honest launcher
  into the Vue Resource Graphs browser, which owns resource selection.
-->
<template>
  <div class="search-box">
    <a
      class="search-box__launch"
      href="/opennms/ui/index.html#/resource-graphs"
      title="Choose a resource and view its graphs"
    >
      <i class="pi pi-chart-line" />
      Open Resource Graphs
    </a>
  </div>
</template>

<script setup lang="ts">
import type { PanelComponentProps } from '@/types/dashboard'

defineProps<PanelComponentProps>()
</script>

<style scoped lang="scss">
.search-box {
  font-size: 0.875rem;

  &__lead {
    margin: 0 0 0.5rem;
    font-weight: 600;
  }

  &__form {
    display: flex;
    gap: 0.25rem;
  }

  &__launch {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.3rem 0.6rem;
    border: 1px solid var(--p-content-border-color, #ccc);
    border-radius: 4px;
    color: inherit;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}
</style>
