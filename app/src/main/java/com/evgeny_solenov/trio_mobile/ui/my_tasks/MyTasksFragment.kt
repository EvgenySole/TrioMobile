package com.example.testandro6.ui.my_tasks

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.navigation.Navigation
import com.example.testandro6.ConnectChecker
import com.example.testandro6.R
import com.example.testandro6.SendData
import com.example.testandro6.databinding.FragmentMyTasksBinding
import org.json.JSONArray
import org.json.JSONException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MyTasksFragment : Fragment() {
    var str: StringBuilder = StringBuilder("")
    private var _binding: FragmentMyTasksBinding? = null
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyTasksBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val buttonBack = binding.button
        buttonBack.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }
        if (SendData.userRole == "user"){
            binding.buttonNewTask.visibility = View.GONE
        }
        binding.buttonNewTask.setOnClickListener {
            view?.let { it1 -> Navigation.findNavController(it1).
            navigate(R.id.action_fragment_my_tasks_to_fragment_add_task) }
        }
        val layout = binding.linearLayout3
        val connection = binding.connection
        var JSONmy: JSONArray? = null
        if (ConnectChecker.isOnline(requireContext()) && !SendData.isBadConnection) {
            connection.visibility = View.GONE

            var threa = Thread {
                str = apiExecute(SendData.userRole, SendData.userId.toInt())
                Log.e("Sas", str.toString())
                try{
                    JSONmy = JSONArray(str.toString())
                } catch (e: JSONException){
                    e.printStackTrace()
                }
            }
            threa.start()
            threa.join()

            for (i in 0..<(JSONmy?.length() ?: 2)) {
                val cardView2 = context?.let { CardView(it) }

                val JSONObject = JSONmy?.getJSONObject(i)

                cardView2?.setOnClickListener {
                    SendData.data = JSONObject.toString()
                    view?.let { it1 ->
                        Navigation.findNavController(it1).navigate(R.id.action_fragment_my_tasks_to_fragment_watch_task)
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

                val noteNumJSON = JSONObject?.getInt("taskId")
                val noteTextJSON = JSONObject?.getString("orderText")
                val noteDateJSON = JSONObject?.getString("dateTime")
                val isDoneJSON = JSONObject?.getString("done")
                this.activity?.runOnUiThread {
                    textView1.text = "№ " + noteNumJSON
                    textView2.text = noteTextJSON
                    var timeTarget = noteDateJSON?.split('T')?.get(1)?.split('.')?.get(0)
                    var dateTarget = noteDateJSON?.split('T')?.get(0)?.split('-')?.get(2) + "." +
                            noteDateJSON?.split('T')?.get(0)?.split('-')?.get(1)
                    textView3.text = timeTarget + "\n" + dateTarget
                    if (isDoneJSON == "true"){
                        textView6.text = "Выполнена"
                        cardView2?.setCardBackgroundColor(Color.valueOf(0.75f, 1f, 0.75f).toArgb())
                    }
                }

                textView2.setPadding(resources.getDimension(R.dimen.left_padding_note_journals).toInt(),
                    resources.getDimension(R.dimen.top_padding_note_journals).toInt(), 0, 0)
                textView3.setPadding(resources.getDimension(R.dimen.left_padding_date_journals).toInt(),
                    resources.getDimension(R.dimen.top_padding_date_journals).toInt(),
                    resources.getDimension(R.dimen.top_padding_date_journals).toInt(), 0)
                textView3.gravity = Gravity.END
                textView1.setPadding(resources.getDimension(R.dimen.left_padding_date_journals).toInt(),
                    resources.getDimension(R.dimen.top_padding_date_journals).toInt(), 0, 0)
                textView1.setLayoutParams(
                    ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                )

                textView6.textSize = resources.getDimension(R.dimen.text_size_date_journals)
                textView1.textSize = resources.getDimension(R.dimen.text_size_note_journals)
                textView2.textSize = resources.getDimension(R.dimen.text_size_note_journals)
                textView3.textSize = resources.getDimension(R.dimen.text_size_date_journals)

                textView1.setTextColor(Color.BLACK)
                textView2.setTextColor(Color.BLACK)
                textView3.setTextColor(Color.GRAY)
                textView6.setTextColor(Color.BLACK)

                textView6.setPadding(resources.getDimension(R.dimen.left_padding_img_journals).toInt(),
                    0 , resources.getDimension(R.dimen.left_padding_img_journals).toInt(),0)
                textView6.gravity = Gravity.END
                val tvParams =
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                textView1.layoutParams = tvParams
                textView2.layoutParams = tvParams
                textView3.layoutParams = tvParams
                textView6.layoutParams = tvParams

                layoutVer1.orientation = LinearLayout.VERTICAL
                layoutVer2.orientation = LinearLayout.VERTICAL
                layoutVer3.orientation = LinearLayout.VERTICAL
                tvParams.weight = 0f
                layoutVer1.layoutParams = tvParams
                tvParams.weight = 0f
                layoutVer3.layoutParams = tvParams
                val tvParams2 =
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                tvParams2.weight = 1F
                layoutVer2.layoutParams = tvParams2
                layoutVer1.addView(textView1)
                layoutVer2.addView(textView2)
                layoutVer3.addView(textView3)
                layoutVer3.addView(textView6)
                layoutVer3.gravity = Gravity.END
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
                layout.addView(cardView2)
                layout.showDividers
            }
        } else {
            connection.visibility = View.VISIBLE
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun apiExecute(role: String, personId: Int): StringBuilder{
        val policy = StrictMode.ThreadPolicy.Builder()
            .permitAll().build()
        StrictMode.setThreadPolicy(policy)
        var myUrl = "http://${SendData.IPSERVER}:${SendData.PORTDB}/tasks"
        if (role == "user"){
            myUrl = "http://${SendData.IPSERVER}:${SendData.PORTDB}/tasks/person/$personId"
        }
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