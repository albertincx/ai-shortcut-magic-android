package com.example.shortcutmagic

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
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

    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try {
                requireContext().contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Fallback for URIs that don't support persistable permissions
            }
            selectedFileUri = it
            binding.textSelectedItem.text = getString(R.string.shortcut_type_file) + ": " + it.path
            if (binding.editShortcutName.text.isNullOrEmpty()) {
                binding.editShortcutName.setText(it.lastPathSegment ?: "File Shortcut")
            }
        }
    }

    private val pickIconLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val bitmap = loadBitmap(it)
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

        binding.btnPickFile.setOnClickListener { pickFileLauncher.launch(arrayOf("*/*")) }
        binding.btnPickApp.setOnClickListener { showAppPickerDialog() }
        binding.btnPickIcon.setOnClickListener { pickIconLauncher.launch("image/*") }

        binding.btnCreateShortcut.setOnClickListener { createShortcut() }
    }

    private fun loadBitmap(uri: Uri): Bitmap {
        val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
        return ImageDecoder.decodeBitmap(source)
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
                binding.textSelectedItem.text = getString(R.string.shortcut_type_app) + ": " + app.name
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

        val type = when (binding.radioGroupType.checkedRadioButtonId) {
            R.id.radio_url -> "URL"
            R.id.radio_file -> "FILE"
            R.id.radio_app -> "APP"
            else -> ""
        }

        val data = when (binding.radioGroupType.checkedRadioButtonId) {
            R.id.radio_url -> {
                val url = binding.editUrl.text.toString()
                if (url.isEmpty()) {
                    binding.editUrl.error = getString(R.string.error_invalid_url)
                    return
                }
                url
            }
            R.id.radio_file -> {
                if (selectedFileUri == null) {
                    Toast.makeText(requireContext(), "Please pick a file", Toast.LENGTH_SHORT).show()
                    return
                }
                selectedFileUri.toString()
            }
            R.id.radio_app -> {
                if (selectedApp == null) {
                    Toast.makeText(requireContext(), "Please pick an app", Toast.LENGTH_SHORT).show()
                    return
                }
                selectedApp?.packageName ?: ""
            }
            else -> return
        }

        ShortcutHelper.pinShortcut(requireContext(), name, type, data, selectedIcon)

        // Save to local storage
        val entry = ShortcutEntry(
            id = UUID.randomUUID().toString(),
            name = name,
            type = type,
            data = data
        )
        ShortcutStorage(requireContext()).saveShortcut(entry)

        findNavController().navigateUp()
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) return drawable.bitmap
        val bitmap = Bitmap.createBitmap(
            if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 1,
            if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 1,
            Bitmap.Config.ARGB_8888
        )
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