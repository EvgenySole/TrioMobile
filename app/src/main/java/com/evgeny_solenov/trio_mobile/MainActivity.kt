package com.example.testandro6
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.testandro6.databinding.ActivityMainBinding
import com.example.testandro6.ui.add_note_done.AddNoteDoneFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONException
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var prefSave: SharedPreferences? = null
    private var strDepart: StringBuilder = StringBuilder("")
    private var strEquip: StringBuilder = StringBuilder("")
    private var strType: StringBuilder = StringBuilder("")
    private var strWasteT: StringBuilder = StringBuilder("")
    private var strWasteG: StringBuilder = StringBuilder("")
    private var pref: SharedPreferences? = null

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pref = getSharedPreferences("ADD_NOTE", Context.MODE_PRIVATE)
        val userId = binding.textView6
        val userName = binding.textView8
        userId.text = "ID: 00000" + SendData.userId
        userName.text = SendData.userName
        val navView: BottomNavigationView = binding.bottomNavigationView
        val navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),122)
        }
        val threeCheck = Thread {
            apiExecute("departments")
        }
        threeCheck.start()
        threeCheck.join()
        if (ConnectChecker.isOnline(this.baseContext) && !SendData.isBadConnection) {
            val thread = Thread {
                strDepart = apiExecute("departments")
                strEquip = apiExecute("equipment_objects")
                strType = apiExecute("event_types")
                strWasteG = apiExecute("waste_groups")
                strWasteT = apiExecute("waste_types")
            }
            thread.start()
            thread.join()
            saveData("strDepart", strDepart.toString())
            saveData("strEquip", strEquip.toString())
            saveData("strType", strType.toString())
            saveData("strWasteG", strWasteG.toString())
            saveData("strWasteT", strWasteT.toString())
            prefSave = getSharedPreferences("NEW_NOTES", Context.MODE_PRIVATE)
            Log.e("NEW_NOTES", prefSave?.all.toString())
            var saveEvent1: JSONObject
            var count = 0
            while (prefSave?.contains("saveNote$count") == true){
                saveEvent1 = JSONObject(prefSave?.getString("saveNote$count", "").toString())
                addData(saveEvent1)
                prefSave?.edit()?.remove("saveNote$count")?.apply()
                count++
            }
            Log.e("NEW_NOTES", prefSave?.all.toString())
        } else {
            Thread {
                while (true){
                    apiExecute("departments")
                    if (ConnectChecker.isOnline(this.baseContext) && !SendData.isBadConnection){
                        runOnUiThread {
                            Toast.makeText(this, "Подключение восстановлено", Toast.LENGTH_SHORT).show()
                        }
                        prefSave = getSharedPreferences("NEW_NOTES", Context.MODE_PRIVATE)
                        var saveEvent2: JSONObject
                        var count = 0
                        while (prefSave?.contains("saveNote$count") == true){
                            saveEvent2 = JSONObject(prefSave?.getString("saveNote$count", "").toString())
                            addData(saveEvent2)
                            prefSave?.edit()?.remove("saveNote$count")?.apply()
                            count++
                        }
                        break
                    }
                    Thread.sleep(2000)
                }
            }.start()
        }
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true /* enabled by default */) {
            override fun handleOnBackPressed() {
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun addData(
        event: JSONObject
    ) {
        val queue = Volley.newRequestQueue(this)
        val url = "http://${SendData.IPSERVER}:${SendData.PORTDB}/events"
        if (ConnectChecker.isOnline(this) && !SendData.isBadConnection) {
            val request: JsonObjectRequest =
                @SuppressLint("CommitPrefEdits")
                object : JsonObjectRequest(Method.POST, url, event, Response.Listener { response ->
                    Toast.makeText(this, "Запись отправлена!", Toast.LENGTH_SHORT).show()
                    try {
                        val jsonObject = response
                        Log.e("SasResponse", jsonObject.toString())
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }, Response.ErrorListener { error ->
                    Log.e("tag", "error is " + error!!.message)
                }) {
                }
            queue.add(request)
        }
    }

    private fun saveData(name: String, res: String){
        val editor = pref?.edit()
        editor?.putString(name ,res)
        editor?.apply()
    }

    fun apiExecute(table: String): StringBuilder {
            val policy = StrictMode.ThreadPolicy.Builder()
                .permitAll().build()
            StrictMode.setThreadPolicy(policy)
            val myUrl = "http://${SendData.IPSERVER}:${SendData.PORTDB}/" + table
            var result = StringBuilder("")
            try {
                val url: URL
                var urlConnection: HttpURLConnection? = null
                try {
                    url = URL(myUrl)
                    urlConnection = url.openConnection() as HttpURLConnection
                    urlConnection.connectTimeout = 1000 * 5
                    val `in` = urlConnection.inputStream
                    val isw = InputStreamReader(`in`)
                    var data = isw.read()
                    while (data != -1) {
                        result.append(data.toChar())
                        data = isw.read()
                    }
                    SendData.isBadConnection = false
                    Log.e("Connection", "IsGood")
                    return result
                } catch (e: Exception) {
                    e.printStackTrace()
                    if (e is ConnectException || e is SocketTimeoutException){
                        SendData.isBadConnection = true
                        Toast.makeText(this, "Нет подключения с сервером", Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    urlConnection?.disconnect()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return StringBuilder("Exception: " + e.message)
            }
            return result
    }
}