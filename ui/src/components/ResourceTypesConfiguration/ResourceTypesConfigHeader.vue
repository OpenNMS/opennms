<template>
  <div class="config-header">
    <div class="left">
      <OnmsButton
        variant="text"
        data-test="resource-types-config-back"
        @click="router.push(RESOURCE_TYPES_PATH)"
      >
        <OnmsIcon :icon="ArrowBack" />
        Go Back
      </OnmsButton>
      <h2 data-test="resource-types-config-name">{{ config.id }}</h2>
    </div>

    <div class="right">
      <OnmsButton variant="outlined" data-test="resource-types-config-download-xml" @click="emit('download', true)">
        Download XML
      </OnmsButton>
      <OnmsButton variant="outlined" data-test="resource-types-config-download-json" @click="emit('download', false)">
        Download JSON
      </OnmsButton>
      <label class="upload-label">
        <input
          type="file"
          accept=".json,.xml,application/json,application/xml,text/xml"
          data-test="resource-types-config-upload"
          @change="onUpload"
        >
        <span class="upload-button">Replace from file</span>
      </label>
      <OnmsButton variant="outlined" data-test="resource-types-config-rename" @click="emit('rename')">
        Rename
      </OnmsButton>
      <OnmsButton variant="outlined" severity="danger" data-test="resource-types-config-delete" @click="emit('delete')">
        Delete
      </OnmsButton>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { RESOURCE_TYPES_PATH } from '@/lib/resourceTypeRoutes'
import type { ResourceTypesConfig } from '@/types/resourceTypesConfig'
import { OnmsButton, OnmsIcon } from '@opennms/onms-ui'
import ArrowBack from '@opennms/onms-ui/icons/navigation/ArrowBack.vue'

defineProps<{
  config: ResourceTypesConfig
}>()

const emit = defineEmits<{
  download: [isXml: boolean]
  upload: [file: File]
  rename: []
  delete: []
}>()

const router = useRouter()

const onUpload = (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  // Reset, so picking the same file again still fires a change event.
  input.value = ''

  if (file) {
    emit('upload', file)
  }
}
</script>

<style lang="scss" scoped>
.config-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 1em;
  margin-bottom: 1.25em;

  .left {
    display: flex;
    align-items: center;
    gap: 20px;

    h2 {
      margin: 0;
    }
  }

  .right {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 0.5em;
  }

  .upload-label input[type='file'] {
    display: none;
  }

  .upload-button {
    display: inline-block;
    cursor: pointer;
    padding: 0.5rem 1rem;
    border: 1px solid var(--p-primary-color);
    border-radius: var(--p-border-radius-md, 4px);
    color: var(--p-primary-color);
  }
}
</style>
