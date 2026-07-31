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

<!-- Dashboard panel replicating the legacy OpenNMS News Feed box. -->
<template>
  <div class="news-feed">
    <p
      v-if="loading"
      class="news-feed__muted"
    >
      Loading…
    </p>
    <p
      v-else-if="!items.length"
      class="news-feed__muted"
    >
      No news available.
    </p>
    <ul
      v-else
      class="news-feed__list"
    >
      <li
        v-for="(item, idx) in items"
        :key="idx"
        class="news-feed__item"
      >
        <a
          class="news-feed__title"
          :href="item.link"
          target="_blank"
          rel="noopener noreferrer"
        >{{ item.title }}</a>
        <p class="news-feed__desc">{{ item.shortDescription }}</p>
        <p
          v-if="item.tags?.length"
          class="news-feed__tags"
        >
          <span
            v-for="tag in item.tags"
            :key="tag"
            class="news-feed__tag"
          >#{{ tag }}</span>
        </p>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import type { PanelComponentProps } from '@/types/dashboard'
import { getNewsFeed, type NewsFeedItem } from '@/services/newsfeedService'

const props = defineProps<PanelComponentProps>()

const loading = ref(true)
const items = ref<NewsFeedItem[]>([])

const load = async () => {
  loading.value = true
  items.value = await getNewsFeed()
  loading.value = false
}

onMounted(load)
watch(() => props.refreshTick, load)
</script>

<style scoped lang="scss">
.news-feed {
  font-size: 0.875rem;

  &__muted {
    color: var(--p-text-muted-color, #666);
  }

  &__list {
    list-style: none;
    margin: 0;
    padding: 0;
  }

  &__item {
    padding: 0.5rem 0;
    border-bottom: 1px solid var(--p-content-border-color, #e0e0e0);
  }

  &__title {
    font-weight: 600;
  }

  &__desc {
    margin: 0.25rem 0 0;
    color: var(--p-text-muted-color, #555);
  }

  &__tags {
    margin: 0.25rem 0 0;
    display: flex;
    flex-wrap: wrap;
    gap: 0.4rem;
  }

  &__tag {
    font-size: 0.75rem;
    color: var(--p-text-muted-color, #888);
  }
}
</style>
