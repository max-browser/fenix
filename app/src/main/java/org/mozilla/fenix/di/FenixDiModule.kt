package org.mozilla.fenix.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.mozilla.fenix.home.mydocuments.MyDocumentsUseCase
import org.mozilla.fenix.library.mydocuments.MyDocumentsViewModel
import org.mozilla.fenix.setdefaultbrowser.DefaultBrowserViewModel
import org.mozilla.fenix.videodownloader.DownloadViewModel

val fenixViewModelModule = module {
    viewModel { MyDocumentsViewModel(get()) }
    viewModel { DownloadViewModel(get(), get()) }
    viewModel { DefaultBrowserViewModel() }
}

val fenixUseCaseModule = module {
    factory { MyDocumentsUseCase(get(), get()) }
}
