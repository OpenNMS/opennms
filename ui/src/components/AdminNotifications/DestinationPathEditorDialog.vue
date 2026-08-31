<template>
  <OnmsDialog
    :visible="visible"
    modal
    :header="isEditing ? `Edit Destination Path: ${originalName}` : 'New Destination Path'"
    class="destination-path-editor-dialog"
    width="min(900px, 95vw)"
    data-test="destination-path-editor-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div class="editor-body">
      <div class="form-row">
        <FormField
          label="Path Name"
          for="path-name"
          required
        >
          <OnmsInputText
            id="path-name"
            v-model="name"
            data-test="path-name-input"
          />
        </FormField>
        <FormField
          label="Initial Delay"
          for="path-initial-delay"
          :error="initialDelayError"
        >
          <template #label-suffix>
            <HelpBadge :content="delayHelp" ariaLabel="Initial Delay help" />
          </template>
          <OnmsSelect
            v-model="initialDelay"
            inputId="path-initial-delay"
            :options="delayOptions"
            editable
            data-test="initial-delay-select"
          />
        </FormField>
      </div>

      <div class="section">
        <div class="section-header">
          <div class="section-title">Initial Targets</div>
          <OnmsButton
            variant="outlined"
            size="small"
            label="Add Target"
            icon="pi pi-plus"
            data-test="add-target-button"
            @click="addTarget(targets)"
          />
        </div>
        <p class="section-hint">
          Who gets notified first, and how. Each target is notified via the selected methods;
          Email and Browser (on-screen, for logged-in users) methods are fully supported today —
          the remaining methods are shown for completeness and will be enabled as their
          configuration screens land.
        </p>
        <TargetRowEditor
          v-for="(row, index) in targets"
          :key="row.key"
          :row="row"
          :users="store.users"
          :groups="store.groups"
          :roles="store.roles"
          :methodOptions="methodOptions"
          :removable="targets.length > 1"
          @remove="targets.splice(index, 1)"
        />
      </div>

      <div class="section">
        <div class="section-header">
          <div class="section-title">Escalations</div>
          <OnmsButton
            variant="outlined"
            size="small"
            label="Add Escalation"
            icon="pi pi-plus"
            data-test="add-escalation-button"
            @click="addEscalation"
          />
        </div>
        <p class="section-hint">
          If the notice is still unacknowledged after the delay, the escalation's targets are
          notified next.
        </p>
        <div
          v-for="(escalation, eIndex) in escalations"
          :key="escalation.key"
          class="escalation-block"
        >
          <div class="escalation-header">
            <FormField
              label="Escalate After"
              :for="`escalation-delay-${eIndex}`"
              :error="escalationDelayError(escalation.delay)"
            >
              <template #label-suffix>
                <HelpBadge :content="escalateHelp" ariaLabel="Escalate After help" />
              </template>
              <OnmsSelect
                v-model="escalation.delay"
                :inputId="`escalation-delay-${eIndex}`"
                :options="delayOptions"
                editable
                data-test="escalation-delay-select"
              />
            </FormField>
            <div class="escalation-actions">
              <OnmsButton
                variant="outlined"
                size="small"
                label="Add Target"
                icon="pi pi-plus"
                data-test="add-escalation-target-button"
                @click="addTarget(escalation.targets)"
              />
              <OnmsButton
                variant="text"
                size="small"
                label="Remove Escalation"
                severity="danger"
                data-test="remove-escalation-button"
                @click="escalations.splice(eIndex, 1)"
              />
            </div>
          </div>
          <TargetRowEditor
            v-for="(row, tIndex) in escalation.targets"
            :key="row.key"
            :row="row"
            :users="store.users"
            :groups="store.groups"
            :roles="store.roles"
            :methodOptions="methodOptions"
            :removable="escalation.targets.length > 1"
            @remove="escalation.targets.splice(tIndex, 1)"
          />
        </div>
      </div>
    </div>

    <template #footer>
      <OnmsButton
        variant="text"
        label="Cancel"
        data-test="cancel-button"
        @click="emit('update:visible', false)"
      />
      <OnmsButton
        :label="isEditing ? 'Save Path' : 'Add Path'"
        :disabled="!isValid || saving"
        data-test="save-button"
        @click="save"
      />
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import { OnmsButton, OnmsDialog, OnmsInputText, OnmsSelect } from '@opennms/onms-ui'

