<template>
  <TableCard
    class="oncall-role-editor-panel"
    data-test="oncall-role-editor-panel"
  >
    <div class="panel-header">
      <OnmsButton
        variant="text"
        class="back-button"
        data-test="back-button"
        @click="emit('close')"
      >
        <OnmsIcon :icon="ArrowBack" />
        Back
      </OnmsButton>
      <h2 class="card-title">{{ isEditing ? `Edit On-Call Role: ${originalName}` : 'Add New On-Call Role' }}</h2>
    </div>
    <div class="editor-body">
      <div
        v-if="errorText"
        class="error-banner"
        role="alert"
        data-test="save-error"
      >{{ errorText }}</div>
      <OnmsTabs v-model:value="activeTab" data-test="oncall-role-editor-tabs">
        <OnmsTabList>
          <OnmsTab value="details" data-test="tab-details">
            Details<TabProblemMarker v-if="detailsTabProblem" />
          </OnmsTab>
          <OnmsTab value="schedules" data-test="tab-schedules">
            Schedules<TabProblemMarker v-if="schedulesProblem" />
          </OnmsTab>
          <OnmsTab value="calendar" data-test="tab-calendar">Calendar</OnmsTab>
        </OnmsTabList>
        <OnmsTabPanels>
          <OnmsTabPanel value="details">
            <div class="form-column">
              <FormField
                v-if="!isEditing"
                label="Name"
                for="oncall-role-name"
                required
                :error="nameProblem || undefined"
              >
                <OnmsInputText
                  id="oncall-role-name"
                  v-model="name"
                  :invalid="!!nameProblem"
                  data-test="role-name-input"
                />
              </FormField>
              <FormField
                label="Description"
                for="oncall-role-description"
                :error="descriptionProblem || undefined"
              >
                <OnmsTextarea
                  id="oncall-role-description"
                  v-model="description"
                  :rows="3"
                  autoResize
                  fluid
                  :invalid="!!descriptionProblem"
                  data-test="role-description-input"
                />
              </FormField>
              <FormField
                label="Supervisor"
                for="oncall-role-supervisor"
                required
                hint="Covers the times nobody is scheduled."
              >
                <OnmsSelect
                  v-model="supervisor"
                  inputId="oncall-role-supervisor"
                  :options="supervisorOptions"
                  optionLabel="label"
                  optionValue="value"
                  filter
                  fluid
                  data-test="supervisor-select"
                />
              </FormField>
              <FormField
                label="Membership Group"
                for="oncall-role-group"
                required
                hint="The group whose members the schedules can name."
              >
                <OnmsSelect
                  v-model="membershipGroup"
                  inputId="oncall-role-group"
                  :options="groupOptions"
                  filter
                  fluid
                  data-test="group-select"
                />
              </FormField>
              <FormField
                v-if="isEditing"
                label="On call now"
              >
                <div data-test="oncall-now">{{ onCallNowText }}</div>
              </FormField>
            </div>
          </OnmsTabPanel>
          <OnmsTabPanel value="schedules">
            <OnCallSchedulesTab
              v-model:cards="cards"
              :members="members"
              :highlightKey="highlightKey"
              @highlighted="highlightKey = null"
            />
          </OnmsTabPanel>
          <OnmsTabPanel value="calendar">
            <!-- mounted only while shown, so that the preview is not requested for every edit elsewhere -->
            <OnCallCalendarTab
              v-if="activeTab === 'calendar'"
              :draft="calendarDraft"
              :cards="cards"
              :members="members"
              :timeZone="store.scheduleSettings?.timeZone ?? ''"
              @add-entry="addEntry"
              @show-schedule="showSchedule"
            />
          </OnmsTabPanel>
        </OnmsTabPanels>
      </OnmsTabs>
    </div>
    <div class="panel-footer">
      <OnmsButton
        variant="text"
        label="Cancel"
        data-test="cancel-button"
        @click="emit('close')"
      />
      <OnmsButton
        :label="isEditing ? 'Save On-Call Role' : 'Add On-Call Role'"
        :disabled="!isValid || saving"
        data-test="save-button"
        @click="save"
      />
    </div>
  </TableCard>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import {
  OnmsButton,
  OnmsIcon,
  OnmsInputText,
  OnmsSelect,
  OnmsTab,
  OnmsTabList,
  OnmsTabPanel,
  OnmsTabPanels,
  OnmsTabs,
  OnmsTextarea
} from '@opennms/onms-ui'

