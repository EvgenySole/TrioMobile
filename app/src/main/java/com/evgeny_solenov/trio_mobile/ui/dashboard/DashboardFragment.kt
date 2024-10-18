package com.example.testandro6.ui.dashboard

import android.annotation.SuppressLint
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
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.testandro6.ConnectChecker
import com.example.testandro6.SendData
import com.example.testandro6.databinding.FragmentDashboardBinding
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.*


class DashboardFragment : Fragment() {
    private var photoView: Bitmap? = null
    private var _binding: FragmentDashboardBinding? = null
    private var pref: SharedPreferences? = null
    private var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private var isPhotoUpdated: Boolean = false
    private var isDataSaved: Boolean = false
    private val binding get() = _binding!!

    @SuppressLint("CommitPrefEdits", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val userId = binding.textView10
        val userName = binding.textView13
        val userRole = binding.textRole
        val btChangeUser = binding.changeUser
        val btEditUser = binding.editUser
        val btSaveData = binding.saveChanges
        val linerEdit = binding.linerEditData

        pref = activity?.getSharedPreferences("USER", Context.MODE_PRIVATE)

        binding.etName.setText(SendData.userName)
        binding.etLogin.setText(SendData.userLogin)
        binding.etPassword.setText(SendData.userPassword)


        userId.text = "ID: 00000" + SendData.userId
        userName.text = SendData.userName
        if (SendData.userRole == "user") {
            userRole.text = ""
        } else if (SendData.userRole == "admin"){
            userRole.text = "Администратор"
        }

        pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: $uri")
                var istr = context?.contentResolver?.openOutputStream(uri)
                Log.d("PhotoPicker", "Selected URI: $istr")
                var bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, uri)
                binding.imageView5.setImageBitmap(bitmap)
                val stream = ByteArrayOutputStream()
                var drawable = binding.imageView5.drawable
                val bitmapDrawable = (drawable as BitmapDrawable)
                bitmap = getResizedBitmap(bitmapDrawable.bitmap, 320)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                val imageInByte = stream.toByteArray()
                imageStr = imageInByte
                isPhotoUpdated = true
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }
        val connection = binding.tvConnection
        var threaCheck = Thread {
            var temp = apiExecute()
        }
        threaCheck.start()
        threaCheck.join()
        if (ConnectChecker.isOnline(requireContext()) && !SendData.isBadConnection) {
            connection.visibility = View.GONE
            loadImage(SendData.userId)

            btEditUser.setOnClickListener {
                linerEdit.visibility = View.VISIBLE
                binding.linerButtonEdit.visibility = View.GONE
                binding.imageView5.setOnClickListener {
                    pickMedia?.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            }

            btSaveData.setOnClickListener {
                linerEdit.visibility = View.GONE
                binding.linerButtonEdit.visibility = View.VISIBLE
                var fileName = "person" + SendData.userId + ".jpg"
                if (binding.etName.text.toString() != "" && binding.etLogin.text.toString() != "" &&
                    binding.etPassword.text.toString() != "") {
                    updateData(
                        SendData.userId.toInt(), binding.etName.text.toString(),
                        binding.etLogin.text.toString(), binding.etPassword.text.toString(), fileName
                    )
                    if (isDataSaved){
                        pref = activity?.getSharedPreferences("USER", Context.MODE_PRIVATE)
                        pref?.edit()?.remove("name")?.apply()
                        pref?.edit()?.remove("login")?.apply()
                        pref?.edit()?.remove("password")?.apply()
                        pref?.edit()?.putString("name", binding.etName.text.toString())?.apply()
                        pref?.edit()?.putString("login", binding.etLogin.text.toString())?.apply()
                        pref?.edit()?.putString("password", binding.etPassword.text.toString())?.apply()
                        SendData.userName = binding.etName.text.toString()
                        SendData.userLogin = binding.etLogin.text.toString()
                        SendData.userPassword = binding.etPassword.text.toString()
                        isDataSaved = false
                    }
                }
                if (isPhotoUpdated) {
                    deleteIfExist(fileName)
                    UploadFilesTask().execute(fileName)
                    isPhotoUpdated = false
                }
                binding.imageView5.setOnClickListener {
                    Toast.makeText(context, "Фото", Toast.LENGTH_SHORT).show()
                }
            }


            btChangeUser.setOnClickListener {
                pref = activity?.getSharedPreferences("USER", Context.MODE_PRIVATE)
                Log.e("Sas", "PREF.account = " + pref?.getString("id", ""))
                pref?.edit()?.remove("id")?.apply()
                pref?.edit()?.remove("name")?.apply()
                pref?.edit()?.remove("login")?.apply()
                pref?.edit()?.remove("password")?.apply()
                pref?.edit()?.remove("role")?.apply()
                Log.e("Sas", "PREF.account2 = " + pref?.getString("id", ""))
                val intent = Intent(Intent.ACTION_MAIN)
                intent.setClassName(
                    "com.example.testandro6",
                    "com.example.testandro6.ui.login.LoginActivity"
                )
                startActivity(intent)
                activity?.finish()
            }
        } else {
            connection.visibility = View.VISIBLE
        }

        return root
    }

    private fun loadImage(id: String){
        var url = "http://${SendData.IPSERVER}:${SendData.PORTDB}/images/db/person$id.jpg"
        val queue = Volley.newRequestQueue(context)
        var request = ImageRequest(url, {
            photoView = it
            binding.imageView5?.setImageBitmap(photoView)
        }, 0, 0, null,
            {
                it.printStackTrace()
            })
        queue.add(request)
    }

    private fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap {
        var width = image.width
        var height = image.height

        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    private fun deleteIfExist(fileName: String) {
        val queue = Volley.newRequestQueue(context)
        var url = "http://${SendData.IPSERVER}:${SendData.PORTDB}/images/db/delete?fileName=$fileName"

        var postparams = JSONObject()

        Log.e("Sas", postparams.toString())

        val request: JsonObjectRequest =
            object : JsonObjectRequest(Method.DELETE, url, postparams, Response.Listener { response ->
                Toast.makeText(context, "Данные сохранены", Toast.LENGTH_SHORT).show()

                try {
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error -> // displaying toast message on response failure.
                Log.e("tag", "error is " + error!!.message)
            }) {
            }
        queue.add(request)
    }

    private class UploadFilesTask : AsyncTask<String?, Int?, Int>() {
        override fun doInBackground(vararg urls: String?): Int {
            return uploadFile(urls[0])
        }
    }

    companion object {
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

                Log.e("Upload file to server Exception", "Exception : " + e, e);
            }
            return serverResponseCode
        }
    }

    fun apiExecute(): StringBuilder {
        val policy = StrictMode.ThreadPolicy.Builder()
            .permitAll().build()
        StrictMode.setThreadPolicy(policy)
        // val myUrl = "http://90.156.226.25:8083/api/v1/student/55"
        //val myUrl = "http://localhost:8083/events/31"
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

    private fun updateData(
        id: Int,
        name: String,
        login: String,
        password: String,
        avatar: String
    ) {
        val queue = Volley.newRequestQueue(context)
        var url = "http://${SendData.IPSERVER}:${SendData.PORTDB}/persons/" + id + "?"

        if (name != ""){
            url += "name=$name&"
        }
        if (login != ""){
            url += "login=$login&"
        }
        if (password == ""){
            url += "password=$password&"
        }
        if (avatar == ""){
            url += "avatar=$avatar"
        }
        url.trim('&')

        var postparams = JSONObject()

        Log.e("Sas", url)
        if (ConnectChecker.isOnline(requireContext()) && !SendData.isBadConnection) {
        val request: JsonObjectRequest =
            object : JsonObjectRequest(Method.PUT, url, postparams, Response.Listener { response ->
                Toast.makeText(context, "Данные сохранены!", Toast.LENGTH_SHORT).show()
                isDataSaved = true
                try {
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error -> // displaying toast message on response failure.
                Log.e("tag", "error is " + error!!.message)
                Toast.makeText(context, "Данные не сохранены", Toast.LENGTH_SHORT)
                    .show()
            }) {
            }
        queue.add(request)
        } else {
            activity?.runOnUiThread {
                Toast.makeText(context, "Данные не сохранены", Toast.LENGTH_SHORT)
                    .show()
            }
            SendData.isBadConnection = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}