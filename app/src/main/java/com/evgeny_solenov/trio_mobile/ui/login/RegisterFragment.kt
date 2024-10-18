package com.example.testandro6.ui.login

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.testandro6.ConnectChecker
import com.example.testandro6.SendData
import com.example.testandro6.databinding.FragmentRegisterBinding
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private var pref: SharedPreferences? = null
    companion object{
        var responseStr = ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val username = binding.username
        val login = binding.login
        val password = binding.password
        val passwordRe = binding.passwordRe
        val usernameNot = binding.usernameNot
        val loginNot = binding.loginNot
        val passwordNot = binding.passwordNot
        val passwordReNot = binding.passwordReNot
        val register = binding.register
        register.isEnabled = true
        val connection = binding.connection
        val constLay = binding.constLay
        val loading = binding.loading

        username.afterTextChanged {
            if (username.text.length < 1){
                username.error = "Не верное имя"
            } else {
                username.setError(null)
            }
        }
        login.afterTextChanged {
            if (username.text.length < 1){
                username.error = "Не верное имя"
            }
            if (login.text.length < 1){
                login.error = "Не верный логин"
            } else {
                login.setError(null)
            }
        }
        password.afterTextChanged {
            if (password.text.length < 6){
                password.error = "Пароль должен быть длиннее 5 знаков"
            } else {
                password.setError(null)
            }
        }
        passwordRe.afterTextChanged {
            if (password.text.toString() != passwordRe.text.toString()){
                passwordRe.error = "Пароли не совпадают"
            } else {
                passwordRe.setError(null)
            }
        }


        passwordRe.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE ->
                    if (ConnectChecker.isOnline(requireContext())) {
                        var th = Thread {
                            registerUser(username.text.toString(), login.text.toString(), password.text.toString())
                        }
                        th.start()
                        th.join()
                        Handler().postDelayed({
                            try {
                                Log.e("Sas", "Respons login - ${responseStr}")
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
                                    SendData.userId = jso.getJSONObject("person").getInt("personId").toString()
                                    SendData.userRole = jso.getJSONObject("person").getString("role")

                                    saveData("name", SendData.userName)
                                    saveData("id", SendData.userId)
                                    saveData("role", SendData.userRole)

                                    val intent = Intent(Intent.ACTION_MAIN)
                                    intent.setClassName(
                                        "com.example.testandro6",
                                        "com.example.testandro6.MainActivity"
                                    )
                                    startActivity(intent)
                                    activity?.finish()
                                } else {
                                    if (jso.getString("line").split(' ')[0] == "Login") {
                                        loginNot?.visibility = View.VISIBLE
                                        passwordNot?.visibility = View.GONE
                                        connection?.visibility = View.GONE
                                    } else if (jso.getString("line").split(' ')[0] == "Password") {
//                                        loginNot?.visibility = View.GONE
//                                        passwordNot?.visibility = View.VISIBLE
//                                        connection?.visibility = View.GONE
                                    }
                                    Log.e("Sas", "Not login")
                                    loading.visibility = View.GONE
                                    constLay?.visibility = View.VISIBLE
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
        ///////
        register.setOnClickListener {
            if (username.error == null && login.error == null && password.error == null && passwordRe.error == null) {
                Toast.makeText(requireContext(), "Register", Toast.LENGTH_SHORT).show()
                if (ConnectChecker.isOnline(requireContext())) {
                    var th = Thread {
                        registerUser(username.text.toString(), login.text.toString(), password.text.toString())
                    }
                    th.start()
                    th.join()
                    //loginViewModel.login(username.text.toString(), password.text.toString())
                    Handler().postDelayed({
                        try {
                            Log.e("Sas", "Respons login - ${responseStr}")
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
                                SendData.userId = jso.getJSONObject("person").getInt("personId").toString()
                                SendData.userRole = jso.getJSONObject("person").getString("role")

                                saveData("name", SendData.userName)
                                saveData("id", SendData.userId)
                                saveData("role", SendData.userRole)

                                val intent = Intent(Intent.ACTION_MAIN)
                                intent.setClassName(
                                    "com.example.testandro6",
                                    "com.example.testandro6.MainActivity"
                                )
                                startActivity(intent)
                                activity?.finish()
                            } else {
                                if (jso.getString("line").split(' ')[0] == "Login") {
                                    loginNot?.visibility = View.VISIBLE
                                    passwordNot?.visibility = View.GONE
                                    connection?.visibility = View.GONE
                                } else if (jso.getString("line").split(' ')[0] == "Password") {
//                                    loginNot?.visibility = View.GONE
//                                    passwordNot?.visibility = View.VISIBLE
//                                    connection?.visibility = View.GONE
                                }
                                Log.e("Sas", "Not login")
                                loading.visibility = View.GONE
                                constLay?.visibility = View.VISIBLE
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



        return root
    }

    private fun saveData(name: String, res: String){
        val editor = pref?.edit()
        editor?.putString(name ,res)
        editor?.apply()
    }

    private fun registerUser(username: String, login: String, password: String) {
        val queue = Volley.newRequestQueue(requireContext())
        var url = "http://${SendData.IPSERVER}:${SendData.PORTDB}/persons/register"
        var postparams = JSONObject()
        var personRealId = JSONArray()
        var personRealIdObj = JSONObject()
        personRealIdObj.put("name",username)
        personRealId.put(personRealIdObj)
        postparams.put("personRealId", personRealId)
        postparams.put("login",login)
        postparams.put("password", password)

        Log.e("Sas", postparams.toString())
        val request: JsonObjectRequest =
            object : JsonObjectRequest(Method.POST, url, postparams, Response.Listener { response ->
                //Toast.makeText(applicationContext, "Добро пожаловать!", Toast.LENGTH_SHORT).show()

                try {
                    // on below line we are extracting data from our json object
                    // and passing our response to our json object.
                    val jsonObject = response
                    Log.e("Sas", "Response $jsonObject")
                    responseStr = jsonObject.toString()
                    // creating a string for our output.
//                    val result = "User Name : " + jsonObject.getString("name") + "\n" + "Job : " +
//                            jsonObject.getString("job") + "\n" + "Updated At : " +
//                            jsonObject.getString("updatedAt")

                    // on below line we are setting
                    // our string to our text view.
//                    Toast.makeText(applicationContext, "Result - $responseStr", Toast.LENGTH_LONG).show()
//
                    //resultTV.setText(result)
                } catch (e: JSONException) {
                    Toast.makeText(requireContext(), e.printStackTrace().toString(), Toast.LENGTH_SHORT)
                        .show()
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error -> // displaying toast message on response failure.
                Log.e("tag", "error is " + error!!.message)
//                Toast.makeText(this, "Fail to update data..", Toast.LENGTH_SHORT)
//                    .show()
            }) {
                override fun getParams(): MutableMap<String, String>? {

                    // below line we are creating a map for storing
                    // our values in key and value pair.
                    val params: MutableMap<String,String> = HashMap()

                    // on below line we are passing our key
                    // and value pair to our parameters.
                    params["login"] = login
                    params["password"] = password
                    // at last we are
                    // returning our params.
                    return params
                }
            }
        // below line is to make
        // a json object request.
        queue.add(request)
    }

}