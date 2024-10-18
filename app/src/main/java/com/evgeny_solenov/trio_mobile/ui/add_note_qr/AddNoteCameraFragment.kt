package com.example.testandro6.ui.add_note_qr

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.testandro6.*
import com.example.testandro6.databinding.FragmentAddNoteCameraBinding
import com.example.testandro6.ui.add_note_manual.AddNoteManualFragment
import com.google.android.material.navigation.NavigationBarView
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.*
import java.sql.Timestamp


class AddNoteCameraFragment : Fragment() {
    private var isFirstResume: Boolean = true
    private var navBar: NavigationBarView? = null
    private var responseUpdateEvent = ""
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

    private val loadingPB: ProgressBar? = null
    private var pref: SharedPreferences? = null
    private var prefSave: SharedPreferences? = null

    private var _binding: FragmentAddNoteCameraBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddNoteCameraBinding.inflate(inflater, container, false)
        val root: View = binding.root
        navBar = requireActivity().findViewById(R.id.bottomNavigationView)
        navBar?.visibility = View.GONE

        var JSONmyTypes: JSONArray? = null
        var JSONmyWasteG: JSONArray? = null
        var JSONmyWasteT: JSONArray? = null

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

        itemsTypes.add("Не выбрано")
        itemsTypesId.add("-1")
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
        val connection = binding.connection
        if (ConnectChecker.isOnline(requireContext()) && !SendData.isBadConnection) {
            connection.visibility = View.GONE
            var threa = Thread {
                strDepart = apiExecute("departments")
                strEquip = apiExecute("equipment_objects")
                strType = apiExecute("event_types")
                strWasteG = apiExecute("waste_groups")
                strWasteT = apiExecute("waste_types")

                JSONmyTypes = JSONArray(strType.toString())
                JSONmyWasteG = JSONArray(strWasteG.toString())
                JSONmyWasteT = JSONArray(strWasteT.toString())
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

            JSONmyTypes = JSONArray(strType.toString())
            JSONmyWasteG = JSONArray(strWasteG.toString())
            JSONmyWasteT = JSONArray(strWasteT.toString())

        }
        if (!strDepart.equals("")) {
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
                    // spinnerWastesT.visibility = View.VISIBLE
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
            var isAllSelectedEvents: Boolean
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
            if (isAllSelectedEvents){
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
                var eqStr = "-1"
                var depStr = "-1"

                if (binding.textEquip.text.toString() != "-"){
                    eqStr = SendData.data.split(",").get(1)
                }
                depStr = SendData.data.split(",").get(0)
                addData(
                    Timestamp(System.currentTimeMillis()).toString().replace(' ', 'T'),
                    depStr, eqStr, SendData.userId, eventTypeAdd, "1", textNote,
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
                            Navigation.findNavController(it1).navigate(R.id.action_add_note_camera_to_fragment_add_note_done)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

    override fun onResume() {
        super.onResume()
            if (SendData.data != "" && SendData.data.length < 6 && SendData.data.contains(',')) {
                if (SendData.data.split(",").get(1).toInt() != -1) {
                    var numEq = SendData.data.split(",").get(1).toInt()
                    var strEq = apiExecute("equipment_objects/" + numEq)
                    var jsonEq: JSONArray? = JSONArray(strEq.toString())
                    var depId = jsonEq?.getJSONObject(0)?.getJSONArray("equipmenthId")?.getJSONObject(0)
                        ?.getJSONObject("departmentId")?.getInt("departmentId")
                    var eqName = jsonEq?.getJSONObject(0)?.getJSONObject("equipmentmId")?.getString("modelName")
                    eqName += "\n Инв. № " + jsonEq?.getJSONObject(0)?.getString("inventoryNum")
                    binding.textEquip.text = eqName.toString()
                    var strDep = apiExecute("departments/" + depId)
                    var jsonDep: JSONArray? = JSONArray(strDep.toString())
                    var depName = jsonDep?.getJSONObject(0)?.getJSONArray("departmentHistoryId")?.getJSONObject(0)
                        ?.getString("departmentName")
                    binding.textDepart.text = depName.toString()
                } else {
                    var strDep = apiExecute("departments/" + SendData.data.split(",").get(0).toInt())
                    var jsonDep: JSONArray? = JSONArray(strDep.toString())
                    var depName = jsonDep?.getJSONObject(0)?.getJSONArray("departmentHistoryId")?.getJSONObject(0)
                        ?.getString("departmentName")
                    binding.textDepart.text = depName.toString()
                    binding.textEquip.text = "-"
                }
            } else {
                if (isFirstResume){
                    isFirstResume = false
                } else {
                    activity?.supportFragmentManager?.popBackStack()
                }
            }
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
                    Toast.makeText(context, "Запись отправлена!", Toast.LENGTH_SHORT).show()
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
        var imageStr: ByteArray? = null
        fun uploadFile(fileNameArg: String?): Int {
            if (imageStr == null){
                return -1
            }
            var fileName = "$fileNameArg"
            var upLoadServerUri = "http://90.156.226.25:${SendData.PORTDB}/images/db/upload"
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
            //var sourceFile: File = File(sourceFileUri)
//            if (!sourceFile.isFile()) {
//                Log.e(
//                    "uploadFile", ("Source File not exist :"
//                            + sourceFileUri))
//                return 0
//            } else
//            {
            try {

                // open a URL connection to the Servlet
//                    var fileInputStream = FileInputStream(sourceFile)
                var bytesInputStream = ByteArrayInputStream(imageStr)
                var url = URL(upLoadServerUri)

                // Open a HTTP  connection to  the URL
                conn = url.openConnection() as HttpURLConnection?
                conn?.setDoInput(true) // Allow Inputs
                conn?.setDoOutput(true) // Allow Outputs
                conn?.setUseCaches(false) // Don't use a Cached Copy
                conn?.setRequestMethod("POST")
                conn?.setRequestProperty("Connection", "Keep-Alive")
                strBld.append("Connection: Keep-Alive")
//                    conn?.setRequestProperty("ENCTYPE", "multipart/form-data")
                conn?.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary + lineEnd)
                strBld.append("Content-Type: multipart/form-data;boundary=" + boundary + lineEnd)
                //conn?.setRequestProperty("file", fileName)

                dos = DataOutputStream(conn?.outputStream)

                dos.writeBytes(twoHyphens + boundary + lineEnd)
                strBld.append(twoHyphens + boundary + lineEnd)

                dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename="
                        + "\"" + fileName + "\""  + lineEnd)
                strBld.append("Content-Disposition: form-data; name=\"file\";filename="
                        + "\"" + fileName + "\""  + lineEnd)
                dos.writeBytes("Content-Type: image/jpeg")
                strBld.append("Content-Type: image/jpeg")
                dos.writeBytes(lineEnd + lineEnd)
                strBld.append(lineEnd + lineEnd)

                Log.i("DataOutputStream: ", dos.toString())

                // create a buffer of  maximum size
//                    bytesAvailable = fileInputStream.available()
                bytesAvailable = bytesInputStream.available()

                bufferSize = bytesAvailable.coerceAtMost(maxBufferSize)
                buffer = ByteArray(bufferSize)

                // read file and write it into form...
//                    bytesRead = fileInputStream.read(buffer, 0, bufferSize)
                bytesRead = bytesInputStream.read(buffer, 0, bufferSize)

                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize)
                    strBld.append(buffer)
//                        bytesAvailable = fileInputStream.available()
                    bytesAvailable = bytesInputStream.available()
                    bufferSize = bytesAvailable.coerceAtMost(maxBufferSize)
//                        bytesRead = fileInputStream.read(buffer, 0, bufferSize)
                    bytesRead = bytesInputStream.read(buffer, 0, bufferSize)

                }


                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd)
                strBld.append(lineEnd)
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)
                strBld.append(twoHyphens + boundary + twoHyphens + lineEnd)



                Log.i("DataOutputStream2: ", strBld.toString())
                // Responses from the server (code and message)
                serverResponseCode = conn!!.responseCode
                val serverResponseMessage = conn.responseMessage

                Log.i(
                    "uploadFile", "HTTP Response is : "
                            + serverResponseMessage + ": " + serverResponseCode
                )


                //close the streams //
//                    fileInputStream.close()
                bytesInputStream.close()
                dos.flush()
                dos.close()

            } catch (e: MalformedURLException) {
                e.printStackTrace()


                Log.e("Upload file to server", "error: $e", e);
            } catch (e:Exception) {
                e.printStackTrace();

                Log.e("Upload file to server Exception", "Exception : " + e, e);
            }
            return serverResponseCode

        } // End else block
    }
}