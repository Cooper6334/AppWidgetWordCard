package com.cooper.wordcard

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.google.api.services.drive.DriveScopes
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.gson.Gson
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_load_sheet.*


class LoadSheetActivity : AppCompatActivity(),
    GoogleApiClient.OnConnectionFailedListener,
    GoogleApiClient.ConnectionCallbacks {

    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var userAccount: GoogleSignInAccount
    private lateinit var credential: GoogleAccountCredential
    private var nextPageToken: String = ""
    private lateinit var adapter: SheetAdapter

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

        sheetListView.layoutManager = LinearLayoutManager(this);
        adapter = SheetAdapter()
        sheetListView.adapter = adapter

        val googleSignInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
        startActivityForResult(googleSignInIntent, 6334)
    }

    private fun initPermission() {
        val googleSignInOptions = GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        ).requestEmail()
            .requestProfile()
            .requestScopes(Scope(DriveScopes.DRIVE))
            .requestScopes(Scope(SheetsScopes.SPREADSHEETS))
            .requestScopes(Scope(SheetsScopes.SPREADSHEETS_READONLY))
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
                            credential = GoogleAccountCredential.usingOAuth2(
                                applicationContext,
                                listOf(SheetsScopes.SPREADSHEETS_READONLY)
                            )
                            credential.selectedAccount = userAccount.account
                            getSheetList()
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

    private fun getSheetList() {
        // drive api
        val googleDriveService = Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            JacksonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName(getString(R.string.app_name))
            .build()
        val threadSheet = Thread(Runnable {
            val request = googleDriveService
                .files()
                .list()
                .setQ("mimeType='application/vnd.google-apps.spreadsheet'")
                .setOrderBy("viewedByMeTime desc")
            if (nextPageToken.isNotEmpty()) {
                request.pageToken = nextPageToken
            }
            var result = request.execute()
            nextPageToken = result.nextPageToken
            val sheetNames = ArrayList<String>()
            val sheetKeys = ArrayList<String>()
            result.files.forEach {
                Log.e("cooper", it.name + ":" + it.id)
                sheetNames.add(it.name)
                sheetKeys.add(it.id)
            }

            this@LoadSheetActivity.runOnUiThread(java.lang.Runnable {
                adapter.addNewData(sheetNames, sheetKeys)
            })
        })
        threadSheet.start()
    }

    fun readSheet(sheetId: String, sheetName: String) {
        // sheet api
        val threadSheet = Thread(Runnable {
            val service = Sheets.Builder(
                AndroidHttp.newCompatibleTransport(),
                JacksonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName(getString(R.string.app_name))
                .build()

            var tabIndex = 0
            val getTabName = service.Spreadsheets().get(sheetId).execute()
            val tabName = getTabName.sheets[tabIndex].properties.title
            //val tabId = getTabName.sheets[tabIndex].properties.sheetId
            readTab(service, sheetId, sheetName, tabName,0)
        })
        threadSheet.start()
    }

    fun readTab(
        service: Sheets,
        sheetId: String,
        sheetName: String,
        tabName: String,
        tabId: Int
    ) {

        val result = service.Spreadsheets().Values().get(sheetId, "$tabName!A1:B").execute()
        val sheetModel = SheetModel()
        sheetModel.sheetName = sheetName
        sheetModel.sheetId = sheetId
        sheetModel.tabName = tabName
        sheetModel.tabId = tabId
        sheetModel.key = "$sheetId$tabId";
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

                var card = WordCardModel()
                card.id = saveCnt;
                card.wordList.add(row[0])
                card.wordList.add(row[1])
                sheetModel.cardList.add(card)
                saveCnt++
                Log.e("cooper", "save pref ${row[0]} ${row[1]}")
            }

//                Log.e("cooper", "${i}:${s}")
        }
        var realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        realm.copyToRealmOrUpdate(sheetModel)
        realm.commitTransaction()
        realm.close()
        runOnUiThread { finish()}

        Log.e("cooper", "apply preference finish cnt ${saveCnt}")
    }

    inner class SheetAdapter : RecyclerView.Adapter<SheetAdapter.Holder>() {
        var sheetList = ArrayList<String>()
        var keyList = ArrayList<String>()

        inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            init {
                itemView.setOnClickListener {
                    //Log.e("cooper", "Click sheet ${sheetList[adapterPosition]}")
                    ProgressDialog.show(this@LoadSheetActivity,"Loading","")
                    this@LoadSheetActivity.readSheet(
                        keyList[adapterPosition],
                        sheetList[adapterPosition]
                    )
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val v: View =
                LayoutInflater.from(parent.context)
                    .inflate(android.R.layout.simple_list_item_1, parent, false)
            return Holder(v)
        }

        override fun getItemCount(): Int {
            return sheetList.size
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val tv: TextView = holder.itemView as TextView
            tv.text = sheetList[position]
        }

        fun addNewData(newName: ArrayList<String>, newKey: ArrayList<String>) {
            newName.forEach { sheetList.add(it) }
            newKey.forEach { keyList.add(it) }
            notifyDataSetChanged()
        }
    }

}
