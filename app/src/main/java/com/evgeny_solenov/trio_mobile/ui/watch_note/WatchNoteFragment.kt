package com.example.testandro6.ui.watch_note

import com.example.testandro6.R
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.testandro6.SendData
import com.example.testandro6.databinding.FragmentWatchNoteBinding
import com.google.android.material.navigation.NavigationBarView
import org.json.JSONException
import org.json.JSONObject

class WatchNoteFragment : Fragment() {
    private var navBar: NavigationBarView? = null
    private var photoView: Bitmap? = null
    private var mImageView: ImageView? = null
    private var _binding: FragmentWatchNoteBinding? = null
    private val binding get() = _binding!!
    var inputText: String? = ""

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWatchNoteBinding.inflate(inflater, container, false)
        val root: View = binding.root
        inputText = SendData.data
        SendData.data = ""
        var JSONmy = JSONObject(inputText.toString())
        Log.e("InputText", inputText.toString())
        var eventId = JSONmy.getInt("eventId")
        binding.textNumEvent.setText("Запись №$eventId")
        var statusEvent = 0
        try {
            statusEvent = JSONmy.getJSONObject("eventlId").getInt("colorCode")
            binding.imageView13.visibility = View.VISIBLE
        } catch (e: JSONException) {

        }
        when (statusEvent) {
            1 -> binding.imageView13.setImageResource(R.drawable.status_green)
            2 -> binding.imageView13.setImageResource(R.drawable.status_yellow)
            3 -> binding.imageView13.setImageResource(R.drawable.status_orange)
            4 -> binding.imageView13.setImageResource(R.drawable.status_red)
        }

        try {
            binding.tvDepartName.setText(
                JSONmy.getJSONObject("departmentId").getJSONArray("departmentHistoryId")
                    .getJSONObject(0).getString("departmentName")
            )
            Log.e(
                "Depart", JSONmy.getJSONObject("departmentId").getJSONArray("departmentHistoryId")
                    .getJSONObject(0).getString("departmentName")
            )
        } catch (e: JSONException) {
        }
        try {
            binding.tvEquipName.setText(
                JSONmy.getJSONObject("equipmentoId").getJSONObject("equipmentmId")
                    .getString("modelName")
            )
        } catch (e: JSONException) {

        }
        try {
            binding.tvEventTypeName.setText(
                JSONmy.getJSONObject("eventtId")
                    .getString("eventTypeName")
            )
            Log.e(
                "EventType", JSONmy.getJSONObject("eventtId")
                    .getString("eventTypeName")
            )
        } catch (e: JSONException) {
        }
        try {
            binding.tvWasteGroupName.setText(
                JSONmy.getJSONObject("wastegId")
                    .getString("wastegName")
            )
            Log.e(
                "WasteGroup", JSONmy.getJSONObject("wastegId")
                    .getString("wastegName")
            )
        } catch (e: JSONException) {

        }
        try {
            binding.tvWasteTypeName.setText(
                JSONmy.getJSONObject("wastetId")
                    .getString("wastetName")
            )
            Log.e(
                "WasteType", JSONmy.getJSONObject("wastetId")
                    .getString("wastetName")
            )
        } catch (e: JSONException) {

        }
        try {
            binding.tvEventNoteName.setText(JSONmy.getString("note"))
        } catch (e: JSONException) {
        }
        try {
            var timeTarget = JSONmy.getString("dateTime").split('T').get(1).split('.').get(0)
            var dateTarget = JSONmy.getString("dateTime").split('T').get(0).split('-').get(2) + "." +
                    JSONmy.getString("dateTime").split('T').get(0).split('-').get(1)
            binding.tvDateTimeName.setText("$timeTarget $dateTarget")
        } catch (e: JSONException) {
        }

        mImageView = binding.ivPhoto
        binding.buttonOpenPhoto.visibility = View.GONE
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true /* enabled by default */) {
            override fun handleOnBackPressed() {
                activity?.supportFragmentManager?.popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        val buttonBack = binding.button
        buttonBack.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }
        binding.buttonEditNote.setOnClickListener {
            SendData.data = inputText.toString()
            view?.let { it1 -> Navigation.findNavController(it1).
            navigate(R.id.action_fragment_watch_note_to_edit_note_fragment) }
        }

        if (JSONmy.getString("imageName") != ""){
            loadImage(JSONmy.getString("imageName"))
            binding.buttonOpenPhoto.visibility = View.VISIBLE
            binding.buttonOpenPhoto.setOnClickListener {
                val intent = Intent(Intent.ACTION_MAIN)
                intent.putExtra("picture", JSONmy.getString("imageName"))
                intent.setClassName(
                    "com.example.testandro6",
                    "com.example.testandro6.PhotoViewActivity"
                )
                startActivity(intent)
            }
        }

        binding.buttonDeleteEvent.setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext())
            dialog.setTitle("Удаление записи")?.setMessage("Хотите удалить?")
                ?.setPositiveButton("ДА") { dialogIn, whichButton ->
                    deleteData(eventId)
                    activity?.supportFragmentManager?.popBackStack()
                }
                ?.setNegativeButton("НЕТ") { dialogIn, whichButton ->
                }
            dialog.show()
        }
        return root
    }

    private fun loadImage(eventImage: String){
        var url = "http://${SendData.IPSERVER}:${SendData.PORTDB}/images/db/$eventImage"
        val queue = Volley.newRequestQueue(context)
        var request = ImageRequest(url, {
            photoView = it
            mImageView?.setImageBitmap(photoView)
        }, 0, 0, null,
            {
                it.printStackTrace()
            })
        queue.add(request)
    }

    private fun deleteData(
        id: Int
    ) {
        val queue = Volley.newRequestQueue(context)
        var url = "http://${SendData.IPSERVER}:${SendData.PORTDB}/events/" + id
        var postparams = JSONObject()

        Log.e("Sas", postparams.toString())

        val request: JsonObjectRequest =
            object : JsonObjectRequest(Method.DELETE, url, postparams, Response.Listener {
                Toast.makeText(context, "Запись удалена", Toast.LENGTH_SHORT).show()
                activity?.supportFragmentManager?.popBackStack()
                try {

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error -> // displaying toast message on response failure.
                Log.e("tag", "error is " + error!!.message)
                Toast.makeText(context, "Запись удалена", Toast.LENGTH_SHORT)
                    .show()
                activity?.supportFragmentManager?.popBackStack()
            }) {
            }
        queue.add(request)
    }
}