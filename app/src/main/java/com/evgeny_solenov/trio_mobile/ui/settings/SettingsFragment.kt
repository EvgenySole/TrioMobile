package com.example.testandro6.ui.settings

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.testandro6.ConnectChecker
import com.example.testandro6.SendData
import com.example.testandro6.databinding.FragmentSettingsBinding
import java.io.InputStreamReader
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL


class SettingsFragment : Fragment() {
    var str = ""
    private var _binding: FragmentSettingsBinding? = null

    private val binding get() = _binding!!

    @SuppressLint("CheckResult", "RtlHardcoded")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.connectOk.visibility = View.GONE
        binding.ping.visibility = View.GONE
        binding.etIp.setText(SendData.IPSERVER)
        binding.etPort.setText(SendData.PORTDB.toString())
        binding.buttonCheckCon.setOnClickListener {

            Thread {
                var startTime = System.currentTimeMillis()
                while (true) {
                    if (binding.etIp.text.toString() != "" && binding.etPort.text.toString() != "") {
                        var temp = apiExecute(binding.etIp.text.toString(), binding.etPort.text.toString())
                        if (ConnectChecker.isOnline(requireContext()) && !SendData.isBadConnection) {
                            var endTime = System.currentTimeMillis()
                            var timeNeed = endTime - startTime
                            activity?.runOnUiThread {
                                binding.connectOk.visibility = View.VISIBLE
                                binding.ping.visibility = View.VISIBLE
                                binding.textView19.text = "Пинг $timeNeed мс"
                                binding.textView19.setTextColor(Color.BLACK)
                            }
                            break
                        }
                    }
                    var endTime = System.currentTimeMillis()
                    var timeNeed = endTime - startTime
                    if (timeNeed > 3000){
                        activity?.runOnUiThread {
                            binding.connectOk.visibility = View.GONE
                            binding.ping.visibility = View.VISIBLE
                            binding.textView19.text = "Подключение не установлено"
                            binding.textView19.setTextColor(Color.RED)
                        }
                        break
                    }
                }
            }.start()
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun apiExecute(ipAdress: String, port: String): StringBuilder {
        val policy = ThreadPolicy.Builder()
            .permitAll().build()
        StrictMode.setThreadPolicy(policy)
        val myUrl = "http://$ipAdress:$port/event_levels"
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