package com.example.linkedindownloader

import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.linkedindownloader.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class MainActivity : AppCompatActivity() {
    lateinit var mainBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        getPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, 101)
        setUpButton()

    }

    private fun getPermissions(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        }
    }

    private fun setUpButton() {
        var stringArray: List<String>
        val downloadManager: DownloadManager =
            getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        var request: DownloadManager.Request
        mainBinding.button.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                if ((mainBinding.urlText.text.toString()).isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        mainBinding.urlText.error = "Cannot be left empty"
                    }
                } else {
                    stringArray = mainBinding.urlText.text.toString().split("posts/")[1].split("?")
                    request =
                        DownloadManager.Request(Uri.parse(getLinkFromLinkedUrlOptimized(mainBinding.urlText.text.toString())))
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                    request.setTitle("Downloading...")
                    request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        stringArray[0] + ".mp4"
                    )
                    downloadManager.enqueue(request)
                }
            }
        }
    }

    private suspend fun getLinkFromLinkedUrlOptimized(linkedInUrl: String): String? {
        val doc = withContext(Dispatchers.IO) {
            Jsoup.connect(linkedInUrl)
        }
        val mainContent = doc.get().getElementById("main-content")
        val videoSrc = mainContent?.firstElementChild()?.firstElementChild()?.select("video")
            ?.attr("data-sources")
        return videoSrc!!.split(",")!!.first()!!.removeRange(0, 8)!!.removeSurrounding("\"")
    }
}
