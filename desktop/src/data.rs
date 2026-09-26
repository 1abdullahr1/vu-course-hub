use crate::models::{Course, Handout, Lecture, ResourceLink};

pub const COURSES_JSON: &str = include_str!("../assets/courses.json");

pub struct CourseData {
    pub courses: Vec<Course>,
    pub departments: Vec<String>,
    pub handouts: Vec<Handout>,
    pub links: Vec<ResourceLink>,
}

impl CourseData {
    pub fn load() -> Self {
        let courses: Vec<Course> = serde_json::from_str(COURSES_JSON).unwrap_or_default();

        let mut depts: Vec<String> = courses
            .iter()
            .map(|c| c.department.clone())
            .filter(|d| !d.is_empty())
            .collect();
        depts.sort();
        depts.dedup();
        let mut departments = vec!["All".to_string()];
        departments.extend(depts);

        let handouts: Vec<Handout> = courses
            .iter()
            .map(|c| Handout {
                course_code: c.courseCode.clone(),
                title: c.clean_title(),
                department: c.department.clone(),
            })
            .collect();

        let links = vec![
            ResourceLink {
                title: "Virtual University LMS (VULMS)".to_string(),
                url: "https://vulms.vu.edu.pk".to_string(),
                description: "Access course announcements, assignments, quizzes, and gradebooks.".to_string(),
            },
            ResourceLink {
                title: "VU Official Portal".to_string(),
                url: "https://www.vu.edu.pk".to_string(),
                description: "University announcements, academic calendar, and official programs.".to_string(),
            },
            ResourceLink {
                title: "VU Digital Library".to_string(),
                url: "https://library.vu.edu.pk".to_string(),
                description: "Research papers, e-books, journals, and comprehensive digital archives.".to_string(),
            },
            ResourceLink {
                title: "VU Date Sheet System".to_string(),
                url: "https://datesheet.vu.edu.pk".to_string(),
                description: "Midterm and final examination date sheet scheduling portal.".to_string(),
            },
            ResourceLink {
                title: "Official YouTube Channel".to_string(),
                url: "https://www.youtube.com/@VirtualUniversityofPakistan".to_string(),
                description: "Official channel broadcasts, live sessions, and ceremony coverage.".to_string(),
            },
            ResourceLink {
                title: "Student Notice Board".to_string(),
                url: "https://www.vu.edu.pk/NewsNotice.aspx".to_string(),
                description: "Urgent announcements, deadline extensions, and university notifications.".to_string(),
            },
            ResourceLink {
                title: "VU Admissions Portal".to_string(),
                url: "https://www.vu.edu.pk/apply".to_string(),
                description: "Program criteria, fee schedules, and new student admission portal.".to_string(),
            },
            ResourceLink {
                title: "VU Past Papers & Solutions".to_string(),
                url: "https://www.google.com/search?q=Virtual+University+past+papers+filetype:pdf".to_string(),
                description: "Past midterm and final exam papers, solved questions, and review files.".to_string(),
            },
        ];

        Self {
            courses,
            departments,
            handouts,
            links,
        }
    }

    pub fn generate_lectures(course: &Course) -> Vec<Lecture> {
        let count = course.videoCount.unwrap_or(45).clamp(1, 100);
        let first_id = course.firstVideoId.clone().unwrap_or_else(|| "j37u6TtRNFQ".to_string());

        (1..=count)
            .map(|num| Lecture {
                id: format!("{}_{}", course.courseCode, num),
                lecture_number: num,
                title: format!("{} - Lecture {:02}", course.courseCode, num),
                video_id: if num == 1 {
                    first_id.clone()
                } else {
                    format!("{}_{}", first_id, num)
                },
                duration: "45:00".to_string(),
            })
            .collect()
    }
}
