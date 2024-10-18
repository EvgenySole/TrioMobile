package com.example.testandro6.ui.logbooks
import android.os.Bundle
import android.os.StrictMode
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.example.testandro6.ConnectChecker
import com.example.testandro6.R
import com.example.testandro6.SendData
import com.example.testandro6.databinding.FragmentLogbooksBinding
import java.io.InputStreamReader
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class LogbooksFragment : Fragment() {
    private var _binding: FragmentLogbooksBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogbooksBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val connection = binding.tvConnection
        val threaCheck = Thread { apiExecute() }
        threaCheck.start()
        threaCheck.join()
        if (ConnectChecker.isOnline(requireContext()) && !SendData.isBadConnection) {
            connection.visibility = View.GONE
            val buttonEvents = binding.imageView4
            buttonEvents.setOnClickListener {
                view?.let { it1 ->
                    Navigation.findNavController(it1).navigate(R.id.action_navigation_tasks_to_fragment_my_notes)}
            }
            val buttonObjects = binding.imageView3
            buttonObjects.setOnClickListener {
                view?.let { it1 ->
                    Navigation.findNavController(it1).navigate(R.id.action_navigation_tasks_to_fragment_my_objects)}
            }
            val buttonTasks = binding.imageView8
            buttonTasks.setOnClickListener {
                view?.let { it1 ->
                    Navigation.findNavController(it1).navigate(R.id.action_navigation_tasks_to_fragment_my_tasks)}
            }
            val buttonMessages = binding.imageView9
            buttonMessages.setOnClickListener {
                view?.let { it1 ->
                    Navigation.findNavController(it1).navigate(R.id.action_navigation_tasks_to_fragment_my_messages)}
            }
        } else { connection.visibility = View.VISIBLE }
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