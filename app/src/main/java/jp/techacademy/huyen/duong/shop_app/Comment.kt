package jp.techacademy.huyen.duong.shop_app

import java.io.Serializable

class Comment(val uid: String, body: String, bytes: ByteArray) :  Serializable{
    val imageBytes: ByteArray

    init {
        imageBytes = bytes.clone()
    }
}