import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCartList } from '@/api/cart'

export const useCartStore = defineStore('cart', () => {
  // 状态
  const cartList = ref([])
  const cartCount = ref(0)

  // 加载购物车
  async function loadCart() {
    try {
      const res = await getCartList()
      cartList.value = res.data || []
      cartCount.value = cartList.value.reduce((sum, item) => sum + item.quantity, 0)
    } catch (error) {
      console.error('加载购物车失败', error)
    }
  }

  // 清空购物车
  function clearCartData() {
    cartList.value = []
    cartCount.value = 0
  }

  return {
    cartList,
    cartCount,
    loadCart,
    clearCartData
  }
})

