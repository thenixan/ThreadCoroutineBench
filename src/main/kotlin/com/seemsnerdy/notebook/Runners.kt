package com.seemsnerdy.notebook

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.sin


fun runInMainThread(api: GitHubApi, user: String) {
    val result = getFollowers(api, user)
        .map { follower -> follower.login }
        .map { followerName -> repositoriesTask(api, followerName) }
        .flatten()
        .size
    println(result)
}


fun runThreads(api: GitHubApi, user: String, count: Int) {
    val followers = getFollowers(api, user)
    val executor: ExecutorService = Executors.newFixedThreadPool(count)
    val result = followers
        .map { follower -> follower.login }
        .map { followerName ->
            val task = { repositoriesTask(api, followerName) }
            executor.submit(task)
        }
        .map {
            it.get()
        }
        .flatten()
        .size
    executor.shutdown()
    println(result)
}


fun runAsync(api: GitHubApi, user: String) {
    runBlocking(Dispatchers.Default) {
        val result = getFollowers(api, user)
            .map { follower ->
                async(Dispatchers.Default) { repositoriesTask(api, follower.login) }
            }
            .awaitAll()
            .flatten()
            .size
        println(result)
    }
}

fun runRx(api: GitHubApi, user: String) {
    val result = getFollowers(api, user)
        .map { follower -> follower.login }
        .map { followerName -> api.listRepositoriesReactive(followerName).subscribeOn(Schedulers.io()) }
        .let { singles -> Single.merge(singles) }
        .flatMapIterable { repositories -> repositories }
        .count()
        .blockingGet()
    println(result)
}