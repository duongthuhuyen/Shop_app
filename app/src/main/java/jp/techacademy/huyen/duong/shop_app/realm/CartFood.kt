package jp.techacademy.huyen.duong.shop_app.realm

import android.util.Log
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

open class CartFood(
    foodIdFirebase: String,
    name: String,
    description: String,
    price: String,
    image: String,
    number: Int,
    genre: Int
) :
    RealmObject , java.io.Serializable{
    @PrimaryKey
    var foodIdFirebase: String = ""
    var image: String = ""
    var name: String = ""
    var description: String = ""
    var price: String = ""
    var number: Int = 0
    var genre: Int = 0

    init {
        this.foodIdFirebase = foodIdFirebase
        this.name = name
        this.image = image
        this.description = description
        this.price = price
        this.number = number
        this.genre = genre
    }

    constructor() : this("", "", "", "", "", 0, 0)

    companion object {
        /**
         * お気に入りのShopを全件取得
         */
        fun findAll(): List<CartFood> {
            // Realmデータベースとの接続を開く
            val config =
                RealmConfiguration.create(schema = setOf(CartFood::class, FavoriteFood::class))
            val realm = Realm.open(config)

            // Realmデータベースからお気に入り情報を取得
            // mapでディープコピーしてresultに代入する
            val result = realm.query<CartFood>().find()
                .map {
                    CartFood(
                        it.foodIdFirebase,
                        it.name,
                        it.description,
                        it.price,
                        it.image,
                        it.number,
                        it.genre
                    )
                }

            // Realmデータベースとの接続を閉じる
            realm.close()

            return result
        }

        /**
         * お気に入りされているShopをidで検索して返す
         * お気に入りに登録されていなければnullで返す
         */
        fun findBy(foodId: String): CartFood? {
            // Realmデータベースとの接続を開く
            val config =
                RealmConfiguration.create(schema = setOf(CartFood::class, FavoriteFood::class))
            Log.d("TEST", "HIII")
            val realm = Realm.open(config)

            val result = realm.query<CartFood>("foodIdFirebase=='$foodId'").first().find()
            val number = result?.number
            // Realmデータベースとの接続を閉じる
            realm.close()

            return result
        }

        fun findNumber(foodId: String): Int? {
            // Realmデータベースとの接続を開く
            val config =
                RealmConfiguration.create(schema = setOf(CartFood::class, FavoriteFood::class))
            val realm = Realm.open(config)

            val result = realm.query<CartFood>("foodIdFirebase=='$foodId'").first().find()
            val number = result?.number
            // Realmデータベースとの接続を閉じる
            Log.d("Number find", "" + number)
            realm.close()

            return number
        }

        /**
         * おカート追加
         */
        suspend fun insert(food: CartFood) {
            // Realmデータベースとの接続を開く
            val config =
                RealmConfiguration.create(schema = setOf(CartFood::class, FavoriteFood::class))
            val realm = Realm.open(config)
            var cartFood: CartFood? = findBy(food.foodIdFirebase)

            // 登録処理
            if (cartFood == null) {
                realm.writeBlocking {
                    copyToRealm(food)
                }
            } else {
                val number = food.number
                val cr =
                    realm.query<CartFood>("foodIdFirebase=='${food.foodIdFirebase}'").find().first()
                if (cr != null) {
                    realm.write {
                        findLatest(cr)?.apply {
                            this.number = number
                        }
                    }
                }
            }

            // Realmデータベースとの接続を閉じる
            realm.close()
        }

        /**
         * idでカートから削除する
         */
        fun delete(id: String) {
            // Realmデータベースとの接続を開く
            val config =
                RealmConfiguration.create(schema = setOf(CartFood::class, FavoriteFood::class))
            val realm = Realm.open(config)

            // 削除処理
            realm.writeBlocking {
                val cartShops = query<CartFood>("foodIdFirebase=='$id'").find()
                cartShops.forEach {
                    delete(it)
                }
            }

            // Realmデータベースとの接続を閉じる
            realm.close()
        }
        fun deleteAll() {
            // Realmデータベースとの接続を開く
            val config =
                RealmConfiguration.create(schema = setOf(CartFood::class, FavoriteFood::class))
            val realm = Realm.open(config)

            // 削除処理
            realm.writeBlocking {
                val cartShops = query<CartFood>().find()
                cartShops.forEach {
                    delete(it)
                }
            }

            // Realmデータベースとの接続を閉じる
            realm.close()
        }
    }
}