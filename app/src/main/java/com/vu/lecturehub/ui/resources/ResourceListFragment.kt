package com.vu.lecturehub.ui.resources

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vu.lecturehub.R
import com.vu.lecturehub.data.model.ResourceItem
import com.vu.lecturehub.data.model.ResourceType
import com.vu.lecturehub.data.repository.ResourcesRepository
import com.vu.lecturehub.databinding.FragmentResourceListBinding
import com.vu.lecturehub.ui.adapters.ResourceAdapter

class ResourceListFragment : Fragment() {

    private var _binding: FragmentResourceListBinding? = null
    private val binding get() = _binding!!

    private var resourceType: ResourceType = ResourceType.HANDOUT
    private lateinit var adapter: ResourceAdapter

    companion object {
        private const val ARG_TYPE = "arg_resource_type"

        fun newInstance(type: ResourceType): ResourceListFragment {
            return ResourceListFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_TYPE, type)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        resourceType = (arguments?.getSerializable(ARG_TYPE) as? ResourceType) ?: ResourceType.HANDOUT
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResourceListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val items = when (resourceType) {
            ResourceType.HANDOUT -> ResourcesRepository.handouts
            ResourceType.LINK -> ResourcesRepository.links
            ResourceType.TOOL -> ResourcesRepository.tools
        }

        adapter = ResourceAdapter(items) { item ->
            handleItemClick(item)
        }

        binding.rvResources.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@ResourceListFragment.adapter
            setHasFixedSize(true)
        }
    }

    private fun handleItemClick(item: ResourceItem) {
        when (item.type) {
            ResourceType.LINK -> {
                val url = item.targetUrl
                if (!url.isNullOrEmpty()) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            ResourceType.HANDOUT -> {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(item.title)
                    .setMessage("${item.description}\n\nFormat: ${item.badgeText ?: "PDF"}\n\nThis official document can be downloaded or accessed directly via your student VULMS portal account.")
                    .setPositiveButton(R.string.close, null)
                    .show()
            }
            ResourceType.TOOL -> {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(item.title)
                    .setMessage("${item.description}\n\n${getString(R.string.tool_placeholder_desc)}")
                    .setPositiveButton(R.string.close, null)
                    .show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
