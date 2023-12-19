package jp.techacademy.huyen.duong.shop_app

import java.io.Serializable
import java.util.ArrayList

class Food(
    val foodUid: String,
    val name: String,
    val description: String,
    val price: String,
    val genre: Int,
    bytes: ByteArray,
    val comments: ArrayList<Comment>
) : Serializable {
    val imageBytes: ByteArray

    init {
        imageBytes = bytes.clone()
    }
}