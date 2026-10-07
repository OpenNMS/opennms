<template>
  <TableCard>
    <div class="address-editor">
      <h3>Addresses</h3>
      <p class="hint">
        Specifics and include ranges add addresses to the filter's selection. Exclude ranges remove them again.
      </p>

      <div class="grid">
        <section>
          <h4>Specific addresses</h4>
          <StringListEditor
            :modelValue="modelValue.specifics"
            itemLabel="address"
            idPrefix="package-specific"
            @update:modelValue="update('specifics', $event)"
          />
        </section>

        <section>
          <h4>Include URLs</h4>
          <StringListEditor
            :modelValue="modelValue.includeUrls"
            itemLabel="URL"
            idPrefix="package-include-url"
            @update:modelValue="update('includeUrls', $event)"
          />
        </section>

        <section>
          <h4>Include ranges</h4>
          <AddressRangeListEditor
            :modelValue="modelValue.includeRanges"
            idPrefix="package-include-range"
            @update:modelValue="update('includeRanges', $event)"
          />
        </section>

        <section>
          <h4>Exclude ranges</h4>
          <AddressRangeListEditor
            :modelValue="modelValue.excludeRanges"
            idPrefix="package-exclude-range"
            @update:modelValue="update('excludeRanges', $event)"
          />
        </section>

        <section class="span-two">
          <h4>Outage calendars</h4>
          <p class="hint">
            Also maintained from the scheduled outages page. Saving this package replaces the list with what
            you see here.
          </p>
          <StringListEditor
            :modelValue="modelValue.outageCalendars"
            itemLabel="calendar"
            idPrefix="package-outage-calendar"
            @update:modelValue="update('outageCalendars', $event)"
          />
        </section>
      </div>
    </div>
  </TableCard>
</template>

<script setup lang="ts">
import TableCard from '@/components/Common/TableCard.vue'
import AddressRangeListEditor from '@/components/ThresholdConfiguration/Common/AddressRangeListEditor.vue'
import StringListEditor from '@/components/ThresholdConfiguration/Common/StringListEditor.vue'
import type { ThreshdPackage } from '@/types/thresholdConfig'

const props = defineProps<{
  modelValue: ThreshdPackage
}>()

const emit = defineEmits<{
  'update:modelValue': [value: ThreshdPackage]
}>()

const update = (field: string, value: unknown) => {
  emit('update:modelValue', { ...props.modelValue, [field]: value })
}
</script>

<style lang="scss" scoped>
.address-editor {
  padding: 0 1em;

  h3 {
    margin: 0;
  }

  h4 {
    margin: 0 0 0.5em 0;
  }

  .hint {
    color: var(--p-text-muted-color);
    margin: 0.25em 0 1em 0;
  }

  .grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 1.5em;
  }

  .span-two {
    grid-column: span 2;
  }

  @media (max-width: 768px) {
    .grid {
      grid-template-columns: minmax(0, 1fr);
    }

    .span-two {
      grid-column: span 1;
    }
  }
}
</style>
