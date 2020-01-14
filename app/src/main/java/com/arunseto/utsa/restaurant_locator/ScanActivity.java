package com.arunseto.utsa.restaurant_locator;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class ScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    private FloatingActionButton btnFlash;
    private LinearLayout llScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        mScannerView = new ZXingScannerView(this);

        btnFlash = findViewById(R.id.btnFlash);
        llScannerView = findViewById(R.id.llScannerView);

        llScannerView.addView(mScannerView);

        btnFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //toggle flash

                mScannerView.setFlash(!mScannerView.getFlash());
                Toast.makeText(ScanActivity.this, (mScannerView.getFlash() ? "Flash is on" : "Flash is off"), Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        Log.v("TAG", rawResult.getText()); // Prints scan results
        Log.v("TAG", rawResult.getBarcodeFormat().toString());
        dialogResult(rawResult);

        mScannerView.resumeCameraPreview(this);
    }

    public void dialogResult(Result rawResult) {

        BottomSheetDialog bsd = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.template_scan_result, null);
        String strResult = rawResult.getText().trim();
        TextView tvScanResult = view.findViewById(R.id.tvScanResult);
        Button btnShareWeb = view.findViewById(R.id.btnShareWeb);
        Button btnShareWhatsapp = view.findViewById(R.id.btnShareWhatsapp);
        Button btnShareContact = view.findViewById(R.id.btnShareContact);
        Button btnShareEmail = view.findViewById(R.id.btnShareEmail);

        tvScanResult.setText(strResult);
        // menjadikan variable result sebagai final untuk diakses di inner class
        final String finalStrResult = strResult;
        btnShareWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionShareWeb(finalStrResult);
            }
        });

        btnShareWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionShareWhatsapp(finalStrResult);
            }
        });

        btnShareContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionShareContact(finalStrResult);
            }
        });

        btnShareEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionShareEmail(finalStrResult);
            }
        });

        bsd.setContentView(view);
        bsd.show();
    }

    public void actionShareWeb(String url) {
        // cek apakah result link?
        if (url.contains("http") | url.contains("https") | url.contains("www") | url.contains(".co")) {

        } else {
            url = "https://www.google.com/search?q=" + url;
        }
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    public void actionShareWhatsapp(String result) {
        PackageManager pm = getPackageManager();
        try {

            Intent waIntent = new Intent(Intent.ACTION_SEND);
            waIntent.setType("text/plain");
            String text = result;

            PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            //Check if package exists or not. If not then code
            //in catch block will be called
            waIntent.setPackage("com.whatsapp");

            waIntent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(waIntent, "Share with"));

        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, "WhatsApp not Installed", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public void actionShareContact(String result) {
        //matches numbers only
        String regexStr = "^[0-9]*$";
        if (result.matches(regexStr)) {
            // Creates a new Intent to insert a contact
            Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
            // Sets the MIME type to match the Contacts Provider
            intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, result);

            startActivity(intent);
        }

    }

    public void actionShareEmail(String result) {
        //matches numbers only
        if (result.contains("http://")) {
            result = result.replace("http://", "");
        } else if (result.contains("https://")) {
            result = result.replace("https://", "");
        }
        if (result.contains("@") && result.contains(".")) {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", result, null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Body");
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
        }
    }

}

