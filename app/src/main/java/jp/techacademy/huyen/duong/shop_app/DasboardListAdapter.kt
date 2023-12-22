package jp.techacademy.huyen.duong.shop_app

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import jp.techacademy.huyen.duong.shop_app.databinding.DashboardRecyclerViewSingleRowBinding
import jp.techacademy.huyen.duong.shop_app.databinding.FavoriteRecyclerViewSingleBinding
import jp.techacademy.huyen.duong.shop_app.realm.CartFood
import jp.techacademy.huyen.duong.shop_app.realm.FavoriteFood
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DasboardListAdapter(context: Context) : BaseAdapter() {
    private var layoutInflater: LayoutInflater
    private var foodArrayList = ArrayList<Food>()
    private var cardArrayList = ArrayList<CartFood>()
    private var orderArrayList = ArrayList<Order>()
    private var genre = 0
    private var number = 0
    private var mListener: OnIncrementListener? = null
    private val limit = 3

    init {
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        if (genre == 6) return cardArrayList.size
        if (genre == 8) return  orderArrayList.size
        return foodArrayList.size
        //return 2
    }

    override fun getItem(p0: Int): Any {
        if (genre == 6) return cardArrayList[p0]
        if (genre == 8) return orderArrayList[p0]
        return foodArrayList[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // ViewBindingを使うための設定
        if (genre!= 8) {
            var binding = if (convertView == null) {
                DashboardRecyclerViewSingleRowBinding.inflate(layoutInflater, parent, false)
            } else {
                DashboardRecyclerViewSingleRowBinding.bind(convertView)
            }
            var view: View = convertView ?: binding.root
            binding.txtAddcomment.isVisible = false
            if (genre != 6) {
                binding.txtCartAdd.isVisible = false
                binding.txtRestaurantName.text = foodArrayList[position].name
                binding.txtPrice.text = foodArrayList[position].price.toString()
                //binding.resTextView.text = questionArrayList[position].answers.size.toString()

                val bytes = foodArrayList[position].imageBytes
                if (bytes.isNotEmpty()) {
                    val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        .copy(Bitmap.Config.ARGB_8888, true)
                    binding.imgRestaurant.setImageBitmap(image)
                }
                val user = FirebaseAuth.getInstance().currentUser
                if (user == null) {
                    binding.txtFavorite.isVisible = false
                    binding.txtNumberCart.isVisible = false
                    binding.txtCart.isVisible = false
                } else {
                    if (genre == 7) {
                        binding.txtFavorite.isVisible = false
                    }
                    if (genre != 7) {
                        val id = foodArrayList[position].foodUid
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
                                            deleteFavorite(foodArrayList[position])
                                            setBackgroundResource(R.drawable.ic_star_border)
                                            favorite = false
                                        }
                                        .setNegativeButton(android.R.string.cancel) { _, _ -> }
                                        .create()
                                        .show()
                                } else {
                                    addFavorite(foodArrayList[position])
                                    setBackgroundResource(R.drawable.ic_star)
                                    favorite = true
                                }
                            }
                        }
                    }
                }
                if (user != null) {
                    var cf = CartFood.findBy(foodArrayList[position].foodUid)
                    if (cf != null) {
                        val number = CartFood.findNumber(foodArrayList[position].foodUid)!!
                        binding.txtNumberCart.setText("${number} item")
                    }
                }
                binding.txtCart.apply {
                    setOnClickListener {
                        if (user != null) {
                            var number = 0
                            var cf = CartFood.findBy(foodArrayList[position].foodUid)
                            if (cf != null) {
                                number = CartFood.findNumber(foodArrayList[position].foodUid)!!
                            }
                            addCart(foodArrayList[position], (number + 1))
                            binding.txtNumberCart.setText("${number + 1} item")
                        }
                    }
                }
            } else if (genre == 6) {
                binding.txtRestaurantName.text = cardArrayList[position].name
                binding.txtPrice.text = cardArrayList[position].price.toString()
                binding.txtCartAdd.setBackgroundResource(R.drawable.ic_add)
                binding.txtCart.setBackgroundResource(R.drawable.ic_minimize)
                //binding.resTextView.text = questionArrayList[position].answers.size.toString()

                val bytes = cardArrayList[position].image
                if (bytes.isNotEmpty()) {
                    val byte = Base64.decode(bytes, Base64.DEFAULT)
                    val image = BitmapFactory.decodeByteArray(byte, 0, byte.size)
                        .copy(Bitmap.Config.ARGB_8888, true)
                    binding.imgRestaurant.setImageBitmap(image)
                }
                val user = FirebaseAuth.getInstance().currentUser
                binding.txtFavorite.setBackgroundResource(R.drawable.ic_delete)
                if (user == null) {
                    binding.txtNumberCart.isVisible = false
                    binding.txtCart.isVisible = false
                }
                if (user != null) {
                    var cf = CartFood.findBy(foodArrayList[position].foodUid)
                    if (cf != null) {
                        val number = CartFood.findNumber(foodArrayList[position].foodUid)!!
                        binding.txtNumberCart.setText("${number} item")
                    }
                }
                binding.txtCartAdd.apply {
                    setOnClickListener {
                        if (user != null) {
                            var number = 1
                            var cf = CartFood.findBy(foodArrayList[position].foodUid)
                            if (cf != null) {
                                number = CartFood.findNumber(foodArrayList[position].foodUid)!!
                            }
                            addCart(foodArrayList[position], (number + 1))
                            binding.txtNumberCart.setText("${number + 1} item")
                        }
                    }
                }
                binding.txtCart.apply {
                    setOnClickListener {
                        if (user != null) {
                            var number = 1
                            var cf = CartFood.findBy(foodArrayList[position].foodUid)
                            if (cf != null) {
                                number = CartFood.findNumber(foodArrayList[position].foodUid)!!
                                if (number > 1) {
                                    addCart(foodArrayList[position], (number - 1))
                                    binding.txtNumberCart.setText("${number - 1} item")
                                } else {
                                    Snackbar.make(view, "削除アイコンをクリックしてください", Snackbar.LENGTH_LONG)
                                        .show()
                                }
                            }
                        }
                    }
                }
                binding.txtFavorite.apply {
                    setOnClickListener() {
                        AlertDialog.Builder(context)
                            .setTitle(R.string.delete_cart_dialog_title)
                            .setMessage(R.string.delete_cart_dialog_message)
                            .setPositiveButton(android.R.string.ok) { _, _ ->
                                var cart = cardArrayList[position]
                                deleteFromCart(cart.foodIdFirebase)
                                mListener?.onNumberIncremented();
                                cardArrayList.remove(cart)
                                notifyDataSetChanged()
                            }
                            .setNegativeButton(android.R.string.cancel) { _, _ -> }
                            .create()
                            .show()
                    }
                }
            }
            return view
        }
        var binding = if (convertView == null) {
            FavoriteRecyclerViewSingleBinding.inflate(layoutInflater, parent, false)
        } else {
            FavoriteRecyclerViewSingleBinding.bind(convertView)
        }
        var view: View = convertView ?: binding.root
        binding.txtOrderAddress.setText(orderArrayList[position].address)
        binding.txtOrderDate.setText(orderArrayList[position].date)
        binding.txtOrderPhone.setText(orderArrayList[position].phone)
        binding.txtOrderPrice.setText("Prices:"+orderArrayList[position].prices+"$")
        return view
    }

    fun setFoodArrayList(
        foodArrayList: ArrayList<Food>,
        genre: Int,
        cardArrayList: ArrayList<CartFood>,
        orderArrayList: ArrayList<Order>
    ) {
        this.foodArrayList = foodArrayList
        this.genre = genre
        this.cardArrayList = cardArrayList
        this.orderArrayList = orderArrayList
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

    fun deleteFavorite(food: Food) {
        FavoriteFood.delete(food.foodUid)
    }

    fun deleteFromCart(foogId: String) {
        CartFood.delete(foogId)
    }
    fun setOnIncrementListener(context: Context) {
        if(context is OnIncrementListener) {
            this.mListener = context
        }
    }
}