import FormField from '@/components/Common/FormField.vue'
import TabProblemMarker from '@/components/Common/TabProblemMarker.vue'
import TableCard from '@/components/Common/TableCard.vue'
import OnCallCalendarTab from '@/components/OnCallRoles/OnCallCalendarTab.vue'
import OnCallSchedulesTab from '@/components/OnCallRoles/OnCallSchedulesTab.vue'
import ArrowBack from '@opennms/onms-ui/icons/navigation/ArrowBack.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import useAdminWriteFeedback from '@/composables/useAdminWriteFeedback'
import { FREE_TEXT_LENGTH, validateOnCallRoleName } from '@/lib/adminValidation'
import { cardProblems, newCard, ScheduleCard, TimeRange, toCard, toSchedule } from '@/lib/onCallSchedule'
import { useOnCallRoleAdminStore } from '@/stores/onCallRoleAdminStore'
import { ManagedOnCallRole, ManagedOnCallRoleCreate } from '@/types/onCallRoleAdmin'

// Rendered in place of the table (see ManageOnCallRoles); `role` is null when creating.
// Emits `close` on Back, Cancel, a successful save, or a save rejected as stale.
const props = defineProps<{
  role: ManagedOnCallRole | null
}>()

const emit = defineEmits<{
  close: []
}>()

const store = useOnCallRoleAdminStore()
const { withSpinner } = useActionFeedback()
const { reportWrite } = useAdminWriteFeedback()

const name = ref('')
const description = ref('')
const supervisor = ref('')
const membershipGroup = ref('')
// the schedule cards of the draft; empty until the schedule settings are loaded
const cards = ref<ScheduleCard[]>([])
const cardsReady = ref(false)
// the card the Schedules tab scrolls to and marks; set by the calendar preview
const highlightKey = ref<string | null>(null)
const saving = ref(false)
const errorText = ref('')

type EditorTab = 'details' | 'schedules' | 'calendar'
const activeTab = ref<EditorTab>('details')

const isEditing = computed(() => props.role !== null)
const originalName = computed(() => props.role?.name ?? '')

const nameProblem = computed(() => (isEditing.value ? null : validateOnCallRoleName(name.value)))

// a stored description that predates the limit stays as it is: the server grandfathers it unchanged
const descriptionUnchanged = computed(() => description.value === (props.role?.description ?? ''))
const descriptionProblem = computed(() =>
  !descriptionUnchanged.value && Array.from(description.value.trim()).length > FREE_TEXT_LENGTH
    ? `The description must not be longer than ${FREE_TEXT_LENGTH} characters.`
    : null)

// a stored supervisor or group the lists lack is still offered, so that it shows as chosen
const supervisorOptions = computed(() => {
  const options = store.users.map(u => ({
    value: u.userId,
    label: u.fullName ? `${u.userId} (${u.fullName})` : u.userId
  }))
  if (supervisor.value && !options.some(o => o.value === supervisor.value)) {
    options.push({ value: supervisor.value, label: supervisor.value })
  }
  return options
})

const groupOptions = computed(() => {
  const names = store.groups.map(g => g.name)
  return membershipGroup.value && !names.includes(membershipGroup.value) ? [...names, membershipGroup.value] : names
})

const onCallNowText = computed(() => {
  const now = props.role?.onCallNow ?? []
  if (!now.length) {
    return 'Nobody'
  }
  const text = now.join(', ')
  return props.role?.onCallNowIsSupervisor ? `${text} (supervisor, nobody scheduled)` : text
})

const members = computed(() => store.groupMembers(membershipGroup.value))

const supervisorMissing = computed(() => !supervisor.value)
const groupMissing = computed(() => !membershipGroup.value)
const detailsTabProblem = computed(() =>
  !!(nameProblem.value || descriptionProblem.value || supervisorMissing.value || groupMissing.value))
const schedulesProblem = computed(() => cards.value.some(c => cardProblems(c).length > 0))

