package com.example.shortcutmagic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shortcutmagic.databinding.FragmentFirstBinding

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ShortcutAdapter
    private lateinit var storage: ShortcutStorage

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        storage = ShortcutStorage(requireContext())
        adapter = ShortcutAdapter(emptyList())
        
        binding.recyclerShortcuts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerShortcuts.adapter = adapter
        
        loadShortcuts()
    }

    override fun onResume() {
        super.onResume()
        loadShortcuts()
    }

    private fun loadShortcuts() {
        val shortcuts = storage.getAllShortcuts()
        if (shortcuts.isEmpty()) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.recyclerShortcuts.visibility = View.GONE
        } else {
            binding.layoutEmpty.visibility = View.GONE
            binding.recyclerShortcuts.visibility = View.VISIBLE
            adapter.updateItems(shortcuts)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}