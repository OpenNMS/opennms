<template>
  <div v-if="model.$invalid" class="p-mtb">
    <p class="p-error errorMsg" v-for="err in model.$errors" :key="err">{{ err.$message }}</p>
  </div>
</template>

<script lang="ts">
export default {
  props: {
    model: {
      type: Object,
      required: true
    }
  }
}
</script>

<style scoped>
.errorMsg {
  margin: 0;
}

.p-mtb {
  margin: 1% 0;
}
</style>
