package jp.techacademy.huyen.duong.shop_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import jp.techacademy.huyen.duong.shop_app.databinding.DashboardRecyclerViewSingleRowBinding
import jp.techacademy.huyen.duong.shop_app.realm.FavoriteFood

class DasboardListAdapter(context: Context): BaseAdapter() {
    private var layoutInflater: LayoutInflater
    private var foodArrayList = ArrayList<Food>()
    init {
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }
    override fun getCount(): Int {
        return foodArrayList.size
    }

    override fun getItem(p0: Int): Any {
        return foodArrayList[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // ViewBindingを使うための設定
        val binding = if (convertView == null) {
            DashboardRecyclerViewSingleRowBinding.inflate(layoutInflater, parent, false)
        } else {
            DashboardRecyclerViewSingleRowBinding.bind(convertView)
        }
        val view: View = convertView ?: binding.root

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
            binding.txtCart.isVisible = false
        } else {
            val id = foodArrayList[position].foodUid
            binding.txtFavorite.apply {
                val favorite = FavoriteFood.findBy(id) != null
                if (!favorite) {
                    setBackgroundResource(R.drawable.ic_star_border)
                    setOnClickListener() {
                        addFavorite(foodArrayList[position])
                        setBackgroundResource(R.drawable.ic_star)
                    }
                } else {
                   setBackgroundResource(R.drawable.ic_star)
                    setOnClickListener {
                        dialogDeleteFavorite(foodArrayList[position], context)
                        setBackgroundResource(R.drawable.ic_star_border)
                    }
                }
            }
        }

        return view
    }
    fun setFoodArrayList(foodArrayList: ArrayList<Food>) {
        this.foodArrayList = foodArrayList
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
       })
    }
    fun dialogDeleteFavorite(food: Food, context: Context) {
        AlertDialog.Builder(context)
            .setTitle(R.string.delete_favorite_dialog_title)
            .setMessage(R.string.delete_favorite_dialog_message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                deleteFavorite(food)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> }
            .create()
            .show()
    }
    fun deleteFavorite(food: Food) {
        FavoriteFood.delete(food.foodUid)
    }
}