package com.vu.lecturehub.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
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
        // Filter out "All" if we only want distinct subjects in the grid
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

            // Load subject image or fallback to stylish gradient placeholder
            binding.ivSubjectImage.load(R.drawable.subject_placeholder) {
                crossfade(true)
            }

            binding.root.setOnClickListener {
                onSubjectClick(department)
            }
        }
    }
}
