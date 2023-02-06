package org.mozilla.fenix.library.mydocuments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.ktx.kotlinx.coroutines.flow.ifChanged
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.mozilla.fenix.NavHostActivity
import org.mozilla.fenix.R
import org.mozilla.fenix.databinding.FragmentMyDocumentsBinding
import org.mozilla.fenix.ext.createOpenPdfIntent
import org.mozilla.fenix.ext.isSystemInDarkTheme
import org.mozilla.fenix.ext.requireComponents
import org.mozilla.fenix.home.mydocuments.MyDocumentsItem

class MyDocumentsFragment : Fragment() {
    companion object {
        const val REQUEST_CODE_PICK_PDF_FILE = 4895
    }

    private var _binding: FragmentMyDocumentsBinding? = null
    private val binding get() = _binding!!

    private val myDocumentsViewModel: MyDocumentsViewModel by viewModel()

    private val adapter by lazy {
        MyDocumentsAdapter(lifecycle) {
            openPdf(it.uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMyDocumentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUi()
        setListeners()
        observeData()

        myDocumentsViewModel.queryDocuments(requireContext())
    }

    private fun initUi() {
        binding.apply {
            rvList.layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            rvList.adapter = adapter
        }
    }

    private fun setListeners() {
        binding.apply {
            srlRefresh.setOnRefreshListener {
                myDocumentsViewModel.queryDocuments(requireContext())
                srlRefresh.isRefreshing = false
            }
            tvChooseOthers.setOnClickListener {
                openPdfChooser()
            }
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                myDocumentsViewModel.uiState.collect { uiState ->
                    when (uiState) {
                        is MyDocumentsUiState.NoPermission -> showRequestPermissionUi()
                        is MyDocumentsUiState.Loading -> showLoadingUi()
                        is MyDocumentsUiState.Empty -> showEmptyUi()
                        is MyDocumentsUiState.Success -> showSuccessUi(uiState.items)
                    }
                }
            }
        }

//        requireComponents.appStore.flowScoped(viewLifecycleOwner) { flow ->
//            flow.map { state -> state.wallpaperState }.ifChanged().collect { currentState ->
//                val context = requireContext()
//                var backgroundColor =
//                    ContextCompat.getColor(context, R.color.fx_mobile_layer_color_2)
//
//                currentState.runIfWallpaperCardColorsAreAvailable { cardColorLight, cardColorDark ->
//                    backgroundColor = if (context.isSystemInDarkTheme()) {
//                        cardColorDark
//                    } else {
//                        cardColorLight
//                    }
//                }
//
//                binding.cvList.setCardBackgroundColor(backgroundColor)
//            }
//        }
    }

    private fun showRequestPermissionUi() {
        binding.apply {
            pbLoading.isVisible = false
            clPermission.isVisible = true
            tvEmpty.isVisible = true
            cvList.isVisible = false
        }
    }

    private fun showLoadingUi() {
        binding.apply {
            pbLoading.isVisible = true
            clPermission.isVisible = false
            tvEmpty.isVisible = false
            cvList.isVisible = false
        }
    }

    private fun showEmptyUi() {
        binding.apply {
            pbLoading.isVisible = false
            clPermission.isVisible = false
            tvEmpty.isVisible = true
            cvList.isVisible = false
        }

    }

    private fun showSuccessUi(items: List<MyDocumentsItem>) {
        binding.apply {
            pbLoading.isVisible = false
            clPermission.isVisible = false
            tvEmpty.isVisible = false
            cvList.isVisible = true
            adapter.submitList(items)
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as NavHostActivity).getSupportActionBarAndInflateIfNecessary().show()
        myDocumentsViewModel.queryDocuments(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openPdf(uri: Uri) {
        startActivity(uri.createOpenPdfIntent(requireContext()))
    }

    private fun openPdfChooser() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.fromFile(Environment.getExternalStorageDirectory()))
            }
        }

        startActivityForResult(intent, REQUEST_CODE_PICK_PDF_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_PICK_PDF_FILE && resultCode ==Activity.RESULT_OK ){
            data?.data?.let { uri->
                openPdf(uri)
            }
        }

    }
}
