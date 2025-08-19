<template>
  <div class="event-config-container">
    <div class="header">
      <h1>Event Configuration Details</h1>
    </div>

    <div class="config-details-box">
      <div class="config-row">
        <div class="config-field name-field">
          <span class="field-label">Name:</span>
          <span class="field-value">{{ config?.filename || 'Compuware.events.xml' }}</span>
        </div>
        <div class="config-field description-field">
          <span class="field-label">Description:</span>
          <span class="field-value">{{ config?.description || 'Enter Description here' }}</span>
        </div>
      </div>
      <div class="config-row">
        <div class="config-field vendor-field">
          <span class="field-label">Vendor:</span>
          <span class="field-value">{{ config?.vendor || '3Com' }}</span>
        </div>
      </div>
      <div class="config-row vertical-fields">
        <div class="config-field">
          <span class="field-label">File Order:</span>
          <span class="field-value">{{ config?.fileOrder || '3' }}</span>
        </div>
        <div class="config-field">
          <span class="field-label">Event Count:</span>
          <span class="field-value">{{ config?.eventCount || '4' }}</span>
        </div>
      </div>
    </div>

    <div class="editor-section">
      <EventConfigFileEditor :config="config" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import EventConfigFileEditor from './EventConfigFileEditor.vue'

interface Config {
  filename?: string
  description?: string
  vendor?: string
  fileOrder?: string | number
  eventCount?: string | number
}

const config = ref<Config | null>({
  filename: 'Compuware.events.xml',
  description: 'Enter Description here',
  vendor: '3Com',
  fileOrder: 3,
  eventCount: 4
})
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/variables";

.event-config-container {
  margin: 0 auto;
  padding: 20px;

}
.header {
  margin-bottom: 20px;
}
.config-details-box {
  border: 1px solid var($primary);
  border-radius: 4px;
  padding: 20px;
  background: white;
  margin-bottom: 30px;
}

.config-row {
  display: flex;
  margin-bottom: 15px;
}

.config-field {
  display: flex;
  align-items: center;
  margin-right: 40px;
}

.vertical-fields {
  flex-direction: column;
  gap: 10px;
}

.name-field {
  min-width: 500px;
}

.description-field {
  min-width: 300px;
}

.vendor-field {
  min-width: 500px;
}

.field-label {
  font-weight: bold;
  margin-right: 10px;
  color: #555;
  min-width: 80px;
}

.field-value {
  color: #333;
}

.editor-section {
  border-bottom: 1px solid var($primary);
  border-radius: 4px;
  padding: 20px;
  background: white;
}

.editor-section h2 {
  font-size: 16px;
  margin-bottom: 15px;
  color: #333;
}
</style>