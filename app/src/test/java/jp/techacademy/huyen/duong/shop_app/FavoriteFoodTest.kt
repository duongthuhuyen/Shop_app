package jp.techacademy.huyen.duong.shop_app

import jp.techacademy.huyen.duong.shop_app.realm.FavoriteFood
import org.junit.Assert
import org.junit.Test


class FavoriteFoodTest {

    @Test
    fun testAddFavorite() {
        var f = FavoriteFood.findBy("1111")
        if (f != null) {
            FavoriteFood.delete("1111")
        }
        var food = FavoriteFood("1111", "1", "1", "1", "1", 2)
        //FavoriteFood.insert(food)
        FavoriteFood.insert(food)
        val number = FavoriteFood.findAll()
        Assert.assertEquals(1, number.size)
    }
}