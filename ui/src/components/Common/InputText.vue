<template>
    <InputText :id="id" :type="type" :placeholder="placeholder" />
</template> 

<script setup lang="ts">

import InputText from 'primevue/inputtext';
import { defineProps } from "vue";

const props = defineProps({
    id: {
        type: String,
        required: false
    },
    type: {
        type: String,
        default: 'text'
    },
    placeholder: {
        type: String,
        required: false
    }
});

</script> 
