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
  Dashboard panel for the legacy "KSC Reports" search box. Simplified launcher:
  links to the KSC report list. (Full report-name typeahead is a follow-up.)
-->
<template>
  <div class="search-box">
    <form
      class="search-box__form"
      action="/opennms/KSC/index.jsp"
      method="get"
    >
      <input
        class="search-box__text"
        name="report"
        placeholder="Type the KSC report name"
      >
      <button
        class="search-box__btn"
        type="submit"
        title="Open KSC reports"
      >
        <i class="pi pi-search" />
      </button>
    </form>
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
