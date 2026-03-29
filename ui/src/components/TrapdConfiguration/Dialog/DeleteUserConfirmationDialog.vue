<template>
  <div class="delete-user-confirmation-dialog" v-if="props.index !== null && store.snmpV3Users[props.index]">
    <FeatherDialog
      v-model="isVisible"
      :labels="label"
      hide-close
      @hidden="emit('close')"
    >
      <div>
        <p>Are you sure you want to delete this SNMPv3 user with security name "{{ store.snmpV3Users[props.index]?.securityName }}"?</p>
        <p><strong>Note:</strong> This action cannot be undone.</p>
      </div>
      <template #footer>
        <FeatherButton
          secondary
          @click="emit('close')"
        >
          Cancel
        </FeatherButton>
        <FeatherButton
          primary
          @click="emit('confirm')"
        >
          Delete
        </FeatherButton>
      </template>
    </FeatherDialog>
  </div>
</template>

<script setup lang="ts">
import { useTrapdConfigStore } from '@/stores/trapdConfigStore'
import { FeatherButton } from '@featherds/button'
import { FeatherDialog } from '@featherds/dialog'

const label = {
  title: 'Snmpv3 User Delete Confirmation'
}
const isVisible = ref(false)
const store = useTrapdConfigStore()

const props = defineProps<{
  visible: boolean,
  index: number | null
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'confirm'): void
}>()

watch(() => props.visible, (newVal) => {
  isVisible.value = newVal
}, { immediate: true })
</script>

<style scoped lang="scss"></style>

