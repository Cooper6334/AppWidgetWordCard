package com.cooper.wordcard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveRequestInitializer
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.gson.Gson
import io.realm.Realm


class LoadSheetActivity : AppCompatActivity(),
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
        setContentView(R.layout.activity_load_sheet)
        initPermission()
        //TODO select function
        downloadGoogleSheet()

    }

    private fun initPermission() {
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
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 6334) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result == null) {
                Log.e("cooper", "no result")
                return
            }
            if (result.isSuccess) {
                if (resultCode == RESULT_OK) {
                    GoogleSignIn.getSignedInAccountFromIntent(data)
                        .addOnSuccessListener { account ->
                            userAccount = account
                            readEmployeeList()
                        }
                    return
                }
            }
            Log.e(
                "cooper",
                "get userAccount failed ${resultCode} ${result.status.statusMessage} ${result.status.statusCode}"
            )
        }

    }

    private fun downloadGoogleSheet() {
        if (userAccount != null) {
            Log.e("cooper", "get userAccount " + userAccount!!.displayName)
            readEmployeeList()
            //Log.e("cooper")
        } else {
            val googleSignInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
            startActivityForResult(googleSignInIntent, 6334)
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
        val httpTransport = AndroidHttp.newCompatibleTransport()

        // drive api
        val googleDriveService = Drive.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName(getString(R.string.app_name))
            .build()
        val threadSheet = Thread(Runnable {
            val request = googleDriveService
                .files()
                .list()
                .setQ("mimeType='application/vnd.google-apps.spreadsheet'")
                .setOrderBy("viewedByMeTime desc")
            var result = request.execute()
            result.files.forEach {
                Log.e("cooper", it.name + ":" + it.id)
            }
            /*
        while(true){
            result.files.forEach {
                Log.e("cooper", it.name+":"+it.id)
            }
            if(result.nextPageToken == null){
                break
            }
            request.pageToken = result.nextPageToken
            result= request.execute()
            }
             */
        })
        threadSheet.start()
        /*
        // sheet api
        val service = Sheets.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName(getString(R.string.app_name))
            .build()
        val threadSheet = Thread(Runnable {
            val result = service.Spreadsheets().Values().get(sheetID, sheetRange).execute()
            Log.e("cooper", "json " + result)
            var realm = Realm.getDefaultInstance()
            realm.beginTransaction()
            val gson = Gson()
            var jsonResult = gson.fromJson(result.toString(), SheetJson::class.java)
            var saveCnt = 0
            for (i in 0..jsonResult.values.size - 1) {
                var row = jsonResult.values[i]
                var s = ""
                for (j in 0..row.size - 1) {
                    s += row[j] + ","
                }
                Log.e("cooper", "rowsize ${row.size}")
                if (row.size >= 2) {
                    //editor.putString("testset${saveCnt}_2", row[2])
                    var card = realm.createObject(WordCardModel::class.java)
                    card.id = saveCnt;
                    card.wordList.add(row[0])
                    card.wordList.add(row[1])
                    saveCnt++
                    Log.e("cooper", "save pref ${row[0]} ${row[1]}")
                }

//                Log.e("cooper", "${i}:${s}")
            }
            realm.commitTransaction()
            realm.close()
            Log.e("cooper", "apply preference finish cnt ${saveCnt}")
        })
        threadSheet.start()
*/
    }
/*
    companion object {
        val sheetID = "1BZGHuQRvC-caZr3-3tcsa0Q04W9vE6p4y_-5q1oy798"
        val sheetRange = "工作表1!A1:B500"
    }

 */
}
