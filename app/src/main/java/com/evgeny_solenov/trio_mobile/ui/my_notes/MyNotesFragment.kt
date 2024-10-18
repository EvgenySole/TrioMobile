package com.example.testandro6.ui.my_notes

import android.annotation.SuppressLint
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
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.testandro6.*
import com.example.testandro6.databinding.FragmentMyNotesBinding
import org.json.JSONArray
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class MyNotesFragment : Fragment() {
    var str: StringBuilder = StringBuilder("")
    private var _binding: FragmentMyNotesBinding? = null
    private val binding get() = _binding!!

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyNotesBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val buttonBack = binding.button
        buttonBack.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }
        Log.e("DENSITY","Density is ${resources.displayMetrics.density}")
        val layout = binding.linearLayout3
        val connection = binding.connection

        var JSONmy: JSONArray? = null
        if (ConnectChecker.isOnline(requireContext()) && !SendData.isBadConnection) {
            connection.visibility = View.GONE
            var threa = Thread {
                str = apiExecute(SendData.userRole, SendData.userId.toInt())
                Log.e("Sas", str.toString())
                JSONmy = JSONArray(str.toString())
            }
            threa.start()
            threa.join()

            for (i in 0..<(JSONmy?.length() ?: 2)) {
                val cardView2 = context?.let { CardView(it) }

                val JSONObject = JSONmy?.getJSONObject(i)

                cardView2?.setOnClickListener {
                    SendData.data = JSONObject.toString()
                    view?.let { it1 ->
                        Navigation.findNavController(it1).navigate(R.id.action_fragment_my_notes_to_fragment_watch_note)
                    }
                }
                val layoutHor = LinearLayout(context)
                val layoutVer1 = LinearLayout(context)
                val layoutVer2 = LinearLayout(context)
                val layoutVer3 = LinearLayout(context)
                val textView2 = TextView(context)
                val textView3 = TextView(context)
                val textView5 = TextView(context)
                val imageStatus = ImageView(context)

                val noteNumJSON = JSONObject?.getInt("eventId")
                val noteTextJSON = JSONObject?.getString("note")
                val noteDateJSON = JSONObject?.getString("dateTime")
                val statusJSON = JSONObject?.getJSONObject("eventlId")?.getInt("eventlId")
                this.activity?.runOnUiThread {
                    textView2.text = noteTextJSON
                    var timeTarget = noteDateJSON?.split('T')?.get(1)?.split('.')?.get(0)
                    var dateTarget = noteDateJSON?.split('T')?.get(0)?.split('-')?.get(2) + "." +
                            noteDateJSON?.split('T')?.get(0)?.split('-')?.get(1)
                    textView3.text = timeTarget + "\n" + dateTarget
                    textView5.text = "№ " + noteNumJSON
                    if (statusJSON == 0){
                        imageStatus.setImageResource(R.drawable.status_green)
                    }
                    if (statusJSON == 1){
                        imageStatus.setImageResource(R.drawable.status_yellow)
                    }
                    if (statusJSON == 2){
                        imageStatus.setImageResource(R.drawable.status_orange)
                    }
                    if (statusJSON == 3){
                        imageStatus.setImageResource(R.drawable.status_red)
                    }
                }

                textView2.setPadding(resources.getDimension(R.dimen.left_padding_note_journals).toInt(),
                    resources.getDimension(R.dimen.top_padding_note_journals).toInt(), 0, 0)
                textView3.setPadding(resources.getDimension(R.dimen.left_padding_date_journals).toInt(),
                    resources.getDimension(R.dimen.top_padding_date_journals).toInt(),
                    resources.getDimension(R.dimen.top_padding_date_journals).toInt(), 0)
                textView3.gravity = Gravity.END
                textView5.setPadding(resources.getDimension(R.dimen.left_padding_date_journals).toInt(),
                    resources.getDimension(R.dimen.top_padding_date_journals).toInt(), 0, 0)
                textView5.setLayoutParams(
                    ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                )
                textView2.textSize = resources.getDimension(R.dimen.text_size_note_journals)
                textView3.textSize = resources.getDimension(R.dimen.text_size_date_journals)
                textView5.textSize = resources.getDimension(R.dimen.text_size_num_journals)

                textView2.setTextColor(Color.BLACK)
                textView3.setTextColor(Color.GRAY)
                textView5.setTextColor(Color.BLACK)

                imageStatus.maxWidth = resources.getDimension(R.dimen.status_img_size_cards_journals).toInt()
                imageStatus.maxHeight = resources.getDimension(R.dimen.status_img_size_cards_journals).toInt()
                imageStatus.foregroundGravity = Gravity.START
                imageStatus.setPadding(resources.getDimension(R.dimen.left_padding_img_journals).toInt(),
                    0 , 0,0)
                imageStatus.setLayoutParams(
                    ViewGroup.LayoutParams(resources.getDimension(R.dimen.status_img_size_cards_journals).toInt(),
                        resources.getDimension(R.dimen.status_img_size_cards_journals).toInt())
                );
                layoutVer1.orientation = LinearLayout.VERTICAL
                layoutVer2.orientation = LinearLayout.VERTICAL
                layoutVer3.orientation = LinearLayout.VERTICAL
                layoutVer1.gravity = Gravity.START
                layoutVer2.gravity = Gravity.START
                layoutVer3.gravity = Gravity.END
                val tvParams =
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                textView2.layoutParams = tvParams
                textView3.layoutParams = tvParams
                textView5.layoutParams = tvParams
                tvParams.weight = 0f
                layoutVer1.layoutParams = tvParams
                tvParams.weight = 0f
                layoutVer3.layoutParams = tvParams
                val tvParams2 =
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                tvParams2.weight = 1F
                layoutVer2.layoutParams = tvParams2
                layoutVer1.addView(textView5)
                layoutVer1.addView(imageStatus)
                layoutVer1.setLayoutParams(
                    ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                )
                layoutVer2.addView(textView2)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun apiExecute(role: String, personId: Int): StringBuilder{
        val policy = StrictMode.ThreadPolicy.Builder()
            .permitAll().build()
        StrictMode.setThreadPolicy(policy)
        var myUrl = "http://${SendData.IPSERVER}:${SendData.PORTDB}/events"
        if (role == "user"){
            myUrl = "http://${SendData.IPSERVER}:${SendData.PORTDB}/events/person/$personId"
        }
        Log.e("Url", myUrl);

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