package com.example.testandro6.ui.add_task

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.navigation.Navigation
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.testandro6.ConnectChecker
import com.example.testandro6.R
import com.example.testandro6.SendData
import com.example.testandro6.databinding.FragmentAddTaskBinding
import com.google.android.material.navigation.NavigationBarView
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.sql.Timestamp

class AddTaskFragment : Fragment() {
    private var navBar: NavigationBarView? = null
    private var selectedEquip = -1
    private var selectedWorker = -1
    var strDepart: StringBuilder = StringBuilder("")
    var strEquip: StringBuilder = StringBuilder("")
    var strWorker: StringBuilder = StringBuilder("")
    var strWasteT: StringBuilder = StringBuilder("")
    var strWasteG: StringBuilder = StringBuilder("")
    private var _binding: FragmentAddTaskBinding? = null
    private val binding get() = _binding!!
    private var pref: SharedPreferences? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddTaskBinding.inflate(inflater, container, false)
        val root: View = binding.root
        navBar = requireActivity().findViewById(R.id.bottomNavigationView)
        navBar?.visibility = View.GONE

        var JSONmyEquip: JSONArray? = null
        var JSONmyWorker: JSONArray? = null

        var spinnerEq = binding.tvAutoEquip
        val itemsEq = ArrayList<String>()
        val itemsEqId = ArrayList<String>()
        itemsEq.add("Не выбрано")
        itemsEqId.add("-1")

        var spinnerWorkers = binding.tvAutoWorker
        val itemsWorkers = ArrayList<String>()
        val itemsWorkersId = ArrayList<String>()
        itemsWorkers.add("Не выбрано")
        itemsWorkersId.add("-1")

