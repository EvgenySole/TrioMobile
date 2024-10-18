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
import com.example.testandro6.databinding.FragmentAddObjectBinding
import com.google.android.material.navigation.NavigationBarView
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class AddObjectFragment : Fragment() {
    private var navBar: NavigationBarView? = null
    private var selectedEquip = -1
    private var selectedDepart = -1
    var strEquip: StringBuilder = StringBuilder("")
    var strDepart: StringBuilder = StringBuilder("")
    private var _binding: FragmentAddObjectBinding? = null
    private val binding get() = _binding!!
    private var pref: SharedPreferences? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddObjectBinding.inflate(inflater, container, false)
        val root: View = binding.root
        navBar = requireActivity().findViewById(R.id.bottomNavigationView)
        navBar?.visibility = View.GONE

        var JSONmyEquip: JSONArray? = null
        var JSONmyDepart: JSONArray? = null

        var spinnerEq = binding.tvAutoEquip
        val itemsEq = ArrayList<String>()
        val itemsEqId = ArrayList<String>()
        itemsEq.add("Не выбрано")
        itemsEqId.add("-1")

        var spinnerDepart = binding.tvAutoDepart
        val itemsDepart = ArrayList<String>()
        val itemsDepartId = ArrayList<String>()
        itemsDepart.add("Не выбрано")
        itemsDepartId.add("-1")

        pref = activity?.getSharedPreferences("ADD_OBJECT", Context.MODE_PRIVATE)
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
                strEquip = apiExecute("equipment_models")
                strDepart = apiExecute("departments")

                JSONmyEquip = JSONArray(strEquip.toString())
                JSONmyDepart = JSONArray(strDepart.toString())
            }
            threa.start()
            threa.join()
        } else {
            connection.visibility = View.VISIBLE
            if (pref?.getString("strEqiup", "") != "") {
                strEquip.clear().append(pref?.getString("strEquip", "")!!)
                strDepart.clear().append(pref?.getString("strDepart", "")!!)
            } else {
                return root
            }
            JSONmyEquip = JSONArray(strEquip.toString())
            JSONmyDepart = JSONArray(strDepart.toString())
        }

        if (!strEquip.equals("")) {
            for (i in 0..<(JSONmyEquip?.length() ?: 1)) {
                var equipStr = ""
                equipStr =
                    JSONmyEquip?.getJSONObject(i)?.getString("modelName")
                        ?: ""
                if (equipStr.length > 30){
                    var equiptemp = equipStr.substring(0, 30)
                    equiptemp += "\n" + equipStr.substring(30)
                    equipStr = equiptemp
                }
                itemsEq.add(equipStr)
                equipStr = JSONmyEquip?.getJSONObject(i)?.getInt("equipmentmId").toString()
                itemsEqId.add(equipStr)

            }
            var adapteraa = ArrayAdapter(requireContext(), R.layout.list_item_my, itemsEq)
            spinnerEq.setAdapter(adapteraa)
            spinnerEq.setOnItemClickListener { parent, view, position, id ->
                selectedEquip = position
                Toast.makeText(requireContext(), selectedEquip.toString(), Toast.LENGTH_SHORT).show()
            }
        }

        if (!strDepart.equals("")) {
            for (i in 0..<(JSONmyDepart?.length() ?: 1)) {
                var departStr = ""
                departStr = "№: " + JSONmyDepart?.getJSONObject(i)?.getInt("departmentId")
                departStr += "  " + JSONmyDepart?.getJSONObject(i)?.getJSONArray("departmentHistoryId")
                    ?.getJSONObject(0)?.getString("departmentName")
                if (departStr.length > 30){
                    var departtemp = departStr.substring(0, 30)
                    departtemp += "\n" + departStr.substring(30)
                    departStr = departtemp
                }
                itemsDepart.add(departStr)
                departStr = JSONmyDepart?.getJSONObject(i)?.getInt("departmentId").toString()
                itemsDepartId.add(departStr)
            }
            var adapteraa = ArrayAdapter(requireContext(), R.layout.list_item_my, itemsDepart)
            spinnerDepart.setAdapter(adapteraa)
            spinnerDepart.setOnItemClickListener { parent, view, position, id ->
                selectedDepart = position
            }
        }

        binding.buttonAddObject.setOnClickListener {
            var isAllSelectedDepart: Boolean
            if (selectedDepart == 0 || selectedDepart == -1) {
                binding.tvDepartNot.visibility = View.VISIBLE
                isAllSelectedDepart = false
            } else {
                binding.tvDepartNot.visibility = View.GONE
                isAllSelectedDepart = true
            }
            var isAllSelectedEquip: Boolean
            if (selectedEquip == 0 || selectedEquip == -1) {
                binding.tvEquipNot.visibility = View.VISIBLE
                isAllSelectedEquip = false
            } else {
                binding.tvEquipNot.visibility = View.GONE
                isAllSelectedEquip = true
            }

            var isTextTyped: Boolean
            if (binding.tvInvNumEdit.text.toString() == "") {
                binding.tvNumNot.text = "Не заполнено"
                binding.tvNumNot.visibility = View.VISIBLE
                isTextTyped = false
            } else {
                binding.tvNumNot.visibility = View.GONE
                isTextTyped = true
            }

            try{
                Integer.valueOf(binding.tvInvNumEdit.text.toString())
                isTextTyped = true
                binding.tvNumNot.visibility = View.GONE
            } catch (e: Exception){
                binding.tvNumNot.text = "В номере только цифры"
                binding.tvNumNot.visibility = View.VISIBLE
                isTextTyped = false
                e.printStackTrace()
            }

            if (isAllSelectedDepart && isTextTyped && isAllSelectedEquip) {
                var equipAdd = "0"
                if (selectedEquip != -1) {
                    equipAdd = itemsEqId[selectedEquip]
                }
                var departAdd = "0"
                if (selectedDepart != -1) {
                    departAdd = itemsDepartId[selectedDepart]
                }

                addData(
                    equipAdd, binding.tvInvNumEdit.text.toString(),SendData.userId, departAdd,
                    binding.tvObjectNoteName.text.toString())
                view?.let { it1 ->
                    Navigation.findNavController(it1).navigate(R.id.action_fragment_add_object_to_fragment_add_object_done)
                }
            } else {
                Toast.makeText(context, "Поля не выбраны!", Toast.LENGTH_SHORT).show()
            }
        }

        return root
    }

    private fun addData(
        equipmentmId: String,
        inventoryNum: String,
        personId: String,
        departmentId: String,
        note: String
    ) {
        val queue = Volley.newRequestQueue(context)
        var url = "http://${SendData.IPSERVER}:${SendData.PORTDB}/equipment_objects"
        var postparams = JSONObject()
        var equipModel = JSONObject()
        equipModel.put("equipmentmId", Integer.valueOf(equipmentmId))
        postparams.put("equipmentmId", equipModel)

        postparams.put("inventoryNum", Integer.valueOf(inventoryNum))

        var person = JSONObject()
        person.put("personId", Integer.valueOf(personId))
        postparams.put("person", person)

        var equipHistory = JSONArray()
        var equipHistoryId = JSONObject()
        var departId = JSONObject()
        departId.put("departmentId", departmentId)
        equipHistoryId.put("departmentId", departId)
        equipHistory.put(equipHistoryId)
        postparams.put("equipmenthId", equipHistory)

        postparams.put("note", note)

        Log.e("SasParams", postparams.toString())

        val request: JsonObjectRequest =
            object : JsonObjectRequest(Method.POST, url, postparams, Response.Listener { response ->
                Toast.makeText(context, "Объект создан!", Toast.LENGTH_SHORT).show()
                try {
                    val jsonObject = response
                    Log.e("SasResponse", jsonObject.toString())
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error ->
                Log.e("tag", "error is " + error!!.message)
                Toast.makeText(context, "Объект не отправлен", Toast.LENGTH_SHORT)
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