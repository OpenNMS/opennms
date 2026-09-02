<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>

  <div class="outage-editor">
    <OnmsCard>
      <template #content>
      <div class="page-header">
        <OnmsButton variant="text" class="back-button" data-test="back-button" @click="goBack">
          <OnmsIcon :icon="ArrowBack" />
          Go Back
        </OnmsButton>
        <h2 class="headline3" data-test="editor-title">{{ isNew ? 'New Scheduled Outage' : 'Edit Scheduled Outage' }}: {{ name }}</h2>
      </div>

      <div v-if="loading" data-test="editor-loading">Loading…</div>

      <template v-else>
        <p v-if="errorMessage" class="error" data-test="editor-error">{{ errorMessage }}</p>

        <div v-if="loadFailed" class="load-failed" data-test="editor-load-failed">
          <OnmsButton variant="outlined" label="Back to Scheduled Outages" @click="goBack" />
        </div>

        <div v-else class="editor-grid">
          <section class="selection">
            <h3 class="section-title">Nodes and Interfaces</h3>
            <div class="pickers">
              <NodeInterfacePicker mode="node" :items="outage.node ?? []" :nodeLabels="nodeLabels" :matchAny="isMatchAny" @add="addNode" @remove="removeNode" />
              <NodeInterfacePicker mode="interface" :items="outage.interface ?? []" :matchAny="isMatchAny" @add="addInterface" @remove="removeInterface" />
            </div>
            <div class="match-any-row">
              <OnmsButton
                variant="outlined"
                label="Select all nodes and interfaces"
                data-test="match-any"
                @click="selectAll"
              />
            </div>
            <p v-if="showSelectionError" class="field-error" data-test="selection-error">
              You must select at least one node or interface for this scheduled outage.
            </p>
          </section>

          <section class="schedule">
            <FormField label="Outage Type" for="outage-type">
              <OnmsSelect
                v-model="outage.type"
                inputId="outage-type"
                :options="OUTAGE_TYPES"
                optionLabel="label"
                optionValue="value"
                data-test="outage-type"
                @change="onTypeChange"
              />
            </FormField>

            <TimeSpanEditor
              v-if="outage.type"
              :type="outage.type"
              :times="outage.time ?? []"
              @add="addTime"
              @remove="removeTime"
            />
            <p v-if="showTimeError" class="field-error" data-test="time-error">
              You must have at least one time span defined.
            </p>
            <p v-if="timeSpanNote" class="field-note" data-test="time-note">{{ timeSpanNote }}</p>
          </section>

          <section class="applies">
            <p v-if="appliesFailed" class="error" data-test="applies-error">
              Failed to load which subsystems this outage applies to. Its memberships are left unchanged when you save; reload the page to try again.
            </p>
            <AppliesToMatrix
              v-else
              :notifications="applicability.notifications"
              :pollers="applicability.pollers"
              :thresholders="applicability.thresholders"
              :collectors="applicability.collectors"
              @update:notifications="applicability.notifications = $event"
              @togglePackage="togglePackage"
              @setAll="setAll"
            />
          </section>
        </div>

        <div v-if="!loadFailed" class="actions">
          <OnmsButton :label="isNew ? 'Create' : 'Save'" data-test="save" :disabled="saving" @click="save" />
          <OnmsButton variant="text" label="Cancel" data-test="cancel-bottom" @click="goBack" />
        </div>
      </template>
      </template>
    </OnmsCard>

    <OnmsConfirmationDialog
      :visible="showSelectAllConfirm"
      title="Apply to All Nodes and Interfaces"
      actionButtonText="Apply to all"
      @ok="confirmSelectAll"
      @cancel="showSelectAllConfirm = false"
    >
      <template #content>
        <p>This clears the specific nodes and interfaces you selected and applies the outage everywhere. Continue?</p>
      </template>
    </OnmsConfirmationDialog>

    <OnmsConfirmationDialog
      :visible="showTypeChangeConfirm"
      title="Change Outage Type"
      actionButtonText="Change type"
      @ok="confirmTypeChange"
      @cancel="cancelTypeChange"
    >
      <template #content>
        <p>Time spans are entered per outage type, so changing the type clears the time spans you already added. Continue?</p>
      </template>
    </OnmsConfirmationDialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { OnmsButton, OnmsCard, OnmsConfirmationDialog, OnmsIcon, OnmsSelect } from '@opennms/onms-ui'
