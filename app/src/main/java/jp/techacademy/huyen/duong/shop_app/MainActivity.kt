package jp.techacademy.huyen.duong.shop_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle // 追加
import androidx.core.view.GravityCompat // 追加
import androidx.core.view.isVisible
import androidx.core.view.marginTop
import com.google.android.material.navigation.NavigationView // 追加
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import jp.techacademy.huyen.duong.shop_app.databinding.ActivityMainBinding
import jp.techacademy.huyen.duong.shop_app.realm.CartFood
import jp.techacademy.huyen.duong.shop_app.realm.FavoriteFood

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    OnIncrementListener {
    private lateinit var binding: ActivityMainBinding

    private var genre = 0  // 追加


    // ----- 追加:ここから -----
    private lateinit var databaseReference: DatabaseReference
    private lateinit var foodArrayList: ArrayList<Food>
    private lateinit var cartArrayList: ArrayList<CartFood>
    private lateinit var orderArrayList: ArrayList<Order>
    private lateinit var adapter: DasboardListAdapter
    private var genreRef: DatabaseReference? = null
    private var keySearch = ""
    private var numberCarts = 0

    //異なるActivity間で通信
    var resultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val intent = result.data
            if (intent != null) {
                val res = intent.getStringExtra(KEY_RESULT_ORDER).toString()
                if (res == "OK") {
                    cartArrayList.clear()
                    adapter.notifyDataSetChanged()
                }
                val resOk = intent.getStringExtra(KEY_RESULT).toString()
                if (resOk == "OK") {
                    binding.content.btnBuy.isVisible = false
                    cartArrayList.clear()
                    adapter.notifyDataSetChanged()
                }
                val resDetail = intent.getStringExtra(KEY_DETAIL).toString()
                if (resDetail == "OK") {
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    private val eventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<*, *>
            val name = map["name"] as? String ?: ""
            if (name.contains(keySearch)) {
                val description = map["description"] as? String ?: ""
                val prices = map["price"] as? String ?: ""
                val imageString = map["image"] as? String ?: ""
                val bytes = if (imageString.isNotEmpty()) {
                    Base64.decode(imageString, Base64.DEFAULT)
                } else {
                    byteArrayOf()
                }

                val commentArrayList = ArrayList<Comment>()
                val answerMap = map["comments"] as Map<*, *>?
                if (answerMap != null) {
                    for (key in answerMap.keys) {
                        val map1 = answerMap[key] as Map<*, *>
                        val map1Body = map1["body"] as? String ?: ""
                        val map1Uid = map1["uid"] as? String ?: ""
                        val map1CommentUid = key as? String ?: ""
                        val imageStringComment = map["image"] as? String ?: ""
                        val byte = if (imageStringComment.isNotEmpty()) {
                            Base64.decode(imageStringComment, Base64.DEFAULT)
                        } else {
                            byteArrayOf()
                        }
                        val date = map1["date"] as? String ?: ""
                        val comment = Comment(map1Uid, map1Body, byte, date, map1CommentUid)
                        commentArrayList.add(comment)
                    }
                }

                val food = Food(
                    dataSnapshot.key ?: "",
                    name,
                    description,
                    prices,
                    genre,
                    bytes,
                    commentArrayList
                )
                foodArrayList.add(food)
                adapter.notifyDataSetChanged()
            }
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<*, *>

            // 変更があったQuestionを探す
            for (food in foodArrayList) {
                if (dataSnapshot.key.equals(food.foodUid)) {
                    // このアプリで変更がある可能性があるのは回答（Answer)のみ
                    food.comments.clear()
                    val commentMap = map["comments"] as Map<*, *>?
                    if (commentMap != null) {
                        for (key in commentMap.keys) {
                            val map1 = commentMap[key] as Map<*, *>
                            val map1Body = map1["body"] as? String ?: ""
                            val map1Uid = map1["uid"] as? String ?: ""
                            val map1CommentUid = key as? String ?: ""
                            val imageStringComment = map["image"] as? String ?: ""
                            val byte = if (imageStringComment.isNotEmpty()) {
                                Base64.decode(imageStringComment, Base64.DEFAULT)
                            } else {
                                byteArrayOf()
                            }
                            val date = map1["date"] as? String ?: ""
                            val comment =
                                Comment(map1Uid, map1Body, byte, date = date, map1CommentUid)
                            food.comments.add(comment)
                        }
                    }

                    adapter.notifyDataSetChanged()
                }
            }
        }

        override fun onChildRemoved(p0: DataSnapshot) {}
        override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
        override fun onCancelled(p0: DatabaseError) {}
    }

    private val eventListenerOrder = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val user = FirebaseAuth.getInstance().currentUser
            val map = dataSnapshot.value as Map<*, *>
            val name = map["name"] as? String ?: ""
            val address = map["address"] as? String ?: ""
            val date = map["date"] as? String ?: ""
            val foods = map["foods"] as? String ?: ""
            val prices = map["prices"] as? String ?: ""
            val phone = map["phone"] as? String ?: ""
            val payment = map["payment"] as? String ?: ""
            val cardNumber = map["cardNumber"] as? String ?: ""
            val cardPasswod = map["cardPassword"] as? String ?: ""
            val order = Order(
                dataSnapshot.key ?: "",
                uid = user!!.uid,
                address,
                date = date,
                prices = prices,
                foods = foods,
                payment = payment,
                cardName = cardNumber,
                cardPass = cardPasswod,
                phone = phone
            )
            orderArrayList.add(order)
            adapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
        }

        override fun onChildRemoved(p0: DataSnapshot) {}
        override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
        override fun onCancelled(p0: DatabaseError) {}
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.content.toolbar)
        binding.content.inner.RLSearch.isVisible = false

        val user = FirebaseAuth.getInstance().currentUser
        binding.navView.menu.getItem(4).setVisible(false)
        if (user != null) {
            binding.navView.menu.getItem(4).setVisible(true)
        }

        // ----- 追加:ここから
        // ナビゲーションドロワーの設定
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.content.toolbar,
            R.string.app_name,
            R.string.app_name
        )
        binding.drawerLayout.addDrawerListener(toggle)
        binding.content.btnBuy.isVisible = false
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)
        binding.content.btnBuy.isVisible = false
        // ----- 追加:ここまで
        // ----- List Question -----
        // Firebase
        databaseReference = FirebaseDatabase.getInstance().reference

        // ListViewの準備
        adapter = DasboardListAdapter(this)
        adapter.setOnIncrementListener(this)
        foodArrayList = ArrayList()
        cartArrayList = ArrayList()
        orderArrayList = ArrayList()
        //adapter.setOnIncrementListener(this)
        adapter.notifyDataSetChanged()

        if (cartArrayList.size > 0) {
            binding.content.btnBuy.isVisible = true
        }
        binding.content.inner.txtTextview.isVisible = false
        binding.content.inner.etSearch.setOnClickListener() {
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(it.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)
            keySearch = binding.content.inner.etSearch.text.toString()
            // 選択したジャンルにリスナーを登録する
            if (genreRef != null) {
                genreRef!!.removeEventListener(eventListener)
            }
            foodArrayList.clear()
            genreRef = databaseReference.child(FoodsPATH).child(genre.toString())
            genreRef!!.addChildEventListener(eventListener)
        }
        binding.content.inner.listView.setOnItemClickListener() { _, _, position, _ ->
            // Questionのインスタンスを渡して質問詳細画面を起動する
            if (genre < 6) {
                val intent = Intent(applicationContext, FoodDetailActivity::class.java)
                intent.putExtra("food", foodArrayList[position])
                intent.putExtra("genre", genre)
                resultLauncher.launch(intent)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_settings) {
            val intent = Intent(this, SettingActivity::class.java)
            resultLauncher.launch(intent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        val navigationView = findViewById<NavigationView>(R.id.nav_view)

        // 1:趣味を既定の選択とする
        if (genre == 0) {
            onNavigationItemSelected(navigationView.menu.getItem(0))
        }
        val user = FirebaseAuth.getInstance().currentUser
        binding.navView.menu.getItem(4).setVisible(false)
        if (user != null) {
            binding.navView.menu.getItem(4).setVisible(true)
        }
    }

    // ----- 追加:ここから
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var user = FirebaseAuth.getInstance().currentUser
        when (item.itemId) {
            R.id.nav_vegetable -> {
                binding.content.toolbar.title = getString(R.string.menu_vegetable_label)
                genre = 1
            }
            R.id.nav_meat -> {
                binding.content.toolbar.title = getString(R.string.menu_meat_label)
                genre = 2
            }
            R.id.nav_drink -> {
                binding.content.toolbar.title = getString(R.string.menu_drink_label)
                genre = 3
            }
            R.id.nav_spice -> {
                binding.content.toolbar.title = getString(R.string.menu_spice_label)
                genre = 4
            }
            R.id.nav_bento -> {
                binding.content.toolbar.title = getString(R.string.menu_bento_label)
                genre = 5
            }
            R.id.nav_cart -> {
                if (user == null) {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                } else {
                    binding.content.toolbar.title = getString(R.string.menu_cart_label)
                    genre = 6
                }
            }
            R.id.nav_favorite -> {
                if (user == null) {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                } else {
                    binding.content.toolbar.title = getString(R.string.menu_favorite_label)
                    genre = 7
                }
            }
            R.id.nav_bought -> {
                if (user == null) {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                } else {
                    binding.content.toolbar.title = getString(R.string.menu_bought_label)
                    genre = 8
                }
            }
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        // ----- 追加:ここから -----
        // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
        foodArrayList.clear()
        adapter.setFoodArrayList(foodArrayList, genre, arrayListOf(),orderArrayList)
        binding.content.inner.listView.adapter = adapter

        // 選択したジャンルにリスナーを登録する
        if (genreRef != null) {
            genreRef!!.removeEventListener(eventListener)
        }
        if (genre < 6) {
            keySearch = ""
//            binding.content.fab.isVisible = true
            binding.content.inner.RLSearch.isVisible = true
            binding.content.inner.txtTextview.isVisible = false
            genreRef = databaseReference.child(FoodsPATH).child(genre.toString())
            genreRef!!.addChildEventListener(eventListener)
        }
        if (genre > 5) {
//            binding.content.fab.isVisible = false
            binding.content.inner.RLSearch.isVisible = false
        }
        // ----- 追加:ここまで -----
        if (genre == 7) {
            var foodFavorite = FavoriteFood.findAll()
            if (foodFavorite != null) {
                foodArrayList.clear()
                foodFavorite.forEach {
                    var id = it.foodIdFirebase
                    var name = it.name
                    var description = it.description
                    var price = it.price
                    var image = it.image
                    var g = it.genre
                    val byte = if (image.isNotEmpty()) {
                        Base64.decode(image, Base64.DEFAULT)
                    } else {
                        byteArrayOf()
                    }

                    var f = Food(id, name, description, price, g, byte, arrayListOf())
                    foodArrayList.add(f)
                }
                adapter.setFoodArrayList(foodArrayList, genre, arrayListOf(),orderArrayList)
                adapter.notifyDataSetChanged()
            }
        }
        if (genre != 6) {
            binding.content.btnBuy.isVisible = false
        }
        if (genre == 8) {
//            keySearch = ""
//            binding.content.fab.isVisible = true
//            binding.content.inner.RLSearch.isVisible = true
            if (user != null) {
                genreRef = databaseReference.child(OrdersPATH).child(user.uid)
                genreRef!!.addChildEventListener(eventListenerOrder)
            }
        }
        if (genre == 6) {
            binding.content.inner.txtTextview.isVisible = true
            binding.content.btnBuy.isVisible = true
            var foodCart = CartFood.findAll()
            if (foodCart != null) {
                foodArrayList.clear()
                cartArrayList.clear()
                foodCart.forEach {
                    var id = it.foodIdFirebase
                    var name = it.name
                    var description = it.description
                    var price = it.price
                    var image = it.image
                    var g = it.genre
                    val byte = if (image.isNotEmpty()) {
                        Base64.decode(image, Base64.DEFAULT)
                    } else {
                        byteArrayOf()
                    }
                    var num = it.number

                    var f = Food(id, name, description, price, g, byte, arrayListOf())
                    foodArrayList.add(f)
                    cartArrayList.add(it)
                }
                numberCarts = cartArrayList.size
                adapter.setFoodArrayList(foodArrayList, genre, cartArrayList, orderArrayList)
                adapter.notifyDataSetChanged()
            }
            var prices = 0.0
            for (i in cartArrayList) {
                prices += i.number * (i.price.toDouble())
            }
            binding.content.btnBuy.setOnClickListener() {
                val intent = Intent(this, OrderActivity::class.java)
                intent.putExtra(
                    KEY_PRICE,
                    prices
                )
                intent.putExtra(KEY_CART, cartArrayList)
                resultLauncher.launch(intent)
            }
            if (cartArrayList.size < 1) {
                binding.content.btnBuy.isVisible = false
            } else {
                binding.content.btnBuy.isVisible = true
            }
        }
        return true
    }

    override fun onNumberIncremented() {
        numberCarts -= 1
        if (numberCarts < 1) {
            binding.content.btnBuy.isVisible = false
        }
    }
    // ----- 追加:ここまで
}

const val KEY_CART = "key_cart"
const val KEY_PRICE = "key_price"
const val KEY_DETAIL = "key_detail"