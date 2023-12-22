package jp.techacademy.huyen.duong.shop_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import jp.techacademy.huyen.duong.shop_app.databinding.ActivitySettingBinding

class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding

    private lateinit var databaseReference: DatabaseReference
    private var check = true

    var resultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val intent = result.data
            if (intent != null) {
                val res = intent.getStringExtra(KEY_RESULT_LOGIN).toString()
                if (res == "OK") {
                    binding.nameText.setText("")
                    check = true
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        onBackPressedDispatcher.addCallback {
            mainActivityBack()
        }
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Preferenceから表示名を取得してEditTextに反映させる
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val name = sp.getString(NameKEY, "")
        binding.nameText.setText(name)

        databaseReference = FirebaseDatabase.getInstance().reference

        // UIの初期設定
        title = getString(R.string.settings_title)

        binding.changeButton.setOnClickListener { v ->
            // キーボードが出ていたら閉じる
            val im = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていない場合は何もしない
                Snackbar.make(v, getString(R.string.no_login_user), Snackbar.LENGTH_LONG).show()
            } else {
                // 変更した表示名をFirebaseに保存する
                val name2 = binding.nameText.text.toString()
                val userRef = databaseReference.child(UsersPATH).child(user.uid)
                val data = HashMap<String, String>()
                data["name"] = name2
                userRef.setValue(data)

                // 変更した表示名をPreferenceに保存する
                val sp2 = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                val editor = sp2.edit()
                editor.putString(NameKEY, name2)
                editor.apply()

                Snackbar.make(v, getString(R.string.change_display_name), Snackbar.LENGTH_LONG)
                    .show()
            }
        }
        val user = FirebaseAuth.getInstance().currentUser
        check = (user == null)
        if (check) {
            binding.nameText.setText("")
            binding.logoutButton.setText(getString(R.string.login_title))
        } else {
            binding.logoutButton.setText("ログアウト")
        }
        binding.logoutButton.setOnClickListener { v ->
            if (check) {
                val intent = Intent(v.context, LoginActivity::class.java)
                resultLauncher.launch(intent)
                check = false
            } else {
                FirebaseAuth.getInstance().signOut()
                binding.nameText.setText("")
                check = true
                binding.logoutButton.setText(getString(R.string.login_title))
                Snackbar.make(v, getString(R.string.logout_complete_message), Snackbar.LENGTH_LONG)
                    .show()
            }
        }
    }

    fun mainActivityBack() {
        val intentSub = Intent()
        intentSub.putExtra(KEY_RESULT, "OK")
        setResult(RESULT_OK, intentSub)
        finish()
    }
}

const val KEY_RESULT = "key_result"
const val KEY_RESULT_LOGIN = "key_result_login"
