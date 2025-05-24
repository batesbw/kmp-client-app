package io.music_assistant.client.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

// Import allModules from the other DI package
import com.mass.client.di.allModules as massAllModules

fun initKoin(
    vararg platformModules: Module,
    config: KoinAppDeclaration? = null
) {
    startKoin {
        config?.invoke(this)
        // Add massAllModules to the list of modules to be loaded
        modules(listOf(sharedModule, *platformModules) + massAllModules)
    }
}