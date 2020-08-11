@file:JvmName("Main")

package com.seemsnerdy.notebook

import kotlinx.cli.*
import kotlinx.coroutines.*
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool

fun main(args: Array<String>) {
    val parser = ArgParser("Concurrency example")

    val login by parser.option(ArgType.String, description = "Login for GitHub API").required()
    val password by parser.option(ArgType.String, description = "Password for GitHub API").required()

    val username by parser.option(
        ArgType.String,
        description = "Username of a GitHub profile"
    ).required()

    val strategy by parser.option(
        ArgType.Choice(listOf("blocking", "threads", "async", "rx")),
        description = "Which strategy should be used for tests"
    ).required()

    val threadCount by parser.option(
        ArgType.Int,
        shortName = "t",
        description = "Number of runners - only used for `threads`"
    )

    val useCpuNumber by parser.option(
        ArgType.Boolean,
        shortName = "cpu",
        description = "Wheather number of runners should be set to CPU number"
    ).default(false)

    val useParallelism by parser.option(
        ArgType.Boolean,
        shortName = "parallelism",
        description = "Wheather number of runners should be set to `ForkJoinPool.commonPool().parallelism`"
    ).default(false)

    parser.parse(args)

    val api = getApi(login, password)

    when (strategy) {
        "blocking" -> runInMainThread(api, username)
        "threads" -> {
            val threadCount = threadCount ?: Runtime.getRuntime().availableProcessors().takeIf { useCpuNumber }
            ?: ForkJoinPool.commonPool().parallelism.takeIf { useParallelism }
            ?: throw java.lang.IllegalArgumentException("Please set the threads number")
            runThreads(api, username, threadCount)
        }
        "async" -> runAsync(api, username)
        "rx" -> runRx(api, username)
    }
}
