package com.vu.lecturehub.ui.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vu.lecturehub.data.model.Department
import com.vu.lecturehub.databinding.ItemDiscoverHeaderBinding

class DiscoverHeaderAdapter(
    private val subjectAdapter: SubjectAdapter,
    private val onSearchQueryChanged: (String) -> Unit,
    private val onSearchSubmitted: (String) -> Unit,
    private val onSearchFocused: () -> Unit,
    private val onTrendingChipClicked: (String) -> Unit,
    private val onResetFilterClicked: () -> Unit
) : RecyclerView.Adapter<DiscoverHeaderAdapter.HeaderViewHolder>() {

    private var binding: ItemDiscoverHeaderBinding? = null
    private var currentCatalogTitle = "Explore all courses"
    private var isFilterActive = false

    fun updateCatalogHeader(title: String, isFiltered: Boolean) {
        currentCatalogTitle = title
        isFilterActive = isFiltered
        binding?.let { b ->
            b.tvCatalogSectionTitle.text = title
            b.btnResetSubjectFilter.visibility = if (isFiltered) View.VISIBLE else View.GONE
        }
    }

    fun setSearchQueryText(text: String) {
        binding?.etHeaderSearch?.let { et ->
            et.setText(text)
            et.setSelection(text.length)
        }
    }

    fun clearSearchInput() {
        binding?.etHeaderSearch?.text?.clear()
    }

    override fun getItemCount(): Int = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val b = ItemDiscoverHeaderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        binding = b
        return HeaderViewHolder(b)
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        holder.bind()
    }

    inner class HeaderViewHolder(
        val b: ItemDiscoverHeaderBinding
    ) : RecyclerView.ViewHolder(b.root) {

        fun bind() {
            b.tvCatalogSectionTitle.text = currentCatalogTitle
            b.btnResetSubjectFilter.visibility = if (isFilterActive) View.VISIBLE else View.GONE

            // Setup Popular Subjects Grid (2 columns)
            if (b.rvPopularSubjects.adapter == null) {
                b.rvPopularSubjects.apply {
                    layoutManager = GridLayoutManager(context, 2)
                    adapter = subjectAdapter
                    isNestedScrollingEnabled = false
                }
            }

            b.btnResetSubjectFilter.setOnClickListener {
                onResetFilterClicked()
            }

            // Search EditText Listeners
            b.etHeaderSearch.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    onSearchFocused()
                }
            }

            b.etHeaderSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val query = s?.toString() ?: ""
                    b.btnHeaderClearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
                    onSearchQueryChanged(query)
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            b.etHeaderSearch.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    val query = b.etHeaderSearch.text.toString().trim()
                    onSearchSubmitted(query)
                    true
                } else {
                    false
                }
            }

            b.btnHeaderSearchAction.setOnClickListener {
                val query = b.etHeaderSearch.text.toString().trim()
                onSearchSubmitted(query)
            }

            b.btnHeaderClearSearch.setOnClickListener {
                b.etHeaderSearch.text.clear()
                onSearchQueryChanged("")
            }

            // Trending Chips
            b.chipTrendCs201.setOnClickListener { onTrendingChipClicked("CS201") }
            b.chipTrendPython.setOnClickListener { onTrendingChipClicked("Python") }
            b.chipTrendData.setOnClickListener { onTrendingChipClicked("Data") }
            b.chipTrendCalculus.setOnClickListener { onTrendingChipClicked("Calculus") }
            b.chipTrendAccounting.setOnClickListener { onTrendingChipClicked("Accounting") }
        }
    }
}
