use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Course {
    pub playlistId: String,
    pub title: String,
    pub courseCode: String,
    pub department: String,
    #[serde(default)]
    pub isAcademicCourse: Option<bool>,
    #[serde(default)]
    pub videoCount: Option<i32>,
    #[serde(default)]
    pub videoCountText: Option<String>,
    #[serde(default)]
    pub thumbnailUrl: Option<String>,
    #[serde(default)]
    pub playlistUrl: Option<String>,
    #[serde(default)]
    pub firstVideoId: Option<String>,
}

impl Course {
    pub fn clean_title(&self) -> String {
        let code = self.courseCode.to_lowercase();
        let title_lower = self.title.to_lowercase();
        if title_lower.starts_with(&code) {
            let rest = &self.title[self.courseCode.len()..];
            let trimmed = rest.trim_start_matches(|c: char| c == '-' || c == '–' || c == '—' || c == ':' || c.is_whitespace());
            if !trimmed.is_empty() {
                return trimmed.to_string();
            }
        }
        self.title.clone()
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Lecture {
    pub id: String,
    pub lecture_number: i32,
    pub title: String,
    pub video_id: String,
    pub duration: String,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Handout {
    pub course_code: String,
    pub title: String,
    pub department: String,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ResourceLink {
    pub title: String,
    pub url: String,
    pub description: String,
}
