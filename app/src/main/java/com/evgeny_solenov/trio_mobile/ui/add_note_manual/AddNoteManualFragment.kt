package com.example.testandro6.ui.add_note_manual

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.Navigation
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.testandro6.*
import com.example.testandro6.databinding.FragmentAddNoteManualBinding
import com.google.android.material.navigation.NavigationBarView
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.*
import java.sql.Timestamp

class AddNoteManualFragment : Fragment() {
    private var navBar: NavigationBarView? = null
    private var responseUpdateEvent = ""
    private var selectedEquip = 0
    private var selectedDepart = 0
    private var selectedEventT = 0
    private var selectedWasteG = 0
    private var selectedWasteT = 0
    var strDepart: StringBuilder = StringBuilder("")
    var strEquip: StringBuilder = StringBuilder("")
    var strType: StringBuilder = StringBuilder("")
    var strWasteT: StringBuilder = StringBuilder("")
    var strWasteG: StringBuilder = StringBuilder("")
    private val CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1888
    private val REQUEST_IMAGE_CAPTURE = 1889
    private var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private var imageBytes: ByteArray? = null

    private val loadingPB: ProgressBar? = null
    private var pref: SharedPreferences? = null
    private var prefSave: SharedPreferences? = null

    private var _binding: FragmentAddNoteManualBinding? = null

    lateinit var currentPhotoPath: String

