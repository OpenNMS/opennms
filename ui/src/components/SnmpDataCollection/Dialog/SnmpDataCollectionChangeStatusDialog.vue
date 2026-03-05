<template>
  <div class="change-status-dialog-modal">
    <FeatherDialog
      v-model="isVisible"
      :labels="label"
      hide-close
      @hidden="close"
    >
      <div
        class="modal-body"
        v-if="props.selected?.id && props.selected?.name"
      >
        <div v-if="props.type === 'source'">
          <p>
            This will {{ props.status.toLowerCase() }} the SNMP Data Collection Source:
            <strong>{{ props.selected.name }}</strong>
          </p>
          <p>
            <strong>Note:</strong> Changing the status of an SNMP Data Collection Source will also affect all associated
            MIB Groups, System Definitions, and Resource Types.
          </p>
        </div>
        <div v-if="props.type === 'mib-group'">
          <p>
            This will {{ props.status.toLowerCase() }} the MIB Group:
            <strong>{{ props.selected.name }}</strong>
          </p>
        </div>
        <div v-if="props.type === 'system-def'">
          <p>
            This will {{ props.status.toLowerCase() }} the System Definition:
            <strong>{{ props.selected.name }}</strong>
          </p>
        </div>
        <div v-if="props.type === 'resource-type'">
          <p>
            This will {{ props.status.toLowerCase() }} the Resource Type:
            <strong>{{ props.selected.name }}</strong>
          </p>
        </div>
        <p>This action can not be undone.</p>
        <p><strong>Are you sure you want to proceed?</strong></p>
      </div>
      <template v-slot:footer>
        <FeatherButton @click="close"> Cancel </FeatherButton>
        <FeatherButton
          primary
          @click="changeStatus"
        >
          Save
        </FeatherButton>
      </template>
    </FeatherDialog>
  </div>
</template>

<script lang="ts" setup>
import { FeatherButton } from '@featherds/button'
import { FeatherDialog } from '@featherds/dialog'

const props = defineProps<{
  status: 'Enable' | 'Disable'
  visible: boolean
  selected: { id: number; name: string, enabled: boolean } | null
  type: 'source' | 'mib-group' | 'system-def' | 'resource-type'
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'confirm', selected: { id: number; name: string } | null, type: string): void
}>()

const isVisible = ref(false)
const label = computed(() => {
  let title = 'Change Status'
  switch (props.type) {
    case 'source':
      title = `${props.status} SNMP Data Collection Source`
      break
    case 'mib-group':
      title = `${props.status} MIB Group`
      break
    case 'system-def':
      title = `${props.status} System Definition`
      break
    case 'resource-type':
      title = `${props.status} Resource Type`
      break
  }
  return { title }
})

const close = () => {
  isVisible.value = false
  emit('close')
}

const changeStatus = () => {
  isVisible.value = false
  emit('confirm', props.selected, props.type)
}

watch(() => props.visible, (visible) => {
  if (visible) {
    isVisible.value = props.visible
  }
}, { immediate: true })
</script>

<style lang="scss" scoped>
.modal-body {
  min-width: 40rem;
}
</style>