        pref = activity?.getSharedPreferences("ADD_TASK", Context.MODE_PRIVATE)
        val buttonBack = binding.button
        buttonBack.setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext())
            dialog.setTitle("Закрытие")?.setMessage("Хотите закрыть окно? Данные не сохранятся")
                ?.setPositiveButton("ДА") { dialogIn, whichButton ->
                    activity?.supportFragmentManager?.popBackStack()
                    navBar = requireActivity().findViewById(R.id.bottomNavigationView)
                    navBar?.visibility = View.VISIBLE
                }
                ?.setNegativeButton("НЕТ") { dialogIn, whichButton ->
                }
            dialog.show()
        }
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true /* enabled by default */) {
            override fun handleOnBackPressed() {
                val dialog = AlertDialog.Builder(requireContext())
                dialog.setTitle("Закрытие")?.setMessage("Хотите закрыть окно? Данные не сохранятся")
                    ?.setPositiveButton("ДА") { dialogIn, whichButton ->
                        activity?.supportFragmentManager?.popBackStack()
                        navBar = requireActivity().findViewById(R.id.bottomNavigationView)
                        navBar?.visibility = View.VISIBLE
                    }
                    ?.setNegativeButton("НЕТ") { dialogIn, whichButton ->
                    }
                dialog.show()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        val connection = binding.connection
        if (ConnectChecker.isOnline(requireContext()) && !SendData.isBadConnection) {
            connection.visibility = View.GONE
            var threa = Thread {
                strEquip = apiExecute("equipment_objects")
                strWorker = apiExecute("persons")

                JSONmyEquip = JSONArray(strEquip.toString())
                JSONmyWorker = JSONArray(strWorker.toString())
            }
            threa.start()
            threa.join()
        } else {
            connection.visibility = View.VISIBLE
            if (pref?.getString("strEqiup", "") != "") {
                strEquip.clear().append(pref?.getString("strEquip", "")!!)
                strWorker.clear().append(pref?.getString("strWorker", "")!!)
            } else {
                return root
            }
            JSONmyEquip = JSONArray(strEquip.toString())
            JSONmyWorker = JSONArray(strWorker.toString())
        }

        if (!strEquip.equals("")) {
            for (i in 0..<(JSONmyEquip?.length() ?: 1)) {
                var equipStr = ""
                if ((JSONmyEquip?.getJSONObject(i)?.getJSONArray("equipmenthId")?.length() ?: 0) != 0) {
                    equipStr =
                        JSONmyEquip?.getJSONObject(i)?.getJSONObject("equipmentmId")?.getString("modelName")
                            ?: ""
                    if (equipStr.length > 30){
                        var equiptemp = equipStr.substring(0, 30)
                        equiptemp += "\n" + equipStr.substring(30)
                        equipStr = equiptemp
                    }
                    equipStr += "\nИнв.№:" + JSONmyEquip?.getJSONObject(i)?.getInt("inventoryNum")
                    itemsEq.add(equipStr)
                    equipStr = JSONmyEquip?.getJSONObject(i)?.getInt("equipmentoId").toString()
                    itemsEqId.add(equipStr)
                }
            }
            var adapteraa = ArrayAdapter(requireContext(), R.layout.list_item_my, itemsEq)
            spinnerEq.setAdapter(adapteraa)
            spinnerEq.setOnItemClickListener { parent, view, position, id ->
                selectedEquip = position
                Toast.makeText(requireContext(), selectedEquip.toString(), Toast.LENGTH_SHORT).show()
            }
        }
        if (!strWorker.equals("")) {
            for (i in 0..<(JSONmyWorker?.length() ?: 1)) {
                var workerStr = ""
                if ((JSONmyWorker?.getJSONObject(i)?.getInt("personId")).toString() != SendData.userId) {
                    workerStr = "Таб.№: " + JSONmyWorker?.getJSONObject(i)?.getInt("personId")
                    workerStr += "  " + JSONmyWorker?.getJSONObject(i)?.getJSONArray("personRealId")
                            ?.getJSONObject(0)?.getString("name")
                    if (workerStr.length > 30){
                        var workertemp = workerStr.substring(0, 30)
                        workertemp += "\n" + workerStr.substring(30)
                        workerStr = workertemp
                    }
                    itemsWorkers.add(workerStr)
                    workerStr = JSONmyWorker?.getJSONObject(i)?.getInt("personId").toString()
                    itemsWorkersId.add(workerStr)
                }
            }
            var adapteraa = ArrayAdapter(requireContext(), R.layout.list_item_my, itemsWorkers)
            spinnerWorkers.setAdapter(adapteraa)
            spinnerWorkers.setOnItemClickListener { parent, view, position, id ->
                selectedWorker = position
            }
        }

        binding.buttonCloseTask.setOnClickListener {
            var isAllSelectedWorker: Boolean
            if (selectedWorker == 0 || selectedWorker == -1) {
                binding.tvWorkerNot.visibility = View.VISIBLE
                isAllSelectedWorker = false
            } else {
                binding.tvWorkerNot.visibility = View.GONE
                isAllSelectedWorker = true
            }

            var isTextTyped: Boolean
            if (binding.tvTaskNoteName.text.toString() == "") {
                binding.tvTaskTextNot.visibility = View.VISIBLE
                isTextTyped = false
            } else {
                binding.tvTaskTextNot.visibility = View.GONE
                isTextTyped = true
            }

            if (isAllSelectedWorker && isTextTyped) {
                var equipAdd = "0"
                if (selectedEquip != -1) {
                    equipAdd = itemsEqId[selectedEquip]
                }
                var workerAdd = "0"
                if (selectedWorker != -1) {
                    workerAdd = itemsWorkersId[selectedWorker]
                }
                addData(
                    binding.tvTaskNoteName.text.toString(), equipAdd, workerAdd, SendData.userId,
                    Timestamp(System.currentTimeMillis()).toString().replace(' ', 'T'),
                    "false", "1"
                )
                view?.let { it1 ->
                    Navigation.findNavController(it1).navigate(R.id.action_fragment_add_task_to_fragment_add_task_done)
                }
            } else {
                Toast.makeText(context, "Поля не выбраны!", Toast.LENGTH_SHORT).show()
            }
        }

        return root
    }

    private fun addData(
        orderText: String,
        equipmentoId: String,
        targetWorkerId: String,
        headPersonId: String,
        dateTime: String,
        done: String,
        eventId: String
    ) {
        val queue = Volley.newRequestQueue(context)
        var url = "http://${SendData.IPSERVER}:${SendData.PORTDB}/tasks"
        var postparams = JSONObject()
        postparams.put("orderText", orderText)
        postparams.put("dateTime", dateTime)
        postparams.put("eventId", Integer.valueOf(eventId))
        var equipObject = JSONObject()
        equipObject.put("equipmentoId", Integer.valueOf(equipmentoId))
        postparams.put("equipmentoId", equipObject)
        var targetWorkerIdObject = JSONObject()
        targetWorkerIdObject.put("personId", Integer.valueOf(targetWorkerId))
        postparams.put("targetWorkerId", targetWorkerIdObject)
        var headPersonIdObject = JSONObject()
        headPersonIdObject.put("personId", Integer.valueOf(headPersonId))
        postparams.put("headPersonId", headPersonIdObject)
        postparams.put("done", done)

        Log.e("SasParams", postparams.toString())

        val request: JsonObjectRequest =
            object : JsonObjectRequest(Method.POST, url, postparams, Response.Listener { response ->
                Toast.makeText(context, "Задача создана!", Toast.LENGTH_SHORT).show()
                try {
                    val jsonObject = response
                    Log.e("SasResponse", jsonObject.toString())
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error ->
                Log.e("tag", "error is " + error!!.message)
                Toast.makeText(context, "Задача не отправлена", Toast.LENGTH_SHORT)
                    .show()
            }) {
            }
        queue.add(request)


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