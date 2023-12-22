package jp.techacademy.huyen.duong.shop_app

import android.Manifest
import android.app.Activity
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
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import jp.techacademy.huyen.duong.shop_app.databinding.ActivityCommentSendBinding
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class CommentSendActivity : AppCompatActivity(), View.OnClickListener,
    DatabaseReference.CompletionListener {
    private lateinit var binding: ActivityCommentSendBinding

    private lateinit var comment: Comment
    private lateinit var food: Food
    private var pictureUri: Uri? = null
    private var genre = 0

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentSendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 渡ってきたQuestionのオブジェクトを保持する
        // API33以上でgetSerializableExtra(key)が非推奨となったため処理を分岐
        @Suppress("UNCHECKED_CAST", "DEPRECATION", "DEPRECATED_SYNTAX_WITH_DEFINITELY_NOT_NULL")
        food = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.getSerializableExtra("food", Food::class.java)!!
        else
            intent.getSerializableExtra("food") as? Food!!

        genre = intent.getIntExtra("genre", 0)

        // UIの準備
        title = getString(R.string.comment_name)
        binding.sendButton.setOnClickListener(this)
        binding.imageView.setOnClickListener(this)
    }

    /**
     * このActivityに戻ってきた時の処理
     */
    private var launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val resultCode: Int = result.resultCode
        val data: Intent? = result.data

        if (resultCode != Activity.RESULT_OK) {
            if (pictureUri != null) {
                contentResolver.delete(pictureUri!!, null, null)
                pictureUri = null
            }
            return@registerForActivityResult
        }

        // 画像を取得
        val uri = if (data == null || data.data == null) pictureUri else data.data

        // URIからBitmapを取得する
        val image: Bitmap
        try {
            val contentResolver = contentResolver
            val inputStream = contentResolver.openInputStream(uri!!)
            image = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
        } catch (e: Exception) {
            return@registerForActivityResult
        }

        // 取得したBimapの長辺を500ピクセルにリサイズする
        val imageWidth = image.width
        val imageHeight = image.height
        val scale =
            (500.toFloat() / imageWidth).coerceAtMost(500.toFloat() / imageHeight) // (1)

        val matrix = Matrix()
        matrix.postScale(scale, scale)

        val resizedImage =
            Bitmap.createBitmap(image, 0, 0, imageWidth, imageHeight, matrix, true)

        // BitmapをImageViewに設定する
        binding.imageView.setImageBitmap(resizedImage)

        pictureUri = null
    }

    override fun onClick(v: View) {
        if (v === binding.imageView) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // 許可されている
                showChooser()
            } else {
                // パーミッションの許可状態を確認する
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // 許可されている
                    showChooser()
                } else {
                    // 許可されていないので許可ダイアログを表示する
                    requestPermissions(
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        CommentSendActivity.PERMISSIONS_REQUEST_CODE
                    )

                    return
                }
            }
        } else if (v === binding.sendButton) {
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)

            val dataBaseReference = FirebaseDatabase.getInstance().reference
            val genreRef = dataBaseReference.child(FoodsPATH).child(genre.toString()).child(food.foodUid).child(CommentsPATH)

            val data = HashMap<String, String>()

            // UID
            data["uid"] = FirebaseAuth.getInstance().currentUser!!.uid

            // タイトルと本文を取得する
            val body = binding.commentEditText.text.toString()

            // Preferenceから名前を取る
            val sp = PreferenceManager.getDefaultSharedPreferences(this)
            //val name = sp.getString(NameKEY, "")

            data["body"] = body
            val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
            val currentDate = sdf.format(Date())
            data["date"] = currentDate

            // 添付画像を取得する
            val drawable = binding.imageView.drawable as? BitmapDrawable

            // 添付画像が設定されていれば画像を取り出してBASE64エンコードする
            if (drawable != null) {
                val bitmap = drawable.bitmap
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
                val bitmapString =
                    Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)

                data["image"] = bitmapString
            }

            genreRef.push().setValue(data, this)
            binding.progressBar.visibility = View.VISIBLE
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CommentSendActivity.PERMISSIONS_REQUEST_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // ユーザーが許可したとき
                    showChooser()
                }
                return
            }
        }
    }

    private fun showChooser() {
        // ギャラリーから選択するIntent
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = "image/*"
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE)

        // カメラで撮影するIntent
        val filename = System.currentTimeMillis().toString() + ".jpg"
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, filename)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        pictureUri = contentResolver
            .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri)

        // ギャラリー選択のIntentを与えてcreateChooserメソッドを呼ぶ
        val chooserIntent = Intent.createChooser(galleryIntent, getString(R.string.get_image))

        // EXTRA_INITIAL_INTENTSにカメラ撮影のIntentを追加
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))

        launcher.launch(chooserIntent)
    }

    override fun onComplete(databaseError: DatabaseError?, databaseReference: DatabaseReference) {
        binding.progressBar.visibility = View.GONE

        if (databaseError == null) {
            finish()
        } else {
            Snackbar.make(
                findViewById(android.R.id.content),
                getString(R.string.question_send_error_message),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }
}