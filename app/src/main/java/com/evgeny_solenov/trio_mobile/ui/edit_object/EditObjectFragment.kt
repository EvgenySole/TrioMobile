package com.example.testandro6.ui.add_task

import android.annotation.SuppressLint
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
import com.example.testandro6.databinding.FragmentEditObjectBinding
import com.google.android.material.navigation.NavigationBarView
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class EditObjectFragment : Fragment() {
    private var navBar: NavigationBarView? = null
    private var selectedEquip = -1
    private var selectedDepart = -1
    private var loadEquip = -1
    private var loadDepart = -1
    private var responseUpdateObject = ""
    var strEquip: StringBuilder = StringBuilder("")
    var strDepart: StringBuilder = StringBuilder("")
    private var _binding: FragmentEditObjectBinding? = null
    private val binding get() = _binding!!
    private var pref: SharedPreferences? = null
    var inputText: String? = ""

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditObjectBinding.inflate(inflater, container, false)
        val root: View = binding.root
        navBar = requireActivity().findViewById(R.id.bottomNavigationView)
        navBar?.visibility = View.GONE

        inputText = SendData.data
        SendData.data = ""
        var JSONmy = JSONObject(inputText.toString())

        binding.textNumObject.text = binding.textNumObject.text.toString() + JSONmy.getInt("equipmentoId")
        binding.tvInvNumEdit.setText(JSONmy.getString("inventoryNum"))
        binding.tvObjectNoteName.setText(JSONmy.getString("note"))

        try {
            loadEquip = JSONmy.getJSONObject("equipmentmId").getInt("equipmentmId")
        } catch (_: JSONException){}
        try {
            loadDepart = JSONmy.getJSONArray("equipmenthId").getJSONObject(0)
                .getJSONObject("departmentId").getInt("departmentId")
        } catch (_: JSONException){}

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
            spinnerEq.setText(itemsEq[itemsEqId.indexOf(loadEquip.toString())])
            var adapteraa = ArrayAdapter(requireContext(), R.layout.list_item_my, itemsEq)
            spinnerEq.setAdapter(adapteraa)
            spinnerEq.setOnItemClickListener { parent, view, position, id ->
                selectedEquip = position
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
            spinnerDepart.setText(itemsDepart[itemsDepartId.indexOf(loadDepart.toString())])
            var adapteraa = ArrayAdapter(requireContext(), R.layout.list_item_my, itemsDepart)
            spinnerDepart.setAdapter(adapteraa)
            spinnerDepart.setOnItemClickListener { parent, view, position, id ->
                selectedDepart = position
            }
        }

        binding.buttonAddObject.setOnClickListener {
            var isAllSelectedDepart: Boolean
            if ((selectedDepart == 0 || selectedDepart == -1) && loadDepart == -1) {
                binding.tvDepartNot.visibility = View.VISIBLE
                isAllSelectedDepart = false
            } else {
                binding.tvDepartNot.visibility = View.GONE
                isAllSelectedDepart = true
            }
            var isAllSelectedEquip: Boolean
            if ((selectedEquip == 0 || selectedEquip == -1) && loadEquip == -1) {
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
                } else {
                    equipAdd = loadEquip.toString()
                }
                var departAdd = "0"
                if (selectedDepart != -1) {
                    departAdd = itemsDepartId[selectedDepart]
                } else {
                    departAdd = loadDepart.toString()
                }

                updateData(JSONmy.getInt("equipmentoId").toString(),
                    equipAdd, binding.tvInvNumEdit.text.toString(),SendData.userId, departAdd,
                    binding.tvObjectNoteName.text.toString())
                Thread{
                    while (responseUpdateObject == ""){

                    }
                    if (responseUpdateObject == "updated"){
                        SendData.data = "object updated"
                    } else {
                        SendData.data = "object error"
                    }
                    activity?.runOnUiThread{
                        view?.let { it1 ->
                            Navigation.findNavController(it1).navigate(R.id.action_edit_object_fragment_to_fragment_add_object_done)
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
        equipmentmId: String,
        inventoryNum: String,
        personId: String,
        departmentId: String,
        note: String
    ) {
        val queue = Volley.newRequestQueue(context)
        var url = "http://${SendData.IPSERVER}:${SendData.PORTDB}/equipment_objects/" + id + "?"
        if (equipmentmId != ""){
            url += "equipmentmId=$equipmentmId&"
        }
        if (inventoryNum != ""){
            url += "inventoryNum=$inventoryNum&"
        }
        if (personId != ""){
            url += "personId=$personId&"
        }
        if (departmentId != ""){
            url += "departmentId=$departmentId&"
        }
        if (note != ""){
            url += "note=$note"
        }

        url.trim('&')

        var postparams = JSONObject()

        Log.e("Sas", url)

        val request: JsonObjectRequest =
            object : JsonObjectRequest(Method.PUT, url, postparams, Response.Listener { response ->
                Toast.makeText(context, "Объект обновлен!", Toast.LENGTH_SHORT).show()
                try {
                    val jsonObject = response
                    responseUpdateObject  = "updated"
                    Log.e("SasResponse", jsonObject.toString())
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error ->
                responseUpdateObject  = "error"
                Log.e("tag", "error is " + error!!.message)
                Toast.makeText(context, "Объект не обновлен", Toast.LENGTH_SHORT)
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