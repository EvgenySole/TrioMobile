package com.example.testandro6.ui.add_task_done

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.Navigation
import com.example.testandro6.R
import com.example.testandro6.SendData
import com.example.testandro6.databinding.FragmentAddTaskDoneBinding
import com.google.android.material.navigation.NavigationBarView

class AddTaskDoneFragment : Fragment() {
    companion object{
        var onPauseHappen = false
        var isClosedFragment = false
        var isButtonPressed = false
    }
    private var navBar: NavigationBarView? = null
    private var _binding: FragmentAddTaskDoneBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddTaskDoneBinding.inflate(inflater, container, false)
        val root: View = binding.root
        navBar = requireActivity().findViewById(R.id.bottomNavigationView)
        navBar?.visibility = View.VISIBLE

        if (SendData.data == "task updated"){
            binding.textNumEvent.setText("Задача обновлена!")
        }
        if (SendData.data == "task error"){
            binding.textNumEvent.setText("Ошибка")
        }
        val buttonBack = binding.buttonBack
        buttonBack.setOnClickListener {
            isButtonPressed = true
            view?.let { it1 -> Navigation.findNavController(it1).
            navigate(R.id.action_fragment_add_task_done_to_navigation_tasks) }
        }
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true /* enabled by default */) {
            override fun handleOnBackPressed() {
                isButtonPressed = true
                view?.let { it1 -> Navigation.findNavController(it1).
                navigate(R.id.action_fragment_add_task_done_to_navigation_tasks) }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        return root
    }

    override fun onPause() {
//        onPauseHappen = true
        Log.e("OnPause", "OnPause = ${onPauseHappen}")
        super.onPause()
    }

    override fun onResume() {
        Log.e("OnResume", "OnResume = ${onPauseHappen}")
        if (onPauseHappen){
            onPauseHappen = false
            isClosedFragment = true
            view?.let { it1 -> Navigation.findNavController(it1).
            navigate(R.id.action_fragment_add_task_done_to_navigation_tasks) }
        }
        super.onResume()
    }

    override fun onDestroyView() {
        if (!isButtonPressed){
            onPauseHappen = true
        }
        if (isClosedFragment){
            onPauseHappen = false
            isClosedFragment = false
        }
        Log.e("OnDestroyView", "OnDestroyView = ${onPauseHappen}")
        isButtonPressed = false
        super.onDestroyView()
    }
}