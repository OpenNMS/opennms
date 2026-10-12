<template>
  <OnmsDialog
    :visible="visible"
    modal
    header="Delete Application?"
    class="application-delete-dialog"
    width="min(560px, 95vw)"
    data-test="application-delete-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div class="body">
      <p class="subtitle">It cannot be undone.</p>

      <dl class="details">
        <dt>Application</dt>
        <dd data-test="application-name">{{ application?.name }}</dd>
        <dt>Services</dt>
        <dd data-test="service-count">{{ application?.serviceCount ?? 0 }}</dd>
        <dt>Perspective locations</dt>
        <dd data-test="perspective-locations">{{ application?.perspectiveLocations.length ? application.perspectiveLocations.join(', ') : 'None' }}</dd>
      </dl>

      <OnmsMessage severity="info" data-test="delete-note">
        Its services, their nodes and their outage history are kept.
        <template v-if="application?.perspectiveLocations.length">Services that no other application checks from these locations stop being checked from there, and their open perspective outages are closed.</template>
      </OnmsMessage>

      <div v-if="errorText" class="dialog-error" role="alert" data-test="dialog-error">{{ errorText }}</div>
    </div>

    <template #footer>
      <OnmsButton variant="ghost" label="Cancel" autofocus data-test="cancel-button" @click="emit('update:visible', false)" />
      <OnmsButton
        severity="danger"
        label="Delete Application"
        :disabled="deleting || !application"
        :loading="deleting"
        data-test="delete-button"
        @click="confirmDelete"
      />
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

import { OnmsButton, OnmsDialog, OnmsMessage, useOnmsToast } from '@opennms/onms-ui'

import { useApplicationAdminStore } from '@/stores/applicationAdminStore'
import { ApplicationSummary } from '@/types/applicationAdmin'

const props = defineProps<{
  visible: boolean
  application: ApplicationSummary | null
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
}>()

const store = useApplicationAdminStore()
const { showToast } = useOnmsToast()

const deleting = ref(false)
const errorText = ref('')

watch(() => props.visible, (isVisible) => {
  if (isVisible) {
    errorText.value = ''
  }
})

const confirmDelete = async () => {
  if (!props.application) {
    return
  }
  const application = props.application
  deleting.value = true
  try {
    const result = await store.deleteApplication(application)
    if (result.success) {
      showToast({ message: `Application '${application.name}' deleted.`, severity: 'success' })
      emit('update:visible', false)
    } else {
      errorText.value = result.message
    }
  } finally {
    deleting.value = false
  }
}
</script>

<style lang="scss" scoped>
.body {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.subtitle {
  margin: 0;
  color: var(--p-text-muted-color);
}

.details {
  display: grid;
  grid-template-columns: max-content 1fr;
  gap: 0.4rem 1rem;
  margin: 0;

  dt {
    font-weight: 600;
  }

  dd {
    margin: 0;
    overflow-wrap: anywhere;
  }
}

.dialog-error {
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
  border: 1px solid var(--p-red-200, #fecaca);
  background: var(--p-red-50, #fef2f2);
  color: var(--p-red-700, #b91c1c);
  font-size: 0.9rem;
}
</style>
