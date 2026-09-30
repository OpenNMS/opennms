<template>
  <TableCard class="notification-queries-bar">
    <div class="query-controls">
      <div class="query-presets">
        <OnmsSelectButton
          :modelValue="mode"
          :options="presetOptions"
          optionLabel="label"
          optionValue="value"
          aria-label="Notification query"
          data-test="query-presets"
          @update:modelValue="onModeChange"
        />
        <OnmsIconButton
          variant="text"
          :icon="Refresh"
          tooltip="Refresh"
          aria-label="Refresh notifications"
          :disabled="store.loading"
          data-test="query-refresh-button"
          @click="refresh"
        />
      </div>
      <div class="query-searches">
        <div class="query-search">
          <OnmsSelectButton
            :modelValue="mode"
            :options="userSearchOptions"
            optionLabel="label"
            optionValue="value"
            aria-label="Notifications for user"
            data-test="query-user-search"
            @update:modelValue="onModeChange"
            @click="runUserSearch"
          />
          <OnmsSearchInput
            ref="userSearchInput"
            v-model="userSearch"
            class="query-search-input"
            inputId="notification-user-search"
            placeholder="User ID"
            ariaLabel="User ID"
            dataTest="user-search-input"
            @focusin="selectMode('userSearch')"
            @keyup.enter="runUserSearch"
            @clear="clearUserSearch"
          />
        </div>
        <div class="query-search">
          <OnmsSelectButton
            :modelValue="mode"
            :options="notificationIdOptions"
            optionLabel="label"
            optionValue="value"
            aria-label="View details for ID"
            data-test="query-notification-id"
            @update:modelValue="onModeChange"
          />
          <OnmsSearchInput
            ref="notificationIdInput"
            v-model="notificationIdSearch"
            class="query-search-input"
            inputId="notification-id-search"
            placeholder="Notification ID"
            ariaLabel="Notification ID"
            dataTest="notification-id-search-input"
            @focusin="selectMode('notificationId')"
            @keyup.enter="goToNotification"
          />
        </div>
      </div>
    </div>
  </TableCard>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import { OnmsIconButton, OnmsSearchInput, OnmsSelectButton } from '@opennms/onms-ui'

import TableCard from '@/components/Common/TableCard.vue'
import Refresh from '@opennms/onms-ui/icons/navigation/Refresh.vue'
import { useMenuStore } from '@/stores/menuStore'
import useActionFeedback from '@/composables/useActionFeedback'
import { useNotificationsStore } from '@/stores/notificationsStore'
import { NotificationQueryPreset } from '@/types/notifications'

// The three select buttons act as one choice: each is bound to `mode` and only
// shows a selection when `mode` is one of its own options. 'notificationId' is
// local to this card: it jumps to a detail page rather than querying the table.
type QueryMode = NotificationQueryPreset | 'notificationId'

interface QueryModeOption {
  label: string
  value: QueryMode
}

const menuStore = useMenuStore()
const store = useNotificationsStore()
const { withSpinner, report } = useActionFeedback()

const applyPreset = async (preset: NotificationQueryPreset, user?: string) => {
  report(await withSpinner(() => store.applyPreset(preset, user)))
}

// Refresh runs what is highlighted: focusing the user field can highlight user
// search before any search has run, and re-running the previous preset under
// that highlight would show one query while claiming another.
const refresh = async () => {
  if (mode.value === 'userSearch') {
    runUserSearch()
    return
  }
  report(await withSpinner(() => store.load()))
}

const baseHref = computed<string>(() => menuStore.mainMenu.baseHref)

const presetOptions: QueryModeOption[] = [
  { label: 'Your outstanding notifications', value: 'yourOutstanding' },
  { label: 'Outstanding for anyone but you', value: 'teamOutstanding' },
  { label: 'All outstanding notifications', value: 'allOutstanding' },
  { label: 'All acknowledged notifications', value: 'allAcknowledged' }
]
const userSearchOptions: QueryModeOption[] = [{ label: 'Notifications for user:', value: 'userSearch' }]
const notificationIdOptions: QueryModeOption[] = [{ label: 'View details for ID:', value: 'notificationId' }]

const mode = ref<QueryMode>(store.preset)
const userSearch = ref(store.preset === 'userSearch' ? store.userFilter ?? '' : '')
const notificationIdSearch = ref('')

const userSearchInput = ref<InstanceType<typeof OnmsSearchInput> | null>(null)
const notificationIdInput = ref<InstanceType<typeof OnmsSearchInput> | null>(null)

// Follow preset changes made elsewhere (e.g. the top-bar bell deep link).
watch(() => store.preset, (preset) => {
  mode.value = preset
})

const onModeChange = (value: unknown) => {
  mode.value = value as QueryMode

  if (mode.value === 'userSearch') {
    // the search itself runs from the button's click handler (runUserSearch)
    userSearchInput.value?.focus()
  } else if (mode.value === 'notificationId') {
    notificationIdInput.value?.focus()
  } else {
    applyPreset(mode.value)
  }
}

// Clicking or tabbing into a search field selects its button. It only selects:
// a user search already typed there runs on Enter, not on focus.
const selectMode = (value: QueryMode) => {
  mode.value = value
}

// Run user search for whatever is typed; with nothing typed, enter the
// awaiting-user state (same as Clear), so the table and its title match the
// highlight instead of still showing the previous preset. Bound to the button's
// native click rather than its selection change: a button already highlighted
// by focusing its field emits no selection change when clicked. (A keyboard
// Enter/Space on the button is a native click too.)
const runUserSearch = () => {
  mode.value = 'userSearch'
  applyPreset('userSearch', userSearch.value.trim() || undefined)
}


// Clearing keeps user search selected and waits for a new ID, rather than
// leaving the previous user's results on screen.
const clearUserSearch = () => {
  applyPreset('userSearch')
}

const goToNotification = () => {
  const notification = notificationIdSearch.value.trim()
  if (notification) {
    window.location.href = `${baseHref.value}notification/detail.jsp?notice=${encodeURIComponent(notification)}`
  }
}
</script>

<style lang="scss" scoped>
.notification-queries-bar {
  padding: 12px 25px;
}

// Sized to its widest row (normally the presets); the search row wraps within
// that width.
.query-controls {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  width: fit-content;
  max-width: 100%;
}

.query-presets {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.query-searches {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-start;
  gap: 0.75rem 1.5rem;
}

.query-search {
  display: flex;
  align-items: center;
  gap: 0.4rem;

  // User and notification IDs are short; ~20 characters plus the search and
  // clear glyphs is plenty.
  .query-search-input {
    width: calc(20ch + 5.5rem);
    min-width: 10rem;
  }
}
</style>