    private val binding get() = _binding!!

    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAddNoteManualBinding.inflate(inflater, container, false)
        val root: View = binding.root
        if (checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA),123)
        }
        pref = activity?.getSharedPreferences("NEW_NOTES", Context.MODE_PRIVATE)
        Log.e("PREFS NOTE" , pref?.all.toString())
        navBar = requireActivity().findViewById(R.id.bottomNavigationView)
        navBar?.visibility = View.GONE

        var JSONmyDepart: JSONArray? = null
        var JSONmyEquip: JSONArray? = null
        var JSONmyTypes: JSONArray? = null
        var JSONmyWasteG: JSONArray? = null
        var JSONmyWasteT: JSONArray? = null

        var spinnerEq = binding.tvAutoEquip
        spinnerEq.visibility = View.GONE
        val itemsEq = ArrayList<String>()
        val itemsEqId = ArrayList<String>()

        var spinnerDep = binding.tvAutoDepart
        val itemsDep = ArrayList<String>()
        val itemsDepId = ArrayList<String>()

        var spinnerWastesG = binding.tvAutoWasteG
        val itemsWastesG = ArrayList<String>()
        val itemsWastesGId = ArrayList<String>()
        spinnerWastesG.visibility = View.GONE

        var spinnerWastesT = binding.tvAutoWasteT
        val itemsWastesT = ArrayList<String>()
        val itemsWastesTId = ArrayList<String>()
        spinnerWastesT.visibility = View.GONE

        var spinnerTypes = binding.tvAutoEventT
        val itemsTypes = ArrayList<String>()
        val itemsTypesId = ArrayList<String>()

        itemsDep.add("Не выбрано")
        itemsDepId.add("-1")
        pref = activity?.getSharedPreferences("ADD_NOTE", Context.MODE_PRIVATE)
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

        var threaCheck = Thread {
            var temp = apiExecute("departments")
        }
        threaCheck.start()
        threaCheck.join()
        val connection = binding.connection
        if (ConnectChecker.isOnline(requireContext()) && !SendData.isBadConnection) {
            connection.visibility = View.GONE
            var threa = Thread {
                strDepart = apiExecute("departments")
                strEquip = apiExecute("equipment_objects")
                strType = apiExecute("event_types")
                strWasteG = apiExecute("waste_groups")
                strWasteT = apiExecute("waste_types")
            }
            threa.start()
            threa.join()
        } else {
            connection.visibility = View.VISIBLE
            if (pref?.getString("strDepart", "") != "") {
                strDepart.clear().append(pref?.getString("strDepart", "")!!)
                strEquip.clear().append(pref?.getString("strEquip", "")!!)
                strType.clear().append(pref?.getString("strType", "")!!)
                strWasteG.clear().append(pref?.getString("strWasteG", "")!!)
                strWasteT.clear().append(pref?.getString("strWasteT", "")!!)
            } else {
                return root
            }
        }
        JSONmyDepart = JSONArray(strDepart.toString())
        JSONmyEquip = JSONArray(strEquip.toString())
        JSONmyTypes = JSONArray(strType.toString())
        JSONmyWasteG = JSONArray(strWasteG.toString())
        JSONmyWasteT = JSONArray(strWasteT.toString())
            if (!strDepart.equals("")) {
                for (i in 0..<(JSONmyDepart?.length() ?: 1)) {
                    var depStr = ""
                    if ((JSONmyDepart?.getJSONObject(i)?.getJSONArray("departmentHistoryId")?.length() ?: 0) != 0) {
                        depStr = JSONmyDepart?.getJSONObject(i)?.getJSONArray("departmentHistoryId")?.getJSONObject(0)
                            ?.getString("departmentName") ?: ""
                    }
                    if (depStr.length > 30){
                        var deptemp = depStr.substring(0, 30)
                        deptemp += "\n" + depStr.substring(30)
                        depStr = deptemp
                    }
                    itemsDep.add(depStr)
                    depStr = JSONmyDepart?.getJSONObject(i)?.getInt("departmentId").toString()
                    itemsDepId.add(depStr)
                }
                var adapteraa = ArrayAdapter(requireContext(), R.layout.list_item_my, itemsDep)
                spinnerDep.setAdapter(adapteraa)
                spinnerDep.setOnItemClickListener { parent, view, position, id ->
                    spinnerEq.setText("Не выбрано")
                    selectedEquip = 0
                    selectedDepart = position

//                    Toast.makeText(requireContext(), selectedDepart.toString(), Toast.LENGTH_SHORT).show()
                    Log.e("SelectedDepart", selectedDepart.toString())
                    itemsEq.clear()
                    itemsEqId.clear()
                    itemsEq.add("Не выбрано")
                    itemsEqId.add("-1")
                    for (i in 0..<(JSONmyEquip?.length() ?: 1)) {
                        var equipStr = ""
                        if ((JSONmyEquip?.getJSONObject(i)?.getJSONArray("equipmenthId")?.length() ?: 0) != 0) {
                            if (JSONmyEquip?.getJSONObject(i)?.getJSONArray("equipmenthId")?.getJSONObject(0)
                                    ?.getJSONObject("departmentId")?.getInt("departmentId") ==
                                itemsDepId[position].toInt()
                            ) {
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
                        }
                    }
                    if (position < 2) {
                        spinnerEq.setAdapter(null)
                        spinnerEq.visibility = View.GONE
                    } else {
                        spinnerEq.visibility = View.VISIBLE
                    }

                }

                itemsTypes.add("Не выбрано")
                itemsTypesId.add("-1")
                for (i in 0..<(JSONmyTypes?.length() ?: 1)) {
                    var typeStr = ""
                    typeStr = JSONmyTypes?.getJSONObject(i)?.getString("eventTypeName") ?: ""
                    if (typeStr.length > 30){
                        var typetemp = typeStr.substring(0, 30)
                        typetemp += "\n" + typeStr.substring(30)
                        typeStr = typetemp
                    }
                    itemsTypes.add(typeStr)
                    typeStr = JSONmyTypes?.getJSONObject(i)?.getInt("eventtId").toString()
                    itemsTypesId.add(typeStr)
                }
                var adapteraa2 = ArrayAdapter(requireContext(), R.layout.list_item_my, itemsTypes)
                spinnerTypes.setAdapter(adapteraa2)
                spinnerTypes.setOnItemClickListener { parent, view, position, id ->
                    spinnerWastesT.setText("Не выбрано")
                    spinnerWastesT.visibility = View.GONE
                    spinnerWastesG.setText("Не выбрано")
                    selectedWasteT = 0
                    selectedWasteG = 0
                    selectedEventT = position
                    itemsWastesG.clear()
                    itemsWastesGId.clear()
                    itemsWastesG.add("Не выбрано")
                    itemsWastesGId.add("-1")
                    for (i in 0..<(JSONmyWasteG?.length() ?: 1)) {
                        var wasteGStr = ""

                        if (JSONmyWasteG?.getJSONObject(i)?.getJSONObject("eventType")?.getInt("eventtId") ==
                            itemsTypesId[position].toInt()
                        ) {
                            wasteGStr = JSONmyWasteG?.getJSONObject(i)?.getString("wastegName") ?: ""
                            if (wasteGStr.length > 30){
                                var wasteGtemp = wasteGStr.substring(0, 30)
                                wasteGtemp += "\n" + wasteGStr.substring(30)
                                wasteGStr = wasteGtemp
                            }
                            itemsWastesG.add(wasteGStr)
                            wasteGStr = JSONmyWasteG?.getJSONObject(i)?.getInt("wastegId").toString()
                            itemsWastesGId.add(wasteGStr)
                        }
                    }
                    var adapteraa2 = ArrayAdapter(requireContext(), R.layout.list_item_my, itemsWastesG)
                    spinnerWastesG.setAdapter(adapteraa2)
                    spinnerWastesG.setOnItemClickListener { parent, view, position, id ->
                        spinnerWastesT.setText("Не выбрано")
                        selectedWasteT = 0
                        selectedWasteG = position
                        itemsWastesT.clear()
                        itemsWastesTId.clear()
                        itemsWastesT.add("Не выбрано")
                        itemsWastesTId.add("-1")
                        for (i in 0..<(JSONmyWasteT?.length() ?: 1)) {
                            var wasteTStr = ""

                            if (JSONmyWasteT?.getJSONObject(i)?.getJSONObject("wasteGroup")?.getInt("wastegId") ==
                                itemsWastesGId[position].toInt()
                            ) {
                                wasteTStr = JSONmyWasteT?.getJSONObject(i)?.getString("wastetName") ?: ""
                                if (wasteTStr.length > 30){
                                    var wasteTtemp = wasteTStr.substring(0, 30)
                                    wasteTtemp += "\n" + wasteTStr.substring(30)
                                    wasteTStr = wasteTtemp
                                }
                                itemsWastesT.add(wasteTStr)
                                wasteTStr = JSONmyWasteT?.getJSONObject(i)?.getInt("wastetId").toString()
                                itemsWastesTId.add(wasteTStr)
                            }
                        }
                        var adapteraa2 = ArrayAdapter(requireContext(), R.layout.list_item_my, itemsWastesT)
                        spinnerWastesT.setAdapter(adapteraa2)
                        spinnerWastesT.setOnItemClickListener { parent, view, position, id ->
                            selectedWasteT = position
                        }
                        if (position < 1) {
                            spinnerWastesT.setAdapter(null)
                            spinnerWastesT.visibility = View.GONE
                        } else if (position == 1 && selectedEventT == 1) {
                            spinnerWastesT.visibility = View.VISIBLE
                        } else {
                            spinnerWastesT.visibility = View.VISIBLE
                            if (itemsWastesT.size < 2){
                                spinnerWastesT.visibility = View.GONE
                            }
                        }
                    }
                if (position == 0) {
                    spinnerWastesG.setAdapter(null)
                    spinnerWastesG.visibility = View.GONE
                    spinnerWastesG.isActivated = false
                    spinnerWastesT.setAdapter(null)
                    spinnerWastesT.visibility = View.GONE
                    spinnerWastesT.isActivated = false
                } else {
                    spinnerWastesG.visibility = View.VISIBLE
                    if (itemsWastesG.size < 2){
                        spinnerWastesG.visibility = View.GONE
                    }
                }
            }
            }

        val statusArr = arrayListOf(
            R.drawable.status_green, R.drawable.status_yellow,
            R.drawable.status_orange, R.drawable.status_red
        )
        var spinnerStatus = binding.spinnerStatus
        val itemStatus = arrayListOf(
            "Работа в штатном режиме", "Незначительное отклонение", "Угроза аварии",
            "Аварийное состояние"
        )

        val trioAdapterImg = context?.let { TrioAdapterImg(it, statusArr, itemStatus) }
        spinnerStatus.setAdapter(trioAdapterImg)

        pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: $uri")
                var istr = context?.contentResolver?.openOutputStream(uri)
                Log.d("PhotoPicker", "Selected URI: $istr")
                var bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, uri)

                binding.ivPhoto.setImageBitmap(bitmap)
                val stream = ByteArrayOutputStream()
                var drawable = binding.ivPhoto.drawable
                val bitmapDrawable = (drawable as BitmapDrawable)
                bitmap = bitmapDrawable.bitmap
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                val imageInByte = stream.toByteArray()
                imageStr = imageInByte
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }
        binding.buttonAddPhoto.setOnClickListener {
            pickMedia?.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        val buttonSave = binding.buttonSaveEvent
        buttonSave.setOnClickListener {
            var isAllSelectedEquip: Boolean
            var isAllSelectedEvents: Boolean
            if (selectedDepart == 0){
                binding.tvDepNot.visibility = View.VISIBLE
                binding.tvEquipNot.visibility = View.GONE
                isAllSelectedEquip = false
            } else {
                binding.tvEquipNot.visibility = View.GONE
                isAllSelectedEquip = true
                binding.tvDepNot.visibility = View.GONE
            }

            if (selectedEventT == 0){
                binding.tvEventTypeNot.visibility = View.VISIBLE
                binding.tvWasteGNot.visibility = View.GONE
                isAllSelectedEvents = false
            } else {
                if (spinnerWastesG.isVisible && selectedWasteG == 0 && itemsWastesG.size > 1){
                    binding.tvWasteGNot.visibility = View.VISIBLE
                    binding.tvWasteTNot.visibility = View.GONE
                    isAllSelectedEvents = false
                } else {
                    if (spinnerWastesT.isVisible && selectedWasteT == 0 && itemsWastesT.size > 1){
                        binding.tvWasteTNot.visibility = View.VISIBLE
                        isAllSelectedEvents = false
                    } else {
                        binding.tvWasteTNot.visibility = View.GONE
                        isAllSelectedEvents = true
                    }
                    binding.tvWasteGNot.visibility = View.GONE
                }
                binding.tvEventTypeNot.visibility = View.GONE
            }
            if (isAllSelectedEquip && isAllSelectedEvents){
                var wasteGroupAdd = "0"
                if (selectedWasteG > 0){
                    wasteGroupAdd = itemsWastesGId[selectedWasteG]
                }
                var wasteTypeAdd = "0"
                if (selectedWasteT > 0){
                    wasteTypeAdd = itemsWastesTId[selectedWasteT]
                }
                var eventTypeAdd = "0"
                if (selectedEventT > 0){
                    eventTypeAdd = itemsTypesId[selectedEventT]
                }
                var fileName = ""
                if (imageStr != null){
                    fileName = "eventImage" + System.currentTimeMillis().toString() + ".jpg"
                }
                var textNote = ""
                if (binding.tvEventNoteName.text.toString() != ""){
                    textNote = binding.tvEventNoteName.text.toString()
                } else {
                    textNote = "Без описания"
                }

                addData(
                    Timestamp(System.currentTimeMillis()).toString().replace(' ', 'T'),
                    itemsDepId[selectedDepart] ,itemsEqId[selectedEquip],
                    SendData.userId, eventTypeAdd, "1", textNote,
                    spinnerStatus.selectedItemPosition.toString(), wasteTypeAdd, wasteGroupAdd, fileName)

                UploadFilesTask().execute(fileName)

                Thread{
                    while (responseUpdateEvent == ""){

                    }
                    if (responseUpdateEvent == "updated"){
                        SendData.data = "note saved"
                    } else {
                        SendData.data = "note error"
                    }
                    activity?.runOnUiThread{
                        view?.let { it1 ->
                            Navigation.findNavController(it1).navigate(R.id.action_add_note_manual_to_fragment_add_note_done)
                        }
                    }
                }.start()

                Log.e("AllSelected", "TRUE")
            } else {
                Toast.makeText(context, "Поля не выбраны!", Toast.LENGTH_SHORT).show()
                Log.e("AllSelected", "FALSE")
            }
        }
        return root
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
                    Toast.makeText(context, "Нет связи с сервером", Toast.LENGTH_SHORT).show()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("PhotoPickerOnResult", "Launch")
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE || requestCode == REQUEST_IMAGE_CAPTURE) {
            Log.d("PhotoPickerOnResult", "Request Code Ok")
            if (resultCode == Activity.RESULT_OK) {
                Log.d("PhotoPickerOnResult", "Result Code Ok")
                var bmp = data?.extras!!["data"] as Bitmap?
                try {
                    binding.ivPhoto.setImageBitmap(bmp)
                    val stream = ByteArrayOutputStream()
                    var drawable = binding.ivPhoto.drawable
                    val bitmapDrawable = (drawable as BitmapDrawable)
                    val bitmap = bitmapDrawable.bitmap
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    val imageInByte = stream.toByteArray()
                    imageStr = imageInByte
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                pickMedia?.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }
    }

    private fun addData(
        dateTime: String,
        departmentId: String,
        equipmentoId: String,
        personId: String,
        eventtId: String,
        taskId: String,
        note: String,
        eventlId: String,
        wastetId: String,
        wastegId: String,
        imageName: String
    ) {
        val queue = Volley.newRequestQueue(context)
        var url = "http://${SendData.IPSERVER}:${SendData.PORTDB}/events"
        var postparams = JSONObject()
        postparams.put("dateTime", dateTime)
        var departObject = JSONObject()
        departObject.put("departmentId", Integer.valueOf(departmentId))
        postparams.put("departmentId", departObject)
        var equipObject = JSONObject()
        equipObject.put("equipmentoId", Integer.valueOf(equipmentoId))
        postparams.put("equipmentoId", equipObject)
        var personObject = JSONObject()
        personObject.put("personId", Integer.valueOf(personId))
        postparams.put("personId", personObject)
        var eventObject = JSONObject()
        eventObject.put("eventtId", Integer.valueOf(eventtId))
        postparams.put("eventtId", eventObject)
        var taskObject = JSONObject()
        taskObject.put("taskId", Integer.valueOf(taskId))
        postparams.put("taskId", taskObject)
        postparams.put("note", note)
        var eventlObject = JSONObject()
        eventlObject.put("eventlId", Integer.valueOf(eventlId))
        postparams.put("eventlId", eventlObject)
        var wastetObject = JSONObject()
        wastetObject.put("wastetId", Integer.valueOf(wastetId))
        postparams.put("wastetId", wastetObject)
        var wastegObject = JSONObject()
        wastegObject.put("wastegId", Integer.valueOf(wastegId))
        postparams.put("wastegId", wastegObject)
        postparams.put("imageName", imageName)

        Log.e("SasParams", postparams.toString())
        var saveEvent = postparams

        if (ConnectChecker.isOnline(requireContext()) && !SendData.isBadConnection) {
            val request: JsonObjectRequest =
                @SuppressLint("CommitPrefEdits")
                object : JsonObjectRequest(Method.POST, url, postparams, Response.Listener { response ->
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Запись отправлена!", Toast.LENGTH_SHORT).show()
                    }
                    try {
                        val jsonObject = response
                        responseUpdateEvent = "updated"
                        Log.e("SasResponse", jsonObject.toString())
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }, Response.ErrorListener { error ->
                    responseUpdateEvent = "error"
                    Log.e("tag", "error is " + error!!.message)
                }) {
                }
            queue.add(request)
        } else {
            activity?.runOnUiThread {
                Toast.makeText(context, "Запись не отправлена", Toast.LENGTH_SHORT)
                    .show()
            }
            responseUpdateEvent = "error"
            prefSave = activity?.getSharedPreferences("NEW_NOTES", Context.MODE_PRIVATE)
            var count = 0
            while (prefSave?.contains("saveNote$count") == true){
                count++
            }
            prefSave?.edit()?.putString("saveNote$count", saveEvent.toString())?.apply()
            Log.e("SasParams", saveEvent.toString())
            Log.e("PREFS Str $count", prefSave?.getString("saveNote$count", "").toString())
            SendData.isBadConnection = false
        }
    }

    private class UploadFilesTask : AsyncTask<String?, Int?, Int>() {
        override fun doInBackground(vararg urls: String?): Int {
            return uploadFile(urls[0])
        }
    }

    companion object {
        var savedState = Bundle()
        var imageStr: ByteArray? = null
        fun uploadFile(fileNameArg: String?): Int {
            if (imageStr == null) {
                return -1
            }
            var fileName = "$fileNameArg"
            var upLoadServerUri = "http://${SendData.IPSERVER}:${SendData.PORTDB}/images/db/upload"
            var serverResponseCode = 0

            var strBld = StringBuilder()

            var conn: HttpURLConnection? = null
            var dos: DataOutputStream? = null
            var lineEnd = "\r\n"
            var twoHyphens = "--"
            var boundary = "WebAppBoundary"
            var bytesRead = 0
            var bytesAvailable = 0
            var bufferSize = 0
            var buffer: ByteArray? = null
            var maxBufferSize = 10 * 1024 * 1024

            try {
                var bytesInputStream = ByteArrayInputStream(imageStr)
                var url = URL(upLoadServerUri)

                conn = url.openConnection() as HttpURLConnection?
                conn?.setDoInput(true) // Allow Inputs
                conn?.setDoOutput(true) // Allow Outputs
                conn?.setUseCaches(false) // Don't use a Cached Copy
                conn?.setRequestMethod("POST")
                conn?.setRequestProperty("Connection", "Keep-Alive")
                strBld.append("Connection: Keep-Alive")
                conn?.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary + lineEnd)
                strBld.append("Content-Type: multipart/form-data;boundary=" + boundary + lineEnd)

                dos = DataOutputStream(conn?.outputStream)

                dos.writeBytes(twoHyphens + boundary + lineEnd)
                strBld.append(twoHyphens + boundary + lineEnd)

                dos.writeBytes(
                    "Content-Disposition: form-data; name=\"file\";filename="
                            + "\"" + fileName + "\"" + lineEnd
                )
                strBld.append(
                    "Content-Disposition: form-data; name=\"file\";filename="
                            + "\"" + fileName + "\"" + lineEnd
                )
                dos.writeBytes("Content-Type: image/jpeg")
                strBld.append("Content-Type: image/jpeg")
                dos.writeBytes(lineEnd + lineEnd)
                strBld.append(lineEnd + lineEnd)

                Log.i("DataOutputStream: ", dos.toString())

                bytesAvailable = bytesInputStream.available()

                bufferSize = bytesAvailable.coerceAtMost(maxBufferSize)
                buffer = ByteArray(bufferSize)
                bytesRead = bytesInputStream.read(buffer, 0, bufferSize)
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize)
                    strBld.append(buffer)
                    bytesAvailable = bytesInputStream.available()
                    bufferSize = bytesAvailable.coerceAtMost(maxBufferSize)
                    bytesRead = bytesInputStream.read(buffer, 0, bufferSize)
                }
                dos.writeBytes(lineEnd)
                strBld.append(lineEnd)
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)
                strBld.append(twoHyphens + boundary + twoHyphens + lineEnd)

                Log.i("DataOutputStream2: ", strBld.toString())
                serverResponseCode = conn!!.responseCode
                val serverResponseMessage = conn.responseMessage
                Log.i(
                    "uploadFile", "HTTP Response is : "
                            + serverResponseMessage + ": " + serverResponseCode
                )
                bytesInputStream.close()
                dos.flush()
                dos.close()

            } catch (e: MalformedURLException) {
                e.printStackTrace()


                Log.e("Upload file to server", "error: $e", e);
            } catch (e: Exception) {
                e.printStackTrace();

                Log.e("Upload file to server Exception", "Exception : $e", e);
            }
            return serverResponseCode
        }
        }
}