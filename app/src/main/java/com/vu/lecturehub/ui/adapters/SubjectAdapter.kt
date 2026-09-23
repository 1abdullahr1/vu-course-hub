package com.vu.lecturehub.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vu.lecturehub.R
import com.vu.lecturehub.data.model.Department
import com.vu.lecturehub.databinding.ItemSubjectCardBinding

class SubjectAdapter(
    initialDepartments: List<Department>,
    private val onSubjectClick: (Department) -> Unit
) : RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder>() {

    private val departments = initialDepartments.toMutableList()

    fun updateDepartments(newDepartments: List<Department>) {
        departments.clear()
        // Filter out "All" so the grid only shows concrete subjects
        val distinctSubjects = newDepartments.filter { it.name != "All" }
        departments.addAll(distinctSubjects)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val binding = ItemSubjectCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SubjectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        holder.bind(departments[position])
    }

    override fun getItemCount(): Int = departments.size

    inner class SubjectViewHolder(
        private val binding: ItemSubjectCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(department: Department) {
            binding.tvSubjectName.text = department.name

            val (iconRes, bgRes) = getCategoryVisuals(department.name)
            binding.ivSubjectIcon.setImageResource(iconRes)
            binding.layoutSubjectIconContainer.setBackgroundResource(bgRes)

            binding.root.setOnClickListener {
                onSubjectClick(department)
            }
        }

        private fun getCategoryVisuals(deptName: String): Pair<Int, Int> {
            val lower = deptName.lowercase()
            return when {
                lower.contains("computer") || lower.contains("information") || lower.contains("bif") || lower.contains("it") ->
                    Pair(R.drawable.ic_code, R.drawable.bg_subject_tech)
                lower.contains("math") || lower.contains("stat") ->
                    Pair(R.drawable.ic_calculate, R.drawable.bg_subject_math)
                lower.contains("finance") || lower.contains("bank") || lower.contains("account") || lower.contains("econom") ->
                    Pair(R.drawable.ic_trending_up, R.drawable.bg_subject_finance)
                lower.contains("manage") || lower.contains("marketing") || lower.contains("resource") || lower.contains("public") || lower.contains("mgt") ->
                    Pair(R.drawable.ic_business, R.drawable.bg_subject_biz)
                lower.contains("bio") || lower.contains("chem") || lower.contains("phys") || lower.contains("zool") ->
                    Pair(R.drawable.ic_science, R.drawable.bg_subject_science)
                else ->
                    Pair(R.drawable.ic_book, R.drawable.bg_subject_humanities)
            }
        }
    }
}
