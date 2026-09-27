use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Course {
    pub playlistId: String,
    pub title: String,
    #[serde(default)]
    pub courseCode: Option<String>,
    #[serde(default)]
    pub department: Option<String>,
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
    pub fn id_or_code(&self) -> &str {
        if let Some(code) = &self.courseCode {
            let trimmed = code.trim();
            if !trimmed.is_empty() {
                return trimmed;
            }
        }
        &self.playlistId
    }

    pub fn code(&self) -> &str {
        self.courseCode.as_deref().unwrap_or("")
    }

    pub fn display_code(&self) -> String {
        let c = self.code().trim();
        if !c.is_empty() {
            c.to_string()
        } else {
            "VU".to_string()
        }
    }

    pub fn dept(&self) -> &str {
        self.department
            .as_deref()
            .map(|s| s.trim())
            .filter(|s| !s.is_empty())
            .unwrap_or("General / Other")
    }

    pub fn clean_title(&self) -> String {
        let code = self.code().to_lowercase();
        let title_lower = self.title.to_lowercase();
        if !code.is_empty() && title_lower.starts_with(&code) {
            let rest = &self.title[self.code().len()..];
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
