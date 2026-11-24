<template>
  <OnmsMessageDialog
    :visible="visible"
    :title="title"
    maxWidth="46em"
    maxHeight="34em"
    data-test="threshold-help-dialog"
    @close="emit('close')"
  >
    <template #content>
      <p v-for="(paragraph, index) in paragraphs" :key="index" class="help-paragraph">{{ paragraph }}</p>
    </template>
  </OnmsMessageDialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { OnmsMessageDialog } from '@opennms/onms-ui'

const props = defineProps<{
  visible: boolean
  title: string
  text: string
}>()

const emit = defineEmits<{
  close: []
}>()

// The help copy is stored as prose with blank lines between paragraphs; split so it renders as such
// rather than as one wall of text.
const paragraphs = computed(() => props.text.split(/\n\s*\n/).map(paragraph => paragraph.replace(/\s+/g, ' ').trim()))
</script>

<style lang="scss" scoped>
.help-paragraph {
  margin: 0 0 1em 0;
  line-height: 1.5;
}
</style>