import ArrowBack from '@opennms/onms-ui/icons/navigation/ArrowBack.vue'

import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import FormField from '@/components/Common/FormField.vue'
import AppliesToMatrix, { Subsystem } from '@/components/ScheduledOutages/AppliesToMatrix.vue'
import NodeInterfacePicker from '@/components/ScheduledOutages/NodeInterfacePicker.vue'
import TimeSpanEditor from '@/components/ScheduledOutages/TimeSpanEditor.vue'
import {
  getNodeLabels,
  getOutageApplicability,
  getScheduledOutage,
  saveScheduledOutage,
  scheduledOutageErrorMessage,
  setNotificationMembership,
  setPackageMembership
} from '@/services/scheduledOutagesService'
import {
  OutageApplicability,
  OutageInterface,
  OutageNode,
  OutageTime,
  OutageType,
  PackageRef,
  ScheduledOutage
} from '@/types/scheduledOutage'
import { BreadCrumb } from '@/types'

const OUTAGE_TYPES = [
  { label: 'Specific', value: 'specific' },
  { label: 'Daily', value: 'daily' },
  { label: 'Weekly', value: 'weekly' },
  { label: 'Monthly', value: 'monthly' }
]

const MATCH_ANY = 'match-any'

const route = useRoute()
const router = useRouter()

const name = String(route.query.name ?? '')
const isNew = route.query.new === 'true'

const breadcrumbs: BreadCrumb[] = [
  { label: 'Home', to: '/' },
  { label: 'Scheduled Outages', to: '/scheduled-outages' },
  { label: name, to: route.fullPath }
]

const loading = ref(true)
// the outage exists but could not be read; saving would overwrite it blind
const loadFailed = ref(false)
const saving = ref(false)
const submitted = ref(false)
const errorMessage = ref('')
const timeSpanNote = ref('')
const showSelectAllConfirm = ref(false)
const showTypeChangeConfirm = ref(false)
// node id -> label for the picker chips; missing entries render as not-found
const nodeLabels = reactive<Record<number, string>>({})
// the type currently reflected by outage.time, so a type change can warn before
// discarding spans that were entered under the previous type
const lastType = ref<OutageType | undefined>(isNew ? 'specific' : undefined)

const outage = reactive<ScheduledOutage>({
  name,
  // a new outage starts on a usable form: type pre-selected, spans addable
  type: isNew ? 'specific' : undefined,
  time: [],
  node: [],
  interface: []
})

const applicability = reactive<OutageApplicability>({
  notifications: false,
  notificationCalendars: [],
  pollers: [],
  thresholders: [],
  collectors: []
})

// snapshot of the loaded membership, to only PUT/DELETE what actually changed
let originalApplicability: OutageApplicability | null = null
// the applies-to read failed: an empty matrix would read as "nothing applied"
const appliesFailed = ref(false)

const isMatchAny = computed(() =>
  (outage.interface ?? []).some(i => i.address === MATCH_ANY)
)

const hasSelection = computed(() =>
  (outage.node ?? []).length > 0 || (outage.interface ?? []).length > 0
)

const hasTimes = computed(() => (outage.time ?? []).length > 0)

const showSelectionError = computed(() => submitted.value && !hasSelection.value)
const showTimeError = computed(() => submitted.value && !hasTimes.value)

onMounted(async () => {
  if (!name) {
    router.replace({ path: '/scheduled-outages' })
    return
  }
  if (!isNew) {
    const loaded = await getScheduledOutage(name)
    if (loaded) {
      outage.type = loaded.type
      outage.time = loaded.time ?? []
      outage.node = loaded.node ?? []
      outage.interface = loaded.interface ?? []
      lastType.value = loaded.type
      await resolveNodeLabels()
    } else {
      // Distinguish "could not read the existing outage" from a new one: with
      // an empty form, Save would POST a whole-object replace and wipe it.
      loadFailed.value = true
      errorMessage.value = `Failed to load scheduled outage "${name}". It may have been deleted, or the server did not respond. Editing is disabled so the existing configuration is not overwritten.`
      loading.value = false
      return
    }
  }
  await loadApplicability(isNew ? undefined : name)
  loading.value = false
})

