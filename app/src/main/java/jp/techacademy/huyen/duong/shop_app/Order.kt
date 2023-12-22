package jp.techacademy.huyen.duong.shop_app

class Order (val orderId: String, val uid: String, val address: String, val date: String, val prices: String, val foods: String, val payment: String, val cardName: String, val cardPass: String, var phone: String):java.io.Serializable{
}