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

<!-- Dashboard panel replicating the legacy notification-box. -->
<template>
  <div class="notif-panel">
    <p
      v-if="loading"
      class="notif-panel__muted"
    >
      Loading…
    </p>
    <template v-else-if="summary">
      <p>
        <i class="pi pi-user" />
        You have
        <a :href="userLink">{{ phrase(summary.userUnacknowledgedCount) }}</a>
      </p>
      <p>
        <i class="pi pi-users" />
        There {{ summary.totalUnacknowledgedCount === 1 ? 'is' : 'are' }}
        <a :href="teamLink">{{ phrase(summary.totalUnacknowledgedCount) }}</a>
      </p>
      <p>
        <i class="pi pi-calendar" />
        <a href="/opennms/roles">On-Call Schedule</a>
      </p>
    </template>
    <p
      v-else
      class="notif-panel__muted"
    >
      Unable to load notifications.
    </p>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import type { PanelComponentProps } from '@/types/dashboard'
import { getNotificationSummary, type NotificationSummary } from '@/services/notificationService'
import { useAuthStore } from '@/stores/authStore'

const props = defineProps<PanelComponentProps>()
const authStore = useAuthStore()

const loading = ref(true)
const summary = ref<NotificationSummary | null>(null)

// The controller honors filter=user=<remoteUser> (as the legacy notification-box
// used); filtertype=user is ignored and would show everyone's notices.
const userLink = computed(() => {
  const user = authStore.whoAmI?.id
  return user
    ? `/opennms/notification/browse?acktype=unack&filter=${encodeURIComponent(`user=${user}`)}`
    : '/opennms/notification/browse?acktype=unack'
})
const teamLink = '/opennms/notification/browse?acktype=unack'

const phrase = (n: number) => (n === 0 ? 'no outstanding notices' : `${n} outstanding notice${n === 1 ? '' : 's'}`)

const load = async () => {
  loading.value = true
  const result = await getNotificationSummary()
  summary.value = result || null
  loading.value = false
}

onMounted(load)
watch(() => props.refreshTick, load)
</script>

<style scoped lang="scss">
.notif-panel {
  font-size: 0.875rem;

  p {
    margin: 0 0 0.5rem;
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }

  &__muted {
    color: var(--p-text-muted-color, #666);
  }
}
</style>
