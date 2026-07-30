<template>
  <Dialog
    :visible="visible"
    modal
    maximizable
    :header="isEditing ? `Edit Destination Path: ${originalName}` : 'New Destination Path'"
    class="destination-path-editor-dialog"
    :style="{ width: '900px', maxWidth: '95vw' }"
    data-test="destination-path-editor-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div class="editor-body">
      <div class="form-row">
        <FormField>
          <IftaLabel>
            <InputText
              id="path-name"
              v-model="name"
              data-test="path-name-input"
            />
            <label for="path-name">Path Name *</label>
          </IftaLabel>
        </FormField>
        <FormField>
          <IftaLabel>
            <Select
              v-model="initialDelay"
              labelId="path-initial-delay"
              :options="delayOptions"
              editable
              data-test="initial-delay-select"
            />
            <label for="path-initial-delay">Initial Delay</label>
          </IftaLabel>
        </FormField>
      </div>

      <div class="section">
        <div class="section-header">
          <div class="section-title">Initial Targets</div>
          <Button
            outlined
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
          <Button
            outlined
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
            <IftaLabel>
              <Select
                v-model="escalation.delay"
                :labelId="`escalation-delay-${eIndex}`"
                :options="delayOptions"
                editable
                data-test="escalation-delay-select"
              />
              <label :for="`escalation-delay-${eIndex}`">Escalate After</label>
            </IftaLabel>
            <div class="escalation-actions">
              <Button
                outlined
                size="small"
                label="Add Target"
                icon="pi pi-plus"
                data-test="add-escalation-target-button"
                @click="addTarget(escalation.targets)"
              />
              <Button
                text
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
      <Button
        text
        label="Cancel"
        data-test="cancel-button"
        @click="emit('update:visible', false)"
      />
      <Button
        :label="isEditing ? 'Save Path' : 'Add Path'"
        :disabled="!isValid || saving"
        data-test="save-button"
        @click="save"
      />
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import IftaLabel from 'primevue/iftalabel'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'

import TargetRowEditor, { MethodOption, TargetRow } from '@/components/AdminNotifications/TargetRowEditor.vue'
import FormField from '@/components/Common/FormField.vue'
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
const initialDelay = ref('0s')
const targets = ref<TargetRow[]>([])
const escalations = ref<EscalationBlock[]>([])
const saving = ref(false)

const isEditing = computed(() => props.path !== null)
const originalName = computed(() => props.path?.name ?? '')

const methodOptions = computed<MethodOption[]>(() => {
  const commands = store.commands.length ? store.commands.map((c) => c.name) : Array.from(ENABLED_METHODS)
  return commands.map((command) => ({
    value: command,
    label: METHOD_LABELS[command] ? `${METHOD_LABELS[command]} (${command})` : command,
    disabled: !ENABLED_METHODS.has(command)
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
      escalations.value = (props.path.escalate ?? []).map((esc) => ({
        key: rowKey++,
        delay: esc.delay ?? '15m',
        targets: (esc.target ?? []).map(toRow)
      }))
      if (!targets.value.length) {
        targets.value = [newTargetRow()]
      }
    } else {
      name.value = ''
      initialDelay.value = '0s'
      targets.value = [newTargetRow()]
      escalations.value = []
    }
  }
)

const rowIsValid = (row: TargetRow) => !!row.name.trim() && row.commands.length > 0

const isValid = computed(() => {
  if (!name.value.trim() || !targets.value.length) {
    return false
  }
  if (!targets.value.every(rowIsValid)) {
    return false
  }
  return escalations.value.every((esc) => esc.targets.length > 0 && esc.targets.every(rowIsValid))
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
      escalate: escalations.value.map((esc) => ({
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
