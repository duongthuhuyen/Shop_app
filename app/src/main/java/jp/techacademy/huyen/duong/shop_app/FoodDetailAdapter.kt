package jp.techacademy.huyen.duong.shop_app

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import jp.techacademy.huyen.duong.shop_app.databinding.DashboardRecyclerViewSingleRowBinding
import jp.techacademy.huyen.duong.shop_app.databinding.ListCommentBinding
import jp.techacademy.huyen.duong.shop_app.realm.CartFood
import jp.techacademy.huyen.duong.shop_app.realm.FavoriteFood
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class FoodDetailAdapter(context: Context, private val food: Food,var genre: Int) : BaseAdapter(), DatabaseReference.CompletionListener {
    private lateinit var databaseReference: DatabaseReference
    companion object {
        private const val TYPE_FOOD = 0
        private const val TYPE_COMMENT = 1
    }

    private var layoutInflater: LayoutInflater
    private var listQidFavorite: ArrayList<String> = arrayListOf()
    private var pictureUri: Uri? = null

    init {
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return 1 + food.comments.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TYPE_FOOD
        } else {
            TYPE_COMMENT
        }
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Any {
        return food
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        if (getItemViewType(position) == TYPE_FOOD) { // gọi getItemViewType -> phán đoán loại layout nào
            // ViewBindingを使うための設定
            val binding = if (convertView == null) {
                DashboardRecyclerViewSingleRowBinding.inflate(layoutInflater, parent, false)
            } else {
                DashboardRecyclerViewSingleRowBinding.bind(convertView)
            }
            val view: View = convertView ?: binding.root
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
            }
            val bytes = food.imageBytes
            if (bytes.isNotEmpty()) {
                val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    .copy(Bitmap.Config.ARGB_8888, true)
                binding.imgRestaurant.setImageBitmap(image)
            }
            binding.txtCartAdd.isVisible = false
            binding.txtRestaurantName.text = food.name
            binding.txtPrice.text = food.price.toString()
            val id = food.foodUid
            binding.txtFavorite.apply {
                var favorite = FavoriteFood.findBy(id) != null
                if (favorite) {
                    setBackgroundResource(R.drawable.ic_star)
                } else {
                    setBackgroundResource(R.drawable.ic_star_border)
                }

                setOnClickListener {
                    //dialogDeleteFavorite(foodArrayList[position], context)
                    if (favorite) {
                        AlertDialog.Builder(context)
                            .setTitle(R.string.delete_favorite_dialog_title)
                            .setMessage(R.string.delete_favorite_dialog_message)
                            .setPositiveButton(android.R.string.ok) { _, _ ->
                                deleteFavorite(food)
                                setBackgroundResource(R.drawable.ic_star_border)
                                favorite = false
                            }
                            .setNegativeButton(android.R.string.cancel) { _, _ -> }
                            .create()
                            .show()
                    } else {
                        addFavorite(food)
                        setBackgroundResource(R.drawable.ic_star)
                        favorite = true
                    }
                }
            }
            if (user != null) {
                var cf = CartFood.findBy(food.foodUid)
                if (cf != null) {
                    val number = CartFood.findNumber(food.foodUid)!!
                    binding.txtNumberCart.setText("${number} item")
                }
            }
            binding.txtCart.apply {
                setOnClickListener {
                    if (user != null) {
                        var number = 0
                        var cf = CartFood.findBy(food.foodUid)
                        if (cf != null) {
                            number = CartFood.findNumber(food.foodUid)!!
                        }
                        addCart(food, (number + 1))
                        binding.txtNumberCart.setText("${number + 1} item")
                    }
                }
            }
            binding.txtAddcomment.setOnClickListener() {
                val intent = Intent(it.context, CommentSendActivity::class.java)
                intent.putExtra("food", food)
                intent.putExtra("genre", genre)
                it.context.startActivity(intent)
            }
            return view
        } else {
            val binding = if (convertView == null) {
                ListCommentBinding.inflate(layoutInflater, parent, false)
            } else {
                ListCommentBinding.bind(convertView)
            }
            val view: View = convertView ?: binding.root
            binding.txtDate.setText(food.comments[position-1].date)
            binding.txtBody.setText(food.comments[position-1].body)
            val bytes = food.comments[position-1].bytes
            if (bytes.isNotEmpty()) {
                val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    .copy(Bitmap.Config.ARGB_8888, true)
                binding.imgRestaurant.setImageBitmap(image)
            }
            return view
        }
    }

    override fun onComplete(error: DatabaseError?, ref: DatabaseReference) {
        TODO("Not yet implemented")
    }
    fun deleteFavorite(food: Food) {
        FavoriteFood.delete(food.foodUid)
    }
    fun addFavorite(food: Food) {
        val bitmapString =
            Base64.encodeToString(food.imageBytes, Base64.DEFAULT)
        FavoriteFood.insert(FavoriteFood().apply {
            foodIdFirebase = food.foodUid
            name = food.name
            description = food.description
            price = food.price
            image = bitmapString
            genre = food.genre
        })
    }
    fun addCart(food: Food, num: Int) {
        val bitmapString =
            Base64.encodeToString(food.imageBytes, Base64.DEFAULT)
        CoroutineScope(Dispatchers.Default).launch {
            CartFood.insert(CartFood().apply {
                foodIdFirebase = food.foodUid
                name = food.name
                description = food.description
                price = food.price
                image = bitmapString
                number = num
                genre = food.genre
            })
        }
    }
}