package jp.techacademy.huyen.duong.shop_app

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import jp.techacademy.huyen.duong.shop_app.databinding.ActivityOrderBinding
import jp.techacademy.huyen.duong.shop_app.realm.CartFood
import java.text.SimpleDateFormat
import java.util.Date

class OrderActivity : AppCompatActivity(), DatabaseReference.CompletionListener {
    private lateinit var binding: ActivityOrderBinding
    private var prices = 0.0
    private var type = 0
    private var statusReturn = "NOT_OK"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = getString(R.string.order_title)
        prices = intent.getDoubleExtra(KEY_PRICE, 0.0)
        binding.txtPrice.setText("支払総額：　" + prices + "$")
        binding.etCardNumber.isVisible = false
        binding.etCardPassword.isVisible = false
        if (binding.card.isChecked == true) {
            type = 2
            binding.etCardNumber.isVisible = true
            binding.etCardPassword.isVisible = true
        } else if (binding.money.isChecked) {
            type = 1
            binding.etCardNumber.isVisible = false
            binding.etCardPassword.isVisible = false
        }
        binding.money.setOnClickListener() {
            binding.etCardNumber.isVisible = false
            binding.etCardPassword.isVisible = false
            type = 1
        }
        binding.card.setOnClickListener() {
            binding.etCardNumber.isVisible = true
            binding.etCardPassword.isVisible = true
            type = 2
        }
        val user = FirebaseAuth.getInstance().currentUser

        var carts = intent.getSerializableExtra(KEY_CART) as ArrayList<CartFood>
        if (user != null) {
            binding.btnRegister.setOnClickListener() {
                // キーボードが出てたら閉じる
                val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                im.hideSoftInputFromWindow(it.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)

                val dataBaseReference = FirebaseDatabase.getInstance().reference
                val genreRef = dataBaseReference.child(OrdersPATH).child(user!!.uid)

                val data = HashMap<String, String>()
                val registerName = binding.etName.text.toString()
                val registerPhoneNumber = binding.etMobileNumber.text.toString()
                val registerAdress = binding.etDeliveryAddress.text.toString()
                val registerCardNumber = binding.etCardNumber.text.toString()
                val registerCardPassword = binding.etCardPassword.text.toString()
                if (registerName.isEmpty() || registerPhoneNumber.isEmpty() || registerAdress.isEmpty()
                    || (type == 2 && (registerCardNumber.isEmpty() || registerCardPassword.isEmpty()) || type == 0)
                ) {
                    Snackbar.make(binding.root, R.string.warning_buy, Snackbar.LENGTH_LONG).show()
                    return@setOnClickListener
                } else if (registerPhoneNumber.length < 10 || registerPhoneNumber.length > 13 || registerPhoneNumber.matches(
                        regex = Regex("^[0-9]+$")
                    ) == false
                ) {
                    Snackbar.make(binding.root, "電話番号フォマット違う", Snackbar.LENGTH_LONG).show()
                    return@setOnClickListener
                } else {
                    var foods = ""
                    for (i in carts) {
                        foods += i.foodIdFirebase + ","
                    }
                    val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
                    val currentDate = sdf.format(Date())
                    data["date"] = currentDate
                    data["name"] = registerName
                    data["prices"] = prices.toString()
                    data["address"] = registerAdress
                    (if (type == 1) {
                        data["payment"] = "現金"
                    } else if (type == 2) {
                        data["payment"] = "カード"
                        data["cardNumber"] = registerCardNumber
                        data["cardPassword"] = registerCardPassword
                    })
                    data["phone"] = registerPhoneNumber.toString()
                    data["foods"] = foods
                    genreRef.push().setValue(data, this)
                }

            }
        }
        onBackPressedDispatcher.addCallback {
            Log.d("CallBack", statusReturn)
            mainActivityBack()
        }
    }

    override fun onComplete(databaseError: DatabaseError?, databaseReference: DatabaseReference) {
        //binding.progressBar.visibility = View.GONE

        if (databaseError == null) {
            CartFood.deleteAll()
            Snackbar.make(
                findViewById(android.R.id.content),
                "Order success!",
                Snackbar.LENGTH_LONG
            ).show()
            Log.d("Database", "Error")
            statusReturn = "OK"
            //finish()
        } else {
            Snackbar.make(
                findViewById(android.R.id.content),
                "False",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    fun mainActivityBack() {
        val intentSub = Intent()
        Log.d("CallBack", statusReturn)
        intentSub.putExtra(KEY_RESULT, statusReturn)
        setResult(RESULT_OK, intentSub)
        finish()
    }

}

const val KEY_RESULT_ORDER = "key_result_order"