import TargetRowEditor, { MethodOption, TargetRow } from '@/components/AdminNotifications/TargetRowEditor.vue'
import FormField from '@/components/Common/FormField.vue'
import HelpBadge from '@/components/Common/HelpBadge.vue'
import { NOTIFD_DURATION_HINT, isValidNotifdDuration } from '@/lib/adminValidation'
import { useNotificationConfigStore } from '@/stores/notificationConfigStore'
import { DestinationPath, DestinationPathTarget } from '@/types/notificationConfig'

const props = defineProps<{
  visible: boolean
  path: DestinationPath | null
}>()

const emit = defineEmits(['update:visible'])

const store = useNotificationConfigStore()

// Methods with a working configuration today. The rest of the commands from
// notificationCommands.xml are listed but disabled until their config lands.
const ENABLED_METHODS = new Set(['javaEmail', 'javaPagerEmail', 'browser'])

// Methods deliberately hidden from the picker (no supported config path). A path
// being edited that already carries one still shows it (originalLegacyCommands).
const HIDDEN_METHODS = new Set([
  'ircCat', 'microblogDM', 'microblogReply', 'microblogUpdate', 'xmppMessage', 'xmppGroupMessage'
])

const delayHelp = 'How long to wait after the notice is created before the first targets are notified (e.g. 30s, 5m, 1h). 0s notifies immediately.'
const escalateHelp = 'If the notice is still unacknowledged after this delay, this escalation’s targets are notified next.'

const METHOD_LABELS: Record<string, string> = {
  javaEmail: 'Email',
  javaPagerEmail: 'Pager Email',
  textPage: 'Text Page',
  numericPage: 'Numeric Page',
  xmppMessage: 'XMPP Message',
  xmppGroupMessage: 'XMPP Group Message',
  microblogUpdate: 'Microblog Update',
  microblogReply: 'Microblog Reply',
  microblogDM: 'Microblog DM',
  ircCat: 'IRC',
  callWorkPhone: 'Call Work Phone',
  callMobilePhone: 'Call Mobile Phone',
  callHomePhone: 'Call Home Phone',
  browser: 'Browser'
}

const delayOptions = ['0s', '30s', '1m', '2m', '5m', '10m', '15m', '30m', '1h', '2h', '6h', '12h', '1d']

interface EscalationBlock {
  key: number
  delay: string
  targets: TargetRow[]
}

let rowKey = 0

const name = ref('')
const initialDelay = ref('30s')
const targets = ref<TargetRow[]>([])
const escalations = ref<EscalationBlock[]>([])
const saving = ref(false)

// Legacy commands the edited path already used (e.g. textPage) that are not in
// ENABLED_METHODS. They stay selectable for this edit so removing one is not a
// one-way trip; brand-new paths cannot add them.
const originalLegacyCommands = ref<Set<string>>(new Set())

const isEditing = computed(() => props.path !== null)
const originalName = computed(() => props.path?.name ?? '')

const methodOptions = computed<MethodOption[]>(() => {
  const commands = store.commands.length ? store.commands.map(c => c.name) : Array.from(ENABLED_METHODS)
  return commands
    .filter(command => !HIDDEN_METHODS.has(command) || originalLegacyCommands.value.has(command))
    .map(command => ({
      value: command,
      label: METHOD_LABELS[command] ? `${METHOD_LABELS[command]} (${command})` : command,
      disabled: !ENABLED_METHODS.has(command) && !originalLegacyCommands.value.has(command)
    }))
})

const newTargetRow = (): TargetRow => ({
  key: rowKey++,
  type: 'user',
  name: '',
  commands: ['javaEmail'],
  interval: undefined,
  autoNotify: undefined
})

const addTarget = (list: TargetRow[]) => {
  list.push(newTargetRow())
}

const addEscalation = () => {
  escalations.value.push({ key: rowKey++, delay: '15m', targets: [newTargetRow()] })
}

const inferType = (targetName: string): TargetRow['type'] => {
  if (targetName.includes('@')) {
    return 'email'
  }
  if (store.groups.includes(targetName)) {
    return 'group'
  }
  if (store.roles.includes(targetName)) {
    return 'role'
  }
  return 'user'
}

