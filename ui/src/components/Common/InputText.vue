<template>
    <InputText
        :id="id"
        :type="type"
        :placeholder="placeholder"
        :class="primeClass === 'sm' ? 'p-inputtext-sm' : (primeClass === 'lg' ? 'p-inputtext-lg' : 'p-inputtext')"
    />
</template> 

<script setup lang="ts">

import InputText from 'primevue/inputtext';
import { defineProps, defineEmits } from "vue";


const props = defineProps({
    id: {
        type: String,
        required: false
    },
    type: {
        type: String,
        required: true
    },
    placeholder: {
        type: String,
        required: false
    },
    primeClass: {
        type: String,
        required: false
    }
});

</script>


