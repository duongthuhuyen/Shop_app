package jp.techacademy.huyen.duong.shop_app.realm

import android.util.Log
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

open class FavoriteFood (foodIdFirebase: String, name: String, description: String, price: String, image: String): RealmObject{
    @PrimaryKey
    var foodIdFirebase: String = ""
    var image: String = ""
    var name: String = ""
    var description: String = ""
    var price: String = ""
    init {
        this.foodIdFirebase = foodIdFirebase
        this.name = name
        this.image = image
        this.description = description
        this.price = price
    }

    constructor() : this("", "", "", "","")

    companion object {
        /**
         * お気に入りのShopを全件取得
         */
        fun findAll(): List<FavoriteFood> {
            // Realmデータベースとの接続を開く
            val config = RealmConfiguration.create(schema = setOf(FavoriteFood::class))
            val realm = Realm.open(config)

            // Realmデータベースからお気に入り情報を取得
            // mapでディープコピーしてresultに代入する
            val result = realm.query<FavoriteFood>().find()
                .map { FavoriteFood(it.foodIdFirebase, it.name, it.description, it.price, it.image) }

            // Realmデータベースとの接続を閉じる
            realm.close()

            return result
        }

        /**
         * お気に入りされているShopをidで検索して返す
         * お気に入りに登録されていなければnullで返す
         */
        fun findBy(foodId: String): FavoriteFood? {
            // Realmデータベースとの接続を開く
            val config = RealmConfiguration.create(schema = setOf(FavoriteFood::class))
            Log.d("TEST","HIII")
            val realm = Realm.open(config)

            val result = realm.query<FavoriteFood>("foodIdFirebase=='$foodId'").first().find()

            // Realmデータベースとの接続を閉じる
            realm.close()

            return result
        }

        /**
         * お気に入り追加
         */
        fun insert(favoriteShop: FavoriteFood) {
            // Realmデータベースとの接続を開く
            val config = RealmConfiguration.create(schema = setOf(FavoriteFood::class))
            val realm = Realm.open(config)

            // 登録処理
            realm.writeBlocking {
                copyToRealm(favoriteShop)
            }

            // Realmデータベースとの接続を閉じる
            realm.close()
        }

        /**
         * idでお気に入りから削除する
         */
        fun delete(id: String) {
            // Realmデータベースとの接続を開く
            val config = RealmConfiguration.create(schema = setOf(FavoriteFood::class))
            val realm = Realm.open(config)

            // 削除処理
            realm.writeBlocking {
                val favoriteShops = query<FavoriteFood>("id=='$id'").find()
                favoriteShops.forEach {
                    delete(it)
                }
            }

            // Realmデータベースとの接続を閉じる
            realm.close()
        }
    }
}