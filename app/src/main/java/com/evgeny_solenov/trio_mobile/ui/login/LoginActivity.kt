package com.example.testandro6.ui.login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.testandro6.ConnectChecker
import com.example.testandro6.R
import com.example.testandro6.SendData
import com.example.testandro6.databinding.ActivityLoginBinding
import com.example.testandro6.ui.watch_note.WatchNoteFragment
import io.reactivex.internal.disposables.DisposableHelper.replace
import org.json.JSONException
import org.json.JSONObject


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private var pref: SharedPreferences? = null
    companion object{
        var responseStr = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

            val username = binding.username
            val password = binding.password
            val login = binding.login
            val register = binding.register
            val loading = binding.loading
            val constLay = binding.constLay
            val loginNot = binding.loginNot
            val passwordNot = binding.passwordNot
            val connection = binding.connection

            register?.setOnClickListener {
                var registerFragment = RegisterFragment()
                supportFragmentManager.commit {
                    setCustomAnimations(
                        R.anim.slide_in, // enter
                        R.anim.fade_out, // exit
                        R.anim.fade_in, // popEnter
                        R.anim.slide_out // popExit
                    )
                    replace(R.id.container, registerFragment)
                    addToBackStack(null)
                }
            }

            pref = getSharedPreferences("USER", Context.MODE_PRIVATE)
            Log.e("Sas", "PREF = " + pref?.getString("id", ""))
            if (pref?.getString("id", "") != "") {
                loading.visibility = View.VISIBLE
                constLay?.visibility = View.GONE
                loginNot?.visibility = View.GONE
                passwordNot?.visibility = View.GONE

                SendData.userName = pref?.getString("name", "")!!
                SendData.userLogin = pref?.getString("login", "")!!
                SendData.userPassword = pref?.getString("password", "")!!
                SendData.userId = pref?.getString("id", "")!!
                SendData.userRole = pref?.getString("role", "")!!

                val intent = Intent(Intent.ACTION_MAIN)
                intent.setClassName(
                    "com.example.testandro6",
                    "com.example.testandro6.MainActivity"
                )
                startActivity(intent)
                finish()
            }

        password.setOnEditorActionListener setOnEditorActionListener@{ v, actionId, event ->
            if (actionId  == EditorInfo.IME_ACTION_DONE) {
                return@setOnEditorActionListener true;
            }
            return@setOnEditorActionListener false;
        }
        username.afterTextChanged {
            if (password.text.length < 6){
                password.error = resources.getString(R.string.invalid_password)
                login.isEnabled = false
            } else {
                login.isEnabled = true
            }
        }
        password.apply {
            afterTextChanged {
                if (password.text.length < 6) {
                    password.error = resources.getString(R.string.invalid_password)
                    login.isEnabled = false
                } else {
                    login.isEnabled = true
                }
            }
            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        if (ConnectChecker.isOnline(context)) {
                            var th = Thread {
                                loginUser(username.text.toString(), password.text.toString())
                            }
                            th.start()
                            th.join()
                            //loginViewModel.login(username.text.toString(), password.text.toString())
                            Handler().postDelayed({
                                try {
                                    Log.e("Sas", "Respons login - $responseStr")
                                    var jso = JSONObject(responseStr)
                                    Log.e("Sas", jso.getString("line"))
                                    if (jso.getString("line").split(' ')[0] == "You") {
                                        Log.e("Sas", "Login")
                                        loading.visibility = View.VISIBLE
                                        constLay?.visibility = View.GONE
                                        loginNot?.visibility = View.GONE
                                        passwordNot?.visibility = View.GONE
                                        connection?.visibility = View.GONE

                                        SendData.userName = jso.getJSONObject("person").getString("name")
                                        SendData.userLogin = binding.username.text.toString()
                                        SendData.userPassword = binding.password.text.toString()
                                        SendData.userId = jso.getJSONObject("person").getInt("personId").toString()
                                        SendData.userRole = jso.getJSONObject("person").getString("role")

                                        saveData("name", SendData.userName)
                                        saveData("login", SendData.userLogin)
                                        saveData("password", SendData.userPassword)
                                        saveData("id", SendData.userId)
                                        saveData("role", SendData.userRole)

                                        val intent = Intent(Intent.ACTION_MAIN)
                                        intent.setClassName(
                                            "com.example.testandro6",
                                            "com.example.testandro6.MainActivity"
                                        )
                                        Toast.makeText(applicationContext, "Добро пожаловать!", Toast.LENGTH_SHORT).show()
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        if (jso.getString("line").split(' ')[0] == "Login") {
                                            loginNot?.visibility = View.VISIBLE
                                            passwordNot?.visibility = View.GONE
                                            connection?.visibility = View.GONE
                                        } else if (jso.getString("line").split(' ')[0] == "Password") {
                                            loginNot?.visibility = View.GONE
                                            passwordNot?.visibility = View.VISIBLE
                                            connection?.visibility = View.GONE
                                        } else {
                                            Log.e("Sas", "Not login")
                                            loading.visibility = View.GONE
                                            constLay?.visibility = View.VISIBLE
                                            connection?.visibility = View.VISIBLE
                                        }
                                    }
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                            }, 1000)
                        } else {
                            connection?.visibility = View.VISIBLE
                        }
                }
                Log.e("Login", "Pressed Enter")
                false
            }

            login.setOnClickListener {
                if (ConnectChecker.isOnline(context)) {
                    var th = Thread {
                        loginUser(username.text.toString(), password.text.toString())
                    }
                    th.start()
                    th.join()
                    Handler().postDelayed({
                        try {
                            Log.e("Sas", "Respons login - $responseStr")
                            var jso = JSONObject(responseStr)
                            Log.e("Sas", jso.getString("line"))
                            if (jso.getString("line").split(' ')[0] == "You") {
                                Log.e("Sas", "Login")
                                loading.visibility = View.VISIBLE
                                constLay?.visibility = View.GONE
                                loginNot?.visibility = View.GONE
                                passwordNot?.visibility = View.GONE
                                connection?.visibility = View.GONE

                                SendData.userName = jso.getJSONObject("person").getString("name")
                                SendData.userLogin = binding.username.text.toString()
                                SendData.userPassword = binding.password.text.toString()
                                SendData.userId = jso.getJSONObject("person").getInt("personId").toString()
                                SendData.userRole = jso.getJSONObject("person").getString("role")

                                saveData("name", SendData.userName)
                                saveData("login", SendData.userLogin)
                                saveData("password", SendData.userPassword)
                                saveData("id", SendData.userId)
                                saveData("role", SendData.userRole)

                                val intent = Intent(Intent.ACTION_MAIN)
                                intent.setClassName(
                                    "com.example.testandro6",
                                    "com.example.testandro6.MainActivity"
                                )
                                Toast.makeText(applicationContext, "Добро пожаловать!", Toast.LENGTH_SHORT).show()
                                startActivity(intent)
                                finish()
                            } else {
                                if (jso.getString("line").split(' ')[0] == "Login") {
                                    loginNot?.visibility = View.VISIBLE
                                    passwordNot?.visibility = View.GONE
                                    connection?.visibility = View.GONE
                                } else if (jso.getString("line").split(' ')[0] == "Password") {
                                    loginNot?.visibility = View.GONE
                                    passwordNot?.visibility = View.VISIBLE
                                    connection?.visibility = View.GONE
                                } else {
                                    Log.e("Sas", "Not login")
                                    loading.visibility = View.GONE
                                    constLay?.visibility = View.VISIBLE
                                    connection?.visibility = View.VISIBLE
                                }

                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }, 1000)
                } else {
                    connection?.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun saveData(name: String, res: String){
        val editor = pref?.edit()
        editor?.putString(name ,res)
        editor?.apply()
    }

    private fun loginUser(
        login: String,
        password: String
    ){
        val queue = Volley.newRequestQueue(this)
        var url = "http://${SendData.IPSERVER}:${SendData.PORTDB}/persons/login"
        var postparams = JSONObject()
        postparams.put("login",login)
        postparams.put("password", password)

        Log.e("Sas", postparams.toString())
        val request: JsonObjectRequest =
            object : JsonObjectRequest(Method.POST, url, postparams, Response.Listener { response ->
               try {
                    val jsonObject = response
                    Log.e("Sas", "Response $jsonObject")
                    responseStr = jsonObject.toString()
                } catch (e: JSONException) {
                    Toast.makeText(applicationContext, e.printStackTrace().toString(), Toast.LENGTH_SHORT)
                        .show()
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error -> // displaying toast message on response failure.
                Log.e("tag", "error is " + error!!.message)
            }) {
            }
        queue.add(request)
    }
}

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}