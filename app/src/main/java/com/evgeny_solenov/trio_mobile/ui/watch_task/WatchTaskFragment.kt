package com.example.testandro6.ui.watch_task

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.Navigation
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.testandro6.R
import com.example.testandro6.SendData
import com.example.testandro6.databinding.FragmentWatchTaskBinding
import org.json.JSONException
import org.json.JSONObject
import java.sql.Timestamp

class WatchTaskFragment : Fragment() {

    private var _binding: FragmentWatchTaskBinding? = null
    private val binding get() = _binding!!
    var inputText: String? = ""

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWatchTaskBinding.inflate(inflater, container, false)
        val root: View = binding.root
        inputText = SendData.data
        SendData.data = ""
        var JSONmy = JSONObject(inputText.toString())
        var taskId = JSONmy.getInt("taskId")
        binding.textNumEvent.setText("Задача №$taskId")
        if (!inputText.toString().contains("\"equipmentoId\":null")){
            binding.tvMObjectName.setText(JSONmy.getJSONObject("equipmentoId").getJSONObject("equipmentmId")
                .getString("modelName") + " Инв.№" + JSONmy.getJSONObject("equipmentoId")
                .getInt("inventoryNum"))
        }
        binding.tvHeadPersonName.setText(JSONmy.getJSONObject("headPersonId").
        getJSONArray("personRealId").getJSONObject(0).getString("name"))
        binding.tvTaskNoteName.setText(JSONmy.getString("orderText"))
        var timeTarget = JSONmy.getString("dateTime").split('T').get(1).split('.').get(0)
        var dateTarget = JSONmy.getString("dateTime").split('T').get(0).split('-').get(2) + "." +
                JSONmy.getString("dateTime").split('T').get(0).split('-').get(1)
        binding.tvDateName.setText("$timeTarget $dateTarget")
        binding.tvTargetWorkerName.setText(JSONmy.getJSONObject("targetWorkerId").
        getJSONArray("personRealId").getJSONObject(0).getString("name"))
        if (JSONmy.getString("done") == "false") {
            binding.tvDoneName.setText("Нет")
        } else if (JSONmy.getString("done") == "true"){
            binding.tvDoneName.setText("Да")
            binding.buttonTaskDone.visibility = View.GONE
        }
        if (SendData.userRole == "user"){
            binding.buttonCloseTask.visibility = View.GONE
        }
        binding.buttonTaskDone.setOnClickListener {
            updateData(taskId)
            binding.tvDoneName.setText("Да")
            binding.buttonTaskDone.visibility = View.GONE
        }
        binding.buttonCloseTask.setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext())
            dialog.setTitle("Закрытие задачи")?.setMessage("Хотите удалить?")
                ?.setPositiveButton("ДА") { dialogIn, whichButton ->
                    deleteData(taskId)
                    activity?.supportFragmentManager?.popBackStack()
                    Toast.makeText(context, "Задача закрыта", Toast.LENGTH_SHORT).show()
                }
                ?.setNegativeButton("НЕТ") { dialogIn, whichButton ->
                }
            dialog.show()
        }
        val buttonBack = binding.button
        buttonBack.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }
        binding.buttonEditTask.setOnClickListener {
            SendData.data = inputText.toString()
            view?.let { it1 -> Navigation.findNavController(it1).
            navigate(R.id.action_fragment_watch_task_to_edit_task_fragment) }
        }
        return root
    }

    private fun updateData(
        id: Int
    ) {
        val queue = Volley.newRequestQueue(context)
        var url = "http://${SendData.IPSERVER}:${SendData.PORTDB}/tasks/" + id + "?done=true&dateTime=" +
                Timestamp(System.currentTimeMillis()).toString()
        var postparams = JSONObject()

        Log.e("Sas", postparams.toString())

        val request: JsonObjectRequest =
            object : JsonObjectRequest(Method.PUT, url, postparams, Response.Listener { response ->
                Toast.makeText(context, "Data Updated..", Toast.LENGTH_SHORT).show()
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

    private fun deleteData(
        id: Int
    ) {
        val queue = Volley.newRequestQueue(context)
        var url = "http://${SendData.IPSERVER}:${SendData.PORTDB}/tasks/" + id
        var postparams = JSONObject()

        Log.e("Sas", postparams.toString())

        val request: JsonObjectRequest =
            object : JsonObjectRequest(Method.DELETE, url, postparams, Response.Listener { response ->
                Toast.makeText(context, "Data Deleted..", Toast.LENGTH_SHORT).show()
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
}