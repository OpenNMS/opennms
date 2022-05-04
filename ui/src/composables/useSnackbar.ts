import { SnackbarProps } from '@/types'
import { isDefined } from '@vueuse/core'

const isDisplayed = ref(false)
const isCentered = ref<boolean | undefined>(true)
const hasError = ref<boolean | undefined>(false)
const message = ref('')
const setTimeout = ref<number | undefined>(4000)

const useSnackbar = () => {
  const showSnackBar = (snackbarProps: SnackbarProps) => {
    const { center, error, msg, timeout } = snackbarProps
    isDisplayed.value = true
    isCentered.value = isDefined(center) ? center : true
    hasError.value = error
    message.value = msg
    setTimeout.value = timeout
  }

  const hideSnackbar = () => {
    isDisplayed.value = false
    message.value = ''
  }

  return {
    showSnackBar,
    hideSnackbar,
    isDisplayed: isDisplayed,
    isCentered: readonly(isCentered),
    hasError: readonly(hasError),
    message: readonly(message),
    setTimeout: readonly(setTimeout)
  }
}

export default useSnackbar
