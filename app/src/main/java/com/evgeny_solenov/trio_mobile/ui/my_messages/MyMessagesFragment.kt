package com.example.testandro6.ui.my_messages

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.testandro6.ConnectChecker
import com.example.testandro6.R
import com.example.testandro6.SendData
import com.example.testandro6.databinding.FragmentMyMessagesBinding
import com.google.android.material.navigation.NavigationBarView
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.sql.Timestamp


class MyMessagesFragment : Fragment() {
    private var navBar: NavigationBarView? = null
    private var photoView: Bitmap? = null
    var strChats: StringBuilder = StringBuilder("")
    var strPersons: StringBuilder = StringBuilder("")
    var bmpArray: ArrayList<Bitmap> = ArrayList()
    private var _binding: FragmentMyMessagesBinding? = null

    private val binding get() = _binding!!

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyMessagesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        navBar = requireActivity().findViewById(R.id.bottomNavigationView)
        navBar?.visibility = View.VISIBLE

        val buttonBack = binding.button
        buttonBack.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }
        val buttonNewChat = binding.buttonNewChat
        val cardPersons = binding.cardPerosons
        val linerPersons = binding.linerPersons
        var JSONmyPersons: JSONArray? = null
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true /* enabled by default */) {
            override fun handleOnBackPressed() {
                if (cardPersons.visibility == View.VISIBLE){
                    cardPersons.visibility = View.GONE
                } else {
                    activity?.supportFragmentManager?.popBackStack()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        buttonNewChat.setOnClickListener {
            if (cardPersons.visibility == View.GONE){
                cardPersons.visibility = View.VISIBLE
                var threa = Thread {
                    strPersons = apiExecute("persons") //not-in-chats-with/${SendData.userId}
                    JSONmyPersons = JSONArray(strPersons.toString())
                }
                threa.start()
                threa.join()
                linerPersons.removeAllViews()

                for (i in 0..<(JSONmyPersons?.length() ?: 0)) {
                    val cardView2 = context?.let { CardView(it) }

                    val JSONObject = JSONmyPersons?.getJSONObject(i)

                    cardView2?.setOnClickListener {
                        Toast.makeText(context, "Person id: " + JSONObject?.getInt("personId"),
                            Toast.LENGTH_SHORT).show()
                        var threa = Thread {
                            addChat(SendData.userId, JSONObject?.getInt("personId").toString())
                        }
                        threa.start()
                        threa.join()
                        SendData.data = "1" + JSONObject.toString()
                        view?.let { it1 ->
                            Navigation.findNavController(it1)
                                .navigate(R.id.action_fragment_my_messages_to_fragment_watch_messages)
                        }
                    }
                    val layoutHor = LinearLayout(context)
                    val layoutVer1 = LinearLayout(context)
                    val layoutVer2 = LinearLayout(context)
                    val layoutVer3 = LinearLayout(context)
                    val textView1 = TextView(context)
                    val textView2 = TextView(context)
                    val textView6 = TextView(context)
                    val imageView1 = ImageView(context)
                    val cardView1 = CardView(requireContext())

                    imageView1.setImageResource(R.drawable.avatar)

                    val personNumJSON = JSONObject?.getInt("personId")
                    var personNameJSON = JSONObject?.getJSONArray("personRealId")
                        ?.getJSONObject(0)?.getString("name")
                    val personId = JSONObject?.getInt("personId")
                    Log.e("PersonId = ", personId.toString())
                    try {
                        loadImage(personId.toString(), imageView1)
                    } catch (e: Exception){
                        Log.e("LoadImage", "Avatar not found")
                    }
                    this.activity?.runOnUiThread {
                        textView1.text = "" + personNumJSON
                        textView2.text = personNameJSON
                        if (JSONObject?.getString("role") == "admin"){
                            textView6.text = "Администратор"
                        } else {
                            textView6.text = "Пользователь"
                        }
                    }
                    val lpImage = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    imageView1.setLayoutParams(lpImage)
                    imageView1.scaleType = ImageView.ScaleType.CENTER_CROP
                    val marginCardView =
                        LinearLayout.LayoutParams(resources.getDimension(R.dimen.avatar_size_img_journals).toInt(),
                            resources.getDimension(R.dimen.avatar_size_img_journals).toInt())
                    marginCardView.setMargins(resources.getDimension(R.dimen.left_margin_avatar_img_journals).toInt(),
                        resources.getDimension(R.dimen.top_margin_avatar_img_journals).toInt(), 0, 0)
                    cardView1.addView(imageView1)
                    cardView1.radius = resources.getDimension(R.dimen.avatar_card_radius_img_journals)
                    cardView1.layoutParams = marginCardView
                    textView1.setPadding(resources.getDimension(R.dimen.left_padding_num_journals).toInt(),
                        0, 0, 0)
                    textView2.setPadding(resources.getDimension(R.dimen.left_padding_note_journals).toInt(),
                        resources.getDimension(R.dimen.top_padding_note_journals).toInt(), 0, 0)
                    textView6.setPadding(resources.getDimension(R.dimen.left_padding_date_journals).toInt(),
                        0, 0, 0)
                    textView6.textSize = resources.getDimension(R.dimen.text_size_date_journals)
                    textView1.textSize = resources.getDimension(R.dimen.text_size_num_journals)
                    textView2.textSize = resources.getDimension(R.dimen.text_size_note_journals)
                    textView1.setTextColor(Color.BLACK)
                    textView2.setTextColor(Color.BLACK)
                    layoutVer1.orientation = LinearLayout.VERTICAL
                    layoutVer2.orientation = LinearLayout.VERTICAL
                    layoutVer3.orientation = LinearLayout.VERTICAL
                    layoutVer1.gravity = Gravity.START
                    layoutVer2.gravity = Gravity.START
                    layoutVer3.gravity = Gravity.END
                    val tvParams =
                        LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    textView1.layoutParams = tvParams
                    textView2.layoutParams = tvParams
                    textView6.layoutParams = tvParams
                    tvParams.weight = 0f
                    layoutVer1.layoutParams = tvParams
                    tvParams.weight = 0f
                    layoutVer3.layoutParams = tvParams
                    val tvParams2 =
                        LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    tvParams2.weight = 1F
                    layoutVer2.layoutParams = tvParams2
                    layoutVer1.addView(cardView1)
                    layoutVer1.addView(textView1)
                    layoutVer2.addView(textView2)
                    layoutVer2.addView(textView6)
                    val tvParamsHor =
                        LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    layoutHor.layoutParams = tvParamsHor
                    layoutHor.addView(layoutVer1)
                    layoutHor.addView(layoutVer2)
                    cardView2?.addView(layoutHor)
                    val marginLayoutParams =
                        LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, resources.getDimension(R.dimen.height_cards_journals).toInt())
                    marginLayoutParams.setMargins(resources.getDimension(R.dimen.left_margin_cards_journals).toInt(),
                        resources.getDimension(R.dimen.top_margin_cards_journals).toInt(),
                        resources.getDimension(R.dimen.right_margin_cards_journals).toInt(),
                        resources.getDimension(R.dimen.bottom_margin_cards_journals).toInt())
                    cardView2?.setLayoutParams(marginLayoutParams)
                    cardView2?.elevation = resources.getDimension(R.dimen.elevation_cards_journals)
                    cardView2?.radius = resources.getDimension(R.dimen.corner_cards_journals)
                    cardView2?.setCardBackgroundColor(Color.WHITE)
                    linerPersons.addView(cardView2)
                    linerPersons.showDividers
                }
            } else {
                cardPersons.visibility = View.GONE
            }
        }

        val layout = binding.linerChats
        val connection = binding.connection
        var JSONmyChats: JSONArray? = null
        if (ConnectChecker.isOnline(requireContext()) && !SendData.isBadConnection) {
            connection.visibility = View.GONE
            var threa = Thread {
                strChats = apiExecute("chats/person/${SendData.userId}")
                try{
                    JSONmyChats = JSONArray(strChats.toString())
                } catch (e: JSONException){
                }
            }
            threa.start()
            threa.join()

            for (i in 0..<(JSONmyChats?.length() ?: 0)) {
                val JSONObject = JSONmyChats?.getJSONObject(i)
                if (JSONObject != null) {
                    var threa = Thread{
                        if (JSONObject?.getJSONObject("person1Id")?.getInt("personId") ==
                            SendData.userId.toInt()){
                            val personId = JSONObject.getJSONObject("person2Id").getInt("personId")
                            Log.e("PersonId = ", personId.toString())
                        } else {
                            val personId = JSONObject.getJSONObject("person1Id").getInt("personId")
                            Log.e("PersonId = ", personId.toString())
                        }
                    }
                    threa.start()
                    threa.join()
                }
            }

            for (i in 0..<(JSONmyChats?.length() ?: 0)) {
                val cardView2 = context?.let { CardView(it) }
                val JSONObject = JSONmyChats?.getJSONObject(i)
                cardView2?.setOnClickListener {
                    SendData.data = JSONObject.toString()
                    view?.let { it1 ->
                        Navigation.findNavController(it1)
                            .navigate(R.id.action_fragment_my_messages_to_fragment_watch_messages)
                    }
                }
                val layoutHor = LinearLayout(context)
                val layoutVer1 = LinearLayout(context)
                val layoutVer2 = LinearLayout(context)
                val layoutVer3 = LinearLayout(context)
                val textView1 = TextView(context)
                val textView2 = TextView(context)
                val textView3 = TextView(context)
                val textView6 = TextView(context)
                val imageView1 = ImageView(context)
                val cardView1 = CardView(requireContext())
                imageView1.setImageResource(R.drawable.avatar)
                val chatNumJSON = JSONObject?.getInt("chatId")
                val chatLastMsg = JSONObject?.getString("lastMessage")
                val chatLastDate = JSONObject?.getString("lastDate")
                var personNameJSON = ""

                if (JSONObject != null) {
                    if (JSONObject?.getJSONObject("person1Id")?.getInt("personId") ==
                        SendData.userId.toInt()){
                        personNameJSON = JSONObject.getJSONObject("person2Id").getJSONArray("personRealId")
                            .getJSONObject(0).getString("name")
                        val personId = JSONObject.getJSONObject("person2Id").getInt("personId")
                        try {
                            loadImage(personId.toString(), imageView1)
                        } catch (e: Exception){
                            Log.e("LoadImage", "Avatar not found")
                        }
                    } else {
                        personNameJSON = JSONObject.getJSONObject("person1Id").getJSONArray("personRealId")
                            .getJSONObject(0).getString("name")
                        val personId = JSONObject.getJSONObject("person1Id").getInt("personId")
                        try {
                            loadImage(personId.toString(), imageView1)
                        } catch (e: Exception){
                            Log.e("LoadImage", "Avatar not found")
                        }
                    }
                }
                this.activity?.runOnUiThread {
                    textView1.text = "" + chatNumJSON
                    textView2.text = personNameJSON
                    var timeTarget = chatLastDate?.split('T')?.get(1)?.split('.')?.get(0)
                    var dateTarget = chatLastDate?.split('T')?.get(0)?.split('-')?.get(2) + "." +
                            chatLastDate?.split('T')?.get(0)?.split('-')?.get(1)
                    textView3.text = timeTarget + "\n" + dateTarget
                    if (chatLastMsg == null || chatLastMsg == ""){
                        textView6.text = ""
                    } else {
                        textView6.text = chatLastMsg
                    }
                }
                val lpImage = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                imageView1.setLayoutParams(lpImage)
                imageView1.scaleType = ImageView.ScaleType.CENTER_CROP
                val marginCardView =
                    LinearLayout.LayoutParams(resources.getDimension(R.dimen.avatar_size_img_journals).toInt(),
                        resources.getDimension(R.dimen.avatar_size_img_journals).toInt())
                marginCardView.setMargins(resources.getDimension(R.dimen.left_margin_avatar_img_journals).toInt(),
                    resources.getDimension(R.dimen.top_margin_avatar_img_journals).toInt(), 0, 0)
                cardView1.addView(imageView1)
                cardView1.radius = resources.getDimension(R.dimen.avatar_card_radius_img_journals)
                cardView1.layoutParams = marginCardView
                textView1.setPadding(resources.getDimension(R.dimen.left_padding_num_journals).toInt(),
                    0, 0, 0)
                textView2.setPadding(resources.getDimension(R.dimen.left_padding_date_journals).toInt(),
                    resources.getDimension(R.dimen.top_margin_avatar_img_journals).toInt(), 0, 0)
                textView3.setPadding(resources.getDimension(R.dimen.left_padding_date_journals).toInt(),
                    resources.getDimension(R.dimen.top_padding_date_journals).toInt(),
                    resources.getDimension(R.dimen.top_padding_date_journals).toInt(), 0)
                textView3.gravity = Gravity.END
                textView6.setPadding(resources.getDimension(R.dimen.top_padding_date_journals).toInt(),
                   0,0, 0)
                textView6.textSize = resources.getDimension(R.dimen.text_size_note_journals)
                textView1.textSize = resources.getDimension(R.dimen.text_size_date_journals)
                textView2.textSize = resources.getDimension(R.dimen.text_size_note_journals)
                textView3.textSize = resources.getDimension(R.dimen.text_size_date_journals)
                textView1.setTextColor(Color.BLACK)
                textView2.setTextColor(Color.BLACK)
                val tvParams =
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                textView1.layoutParams = tvParams
                textView2.layoutParams = tvParams
                textView3.layoutParams = tvParams
                textView6.layoutParams = tvParams
                textView1.gravity = Gravity.START
                textView2.gravity = Gravity.START
                textView3.gravity = Gravity.END
                textView6.gravity = Gravity.START
                layoutVer1.orientation = LinearLayout.VERTICAL
                layoutVer2.orientation = LinearLayout.VERTICAL
                layoutVer3.orientation = LinearLayout.VERTICAL
                layoutVer1.gravity = Gravity.START
                layoutVer2.gravity = Gravity.START
                layoutVer3.gravity = Gravity.END
                tvParams.weight = 0f
                layoutVer1.layoutParams = tvParams
                tvParams.weight = 0f
                layoutVer3.layoutParams = tvParams
                val tvParams2 =
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                tvParams2.weight = 1F
                layoutVer2.layoutParams = tvParams2
                layoutVer1.addView(cardView1)
                layoutVer1.addView(textView1)
                layoutVer2.addView(textView2)
                layoutVer2.addView(textView6)
                layoutVer3.addView(textView3)
                val tvParamsHor =
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                layoutHor.layoutParams = tvParamsHor
                layoutHor.addView(layoutVer1)
                layoutHor.addView(layoutVer2)
                layoutHor.addView(layoutVer3)
                cardView2?.addView(layoutHor)
                val marginLayoutParams =
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, resources.getDimension(R.dimen.height_cards_journals).toInt())
                marginLayoutParams.setMargins(resources.getDimension(R.dimen.left_margin_cards_journals).toInt(),
                    resources.getDimension(R.dimen.top_margin_cards_journals).toInt(),
                    resources.getDimension(R.dimen.right_margin_cards_journals).toInt(),
                    resources.getDimension(R.dimen.bottom_margin_cards_journals).toInt())
                cardView2?.setLayoutParams(marginLayoutParams)
                cardView2?.elevation = resources.getDimension(R.dimen.elevation_cards_journals)
                cardView2?.radius = resources.getDimension(R.dimen.corner_cards_journals)
                cardView2?.setCardBackgroundColor(Color.WHITE)
                layout.addView(cardView2)
                layout.showDividers
            }
        } else {
            connection.visibility = View.VISIBLE
        }
        return root
    }

    private fun loadImage(id: String, image: ImageView) {
        var url = "http://${SendData.IPSERVER}:${SendData.PORTDB}/images/db/person$id.jpg"
        val queue = Volley.newRequestQueue(context)
        var request = ImageRequest(url, {
            photoView = it
            image.setImageBitmap(photoView)
        }, 0, 0, null,
            {
                it.printStackTrace()
            })
        queue.add(request)
    }

    private fun addChat(person1Id: String, person2Id: String){
        val queue = Volley.newRequestQueue(context)
        var url = "http://${SendData.IPSERVER}:${SendData.PORTDB}/chats"
        var postparams = JSONObject()
        var person1Object = JSONObject()
        person1Object.put("personId", Integer.valueOf(person1Id))
        postparams.put("person1Id", person1Object)
        var person2Object = JSONObject()
        person2Object.put("personId", Integer.valueOf(person2Id))
        postparams.put("person2Id", person2Object)
        postparams.put("lastDate", Timestamp(System.currentTimeMillis()).toString().replace(' ', 'T'))
        Log.e("ChatParams", postparams.toString())

        val request: JsonObjectRequest =
            object : JsonObjectRequest(Method.POST, url, postparams, Response.Listener { response ->
                Toast.makeText(context, "Data Updated..", Toast.LENGTH_SHORT).show()
                try {
                    val jsonObject = response
                    Log.e("ChatResponse", jsonObject.toString())
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error ->
                Log.e("tag", "error is " + error!!.message)
                Toast.makeText(context, "Fail to update data..", Toast.LENGTH_SHORT)
                    .show()
            }) {
            }
        queue.add(request)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
                //open a URL connection
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