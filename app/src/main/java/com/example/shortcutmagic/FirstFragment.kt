package com.example.shortcutmagic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shortcutmagic.databinding.FragmentFirstBinding

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ShortcutAdapter
    private lateinit var storage: ShortcutStorage
    private var deleteMenuItem: MenuItem? = null

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
        adapter = ShortcutAdapter(emptyList(), { shortcut ->
            showShortcutDetails(shortcut)
        }, { count ->
            deleteMenuItem?.isVisible = count > 0
        })
        
        binding.recyclerShortcuts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerShortcuts.adapter = adapter

        setupMenu()
        loadShortcuts()
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Menu is inflated by Activity, we just find the item
                deleteMenuItem = menu.findItem(R.id.action_delete_selected)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_delete_selected -> {
                        deleteSelectedShortcuts()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun deleteSelectedShortcuts() {
        val selectedIds = adapter.getSelectedIds()
        if (selectedIds.isNotEmpty()) {
            storage.deleteShortcuts(selectedIds)
            Toast.makeText(
                requireContext(),
                getString(R.string.msg_deleted_count, selectedIds.size),
                Toast.LENGTH_SHORT
            ).show()
            loadShortcuts()
            deleteMenuItem?.isVisible = false
        }
    }

    private fun showShortcutDetails(shortcut: ShortcutEntry) {
        AlertDialog.Builder(requireContext())
            .setTitle(shortcut.name)
            .setMessage("Type: ${shortcut.type}\nData: ${shortcut.data}")
            .setPositiveButton("Regenerate") { _, _ ->
                ShortcutHelper.pinShortcut(
                    requireContext(),
                    shortcut.name,
                    shortcut.type,
                    shortcut.data
                )
            }
            .setNegativeButton("Close", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadShortcuts()
    }

    fun loadShortcuts() {
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