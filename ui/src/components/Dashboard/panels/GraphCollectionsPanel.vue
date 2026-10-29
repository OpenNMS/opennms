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
  Dashboard panel replicating the legacy "KSC Reports" (Graph Collections)
  search box: a typeahead over the collection list that opens the chosen
  collection's view page, matching the ksc-box.jsp behavior.
-->
<template>
  <div class="search-box">
    <form
      class="search-box__form"
      @submit.prevent="open"
    >
      <input
        v-model="query"
        class="search-box__text"
        list="graph-collections-list"
        placeholder="Type the Graph Collection name"
        :aria-invalid="notFound"
        @input="notFound = false"
      >
      <datalist id="graph-collections-list">
        <option
          v-for="collection in collections"
          :key="collection.id"
          :value="collection.title"
        />
      </datalist>
      <button
        class="search-box__btn"
        type="submit"
        title="Open the graph collection"
      >
        <i class="pi pi-search" />
      </button>
    </form>
    <p
      v-if="notFound"
      class="search-box__hint"
    >No graph collection with that name.</p>
    <p
      v-if="loadError"
      class="search-box__hint"
      data-test="load-error"
    >{{ loadError }}</p>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { PanelComponentProps } from '@/types/dashboard'
import { getGraphCollections } from '@/services/graphCollectionsService'
import type { GraphCollection } from '@/types/graphCollection'

defineProps<PanelComponentProps>()

const router = useRouter()
const query = ref('')
const notFound = ref(false)
const loadError = ref('')
const collections = ref<GraphCollection[]>([])

const load = async () => {
  const result = await getGraphCollections()
  if (result.success) {
    collections.value = result.payload ?? []
    loadError.value = ''
  } else {
    loadError.value = result.message
  }
}

// Resolve the typed title to a collection and open its view; an empty box opens the list.
const open = () => {
  const text = query.value.trim().toLowerCase()
  if (!text) {
    router.push('/graph-collections')
    return
  }
  const match = collections.value.find(c => c.title.toLowerCase() === text)
    ?? collections.value.find(c => c.title.toLowerCase().includes(text))
  if (!match) {
    notFound.value = true
    return
  }
  router.push(`/graph-collections/${match.id}`)
}

// Read once: the list carries every collection with all its graphs, and titles seldom change,
// so the dashboard's refresh tick leaves it alone.
onMounted(load)
</script>

<style scoped lang="scss">
.search-box {
  font-size: 0.875rem;

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

  &__hint {
    margin: 0.35rem 0 0;
    color: var(--p-red-500, #d32f2f);
  }
}
</style>
