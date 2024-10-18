package com.example.testandro6.ui.add_note
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.testandro6.*
import com.example.testandro6.databinding.FragmentAddNoteBinding
import com.google.android.material.navigation.NavigationBarView
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.sql.Connection

class AddNoteFragment : Fragment() {
    private var navBar: NavigationBarView? = null
    var connection: Connection? = null
    private var _binding: FragmentAddNoteBinding? = null
    private var prefSave: SharedPreferences? = null
    private val binding get() = _binding!!

    @SuppressLint("SetTextI18n", "ResourceAsColor")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddNoteBinding.inflate(inflater, container, false)
        val root: View = binding.root
        navBar = requireActivity().findViewById(R.id.bottomNavigationView)
        navBar?.visibility = View.VISIBLE
        val connection = binding.tvConnection
        val threaCheck = Thread { apiExecute() }
        threaCheck.start()
        threaCheck.join()
        if (ConnectChecker.isOnline(requireContext()) && !SendData.isBadConnection) {
            connection.visibility = View.GONE
        } else {
            connection.visibility = View.VISIBLE
        }
        val buttonManual = binding.imageView3
        buttonManual.setOnClickListener {
            view?.let { it1 ->
                Navigation.findNavController(it1).navigate(R.id.action_navigation_note_to_navigation_add_note_manual)
            }
        }
        val buttonQR = binding.imageView4
        buttonQR.setOnClickListener {
            view?.let { it1 ->
                Navigation.findNavController(it1)
                    .navigate(R.id.action_navigation_add_note_to_navigation_add_note_camera)
            }
            val intent = Intent(activity, QRCodeScannerActivity::class.java)
            activity?.startActivity(intent)
        }
        binding.tvEventsWait.visibility = View.GONE
        prefSave = activity?.getSharedPreferences("NEW_NOTES", Context.MODE_PRIVATE)
        var count = 0
        while (prefSave?.contains("saveNote$count") == true) {
            count++
        }
        if (count > 0) {
            binding.tvEventsWait.text = binding.tvEventsWait.text.toString() + count
            binding.tvEventsWait.visibility = View.VISIBLE
        }
        return root
    }

    fun apiExecute(): StringBuilder {
        val policy = StrictMode.ThreadPolicy.Builder()
            .permitAll().build()
        StrictMode.setThreadPolicy(policy)
        val myUrl = "http://${SendData.IPSERVER}:${SendData.PORTDB}/event_levels"
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
                return result
            } catch (e: Exception) {
                e.printStackTrace()
                if (e is ConnectException || e is SocketTimeoutException){
                    SendData.isBadConnection = true
                    Toast.makeText(context, "Нет подключения с сервером", Toast.LENGTH_SHORT).show()
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