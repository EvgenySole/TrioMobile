package com.example.testandro6.ui.watch_message

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.testandro6.R
import com.example.testandro6.SendData
import com.example.testandro6.StompUtils
import com.example.testandro6.databinding.FragmentWatchMessageBinding
import com.google.android.material.navigation.NavigationBarView
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import ua.naiksoftware.stomp.Stomp
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*


class WatchMessageFragment : Fragment() {
    private var navBar: NavigationBarView? = null
    var strMessages: StringBuilder = StringBuilder("")
    var lastMessage = ""
    var lastDate = ""
    var chatId = -1L
    private var _binding: FragmentWatchMessageBinding? = null
    private val binding get() = _binding!!
    var inputText: String? = ""

    @SuppressLint("CheckResult", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWatchMessageBinding.inflate(inflater, container, false)
        val root: View = binding.root

        navBar = requireActivity().findViewById(R.id.bottomNavigationView)
        navBar?.visibility = View.GONE

        val sendButton = binding.buttonSend
        val message = binding.etMessage
        var scroll = binding.scroll
        var liner = binding.liner
        var coPerson = binding.textNumEvent

        var pref = activity?.getSharedPreferences("USER", Context.MODE_PRIVATE)
        var JSONcurrentChat = JSONObject("{\"Num\":\"333\"}")
        var JSONmyMessages: JSONArray? = null
        var threa = Thread {
            if (SendData.data[0] == '1'){
                Thread.sleep(1000)
                var temp = JSONArray((apiExecute("chats")).toString())
                SendData.data = SendData.data.substring(1)
                JSONcurrentChat = temp.getJSONObject(temp.length()-1)
            } else {
                JSONcurrentChat = JSONObject(SendData.data)
            }
            chatId = JSONcurrentChat.getLong("chatId")
            strMessages = apiExecute("messages/chat/$chatId")
            Log.e("Messages --", strMessages.toString())
            if (!strMessages.equals("")){
                JSONmyMessages = JSONArray(strMessages.toString())
            }
        }
        threa.start()
        threa.join()

        activity?.runOnUiThread {
            if (JSONcurrentChat?.getJSONObject("person1Id")?.getString("personId")
                == SendData.userId) {
                coPerson.text = JSONcurrentChat?.getJSONObject("person2Id")
                    ?.getJSONArray("personRealId")?.getJSONObject(0)?.getString("name")
            } else {
                coPerson.text = JSONcurrentChat?.getJSONObject("person1Id")
                    ?.getJSONArray("personRealId")?.getJSONObject(0)?.getString("name")
            }
        }
        val buttonBack = binding.button
        buttonBack.setOnClickListener {
            if (lastMessage == "" && lastDate == ""){
                var threa = Thread {
                    reloadLastMessageAndDate("Нет сообщений",
                        LocalDateTime.now().toString().replace('T', ' '), chatId)
                }
                threa.start()
                threa.join()
            }
            activity?.supportFragmentManager?.popBackStack()
        }
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true /* enabled by default */) {
            override fun handleOnBackPressed() {
                if (lastMessage == "" && lastDate == ""){
                    var threa = Thread {
                        reloadLastMessageAndDate("Нет сообщений",
                            LocalDateTime.now().toString().replace('T', ' '), chatId)
                    }
                    threa.start()
                    threa.join()
                }
                activity?.supportFragmentManager?.popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        for (i in 0..<(JSONmyMessages?.length() ?: 0)) {
            val JSONObject = JSONmyMessages?.getJSONObject(i)
            var requestNew = TextView(context)
            var requestNewTime = TextView(context)
            activity?.runOnUiThread {
                if (JSONObject?.getJSONObject("senderId")?.getString("personId") == SendData.userId){
                    requestNew.gravity = Gravity.END
                    requestNewTime.gravity = Gravity.END
                }
                requestNew.setPadding(resources.getDimension(R.dimen.top_padding_date_journals).toInt(),
                    resources.getDimension(R.dimen.left_padding_date_journals).toInt(),
                    resources.getDimension(R.dimen.top_padding_date_journals).toInt(), 0)
                requestNew.textSize = resources.getDimension(R.dimen.text_size_note_journals)
                requestNew.setTextColor(Color.BLACK)
                requestNew.text = JSONObject?.getString("content")
                requestNewTime.setPadding(resources.getDimension(R.dimen.left_padding_img_journals).toInt(),
                    0, resources.getDimension(R.dimen.top_padding_img_journals).toInt(),
                    resources.getDimension(R.dimen.left_padding_img_journals).toInt())
                requestNewTime.textSize = resources.getDimension(R.dimen.text_size_date_journals)
                requestNewTime.setTextColor(Color.GRAY)
                var timeTarget = JSONObject?.getString("dateTime")?.split('T')?.get(1)?.split('.')?.get(0)
                var dateTarget = JSONObject?.getString("dateTime")?.split('T')?.get(0)?.split('-')?.get(2) + "." +
                        JSONObject?.getString("dateTime")?.split('T')?.get(0)?.split('-')?.get(1)
                requestNewTime.text = timeTarget + "  " + dateTarget
                liner.addView(requestNew)
                liner.addView(requestNewTime)
            }
        }
        activity?.runOnUiThread {
            scroll.post { scroll.fullScroll(ScrollView.FOCUS_DOWN) }
        }
        if ((JSONmyMessages?.length() ?: 0) > 0){
            lastDate = JSONmyMessages?.getJSONObject(JSONmyMessages!!.length()-1)?.getString("dateTime").toString()
            Log.e("ChatDate", lastDate)
        }
        val stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP,
            "ws://${SendData.IPSERVER}:${SendData.PORTDB}/hello/websocket")
        Log.i("Stomp" ,"Start connecting to server")
        stompClient.connect()
        StompUtils.lifecycle(stompClient)

        Log.i("Stomp", "Subscribe chat endpoint to receive response")
        stompClient.topic("/user/${SendData.userId}/queue/messages").subscribe({
            try {
                val jsonObject = JSONObject(it.payload)
                Log.i("Stomp", "Receive: $jsonObject")
                activity?.runOnUiThread {
                    var responseNew = TextView(context)
                    var requestNewTime = TextView(context)
                    responseNew.setPadding(resources.getDimension(R.dimen.top_padding_date_journals).toInt(),
                        resources.getDimension(R.dimen.left_padding_date_journals).toInt(),
                        resources.getDimension(R.dimen.top_padding_date_journals).toInt(), 0)
                    responseNew.textSize = resources.getDimension(R.dimen.text_size_note_journals)
                    responseNew.setTextColor(Color.BLACK)
                    responseNew.text = jsonObject.getString("content")
                    requestNewTime.gravity = Gravity.END
                    requestNewTime.setPadding(resources.getDimension(R.dimen.left_padding_img_journals).toInt(),
                        0, resources.getDimension(R.dimen.top_padding_img_journals).toInt(),
                        resources.getDimension(R.dimen.left_padding_img_journals).toInt())
                    requestNewTime.textSize = resources.getDimension(R.dimen.text_size_date_journals)
                    requestNewTime.setTextColor(Color.GRAY)
                    lastMessage = jsonObject.getString("content")
                    var timeTarget = lastDate.split(' ').get(1).split('.').get(0)
                    var dateTarget = lastDate.split(' ').get(0).split('-').get(2) + "." +
                            lastDate.split(' ').get(0).split('-').get(1)
                    requestNewTime.text = timeTarget + "  " + dateTarget
                    liner.addView(responseNew)
                    liner.addView(requestNewTime)
                    scroll.post { scroll.fullScroll(ScrollView.FOCUS_DOWN) }
                    if (chatId > -1L && lastMessage != "" && lastDate != ""){
                        var threa = Thread {
                            reloadLastMessageAndDate(lastMessage, lastDate, chatId)
                        }
                        threa.start()
                        threa.join()
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }, {
        } )

        sendButton.setOnClickListener {
            if (message.text.toString() != "") {
                val jsonObject = JSONObject()
                try {
                    var senderJSON = JSONObject()
                    senderJSON.put("personId", SendData.userId.toInt())
                    jsonObject.put("senderId", senderJSON)
                    var getterJSON = JSONObject()
                    if (JSONcurrentChat?.getJSONObject("person1Id")?.getInt("personId") == SendData.userId.toInt()) {
                        getterJSON.put("personId", JSONcurrentChat?.getJSONObject("person2Id")?.getInt("personId"))
                    } else {
                        getterJSON.put("personId", JSONcurrentChat?.getJSONObject("person1Id")?.getInt("personId"))
                    }
                    jsonObject.put("getterId", getterJSON)
                    jsonObject.put("content", message.text)
                    var chatIdJSON = JSONObject()
                    chatIdJSON.put("chatId", JSONcurrentChat?.getInt("chatId"))
                    jsonObject.put("chatId", chatIdJSON)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                stompClient.send("/app/chat", jsonObject.toString()).subscribe()
                activity?.runOnUiThread {
                    var requestNew = TextView(context)
                    var requestNewTime = TextView(context)
                    requestNew.gravity = Gravity.END
                    requestNew.setPadding(resources.getDimension(R.dimen.top_padding_date_journals).toInt(),
                        resources.getDimension(R.dimen.left_padding_date_journals).toInt(),
                        resources.getDimension(R.dimen.top_padding_date_journals).toInt(), 0)
                    requestNew.textSize = resources.getDimension(R.dimen.text_size_note_journals)
                    requestNew.setTextColor(Color.BLACK)
                    requestNew.text = message.text
                    requestNewTime.gravity = Gravity.END
                    requestNewTime.setPadding(resources.getDimension(R.dimen.left_padding_img_journals).toInt(),
                        0, resources.getDimension(R.dimen.top_padding_img_journals).toInt(),
                        resources.getDimension(R.dimen.left_padding_img_journals).toInt())
                    requestNewTime.textSize = resources.getDimension(R.dimen.text_size_date_journals)
                    requestNewTime.setTextColor(Color.GRAY)

                    lastMessage = message.text.toString()
                    lastDate = LocalDateTime.now().toString().replace('T', ' ')
                    var timeTarget = lastDate.split(' ').get(1).split('.').get(0)
                    var dateTarget = lastDate.split(' ').get(0).split('-').get(2) + "." +
                            lastDate.split(' ').get(0).split('-').get(1)
                    requestNewTime.text = timeTarget + "  " + dateTarget
                    message.setText("")
                    liner.addView(requestNew)
                    liner.addView(requestNewTime)
                    scroll.post { scroll.fullScroll(ScrollView.FOCUS_DOWN) }
                    if (chatId > -1L && lastMessage != "" && lastDate != "") {
                        var threa = Thread {
                            reloadLastMessageAndDate(lastMessage, lastDate, chatId)
                        }
                        threa.start()
                        threa.join()
                    }
                }
            }
        }
        return root
    }

    private fun reloadLastMessageAndDate(lastMessage: String, lastDate: String, chatId: Long) {
        val queue = Volley.newRequestQueue(context)
        var url = "http://${SendData.IPSERVER}:${SendData.PORTDB}" +
                "/chats/lastMsgAndDate/$chatId?lastMessage=$lastMessage&lastDate=$lastDate"
        var postparams = JSONObject()
        postparams.put("lastMessage", lastMessage)
        postparams.put("lastDate", lastDate)

        Log.e("LastParams", postparams.toString())

        val request: JsonObjectRequest =
            object : JsonObjectRequest(Method.PUT, url, null, Response.Listener { response ->
               // Toast.makeText(context, "Сообщение отправлено", Toast.LENGTH_SHORT).show()
                try {
                    val jsonObject = response
                    Log.e("LastResponse", jsonObject.toString())
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error ->
                Log.e("tag", "error is " + error!!.message)
            }) {
            }
        queue.add(request)
    }

    fun apiExecute(table: String): StringBuilder{
        val policy = StrictMode.ThreadPolicy.Builder()
            .permitAll().build()
        StrictMode.setThreadPolicy(policy)
        val myUrl = "http://${SendData.IPSERVER}:${SendData.PORTDB}/$table"
        var result = StringBuilder("")
        try {
            val url: URL
            var urlConnection: HttpURLConnection? = null
            try {
                url = URL(myUrl)
                urlConnection = url.openConnection() as HttpURLConnection
                val `in` = urlConnection.inputStream
                val isw = InputStreamReader(`in`)
                var data = isw.read()
                while (data != -1) {
                    result.append(data.toChar())
                    data = isw.read()
                }
                return result
            } catch (e: Exception) {
                e.printStackTrace()
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