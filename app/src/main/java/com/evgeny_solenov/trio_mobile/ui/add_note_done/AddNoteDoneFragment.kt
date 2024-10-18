package com.example.testandro6.ui.add_note_done

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.Navigation
import com.example.testandro6.R
import com.example.testandro6.SendData
import com.example.testandro6.databinding.FragmentAddNoteDoneBinding
import com.google.android.material.navigation.NavigationBarView


class AddNoteDoneFragment : Fragment() {
    private var navBar: NavigationBarView? = null
    companion object{
        var onPauseHappen = false
        var isClosedFragment = false
        var isButtonPressed = false
    }
    private var _binding: FragmentAddNoteDoneBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddNoteDoneBinding.inflate(inflater, container, false)
        val root: View = binding.root
        navBar = requireActivity().findViewById(R.id.bottomNavigationView)
        navBar?.visibility = View.VISIBLE

        if (SendData.data == "note updated"){
            binding.textNumEvent.setText("Запись обновлена!")
        }
        if (SendData.data == "note error"){
            binding.textNumEvent.setText("Ошибка")
        }

        val buttonBack = binding.buttonBack
        buttonBack.setOnClickListener {
            isButtonPressed = true
            view?.let { it1 -> Navigation.findNavController(it1).
            navigate(R.id.action_add_note_done_fragment_to_fragment_add_note) }
        }
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true /* enabled by default */) {
            override fun handleOnBackPressed() {
                isButtonPressed = true
                view?.let { it1 -> Navigation.findNavController(it1).
                navigate(R.id.action_add_note_done_fragment_to_fragment_add_note) }
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        return root
    }

    override fun onPause() {
//        onPauseHappen = true
        Log.e("OnPause", "OnPause = $onPauseHappen")
        super.onPause()
    }

    override fun onResume() {
        Log.e("OnResume", "OnResume = $onPauseHappen")
        if (onPauseHappen){
            onPauseHappen = false
            isClosedFragment = true
            view?.let { it1 -> Navigation.findNavController(it1).
            navigate(R.id.action_add_note_done_fragment_to_fragment_add_note) }
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
        Log.e("OnDestroyView", "OnDestroyView = $onPauseHappen")
        isButtonPressed = false
        val fm: FragmentManager = requireActivity().supportFragmentManager
//        for (i in 0 until fm.getBackStackEntryCount()) {
        if (fm.backStackEntryCount > 0) {
            val entry = fm.getBackStackEntryAt(0)
            fm.popBackStack(entry.id, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
//        }
        super.onDestroyView()
    }
}