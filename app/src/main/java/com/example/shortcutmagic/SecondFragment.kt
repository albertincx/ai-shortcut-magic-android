package com.example.shortcutmagic

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.shortcutmagic.databinding.FragmentSecondBinding
import java.util.*

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    private var selectedFileUri: Uri? = null
    private var selectedApp: AppInfo? = null
    private var selectedIcon: Bitmap? = null

    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedFileUri = it
            binding.textSelectedItem.text = "Selected File: ${it.path}"
            if (binding.editShortcutName.text.isNullOrEmpty()) {
                binding.editShortcutName.setText(it.lastPathSegment ?: "File Shortcut")
            }
        }
    }

    private val pickIconLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, it)
            selectedIcon = bitmap
            binding.imageIconPreview.setImageBitmap(bitmap)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.radioGroupType.setOnCheckedChangeListener { _, checkedId ->
            binding.layoutUrl.visibility = if (checkedId == R.id.radio_url) View.VISIBLE else View.GONE
            binding.btnPickFile.visibility = if (checkedId == R.id.radio_file) View.VISIBLE else View.GONE
            binding.btnPickApp.visibility = if (checkedId == R.id.radio_app) View.VISIBLE else View.GONE
        }

        binding.btnPickFile.setOnClickListener { pickFileLauncher.launch("*/*") }
        binding.btnPickApp.setOnClickListener { showAppPickerDialog() }
        binding.btnPickIcon.setOnClickListener { pickIconLauncher.launch("image/*") }

        binding.btnCreateShortcut.setOnClickListener { createShortcut() }
    }

    private fun showAppPickerDialog() {
        val pm = requireContext().packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        
        val resolvedInfos = pm.queryIntentActivities(mainIntent, 0)
        val apps = resolvedInfos.map {
            AppInfo(
                it.loadLabel(pm).toString(),
                it.activityInfo.packageName,
                it.loadIcon(pm),
                pm.getLaunchIntentForPackage(it.activityInfo.packageName)
            )
        }.sortedBy { it.name }

        val adapter = object : ArrayAdapter<AppInfo>(requireContext(), android.R.layout.select_dialog_item, apps) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val app = getItem(position)
                (view as android.widget.TextView).apply {
                    text = app?.name
                    setCompoundDrawablesWithIntrinsicBounds(app?.icon?.apply {
                        setBounds(0, 0, 64, 64)
                    }, null, null, null)
                    compoundDrawablePadding = 16
                }
                return view
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Select App")
            .setAdapter(adapter) { _, which ->
                val app = apps[which]
                selectedApp = app
                binding.textSelectedItem.text = "Selected App: ${app.name}"
                if (binding.editShortcutName.text.isNullOrEmpty()) {
                    binding.editShortcutName.setText(app.name)
                }
                selectedIcon = drawableToBitmap(app.icon)
                binding.imageIconPreview.setImageDrawable(app.icon)
            }
            .show()
    }

    private fun createShortcut() {
        val name = binding.editShortcutName.text.toString()
        if (name.isEmpty()) {
            binding.editShortcutName.error = getString(R.string.error_empty_name)
            return
        }

        val intent = when (binding.radioGroupType.checkedRadioButtonId) {
            R.id.radio_url -> {
                val url = binding.editUrl.text.toString()
                if (url.isEmpty()) {
                    binding.editUrl.error = getString(R.string.error_invalid_url)
                    return
                }
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
            }
            R.id.radio_file -> {
                if (selectedFileUri == null) {
                    Toast.makeText(requireContext(), "Please pick a file", Toast.LENGTH_SHORT).show()
                    return
                }
                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(selectedFileUri, requireContext().contentResolver.getType(selectedFileUri!!))
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }
            R.id.radio_app -> {
                if (selectedApp == null) {
                    Toast.makeText(requireContext(), "Please pick an app", Toast.LENGTH_SHORT).show()
                    return
                }
                selectedApp?.launchIntent ?: return
            }
            else -> return
        }

        val icon = if (selectedIcon != null) {
            IconCompat.createWithBitmap(selectedIcon!!)
        } else {
            IconCompat.createWithResource(requireContext(), R.mipmap.ic_launcher)
        }

        val shortcut = ShortcutInfoCompat.Builder(requireContext(), UUID.randomUUID().toString())
            .setShortLabel(name)
            .setIcon(icon)
            .setIntent(intent)
            .build()

        if (ShortcutManagerCompat.isRequestPinShortcutSupported(requireContext())) {
            ShortcutManagerCompat.requestPinShortcut(requireContext(), shortcut, null)
            Toast.makeText(requireContext(), R.string.msg_shortcut_created, Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        } else {
            Toast.makeText(requireContext(), "Pinned shortcuts are not supported on this device", Toast.LENGTH_SHORT).show()
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) return drawable.bitmap
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}