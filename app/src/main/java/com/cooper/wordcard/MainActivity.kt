package com.cooper.wordcard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_main.*
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import android.content.Intent
import android.util.JsonReader
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp.newCompatibleTransport
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.drive.DriveScopes;
import com.google.gson.Gson

class MainActivity : AppCompatActivity(),
    GoogleApiClient.OnConnectionFailedListener,
    GoogleApiClient.ConnectionCallbacks {

    lateinit var googleApiClient: GoogleApiClient
    var userAccount: GoogleSignInAccount? = null

    override fun onConnectionSuspended(p0: Int) {
        Log.e("cooper", "get userAccount onConnectionSuspended")
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onConnected(p0: Bundle?) {
        Log.e("cooper", "get userAccount onConnected")
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.e("cooper", "get userAccount onConnectionFailed")
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            if (userAccount != null) {
                Log.e("cooper", "get userAccount " + userAccount!!.displayName)
                //Log.e("cooper")
            } else {
                val googleSignInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
                startActivityForResult(googleSignInIntent, 6334)
            }
        }

        val googleSignInOptions = GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        ).requestEmail()
            .requestProfile()
            .requestScopes(Scope(DriveScopes.DRIVE))
            .requestScopes(Scope(SheetsScopes.SPREADSHEETS))
            .requestScopes(Scope(SheetsScopes.SPREADSHEETS_READONLY))
            //.requestScopes(Drive.SCOPE_FILE)
            .build()

        googleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this, this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
            .build()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        Log.e("cooper", "get userAccount onActivityResult")
        if (requestCode == 6334) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) {
                if (resultCode == RESULT_OK) {
                    GoogleSignIn.getSignedInAccountFromIntent(data)
                        .addOnSuccessListener { account ->
                            userAccount = account
                            readEmployeeList()
                        }
                }
            }
        }
    }

    private fun readEmployeeList() {
        // サービスのスコープとしてSpreadSheetsのReadOnlyを設定
        val scopes = listOf(SheetsScopes.SPREADSHEETS_READONLY)
        val credential = GoogleAccountCredential.usingOAuth2(applicationContext, scopes)
        credential.selectedAccount = userAccount!!.account
        if (credential.selectedAccount == null) {
            Log.e("cooper", "account null")
        }
        val jsonFactory = JacksonFactory.getDefaultInstance()
        val httpTransport = newCompatibleTransport()

        // drive api
        val googleDriveService = Drive.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName(getString(R.string.app_name))
            .build()

        val threadDrive = Thread(Runnable {

            var pt: String? = null
            var page = 1;
            do {
                val result = googleDriveService.files().list().apply {
                    q = "mimeType='application/vnd.google-apps.spreadsheet'"
                    spaces = "drive"
                    fields = "nextPageToken, files(id, name)"
                    pageToken = pt
                }.execute()
                for (file in result.files) {
                    Log.e("cooper", "" + page + ":name=${file.name}, id=${file.id}")
                }
                pt = result.nextPageToken
                page++
            } while (pt != null)
            Log.e("cooper", "finish")

        })
        //threadDrive.start()


        // sheet api
        val service = Sheets.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName(getString(R.string.app_name))
            .build()
        val threadSheet = Thread(Runnable {
            val result = service.Spreadsheets().Values().get(sheetID, sheetRange).execute()
            Log.e("cooper", "json " + result)
            val gson = Gson()
            var jsonResult = gson.fromJson(result.toString(), SheetJson::class.java)
            for (i in 0..jsonResult.values.size - 1) {
                var row = jsonResult.values[i]
                var s = ""
                for (j in 0..row.size - 1) {
                    s += row[j] + ","
                }
                Log.e("cooper", "${i}:${s}")
            }
        })
        threadSheet.start()

    }

    companion object {
        val sheetID = "1BZGHuQRvC-caZr3-3tcsa0Q04W9vE6p4y_-5q1oy798"
        val sheetRange = "工作表1!A1:B500"
    }

}
