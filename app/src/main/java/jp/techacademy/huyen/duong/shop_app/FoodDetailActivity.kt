package jp.techacademy.huyen.duong.shop_app

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.addCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import jp.techacademy.huyen.duong.shop_app.databinding.ActivityFoodDetailBinding

class FoodDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFoodDetailBinding

    private lateinit var food: Food
    private lateinit var adapter: FoodDetailAdapter
    private lateinit var commentRef: DatabaseReference
    private var genre = 0

    private val eventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<*, *>

            val commentUid = dataSnapshot.key ?: ""

            for (comment in food.comments) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (commentUid == comment.commentUid) {
                    return
                }
            }

            val body = map["body"] as? String ?: ""
            val name = map["name"] as? String ?: ""
            val uid = map["uid"] as? String ?: ""
            val imageString = map["image"] as? String ?: ""
            val date = map["date"] as? String ?: ""
            val byte = if (imageString.isNotEmpty()) {
                Base64.decode(imageString, Base64.DEFAULT)
            } else {
                byteArrayOf()
            }

            val comment = Comment(uid,body, byte, date, commentUid)
            food.comments.add(comment)
            adapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
        override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
        override fun onCancelled(databaseError: DatabaseError) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        onBackPressedDispatcher.addCallback {
            mainActivityBack()
        }
        super.onCreate(savedInstanceState)
        binding = ActivityFoodDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 渡ってきたQuestionのオブジェクトを保持する
        // API33以上でgetSerializableExtra(key)が非推奨となったため処理を分岐
        @Suppress("UNCHECKED_CAST", "DEPRECATION", "DEPRECATED_SYNTAX_WITH_DEFINITELY_NOT_NULL")
        food = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.getSerializableExtra("food", Food::class.java)!!
        else
            intent.getSerializableExtra("food") as? Food!!

        title = food.name

        genre = intent.getIntExtra("genre",0)
        // ListViewの準備
        adapter = FoodDetailAdapter(this, food, genre)
        binding.listView.adapter = adapter
        adapter.notifyDataSetChanged()
        binding.btnReturn.setOnClickListener() {
            mainActivityBack()
        }

        val dataBaseReference = FirebaseDatabase.getInstance().reference
        commentRef = dataBaseReference.child(FoodsPATH).child(genre.toString())
            .child(food.foodUid).child(CommentsPATH)
        commentRef.addChildEventListener(eventListener)
    }
    fun mainActivityBack() {
        val intentSub = Intent()
        intentSub.putExtra(KEY_DETAIL, "OK")
        setResult(RESULT_OK, intentSub)
        finish()
    }

}
