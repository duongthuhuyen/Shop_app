package jp.techacademy.huyen.duong.shop_app

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import jp.techacademy.huyen.duong.shop_app.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var createAccountListener: OnCompleteListener<AuthResult>
    private lateinit var loginListener: OnCompleteListener<AuthResult>
    private lateinit var databaseReference: DatabaseReference

    // アカウント作成時にフラグを立て、ログイン処理後に名前をFirebaseに保存する
    private var isCreateAccount = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseReference = FirebaseDatabase.getInstance().reference

        // FirebaseAuthのオブジェクトを取得する
        auth = FirebaseAuth.getInstance()

        // アカウント作成処理のリスナー
        createAccountListener = OnCompleteListener { task ->
            if (task.isSuccessful) {
                // 成功した場合
                // ログインを行う
//                val email = binding.emailText.text.toString()
//                val password = binding.passwordText.text.toString()
//                val user = auth.currentUser
                val userRef = databaseReference.child(UsersPATH).child(task.result.user!!.uid)
                    // アカウント作成の時は表示名をFirebaseに保存する
                val name = binding.etName.text.toString()

                val data = HashMap<String, String>()
                data["name"] = name
                userRef.setValue(data)
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {

                // 失敗した場合
                // エラーを表示する
                val view = findViewById<View>(android.R.id.content)
                Snackbar.make(
                    view,
                    getString(R.string.create_account_failure_message),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
        // タイトルの設定
        title = getString(R.string.login_title)

        binding.btnRegister.setOnClickListener { v ->
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val name = binding.etName.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (email.isNotEmpty() && password.length >= 6 && name.isNotEmpty() && password == confirmPassword) {
                // ログイン時に表示名を保存するようにフラグを立てる
                isCreateAccount = true

                createAccount(email, password)
            } else {
                // エラーを表示する
                Snackbar.make(v, getString(R.string.login_error_message), Snackbar.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun createAccount(email: String, password: String) {
        // アカウントを作成する
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(createAccountListener)
    }
}