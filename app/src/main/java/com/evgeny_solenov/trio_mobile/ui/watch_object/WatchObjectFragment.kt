package com.example.testandro6.ui.watch_object

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
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.testandro6.R
import com.example.testandro6.SendData
import com.example.testandro6.databinding.FragmentWatchObjectBinding
import com.google.android.material.navigation.NavigationBarView
import org.json.JSONException
import org.json.JSONObject


class WatchObjectFragment : Fragment() {
    private var navBar: NavigationBarView? = null
    private var photoView: Bitmap? = null
    private var mImageView: ImageView? = null
    private var _binding: FragmentWatchObjectBinding? = null
    private val binding get() = _binding!!
    var inputText: String? = ""

    @SuppressLint("ResourceType", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWatchObjectBinding.inflate(inflater, container, false)
        val root: View = binding.root
        mImageView = binding.ivPhoto
        binding.buttonWatchPhoto.visibility = View.GONE
        inputText = SendData.data
        SendData.data = ""
        var JSONmy = JSONObject()
        try {
            JSONmy = JSONObject(inputText.toString())
            var equipmentoId = JSONmy.getInt("equipmentoId")
            binding.tvEquipName.setText(JSONmy.getJSONObject("equipmentmId").getString("modelName"))
            binding.tvEventTypeName.setText(JSONmy.getJSONObject("equipmentmId").getInt("equipmentmId").toString())
            binding.tvInvNumName.setText(JSONmy.getString("inventoryNum"))
            if (JSONmy.getString("note") != "null") {
                binding.tvEventNoteName.setText(JSONmy.getString("note"))
            } else {
                binding.tvEventNoteName.setText("Без описания")
            }
            binding.tvDepartName.setText(
                JSONmy.getJSONArray("equipmenthId").getJSONObject(0)
                    .getJSONObject("departmentId").getJSONArray("departmentHistoryId").getJSONObject(0)
                    .getString("departmentName")
            )
            binding.textNumEquip.setText("Объект №$equipmentoId")
            Log.e("ImageSas", JSONmy.getJSONObject("equipmentmId").getString("imageName"))
        } catch (e: JSONException) {
        }

        binding.buttonEditObject.setOnClickListener {
            SendData.data = inputText.toString()
            view?.let { it1 ->
                Navigation.findNavController(it1).navigate(R.id.action_fragment_watch_object_to_edit_object_fragment)
            }
        }
        binding.buttonWorkGraph.setOnClickListener {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.setClassName(
                "com.example.testandro6",
                "com.evgeny_solenov.trio_mobile.ChartActivity"
            )
            startActivity(intent)
        }
        val buttonBack = binding.button
        buttonBack.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }

        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true /* enabled by default */) {
            override fun handleOnBackPressed() {
                activity?.supportFragmentManager?.popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        if (JSONmy.getJSONObject("equipmentmId").getString("imageName") != "") {

            loadImage(JSONmy.getJSONObject("equipmentmId").getString("imageName"))

            binding.buttonWatchPhoto.visibility = View.VISIBLE
            binding.buttonWatchPhoto.setOnClickListener {
                val intent = Intent(Intent.ACTION_MAIN)
                intent.putExtra("picture", JSONmy.getJSONObject("equipmentmId").getString("imageName"))
                intent.setClassName(
                    "com.example.testandro6",
                    "com.example.testandro6.PhotoViewActivity"
                )
                startActivity(intent)
            }
        }
        return root
    }

    private fun loadImage(objectImage: String) {
        var url = "http://${SendData.IPSERVER}:${SendData.PORTDB}/images/db/$objectImage"
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
}