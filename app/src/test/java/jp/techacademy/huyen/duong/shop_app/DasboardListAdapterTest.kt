package jp.techacademy.huyen.duong.shop_app

import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
//import com.nhaarman.mockito_kotlin.mock
import jp.techacademy.huyen.duong.shop_app.realm.CartFood
import jp.techacademy.huyen.duong.shop_app.realm.FavoriteFood
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test


class DasboardListAdapterTest{
    @MockK
    private lateinit var cartFood: CartFood
    @MockK
    private lateinit var context: Context
    @MockK
    private lateinit var dasboardListAdapter: DasboardListAdapter
    @InjectMockKs
    var food = Food("123","name","description","123",1, byteArrayOf(), arrayListOf())
    @Before
    fun setUp() = MockKAnnotations.init(this,relaxUnitFun = true)
    @Test
    fun testAddToCart() {
        dasboardListAdapter.addFavorite(food)
        var foods = FavoriteFood.findAll()
        Assert.assertEquals(1,foods.size)
    }
}