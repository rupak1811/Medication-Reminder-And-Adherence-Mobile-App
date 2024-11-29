package com.futsch1.medtimer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.view.MenuProvider;
import androidx.navigation.Navigation;

import com.futsch1.medtimer.exporters.CSVExport;
import com.futsch1.medtimer.exporters.Exporter;
import com.futsch1.medtimer.exporters.PDFExporter;
import com.futsch1.medtimer.helpers.FileHelper;
import com.futsch1.medtimer.helpers.PathHelper;
import com.futsch1.medtimer.reminders.ReminderProcessor;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class OptionsMenu implements MenuProvider {
    private final Context context;
    private final MedicineViewModel medicineViewModel;
    private final View view;
    private final HandlerThread backgroundThread;
    private final ActivityResultLauncher<Intent> openFileLauncher;
    private Menu menu;
    private BackupManager backupManager;

    public OptionsMenu(Context context, MedicineViewModel medicineViewModel, ActivityResultCaller caller, View view) {
        this.context = context;
        this.medicineViewModel = medicineViewModel;
        this.view = view;
        this.openFileLauncher = caller.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                this.fileSelected(result.getData().getData());
            }
        });
        backgroundThread = new HandlerThread("Export");
        backgroundThread.start();
    }

    public void fileSelected(Uri data) {
        backupManager.fileSelected(data);
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.main, menu);
        menu.setGroupDividerEnabled(true);
        enableOptionalIcons(menu);

        this.backupManager = new BackupManager(context, menu, medicineViewModel, openFileLauncher);
        this.menu = menu;
        setupSettings();
        setupVersion();

        setupClearEvents();
        setupExport();
    }

    @SuppressLint("RestrictedApi")
    private static void enableOptionalIcons(@NonNull Menu menu) {
        if (menu instanceof MenuBuilder) {
            try {
                Method m = menu.getClass().getDeclaredMethod(
                        "setOptionalIconsVisible", Boolean.TYPE);
                m.invoke(menu, true);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                Log.e("Menu", "onMenuOpened", e);
            }
        }
    }

    private void setupSettings() {
        MenuItem item = menu.findItem(R.id.settings);
        item.setOnMenuItemClickListener(menuItem -> {
            Navigation.findNavController(view).navigate(R.id.action_global_preferencesFragment);
            return true;
        });
    }

    private void setupVersion() {
        MenuItem item = menu.findItem(R.id.version);
        item.setTitle(context.getString(R.string.version, BuildConfig.VERSION_NAME));
    }

    private void setupClearEvents() {
        MenuItem item = menu.findItem(R.id.clear_events);
        item.setOnMenuItemClickListener(menuItem -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.confirm);
            builder.setMessage(R.string.are_you_sure_delete_events);
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.yes, (dialogInterface, i) -> medicineViewModel.deleteReminderEvents());
            builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> { // Intentionally left empty
            });
            builder.show();
            ReminderProcessor.requestReschedule(context);
            return true;
        });
    }

    private void setupExport() {
        Handler handler = new Handler(backgroundThread.getLooper());

        MenuItem item = menu.findItem(R.id.export_csv);
        item.setOnMenuItemClickListener(menuItem -> {
            handler.post(() -> export(new CSVExport(medicineViewModel.medicineRepository.getAllReminderEventsWithoutDeleted(), context)));
            return true;
        });
        item = menu.findItem(R.id.export_pdf);
        item.setOnMenuItemClickListener(menuItem -> {
            handler.post(() -> export(new PDFExporter(medicineViewModel.medicineRepository.getAllReminderEventsWithoutDeleted(), context)));
            return true;
        });
    }

    private void export(Exporter exporter) {
        // Get the export filename from PathHelper
        String filename = PathHelper.getExportFilename(exporter);

        // Create the file
        File file = new File(context.getCacheDir(), filename);

        try {
            // Perform the export
            exporter.export(file);

            // Share the file
            FileHelper.shareFile(context, file);
        } catch (Exporter.ExporterException e) {
            Toast.makeText(context, R.string.export_failed, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }
}
