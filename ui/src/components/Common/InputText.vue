<template>
    <InputText
        :type="type"
        :placeholder="placeholder"
        :class="cmclass === 'sm' ? 'p-inputtext-sm' : (cmclass === 'lg' ? 'p-inputtext-lg' : 'p-inputtext')"
    />
</template> 

<script setup lang="ts">

import InputText from 'primevue/inputtext';
import { defineProps, defineEmits } from "vue";

let enteredText;

const props = defineProps({
    type: {
        type: String,
        required: true
    },
    placeholder: {
        type: String,
        required: false
    },
    cmclass: {
        type: String,
        required: false
    }
});

</script>


