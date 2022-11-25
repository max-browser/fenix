package org.mozilla.fenix.library.mydocuments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.mozilla.fenix.NavHostActivity
import org.mozilla.fenix.databinding.FragmentMyDocumentsBinding
import org.mozilla.fenix.ext.createOpenPdfIntent
import org.mozilla.fenix.home.mydocuments.MyDocumentsItem

class MyDocumentsFragment : Fragment() {

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
}
