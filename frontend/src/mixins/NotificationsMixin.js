export default {
  methods: {
    displayNotification (props) {
      this.$bvToast.toast(props.message, {
        title: 'Success!',
        autoHideDelay: 3000,
        appendToast: true,
        solid: true,
        toaster: 'b-toaster-bottom-right',
        variant: 'success',
        ...props
      })
    },
    displayError (err) {
      this.displayNotification({
        message: err,
        title: 'Error',
        variant: 'danger'
      })
    }
  }
}
