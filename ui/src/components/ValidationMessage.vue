<template>
    <div v-if="model.$invalid" class="p-mtb">
        <li class="p-error" v-for="err in model.$errors" :key="err">{{ err.$message }}</li>
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
li {
    list-style-type: disc;
}

.p-mtb {
    margin: 1% 0;
}
</style>