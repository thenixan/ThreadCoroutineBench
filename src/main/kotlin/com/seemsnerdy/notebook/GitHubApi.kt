package com.seemsnerdy.notebook

import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins
import okhttp3.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface GitHubApi {

    @GET("users/{user}/followers")
    fun listFollowers(@Path("user") user: String): Call<List<Follower>>

    @GET("users/{user}/repos")
    fun listRepositories(@Path("user") user: String): Call<List<Repository>>

    @GET("users/{user}/repos")
    fun listRepositoriesReactive(@Path("user") user: String): Single<List<Repository>>

}

private fun authenticateWith(username: String, password: String): Interceptor = object : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val authenticatedRequest = request.newBuilder()
            .header(
                "Authorization",
                Credentials.basic(username, password)
            ).build()
        return chain.proceed(authenticatedRequest)
    }
}

fun getApi(username: String, password: String): GitHubApi =
    Retrofit
        .Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .client(OkHttpClient.Builder().addInterceptor(authenticateWith(username, password)).build())
        .baseUrl("https://api.github.com/")
        .build()
        .create(GitHubApi::class.java)
