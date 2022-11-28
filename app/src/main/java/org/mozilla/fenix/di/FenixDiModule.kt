package org.mozilla.fenix.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.mozilla.fenix.home.mydocuments.MyDocumentsUseCase
import org.mozilla.fenix.library.mydocuments.MyDocumentsViewModel


val fenixViewModelModule = module {
    viewModel { MyDocumentsViewModel(get()) }
}

val fenixUseCaseModule = module {
    factory { MyDocumentsUseCase(get(), get()) }
}
