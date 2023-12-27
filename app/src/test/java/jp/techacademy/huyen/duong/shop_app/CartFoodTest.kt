package jp.techacademy.huyen.duong.shop_app

import jp.techacademy.huyen.duong.shop_app.realm.CartFood
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.junit.Assert
import org.junit.Test

class CartFoodTest {
    private lateinit var food: CartFood

    @Test
    fun addCartTest() {
        food = CartFood("12222", "b", "c", "d", "e", 1, 1)
        var f = CartFood.findBy("12222")
        if (f != null) {
            CoroutineScope(Dispatchers.Default).launch {
                CartFood.delete("12222")
            }
        }
        f = CartFood.findBy("12222")
        if (f == null) {
            CoroutineScope(Dispatchers.Default).launch {
                CartFood.insert(food)
            }
        }
        //var number = CartFood.findAll()
        food.number = 2
        CoroutineScope(Dispatchers.Default).launch {
            CartFood.insert(food)
        }

        CartFood.deleteAll()
        var number = CartFood.findAll()
        Assert.assertEquals(0, number.size)
    }
    @Test
    fun deleteCartTest() {
       // food = CartFood("12222","b","c","d","e",1,1)

        food = CartFood("12222", "b", "c", "d", "e", 1, 1)
        var f = CartFood.findBy("12222")
        if (f == null) {
            CoroutineScope(Dispatchers.Default).launch {
                CartFood.insert(food)
            }
        }
        CoroutineScope(Dispatchers.Default).launch {
            CartFood.delete("12222")
        }
        val number = CartFood.findAll()
        Assert.assertEquals(0, number.size)
    }
}