package jp.techacademy.huyen.duong.shop_app

import java.io.Serializable

class Comment(val uid: String,val body: String,val bytes: ByteArray,val date: String, val commentUid: String) :  Serializable{
    val imageBytes: ByteArray

    init {
        imageBytes = bytes.clone()
    }
}