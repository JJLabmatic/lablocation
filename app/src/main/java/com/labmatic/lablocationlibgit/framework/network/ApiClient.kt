package com.labmatic.lablocation.framework.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import org.simpleframework.xml.convert.AnnotationStrategy
import org.simpleframework.xml.core.Persister
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import java.util.concurrent.TimeUnit

fun createApiClient(baseUrl: String,
                    addCloseConnectionHeader: Boolean = false,
                    headers: HashMap<String, String>? = null,
                    debug: Boolean = false,
                    xml: Boolean = false): Retrofit {
    if(xml) {
        return retrofitXMLClient(baseUrl, httpClient(debug, headers, addCloseConnectionHeader))
    } else {
        return retrofitClient(baseUrl, httpClient(debug, headers, addCloseConnectionHeader))
    }
}

private fun getRequestHeader(chain: Interceptor.Chain): Request {
    return chain.request().newBuilder()
        .addHeader("Connection", "close")
        .build()
}

private fun httpClient(debug: Boolean, headers: HashMap<String, String>?, addCloseConnectionHeader: Boolean): OkHttpClient {
    val httpLoggingInterceptor = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT)
    val clientBuilder = OkHttpClient.Builder()

    clientBuilder.connectTimeout(30, TimeUnit.SECONDS)
    clientBuilder.writeTimeout(30, TimeUnit.SECONDS)
    clientBuilder.readTimeout(30, TimeUnit.SECONDS)
    clientBuilder.retryOnConnectionFailure(true)

    if(headers != null) {
        val headerAuthorizationInterceptor = Interceptor {
            var request = it.request()

            val headerBuilder = request.headers().newBuilder()

            for((name, value) in headers) {
                headerBuilder.add(name, value)
            }

            request = request.newBuilder().headers(headerBuilder.build()).build()

            it.proceed(request)
        }

        clientBuilder.addInterceptor(headerAuthorizationInterceptor)
    }

    if(debug) {
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        clientBuilder.addInterceptor(httpLoggingInterceptor)
    }
    if(addCloseConnectionHeader) {
        clientBuilder.addInterceptor {
                chain -> chain.proceed(getRequestHeader(chain))
        }
    }

    return clientBuilder.build()
}

private fun retrofitClient(baseUrl: String, httpClient: OkHttpClient): Retrofit =
    Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        //.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()

private fun retrofitXMLClient(baseUrl: String, httpClient: OkHttpClient): Retrofit =
    Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(httpClient)
        .addConverterFactory(
            SimpleXmlConverterFactory.createNonStrict(
                Persister(AnnotationStrategy())
            ))
        .addConverterFactory(GsonConverterFactory.create())
        //.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()