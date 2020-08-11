package com.seemsnerdy.notebook

import java.io.IOException


fun getFollowers(api: GitHubApi, user: String): List<Follower> =
    api
        .listFollowers(user)
        .execute()
        .let { response ->
            response.takeIf { it.isSuccessful }
                ?.body()
                ?: throw IOException("Unable to load followers: ${response.errorBody()?.string()}")
        }

fun repositoriesTask(api: GitHubApi, user: String): List<String> =
    api
        .listRepositories(user)
        .execute()
        .takeIf { it.isSuccessful }
        ?.body()
        ?.map { repo -> repo.toString() }
        ?: throw IOException("Cannot load repos for user: $user")
