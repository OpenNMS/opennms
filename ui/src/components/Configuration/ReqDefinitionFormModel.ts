import useVuelidate from '@vuelidate/core'
import { required, minLength, maxLength, helpers } from '@vuelidate/validators'
import { ref } from 'vue'
import { firstCharValidator, nameValidator, hostValidator } from '../../Validators/CustomValidators'

export default class reqDefModel {
  name = ref('')
  host = ref('')
  type = ref('')
  foreignSource = ref('')
  schedulePeriod = ref('')
  schedulePeriodNumber = ref(1)

  get rules() {
    return {
      name: {
        required: helpers.withMessage('Please enter name', required),
        firstCharValidator,
        nameValidator,
        minLength: helpers.withMessage('Min allowed length is 2', minLength(2)),
        maxLength: helpers.withMessage('Max allowed length is 100', maxLength(100))
      },
      type: { required },
      host: {
        required: helpers.withMessage('Please enter hostname or ip address', required),
        hostValidator
      },
      foreignSource: {
        required: helpers.withMessage('Please enter foreign source', required),
        minLength: helpers.withMessage('Min allowed length is 2', minLength(2)),
        maxLength: helpers.withMessage('Max allowed length is 100', maxLength(100))
      },
      schedulePeriod: { required },
      schedulePeriodNumber: { required }
    }
  }

  toModel() {
    return useVuelidate(this.rules, this)
  }
}
