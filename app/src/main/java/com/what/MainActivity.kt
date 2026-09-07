package com.what;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainActivity extends Activity {

    private static final int PICK_ZIP_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnRestore = findViewById(R.id.btn_restore);
        btnRestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // فتح مستعرض الملفات لاختيار ملف ZIP
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/zip");
                startActivityForResult(intent, PICK_ZIP_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_ZIP_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri zipUri = data.getData();
            if (zipUri != null) {
                processZipFile(zipUri);
            }
        }
    }

    private void processZipFile(Uri zipUri) {
        // المسار الأساسي لبيانات التطبيق: /data/data/com.what/
        String appDataPath = getApplicationInfo().dataDir;
        File dataDir = new File(appDataPath);

        try {
            // 1. تنظيف مسار الحزمة
            cleanDirectory(dataDir);

            // 2. استخراج الملف المضغوط إلى المسار
            unzip(zipUri, dataDir);

            // 3. الخروج وإيقاف التطبيق إجبارياً
            forceKillApp();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "حدث خطأ: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // دالة لتنظيف المجلد بالكامل مع استثناء مجلد lib الخاص بالنظام
    private void cleanDirectory(File dir) {
        if (dir != null && dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (child.getName().equals("lib")) {
                        continue; // تخطي مجلد المكتبات لتجنب انهيار التطبيق
                    }
                    if (child.isDirectory()) {
                        cleanDirectory(child);
                    }
                    child.delete();
                }
            }
        }
    }

    // دالة لفك الضغط عن الملف باستخدام Uri
    private void unzip(Uri zipUri, File targetDirectory) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(zipUri);
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(inputStream));
        ZipEntry ze;

        byte[] buffer = new byte[8192];
        int count;

        while ((ze = zis.getNextEntry()) != null) {
            File file = new File(targetDirectory, ze.getName());
            File dir = ze.isDirectory() ? file : file.getParentFile();

            if (!dir.isDirectory() && !dir.mkdirs()) {
                throw new Exception("فشل في إنشاء المجلد: " + dir.getAbsolutePath());
            }

            if (ze.isDirectory()) {
                continue;
            }

            FileOutputStream fout = new FileOutputStream(file);
            while ((count = zis.read(buffer)) != -1) {
                fout.write(buffer, 0, count);
            }
            fout.close();
            zis.closeEntry();
        }
        zis.close();
    }

    // دالة للإغلاق الإجباري من الخلفية
    private void forceKillApp() {
        // إغلاق جميع الواجهات المرتبطة بالتطبيق
        finishAffinity();
        // قتل العملية من الجذور
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}
