package com.example.testandro6.ui.add_task

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
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
import com.example.testandro6.TrioAdapter
import com.example.testandro6.databinding.FragmentAddTaskBinding
import com.example.testandro6.databinding.FragmentEditTaskBinding
import com.example.testandro6.ui.add_note_manual.AddNoteManualFragment
import com.google.android.material.navigation.NavigationBarView
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.sql.Timestamp

class EditTaskFragment : Fragment() {
    private var navBar: NavigationBarView? = null
    private var selectedEquip = -1
    private var selectedWorker = -1
    private var loadEquip = -1
    private var loadWorker = -1
    private var responseUpdateTask = ""
    var strDepart: StringBuilder = StringBuilder("")
    var strEquip: StringBuilder = StringBuilder("")
    var strWorker: StringBuilder = StringBuilder("")
    var strWasteT: StringBuilder = StringBuilder("")
    var strWasteG: StringBuilder = StringBuilder("")
    private var _binding: FragmentEditTaskBinding? = null
    private val binding get() = _binding!!
    private var pref: SharedPreferences? = null
    var inputText: String? = ""

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditTaskBinding.inflate(inflater, container, false)
        val root: View = binding.root
        navBar = requireActivity().findViewById(R.id.bottomNavigationView)
        navBar?.visibility = View.GONE

        inputText = SendData.data
        SendData.data = ""
        var JSONmy = JSONObject(inputText.toString())

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
        binding.textNumEvent.text = binding.textNumEvent.text.toString() + JSONmy.getInt("taskId")
        binding.tvTaskNoteName.setText(JSONmy.getString("orderText"))

        try {
            loadEquip = JSONmy.getJSONObject("equipmentoId").getInt("equipmentoId")
        } catch (e: JSONException){}

        loadWorker = JSONmy.getJSONObject("targetWorkerId").getInt("personId")


        pref = activity?.getSharedPreferences("ADD_TASK", Context.MODE_PRIVATE)
        val buttonBack = binding.button
        buttonBack.setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext())
            dialog.setTitle("Закрытие")?.setMessage("Хотите закрыть окно? Данные не сохранятся")
                ?.setPositiveButton("ДА") { dialogIn, whichButton ->
                    SendData.data = inputText.toString()
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
                        SendData.data = inputText.toString()
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
            spinnerEq.setText(itemsEq[itemsEqId.indexOf(loadEquip.toString())])
            var adapteraa = ArrayAdapter(requireContext(), R.layout.list_item_my, itemsEq)
            spinnerEq.setAdapter(adapteraa)
            spinnerEq.setOnItemClickListener { parent, view, position, id ->
                selectedEquip = position
            }
        }

        if (!strWorker.equals("")) {
            for (i in 0..<(JSONmyWorker?.length() ?: 1)) {
                var workerStr = ""
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
            val index = itemsWorkersId.indexOf(loadWorker.toString())
            if (index >= 0){
                spinnerWorkers.setText(itemsWorkers[index])
            }
            var adapteraa = ArrayAdapter(requireContext(), R.layout.list_item_my, itemsWorkers)
            spinnerWorkers.setAdapter(adapteraa)
            spinnerWorkers.setOnItemClickListener { parent, view, position, id ->
                selectedWorker = position
            }
        }

        binding.buttonCloseTask.setOnClickListener {
            Log.e("LOAD WORKER" , loadWorker.toString())
            var isAllSelectedWorker: Boolean
            if ((selectedWorker == 0 || selectedWorker == -1) && loadWorker == -1) {
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
                } else {
                    equipAdd = loadEquip.toString()
                }
                var workerAdd = "0"
                if (selectedWorker != -1) {
                    workerAdd = itemsWorkersId[selectedWorker]
                } else {
                    workerAdd = loadWorker.toString()
                }

                updateData(JSONmy.getInt("taskId").toString(),
                    binding.tvTaskNoteName.text.toString(), equipAdd, workerAdd, SendData.userId,
                    Timestamp(System.currentTimeMillis()).toString(),
                    "false", "1"
                )
                Thread{
                    while (responseUpdateTask == ""){

                    }
                    if (responseUpdateTask == "updated"){
                        SendData.data = "task updated"
                    } else {
                        SendData.data = "task error"
                    }
                    activity?.runOnUiThread{
                        view?.let { it1 ->
                            Navigation.findNavController(it1).navigate(R.id.action_edit_task_fragment_to_fragment_add_task_done)
                        }
                    }
                }.start()

            } else {
                Toast.makeText(context, "Поля не выбраны!", Toast.LENGTH_SHORT).show()
            }
        }

        return root
    }

    private fun updateData(
        id: String,
        orderText: String,
        equipmentoId: String,
        targetWorkerId: String,
        headPersonId: String,
        dateTime: String,
        done: String,
        eventId: String
    ) {
        val queue = Volley.newRequestQueue(context)
        var url = "http://${SendData.IPSERVER}:${SendData.PORTDB}/tasks/" + id + "?"
        if (dateTime != ""){
            url += "dateTime=$dateTime&"
        }
        if (orderText != ""){
            url += "order=$orderText&"
        }
        if (equipmentoId != ""){
            url += "equipmentoId=$equipmentoId&"
        }
        if (targetWorkerId != ""){
            url += "targetWorkerId=$targetWorkerId&"
        }
        if (headPersonId != ""){
            url += "headPersonId=$headPersonId&"
        }
        if (done != ""){
            url += "done=$done&"
        }
        if (eventId != ""){
            url += "eventId=$eventId"
        }

        url.trim('&')

        var postparams = JSONObject()

        Log.e("Sas", url)

        val request: JsonObjectRequest =
            object : JsonObjectRequest(Method.PUT, url, postparams, Response.Listener { response ->
                Toast.makeText(context, "Задача создана!", Toast.LENGTH_SHORT).show()
                try {
                    val jsonObject = response
                    responseUpdateTask = "updated"
                    Log.e("SasResponse", jsonObject.toString())
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error ->
                responseUpdateTask = "error"
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