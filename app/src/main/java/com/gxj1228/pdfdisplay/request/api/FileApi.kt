package com.gxj1228.pdfdisplay.request.api

import io.reactivex.Flowable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

/**
 * Created by gxj on 2018/6/4.
 */
interface FileApi {
    /**
     * 下载pdf
     * @param pdfUrl pdf链接
     * @return
     */
    @Streaming
    @GET
    fun downloadPdf(@Url pdfUrl: String): Flowable<ResponseBody>
}