const toRow = (target: DestinationPathTarget): TargetRow => ({
  key: rowKey++,
  type: inferType(target.name),
  name: target.name,
  commands: [...(target.command ?? [])],
  interval: target.interval ?? undefined,
  autoNotify: target.autoNotify ?? undefined
})

watch(
  () => props.visible,
  (isVisible) => {
    if (!isVisible) {
      return
    }
    if (props.path) {
      name.value = props.path.name
      initialDelay.value = props.path['initial-delay'] ?? '0s'
      targets.value = (props.path.target ?? []).map(toRow)
      escalations.value = (props.path.escalate ?? []).map(esc => ({
        key: rowKey++,
        delay: esc.delay ?? '15m',
        targets: (esc.target ?? []).map(toRow)
      }))
      // remember which non-standard commands this path already carried so they
      // remain re-addable while editing
      const legacy = new Set<string>()
      const collect = (rows: TargetRow[]) => rows.forEach(r => r.commands.forEach((c) => {
        if (!ENABLED_METHODS.has(c)) {
          legacy.add(c)
        }
      }))
      collect(targets.value)
      escalations.value.forEach(esc => collect(esc.targets))
      originalLegacyCommands.value = legacy
      if (!targets.value.length) {
        targets.value = [newTargetRow()]
      }
    } else {
      name.value = ''
      initialDelay.value = '30s'
      targets.value = [newTargetRow()]
      escalations.value = []
      originalLegacyCommands.value = new Set()
    }
  }
)

const rowIsValid = (row: TargetRow) =>
  !!row.name.trim() && row.commands.length > 0 && isValidNotifdDuration(row.interval)

// blank is allowed (server skips it); anything present must parse
const initialDelayError = computed(() =>
  isValidNotifdDuration(initialDelay.value) ? undefined : NOTIFD_DURATION_HINT)

const escalationDelayError = (delay: string) =>
  isValidNotifdDuration(delay) ? undefined : NOTIFD_DURATION_HINT

const isValid = computed(() => {
  if (!name.value.trim() || !targets.value.length) {
    return false
  }
  if (!isValidNotifdDuration(initialDelay.value)) {
    return false
  }
  if (!targets.value.every(rowIsValid)) {
    return false
  }
  return escalations.value.every(esc =>
    isValidNotifdDuration(esc.delay) && esc.targets.length > 0 && esc.targets.every(rowIsValid))
})

const toTarget = (row: TargetRow): DestinationPathTarget => ({
  name: row.name.trim(),
  command: [...row.commands],
  ...(row.interval ? { interval: row.interval } : {}),
  ...(row.autoNotify ? { autoNotify: row.autoNotify } : {})
})

const save = async () => {
  saving.value = true
  try {
    const path: DestinationPath = {
      name: name.value.trim(),
      'initial-delay': initialDelay.value,
      target: targets.value.map(toTarget),
      escalate: escalations.value.map(esc => ({
        delay: esc.delay,
        target: esc.targets.map(toTarget)
      }))
    }
    const ok = isEditing.value
      ? await store.updateDestinationPath(originalName.value, path)
      : await store.addDestinationPath(path)
    if (ok) {
      emit('update:visible', false)
    }
  } finally {
    saving.value = false
  }
}
</script>

<style lang="scss" scoped>
.editor-body {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  padding-top: 0.5rem;
}

.form-row {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;

  :deep(input),
  :deep(.p-select) {
    min-width: 220px;
  }
}

.section {
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 0.25rem;

    .section-title {
      font-size: 1rem;
      font-weight: 600;
    }
  }

  .section-hint {
    margin: 0 0 0.75rem 0;
    font-size: 0.875rem;
    color: var(--p-text-muted-color);
    max-width: 80ch;
  }
}

.escalation-block {
  border: 1px solid var(--p-content-border-color);
  border-radius: 6px;
  padding: 0.75rem;
  margin-bottom: 0.75rem;

  .escalation-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 1rem;
    margin-bottom: 0.75rem;
    flex-wrap: wrap;

    :deep(.p-select) {
      min-width: 160px;
    }

    .escalation-actions {
      display: flex;
      gap: 0.5rem;
    }
  }
}
</style>
