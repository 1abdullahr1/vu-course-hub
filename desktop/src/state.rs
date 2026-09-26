use std::collections::HashSet;
use crate::models::{Course, Lecture};
use crate::theme::ThemeMode;

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum Tab {
    Home,
    Courses,
    Player,
    Saved,
    Handouts,
    Links,
}

#[derive(Debug, Clone)]
pub struct AppState {
    pub current_tab: Tab,
    pub selected_department: String,
    pub course_search_query: String,
    pub handout_search_query: String,
    pub selected_course: Option<Course>,
    pub active_lecture: Option<Lecture>,
    pub current_course_lectures: Vec<Lecture>,
    pub is_playing: bool,
    pub playback_speed: f32,
    pub bookmarked_courses: HashSet<String>,
    pub watched_history: Vec<(String, String, f32)>, // (course_code, lecture_title, progress)
    pub theme_mode: ThemeMode,
}

impl Default for AppState {
    fn default() -> Self {
        Self {
            current_tab: Tab::Home,
            selected_department: "All".to_string(),
            course_search_query: String::new(),
            handout_search_query: String::new(),
            selected_course: None,
            active_lecture: None,
            current_course_lectures: Vec::new(),
            is_playing: false,
            playback_speed: 1.0,
            bookmarked_courses: HashSet::new(),
            watched_history: Vec::new(),
            theme_mode: ThemeMode::Light,
        }
    }
}

impl AppState {
    pub fn toggle_bookmark(&mut self, course_code: &str) {
        if self.bookmarked_courses.contains(course_code) {
            self.bookmarked_courses.remove(course_code);
        } else {
            self.bookmarked_courses.insert(course_code.to_string());
        }
    }

    pub fn is_bookmarked(&self, course_code: &str) -> bool {
        self.bookmarked_courses.contains(course_code)
    }

    pub fn set_active_course(&mut self, course: Course, lectures: Vec<Lecture>) {
        if let Some(first_lecture) = lectures.first().cloned() {
            self.active_lecture = Some(first_lecture);
        }
        self.current_course_lectures = lectures;
        self.selected_course = Some(course);
        self.current_tab = Tab::Player;
    }

    pub fn toggle_theme(&mut self) {
        self.theme_mode = match self.theme_mode {
            ThemeMode::Light => ThemeMode::Dark,
            ThemeMode::Dark => ThemeMode::Light,
        };
    }
}
