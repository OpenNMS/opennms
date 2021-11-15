
<template>
  <Dropdown
    :options="options"
    :optionLabel="optionLabel"
    :optionValue="optionValue"
    :placeholder="placeholder"
    :editable="isEditable"
    :filter="isFilter"
  />
</template>

<script setup lang="ts">
import Dropdown from 'primevue/dropdown'
const props = defineProps({
  placeholder: {
    type: String,
    required: false
  },
  options: {
    type: Array,
    required: true
  },
  optionLabel: {
    type: String,
    required: true
  },
  optionValue: {
    type: String,
    required: true
  },
  isEditable: {
    type: Boolean,
    required: false
  },
  isFilter: {
    type: Boolean,
    required: false
  }
})

</script>

<style lang="scss" scoped>
.p-dropdown {
  width: 14rem;
}
.country-item {
  img {
    width: 17px;
    margin-right: 0.5rem;
  }
}
</style>
