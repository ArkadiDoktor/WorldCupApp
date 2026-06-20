package com.worldcup.app.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.worldcup.app.R
import com.worldcup.app.databinding.FragmentSettingsBinding
import com.worldcup.app.utils.Constants
import com.worldcup.app.utils.showToast
import com.worldcup.app.workers.SyncWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            saveNotificationsPref(true)
            binding.switchNotifications.isChecked = true
            showToast(getString(R.string.notifications_enabled))
        } else {
            binding.switchNotifications.isChecked = false
            showToast(getString(R.string.notifications_denied))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSavedPreferences()
        setupListeners()
    }

    private fun loadSavedPreferences() {
        val prefs = requireContext().getSharedPreferences(
            Constants.PREF_NAME, android.content.Context.MODE_PRIVATE
        )
        val isDarkMode = prefs.getBoolean(Constants.PREF_DARK_MODE, false)
        val notificationsEnabled = prefs.getBoolean(Constants.PREF_NOTIFICATIONS, false)

        binding.switchDarkMode.isChecked = isDarkMode
        binding.switchNotifications.isChecked = notificationsEnabled
    }

    private fun setupListeners() {
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            saveDarkModePref(isChecked)
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                requestNotificationPermission()
            } else {
                saveNotificationsPref(false)
                cancelSyncWork()
                showToast(getString(R.string.notifications_disabled))
            }
        }

        binding.btnSyncNow.setOnClickListener {
            scheduleSyncWork(immediate = true)
            showToast(getString(R.string.sync_started))
        }

        binding.tvVersion.text = getString(R.string.version_format, getAppVersion())
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    saveNotificationsPref(true)
                    scheduleSyncWork(immediate = false)
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.permission_required))
                        .setMessage(getString(R.string.notification_permission_rationale))
                        .setPositiveButton(getString(R.string.grant)) { _, _ ->
                            notificationPermissionLauncher.launch(
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        }
                        .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                            binding.switchNotifications.isChecked = false
                        }
                        .show()
                }
                else -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            saveNotificationsPref(true)
            scheduleSyncWork(immediate = false)
        }
    }

    private fun scheduleSyncWork(immediate: Boolean) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val interval = if (immediate) 15L else Constants.SYNC_INTERVAL_HOURS * 60L

        val workRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            interval, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag(Constants.WORK_TAG_SYNC)
            .build()

        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
            Constants.WORK_TAG_SYNC,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    private fun cancelSyncWork() {
        WorkManager.getInstance(requireContext())
            .cancelAllWorkByTag(Constants.WORK_TAG_SYNC)
    }

    private fun saveDarkModePref(enabled: Boolean) {
        requireContext().getSharedPreferences(Constants.PREF_NAME, android.content.Context.MODE_PRIVATE)
            .edit()
            .putBoolean(Constants.PREF_DARK_MODE, enabled)
            .apply()
    }

    private fun saveNotificationsPref(enabled: Boolean) {
        requireContext().getSharedPreferences(Constants.PREF_NAME, android.content.Context.MODE_PRIVATE)
            .edit()
            .putBoolean(Constants.PREF_NOTIFICATIONS, enabled)
            .apply()
    }

    private fun getAppVersion(): String {
        return try {
            val packageInfo = requireContext().packageManager
                .getPackageInfo(requireContext().packageName, 0)
            packageInfo.versionName ?: "1.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "1.0"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