const isValid = computed(() =>
  cardsReady.value && !detailsTabProblem.value && !schedulesProblem.value && (isEditing.value || !!name.value))

// a specific schedule can only be read once the month names are known; before that it would turn legacy
const buildCards = () => {
  const settings = store.scheduleSettings
  if (!settings) {
    return
  }
  cards.value = (props.role?.schedules ?? []).map(s => toCard(s, settings.shortMonths))
  cardsReady.value = true
}

watch(
  () => props.role,
  () => {
    errorText.value = ''
    activeTab.value = 'details'
    cards.value = []
    cardsReady.value = false
    name.value = ''
    description.value = props.role?.description ?? ''
    supervisor.value = props.role?.supervisor ?? ''
    membershipGroup.value = props.role?.membershipGroup ?? ''
    buildCards()
  },
  { immediate: true }
)

watch(
  () => store.scheduleSettings,
  () => {
    if (!cardsReady.value) {
      buildCards()
    }
  }
)

// The role as the draft stands, schedules in card order. Call it only while no card has problems:
// a card with malformed values cannot be serialized.
const draftRole = (): ManagedOnCallRoleCreate => {
  const shortMonths = store.scheduleSettings?.shortMonths ?? []
  return {
    name: isEditing.value ? originalName.value : name.value,
    description: descriptionUnchanged.value && props.role ? (props.role.description ?? '') : description.value.trim(),
    membershipGroup: membershipGroup.value,
    supervisor: supervisor.value,
    schedules: cards.value.map(c => toSchedule(c, shortMonths))
  }
}

// the draft for the calendar preview; null while a schedule has problems, as it cannot be serialized then
const calendarDraft = computed(() => cardsReady.value && !schedulesProblem.value ? draftRole() : null)

// A time added from the calendar goes to a card added in this session for that user, never to a stored one.
const addEntry = (entry: { user: string, start: string, end: string, endOfDay: boolean }) => {
  const range: TimeRange = { id: null, day: null, begin: entry.start, end: entry.end, endOfDay: entry.endOfDay, original: null }
  const existing = cards.value.findIndex(c => c.addedThisSession && c.type === 'specific' && c.user === entry.user)
  if (existing >= 0) {
    cards.value = cards.value.map((c, i) => i === existing ? { ...c, times: [...c.times, range], dirty: true } : c)
  } else {
    cards.value = [...cards.value, { ...newCard(entry.user, 'specific'), addedThisSession: true, times: [range] }]
  }
}

const showSchedule = (index: number) => {
  const card = cards.value[index]
  if (card) {
    activeTab.value = 'schedules'
    highlightKey.value = card.key
  }
}

defineExpose({ draftRole, cards })

const save = async () => {
  saving.value = true
  try {
    const fields = draftRole()
    // the stored role without the computed fields, so anything else it carries is kept
    const { onCallNow: _now, onCallNowIsSupervisor: _isSupervisor, ...stored } = props.role ?? {} as Partial<ManagedOnCallRole>
    const result = await withSpinner(() => props.role
      ? store.updateRole({ ...stored, ...fields, version: props.role.version })
      : store.createRole(fields))
    reportWrite(result, props.role ? `On-call role '${fields.name}' updated.` : `On-call role '${fields.name}' created.`, {
      close: () => emit('close'),
      banner: (msg) => {
        errorText.value = msg
      }
    })
  } finally {
    saving.value = false
  }
}
</script>

<style lang="scss" scoped>
.oncall-role-editor-panel {
  padding: 25px;
}

.panel-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;

  .back-button {
    padding-left: 0;
  }

  .card-title {
    font-size: 1.1rem;
    font-weight: 600;
    margin: 0;
  }
}

.editor-body,
.panel-footer {
  max-width: 1040px;
}

.panel-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 1.5rem;
}

.error-banner {
  margin-bottom: 1rem;
  padding: 0.6rem 0.85rem;
  border: 1px solid var(--p-red-400, #e57373);
  border-radius: 4px;
  color: var(--p-red-400, #e57373);
  background: color-mix(in srgb, var(--p-red-400, #e57373) 12%, transparent);
}

.form-column {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding-top: 0.5rem;

  :deep(input),
  :deep(textarea),
  :deep(.p-multiselect) {
    width: 100%;
  }
}
</style>
