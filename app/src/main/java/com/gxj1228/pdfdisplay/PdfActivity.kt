package com.gxj1228.pdfdisplay

import android.annotation.SuppressLint
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.FragmentActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.gxj1228.pdfdisplay.databinding.ActivityPdfBinding
import com.gxj1228.pdfdisplay.request.HttpHelp
import com.gxj1228.pdfdisplay.util.FileUtil
import com.gxj1228.pdfdisplay.util.Md5Util
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.ResourceSubscriber
import java.io.File
import java.net.MalformedURLException

class PdfActivity : FragmentActivity() {
    private lateinit var mBinding: ActivityPdfBinding
    private var mProgress: Long = 0
    private var mTotal: Long = 0
    private val mDispose = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_pdf)
        initView()
        //网络url
        loadWebPdf()
        //本地asset文件
//        loadLocalPdf()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initView() {
        val webSettings = mBinding.webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.allowFileAccess = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webSettings.allowFileAccessFromFileURLs = true
            webSettings.allowUniversalAccessFromFileURLs = true
        }
    }

    /**
     * 加载assets 文件中的pdf文件
     */
    private fun loadLocalPdf() {
        val pdfUrl = "file:///android_asset/kotlin-reference-chinese.pdf"
        mBinding.webView.loadUrl("file:///android_asset/pdfjs/web/viewer.html?file=$pdfUrl")
    }

    /**
     * 加载网络url pdf文件
     */
    private fun loadWebPdf() {
        val pdfUrl = "http://static-public.jesselivermore.com/listedco/listconews/gem/2018/0313/GLN20180313006_C.pdf"
        val pdfName = Md5Util.getMD5(pdfUrl)

        val pdfPath = filesDir.absolutePath + File.separator + pdfName + ".pdf"
        val file = File(pdfPath)

        try {
            if (file.exists()) {//如果已经下载过,直接加载
                mBinding.webView.loadUrl("file:///android_asset/pdfjs/web/viewer.html?file=$pdfPath")
            } else {//下载网络文件
                mDispose.add(HttpHelp.getInstance().getFileApi { progress, total, done ->
                    mProgress = progress
                    mTotal = total
                    Log.e("gxj progress : ", (mProgress * 100 / mTotal.toFloat()).toString())
                    handler.sendEmptyMessage(1)
                    if (done) {
                        handler.sendEmptyMessage(2)
                    }
                }
                        .downloadPdf(pdfUrl)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .map {
                            FileUtil.writeFileToDisk(pdfPath, it)
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : ResourceSubscriber<String>() {
                            override fun onError(t: Throwable?) {
                            }

                            override fun onComplete() {
                            }

                            override fun onNext(path: String) {
                                if (!TextUtils.isEmpty(path)) {
                                    mBinding.webView.loadUrl("file:///android_asset/pdfjs/web/viewer.html?file=$path")
                                }
                            }
                        }))
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
    }

    private var handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                1 -> {
                    val progress = (mProgress * 100 / mTotal.toFloat()).toInt()
                    mBinding.progressBar.progress = progress
                }
                2 -> mBinding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!mDispose.isDisposed) {
            mDispose.clear()
        }
        handler.removeCallbacksAndMessages(null)
    }
}
