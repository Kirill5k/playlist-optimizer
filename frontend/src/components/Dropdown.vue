<template>
  <b-card
    no-body
    bg-variant="dark"
    text-variant="white"
    border-variant="light"
    class="dropdown mt-1 w-100"
  >
    <b-card-header
      header-bg-variant="dark"
      header-border-variant="light"
      header-text-variant="white"
      header-tag="header"
      class="p-1 dropdown__header"
      role="tab"
    >
      <p v-b-toggle="'dropdown'+id" class="mb-0 p-1 w-100 d-inline-flex">
        <slot name="header"></slot>
      </p>
    </b-card-header>
    <b-collapse
      :id="'dropdown'+id"
      accordion="my-accordion"
      role="tabpanel"
    >
      <slot name="body"></slot>
    </b-collapse>
  </b-card>
</template>

<script>
import { BCard, BCardHeader, BCollapse } from 'bootstrap-vue'

export default {
  name: 'Dropdown',
  components: {
    BCard,
    BCardHeader,
    BCollapse
  },
  props: {
    id: {
      type: String,
      required: true
    }
  }
}
</script>

<style scoped lang="scss">
.dropdown {
  width: 100%;
  align-self: center;
  text-align: left;

  &__header {
    display: flex;
    justify-content: space-between;
    line-height: 2;

    *:focus {
      outline: none;
    }
  }
}
</style>