const loadApplicability = async (outageName?: string) => {
  const appl = await getOutageApplicability(outageName)
  appliesFailed.value = appl === null
  if (appl) {
    applicability.notifications = appl.notifications
    applicability.pollers = appl.pollers
    applicability.thresholders = appl.thresholders
    applicability.collectors = appl.collectors
    originalApplicability = clone(appl)
  }
}

const resolveNodeLabels = async () => {
  const labels = await getNodeLabels((outage.node ?? []).map(n => n.id))
  Object.assign(nodeLabels, labels)
}

const addNode = (value: OutageNode | OutageInterface, label?: string) => {
  const node = value as OutageNode
  // a specific node ends "all": match-any would otherwise hide the node in the
  // pickers and still be written next to it on save
  outage.interface = (outage.interface ?? []).filter(i => i.address !== MATCH_ANY)
  if (!(outage.node ?? []).some(n => n.id === node.id)) {
    outage.node = [...(outage.node ?? []), { id: node.id }]
    if (label) {
      nodeLabels[node.id] = label
    }
  }
}
const removeNode = (index: number) => {
  outage.node = (outage.node ?? []).filter((_, i) => i !== index)
}
const addInterface = (value: OutageNode | OutageInterface) => {
  const iface = value as OutageInterface
  // adding a specific interface clears the "all" match-any pseudo-interface
  const existing = (outage.interface ?? []).filter(i => i.address !== MATCH_ANY)
  if (!existing.some(i => i.address === iface.address)) {
    outage.interface = [...existing, iface]
  }
}
const removeInterface = (index: number) => {
  outage.interface = (outage.interface ?? []).filter((_, i) => i !== index)
}

const selectAll = () => {
  // this replaces any curated node/interface list with "all"; confirm first so a
  // stray click can't silently discard an existing selection
  const hasExisting = (outage.node ?? []).length > 0 ||
    (outage.interface ?? []).some(i => i.address !== MATCH_ANY)
  if (hasExisting) {
    showSelectAllConfirm.value = true
    return
  }
  applyMatchAny()
}

const confirmSelectAll = () => {
  showSelectAllConfirm.value = false
  applyMatchAny()
}

const applyMatchAny = () => {
  outage.node = []
  outage.interface = [{ address: MATCH_ANY }]
}

const onTypeChange = () => {
  // begins/ends are formatted per type, so spans entered under the old type no
  // longer apply; confirm before discarding them so a stray change can't lose data
  if ((outage.time ?? []).length > 0 && outage.type !== lastType.value) {
    showTypeChangeConfirm.value = true
    return
  }
  lastType.value = outage.type
  timeSpanNote.value = ''
}

const confirmTypeChange = () => {
  showTypeChangeConfirm.value = false
  outage.time = []
  lastType.value = outage.type
  timeSpanNote.value = ''
}

const cancelTypeChange = () => {
  showTypeChangeConfirm.value = false
  outage.type = lastType.value
}

const addTime = (time: OutageTime) => {
  const exists = (outage.time ?? []).some(
    t => t.begins === time.begins && t.ends === time.ends && (t.day ?? '') === (time.day ?? '')
  )
  if (exists) {
    timeSpanNote.value = 'That time span is already in the list.'
    return
  }
  timeSpanNote.value = ''
  outage.time = [...(outage.time ?? []), time]
}
const removeTime = (index: number) => {
  outage.time = (outage.time ?? []).filter((_, i) => i !== index)
}

const togglePackage = (subsystem: Subsystem, pkgName: string, value: boolean) => {
  const list = listFor(subsystem)
  const pkg = list.find(p => p.name === pkgName)
  if (pkg) {
    pkg.applied = value
  }
}
const setAll = (subsystem: Subsystem, value: boolean) => {
  listFor(subsystem).forEach((p) => {
    p.applied = value
  })
}
const listFor = (subsystem: Subsystem): PackageRef[] =>
  subsystem === 'pollerd' ? applicability.pollers
    : subsystem === 'threshd' ? applicability.thresholders
      : applicability.collectors

