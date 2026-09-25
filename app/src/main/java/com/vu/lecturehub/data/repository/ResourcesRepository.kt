package com.vu.lecturehub.data.repository

import com.vu.lecturehub.R
import com.vu.lecturehub.data.model.ResourceItem
import com.vu.lecturehub.data.model.ResourceType

object ResourcesRepository {

    val handouts: List<ResourceItem> = listOf(
        ResourceItem(
            id = "handout_1",
            title = "Student Handbook",
            description = "Official student guide, policies, code of conduct, and academic integrity guidelines.",
            type = ResourceType.HANDOUT,
            badgeText = "PDF • 2.4 MB",
            iconRes = R.drawable.ic_book
        ),
        ResourceItem(
            id = "handout_2",
            title = "Academic Regulations",
            description = "General regulations governing semester rules, grading scale, and degree requirements.",
            type = ResourceType.HANDOUT,
            badgeText = "PDF • 1.8 MB",
            iconRes = R.drawable.ic_book
        ),
        ResourceItem(
            id = "handout_3",
            title = "Course Selection Guide",
            description = "Instructions on selecting core, elective, and prerequisite courses each semester.",
            type = ResourceType.HANDOUT,
            badgeText = "PDF • 950 KB",
            iconRes = R.drawable.ic_book
        ),
        ResourceItem(
            id = "handout_4",
            title = "Examination Rules & Grading System",
            description = "Detailed guidelines on midterm and final term exams, weightages, and paper patterns.",
            type = ResourceType.HANDOUT,
            badgeText = "PDF • 1.2 MB",
            iconRes = R.drawable.ic_book
        ),
        ResourceItem(
            id = "handout_5",
            title = "Fee Structure & Installment Guide",
            description = "Comprehensive schedule of fees, payment vouchers, and financial aid procedures.",
            type = ResourceType.HANDOUT,
            badgeText = "PDF • 800 KB",
            iconRes = R.drawable.ic_book
        ),
        ResourceItem(
            id = "handout_6",
            title = "Degree & Transcript Issuance Procedure",
            description = "Step-by-step instructions for obtaining interim transcripts and final degree certificates.",
            type = ResourceType.HANDOUT,
            badgeText = "PDF • 650 KB",
            iconRes = R.drawable.ic_book
        ),
        ResourceItem(
            id = "handout_7",
            title = "LMS Assignment & Quiz Policies",
            description = "Rules regarding assignment deadlines, plagiarism checks, and quiz conduct.",
            type = ResourceType.HANDOUT,
            badgeText = "PDF • 520 KB",
            iconRes = R.drawable.ic_book
        )
    )

    val links: List<ResourceItem> = listOf(
        ResourceItem(
            id = "link_1",
            title = "Virtual University LMS (VULMS)",
            description = "Access your enrolled courses, announcements, assignments, and lecture materials.",
            type = ResourceType.LINK,
            targetUrl = "https://vulms.vu.edu.pk",
            badgeText = "vulms.vu.edu.pk",
            iconRes = R.drawable.ic_school
        ),
        ResourceItem(
            id = "link_2",
            title = "Official VU Website",
            description = "Virtual University main web portal, admissions, academic calendar, and faculties.",
            type = ResourceType.LINK,
            targetUrl = "https://www.vu.edu.pk",
            badgeText = "vu.edu.pk",
            iconRes = R.drawable.ic_school
        ),
        ResourceItem(
            id = "link_3",
            title = "VU Digital Library",
            description = "Access thousands of online research journals, e-books, and reference databases.",
            type = ResourceType.LINK,
            targetUrl = "https://library.vu.edu.pk",
            badgeText = "library.vu.edu.pk",
            iconRes = R.drawable.ic_library
        ),
        ResourceItem(
            id = "link_4",
            title = "Official YouTube Channel",
            description = "Stream official Virtual University lecture broadcasts, webinars, and ceremonies.",
            type = ResourceType.LINK,
            targetUrl = "https://www.youtube.com/@VirtualUniversityofPakistan",
            badgeText = "youtube.com",
            iconRes = R.drawable.ic_subscriptions
        ),
        ResourceItem(
            id = "link_5",
            title = "Student Notice Board",
            description = "Stay updated with important announcements, date sheets, and result notices.",
            type = ResourceType.LINK,
            targetUrl = "https://www.vu.edu.pk/NewsNotice.aspx",
            badgeText = "vu.edu.pk/notice",
            iconRes = R.drawable.ic_bell
        ),
        ResourceItem(
            id = "link_6",
            title = "VU Examination Portal",
            description = "Download official VU Exam Software and manage your examination schedule.",
            type = ResourceType.LINK,
            targetUrl = "https://datesheet.vu.edu.pk",
            badgeText = "datesheet.vu.edu.pk",
            iconRes = R.drawable.ic_school
        )
    )

    val tools: List<ResourceItem> = listOf(
        ResourceItem(
            id = "tool_1",
            title = "Semester GPA Calculator",
            description = "Calculate your estimated Grade Point Average for the current semester based on credit hours and grades.",
            type = ResourceType.TOOL,
            badgeText = "Calculator",
            iconRes = R.drawable.ic_calculate
        ),
        ResourceItem(
            id = "tool_2",
            title = "Cumulative CGPA Calculator",
            description = "Compute your overall CGPA across all completed semesters and projected future terms.",
            type = ResourceType.TOOL,
            badgeText = "Calculator",
            iconRes = R.drawable.ic_calculate
        ),
        ResourceItem(
            id = "tool_3",
            title = "Percentage to CGPA Converter",
            description = "Convert percentage scores to the official Virtual University 4.0 scale accurately.",
            type = ResourceType.TOOL,
            badgeText = "Converter",
            iconRes = R.drawable.ic_calculate
        ),
        ResourceItem(
            id = "tool_4",
            title = "Final Exam Target Score Estimator",
            description = "Determine the exact final exam score needed to attain your desired letter grade.",
            type = ResourceType.TOOL,
            badgeText = "Estimator",
            iconRes = R.drawable.ic_calculate
        ),
        ResourceItem(
            id = "tool_5",
            title = "Credit Hours & Workload Planner",
            description = "Plan your weekly study schedule and coursework balance across enrolled subjects.",
            type = ResourceType.TOOL,
            badgeText = "Planner",
            iconRes = R.drawable.ic_calculate
        )
    )

    val allResources: List<ResourceItem> = handouts + links + tools

    fun searchResources(query: String): List<ResourceItem> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        return allResources.filter {
            it.title.lowercase().contains(q) ||
            it.description.lowercase().contains(q) ||
            it.badgeText?.lowercase()?.contains(q) == true
        }
    }
}
