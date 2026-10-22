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
        <!-- Not a query: Enter opens that notification's detail page, so this is a
             plain label and takes no part in the selection above. -->
        <div class="query-search">
          <label
            for="notification-id-search"
            class="query-search-label"
            data-test="notification-id-label"
          >View details for ID:</label>
          <OnmsSearchInput
            v-model="notificationIdSearch"
            class="query-search-input"
            inputId="notification-id-search"
            placeholder="Notification ID"
            ariaLabel="Notification ID"
            dataTest="notification-id-search-input"
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

// The preset and user-search select buttons act as one choice: each is bound to
// `mode` and only shows a selection when `mode` is one of its own options.
type QueryMode = NotificationQueryPreset

interface QueryModeOption {
  label: string
  value: QueryMode
}

const menuStore = useMenuStore()
const store = useNotificationsStore()
const { withSpinner, report, showError } = useActionFeedback()

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

const mode = ref<QueryMode>(store.preset)
const userSearch = ref(store.preset === 'userSearch' ? store.userFilter ?? '' : '')
const notificationIdSearch = ref('')

const userSearchInput = ref<InstanceType<typeof OnmsSearchInput> | null>(null)

// Follow preset changes made elsewhere (e.g. the top-bar bell deep link).
watch(() => store.preset, (preset) => {
  mode.value = preset
})

const onModeChange = (value: unknown) => {
  mode.value = value as QueryMode

  if (mode.value === 'userSearch') {
    // the search itself runs from the button's click handler (runUserSearch)
    userSearchInput.value?.focus()
  } else {
    applyPreset(mode.value)
  }
}

// Clicking or tabbing into the user field selects its button. It only selects:
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

// notification ids are positive integers; anything else would only reach the
// legacy detail page as an error (leading zeros are fine: "007" is notice 7)
const isNotificationId = (value: string) => /^\d+$/.test(value) && Number(value) > 0

const goToNotification = () => {
  const id = notificationIdSearch.value.trim()
  if (!id) {
    return
  }
  if (!isNotificationId(id)) {
    showError('Enter a notification ID as a positive whole number, e.g. 42.')
    return
  }
  window.location.href = `${baseHref.value}notification/detail.jsp?notice=${Number(id)}`
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

  // same text as the select-button labels beside it, but plainly not a button
  // (a global label rule would otherwise shrink it to 14px)
  .query-search-label {
    padding: 0 0.5rem;
    font-size: 1rem;
    white-space: nowrap;
  }

  // User and notification IDs are short; ~20 characters plus the search and
  // clear glyphs is plenty.
  .query-search-input {
    width: calc(20ch + 5.5rem);
    min-width: 10rem;
  }
}
</style>
