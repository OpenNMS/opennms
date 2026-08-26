<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>

  <div class="outage-editor">
    <OnmsCard>
      <div class="page-header">
        <h2 class="headline3" data-test="editor-title">Editing Outage: {{ name }}</h2>
        <OnmsButton variant="text" label="Cancel" data-test="cancel" @click="goBack" />
      </div>

      <div v-if="loading" data-test="editor-loading">Loading…</div>

      <template v-else>
        <p v-if="errorMessage" class="error" data-test="editor-error">{{ errorMessage }}</p>

        <div class="editor-grid">
          <section class="selection">
            <h3 class="section-title">Nodes and Interfaces</h3>
            <div class="pickers">
              <NodeInterfacePicker mode="node" :items="outage.node ?? []" @add="addNode" @remove="removeNode" />
              <NodeInterfacePicker mode="interface" :items="outage.interface ?? []" @add="addInterface" @remove="removeInterface" />
            </div>
            <div class="match-any-row">
              <OnmsButton
                variant="outlined"
                label="Select all nodes and interfaces"
                data-test="match-any"
                @click="selectAll"
              />
              <span v-if="isMatchAny" class="match-any-note" data-test="match-any-note">
                Applies to all nodes and interfaces.
              </span>
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
          </section>

          <section class="applies">
            <AppliesToMatrix
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

        <div class="actions">
          <OnmsButton label="Save Outage" data-test="save" :disabled="saving" @click="save" />
          <OnmsButton variant="text" label="Cancel" data-test="cancel-bottom" @click="goBack" />
        </div>
      </template>
    </OnmsCard>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { OnmsButton, OnmsCard, OnmsSelect } from '@opennms/onms-ui'

import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import FormField from '@/components/Common/FormField.vue'
import AppliesToMatrix, { Subsystem } from '@/components/ScheduledOutages/AppliesToMatrix.vue'
import NodeInterfacePicker from '@/components/ScheduledOutages/NodeInterfacePicker.vue'
import TimeSpanEditor from '@/components/ScheduledOutages/TimeSpanEditor.vue'
import {
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
  { label: name, to: '' }
]

const loading = ref(true)
const saving = ref(false)
const submitted = ref(false)
const errorMessage = ref('')

const outage = reactive<ScheduledOutage>({
  name,
  type: undefined,
  time: [],
  node: [],
  interface: []
})

const applicability = reactive<OutageApplicability>({
  notifications: false,
  pollers: [],
  thresholders: [],
  collectors: []
})

// snapshot of the loaded membership, to only PUT/DELETE what actually changed
let originalApplicability: OutageApplicability | null = null

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
  if (!isNew) {
    const loaded = await getScheduledOutage(name)
    if (loaded) {
      outage.type = loaded.type
      outage.time = loaded.time ?? []
      outage.node = loaded.node ?? []
      outage.interface = loaded.interface ?? []
    }
  }
  const appl = await getOutageApplicability(isNew ? undefined : name)
  if (appl) {
    applicability.notifications = appl.notifications
    applicability.pollers = appl.pollers
    applicability.thresholders = appl.thresholders
    applicability.collectors = appl.collectors
    originalApplicability = clone(appl)
  }
  loading.value = false
})

const addNode = (value: OutageNode | OutageInterface) => {
  const node = value as OutageNode
  if (!(outage.node ?? []).some(n => n.id === node.id)) {
    outage.node = [...(outage.node ?? []), node]
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
  outage.node = []
  outage.interface = [{ address: MATCH_ANY }]
}

const onTypeChange = () => {
  // begins/ends are formatted per type, so existing spans no longer apply
  outage.time = []
}

const addTime = (time: OutageTime) => {
  const exists = (outage.time ?? []).some(
    t => t.begins === time.begins && t.ends === time.ends && (t.day ?? '') === (time.day ?? '')
  )
  if (!exists) {
    outage.time = [...(outage.time ?? []), time]
  }
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
  try {
    await saveScheduledOutage({
      name: outage.name,
      type: outage.type,
      time: outage.time,
      node: outage.node,
      interface: outage.interface
    })
    await applyMembershipChanges()
    goBack()
  } catch (err: any) {
    errorMessage.value = scheduledOutageErrorMessage(err, 'Failed to save the scheduled outage.')
  } finally {
    saving.value = false
  }
}

// PUT/DELETE only the memberships that differ from what was loaded.
const applyMembershipChanges = async () => {
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
  pollers: a.pollers.map(p => ({ ...p })),
  thresholders: a.thresholders.map(p => ({ ...p })),
  collectors: a.collectors.map(p => ({ ...p }))
})
</script>

<style scoped lang="scss">
.outage-editor {
  .page-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 1rem;
    margin-bottom: 0.5rem;
  }

  .editor-grid {
    display: grid;
    grid-template-columns: 2fr 1fr;
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

  .match-any-note {
    color: var(--p-text-muted-color);
  }

  .field-error,
  .error {
    color: var(--p-red-500, #d32f2f);
    margin: 0.5rem 0 0 0;
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