const save = async () => {
  submitted.value = true
  errorMessage.value = ''
  if (!outage.type) {
    errorMessage.value = 'Please choose an outage type.'
    return
  }
  if (!hasSelection.value || !hasTimes.value) {
    return
  }
  saving.value = true
  let outageSaved = false
  try {
    await saveScheduledOutage({
      name: outage.name,
      type: outage.type,
      time: outage.time,
      node: outage.node,
      interface: outage.interface
    })
    outageSaved = true
    await applyMembershipChanges()
    goBack()
  } catch (err: any) {
    if (outageSaved) {
      // The outage persisted but a subsystem membership call failed partway.
      // Re-read the actual state so the matrix reflects what really got applied.
      errorMessage.value = scheduledOutageErrorMessage(
        err, 'The outage was saved, but applying some subsystem settings failed. The list below now shows the settings that were actually applied — review and Save again.')
      await reloadApplicability()
    } else {
      errorMessage.value = scheduledOutageErrorMessage(err, 'Failed to save the scheduled outage.')
    }
  } finally {
    saving.value = false
  }
}

const reloadApplicability = () => loadApplicability(outage.name)

// PUT/DELETE only the memberships that differ from what was loaded.
const applyMembershipChanges = async () => {
  if (appliesFailed.value) {
    return
  }
  const original = originalApplicability
  const wasApplied = (subsystem: Subsystem, pkgName: string): boolean => {
    if (!original) {
      return false
    }
    const list = subsystem === 'pollerd' ? original.pollers
      : subsystem === 'threshd' ? original.thresholders : original.collectors
    return !!list.find(p => p.name === pkgName)?.applied
  }
  const subsystems: Subsystem[] = ['pollerd', 'threshd', 'collectd']
  for (const subsystem of subsystems) {
    for (const pkg of listFor(subsystem)) {
      if (pkg.applied !== wasApplied(subsystem, pkg.name)) {
        await setPackageMembership(subsystem, outage.name, pkg.name, pkg.applied)
      }
    }
  }
  if (applicability.notifications !== (original?.notifications ?? false)) {
    await setNotificationMembership(outage.name, applicability.notifications)
  }
}

const goBack = () => {
  router.push({ path: '/scheduled-outages' })
}

const clone = (a: OutageApplicability): OutageApplicability => ({
  notifications: a.notifications,
  notificationCalendars: [...a.notificationCalendars],
  pollers: a.pollers.map(p => ({ ...p })),
  thresholders: a.thresholders.map(p => ({ ...p })),
  collectors: a.collectors.map(p => ({ ...p }))
})
</script>

<style scoped lang="scss">
.outage-editor {
  .page-header {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 0.25rem;
    margin-bottom: 0.75rem;
  }

  .back-button {
    padding-left: 0;
  }

  .editor-grid {
    display: grid;
    grid-template-columns: 2fr 1fr;
    // the applies column spans both rows; without content-sized rows the
    // browser distributes its extra height as blank space above the schedule
    grid-template-rows: min-content 1fr;
    grid-template-areas:
      'selection applies'
      'schedule applies';
    gap: 1.5rem 2rem;
  }

  .selection { grid-area: selection; }
  .schedule { grid-area: schedule; }
  .applies { grid-area: applies; }

  .section-title {
    margin: 0 0 0.75rem 0;
  }

  .pickers {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 1.5rem;
  }

  .match-any-row {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    margin-top: 1rem;
  }

  .field-error,
  .error {
    color: var(--p-red-500, #d32f2f);
    margin: 0.5rem 0 0 0;
  }

  .field-note {
    color: var(--p-text-muted-color);
    margin: 0.5rem 0 0 0;
  }

  .load-failed {
    margin-top: 0.75rem;
  }

  .actions {
    display: flex;
    gap: 0.75rem;
    margin-top: 1.5rem;
  }

  @media (max-width: 64rem) {
    .editor-grid {
      grid-template-columns: minmax(0, 1fr);
      grid-template-areas:
        'selection'
        'schedule'
        'applies';
    }

    .pickers {
      grid-template-columns: minmax(0, 1fr);
    }
  }
}
</style>
