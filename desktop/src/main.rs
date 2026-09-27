#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

mod data;
mod embedded_player;
mod models;
mod player;
mod state;
mod theme;
mod views;

use std::borrow::Cow;
use gpui::prelude::*;
use gpui::{
    App, AssetSource, Bounds, Context, Render, Result, SharedString, TitlebarOptions, Window, WindowBounds, WindowOptions,
    div, point, px, size,
};
use crate::data::CourseData;
use crate::embedded_player::EmbeddedPlayerManager;
use crate::models::{Course, Lecture};
use crate::player::PlayerLauncher;

pub struct EmbeddedAssets;

impl AssetSource for EmbeddedAssets {
    fn load(&self, path: &str) -> Result<Option<Cow<'static, [u8]>>> {
        let clean = path.trim_start_matches('/');
        match clean {
            "il_learn.png" | "assets/il_learn.png" => {
                Ok(Some(Cow::Borrowed(include_bytes!("../assets/il_learn.png"))))
            }
            "il_my_learning.png" | "assets/il_my_learning.png" => {
                Ok(Some(Cow::Borrowed(include_bytes!("../assets/il_my_learning.png"))))
            }
            _ => Ok(None),
        }
    }

    fn list(&self, _path: &str) -> Result<Vec<SharedString>> {
        Ok(vec![
            "il_learn.png".into(),
            "il_my_learning.png".into(),
        ])
    }
}
use crate::state::{AppState, Tab};
use crate::views::{
    courses_view, handouts_view, home_view, links_view, player_view, saved_view, sidebar,
};

pub struct RootView {
    pub state: AppState,
    pub data: CourseData,
}

impl RootView {
    pub fn new(_cx: &mut Context<Self>) -> Self {
        Self {
            state: AppState::default(),
            data: CourseData::load(),
        }
    }

    pub fn set_tab(&mut self, tab: Tab, _window: &mut Window, cx: &mut Context<Self>) {
        self.state.current_tab = tab;
        if tab == Tab::Player {
            EmbeddedPlayerManager::show();
        } else {
            EmbeddedPlayerManager::hide();
        }
        cx.notify();
    }

    pub fn select_course(&mut self, course: Course, window: &mut Window, cx: &mut Context<Self>) {
        let lectures = CourseData::generate_lectures(&course);
        self.state.set_active_course(course.clone(), lectures);
        self.state.is_playing = true;
        self.set_tab(Tab::Player, window, cx);
        EmbeddedPlayerManager::navigate_to_lecture(&course.playlistId, 1, course.firstVideoId.as_deref());
        cx.notify();
    }

    pub fn select_lecture(&mut self, lecture: Lecture, _window: &mut Window, cx: &mut Context<Self>) {
        let playlist_id = self.state.selected_course.as_ref().map(|c| c.playlistId.clone()).unwrap_or_default();
        let first_id = self.state.selected_course.as_ref().and_then(|c| c.firstVideoId.clone());
        self.state.active_lecture = Some(lecture.clone());
        self.state.is_playing = true;
        EmbeddedPlayerManager::navigate_to_lecture(&playlist_id, lecture.lecture_number, first_id.as_deref());
        cx.notify();
    }

    pub fn play_current_lecture(&mut self, _window: &mut Window, cx: &mut Context<Self>) {
        if let (Some(course), Some(lec)) = (&self.state.selected_course, &self.state.active_lecture) {
            self.state.is_playing = true;
            EmbeddedPlayerManager::navigate_to_lecture(&course.playlistId, lec.lecture_number, course.firstVideoId.as_deref());
            cx.notify();
        }
    }

    pub fn open_current_in_browser(&mut self, _window: &mut Window, cx: &mut Context<Self>) {
        if let (Some(course), Some(lec)) = (&self.state.selected_course, &self.state.active_lecture) {
            PlayerLauncher::open_in_browser(&course.playlistId, lec.lecture_number, course.firstVideoId.as_deref());
            cx.notify();
        }
    }

    pub fn next_lecture(&mut self, _window: &mut Window, cx: &mut Context<Self>) {
        if let Some(next_lec) = self.state.next_lecture() {
            if let Some(course) = &self.state.selected_course {
                self.state.is_playing = true;
                EmbeddedPlayerManager::navigate_to_lecture(&course.playlistId, next_lec.lecture_number, course.firstVideoId.as_deref());
            }
            cx.notify();
        }
    }

    pub fn prev_lecture(&mut self, _window: &mut Window, cx: &mut Context<Self>) {
        if let Some(prev_lec) = self.state.prev_lecture() {
            if let Some(course) = &self.state.selected_course {
                self.state.is_playing = true;
                EmbeddedPlayerManager::navigate_to_lecture(&course.playlistId, prev_lec.lecture_number, course.firstVideoId.as_deref());
            }
            cx.notify();
        }
    }

    pub fn toggle_play(&mut self, window: &mut Window, cx: &mut Context<Self>) {
        self.play_current_lecture(window, cx);
    }

    pub fn set_speed(&mut self, speed: f32, _window: &mut Window, cx: &mut Context<Self>) {
        self.state.playback_speed = speed;
        cx.notify();
    }

    pub fn toggle_bookmark(&mut self, course_code: String, _window: &mut Window, cx: &mut Context<Self>) {
        self.state.toggle_bookmark(&course_code);
        cx.notify();
    }

    pub fn select_dept(&mut self, dept: String, _window: &mut Window, cx: &mut Context<Self>) {
        self.state.selected_department = dept;
        cx.notify();
    }

    pub fn toggle_theme(&mut self, _window: &mut Window, cx: &mut Context<Self>) {
        self.state.toggle_theme();
        cx.notify();
    }
}

impl Render for RootView {
    fn render(&mut self, _window: &mut Window, cx: &mut Context<Self>) -> impl IntoElement {
        let theme = match self.state.theme_mode {
            theme::ThemeMode::Light => theme::Theme::light(),
            theme::ThemeMode::Dark => theme::Theme::dark(),
        };

        div()
            .flex()
            .flex_row()
            .w_full()
            .h_full()
            .bg(theme.background)
            .child(sidebar::render_sidebar(
                &self.state,
                &theme,
                cx,
            ))
            .child(
                div()
                    .flex_1()
                    .h_full()
                    .child(match self.state.current_tab {
                        Tab::Home => home_view::render_home(
                            &self.state,
                            &theme,
                            cx,
                        ).into_any_element(),
                        Tab::Courses => courses_view::render_courses(
                            &self.state,
                            &self.data.courses,
                            &self.data.departments,
                            &theme,
                            cx,
                        ).into_any_element(),
                        Tab::Player => player_view::render_player(
                            &self.state,
                            &theme,
                            cx,
                        ).into_any_element(),
                        Tab::Saved => saved_view::render_saved(
                            &self.state,
                            &self.data.courses,
                            &theme,
                            cx,
                        ).into_any_element(),
                        Tab::Handouts => handouts_view::render_handouts(
                            &self.data.handouts,
                            &theme,
                        ).into_any_element(),
                        Tab::Links => links_view::render_links(
                            &self.data.links,
                            &theme,
                        ).into_any_element(),
                    })
            )
    }
}

fn main() {
    let app = gpui_platform::application().with_assets(EmbeddedAssets);
    app.run(|cx: &mut App| {
        let bounds = Bounds {
            origin: point(px(80.0), px(60.0)),
            size: size(px(1200.0), px(780.0)),
        };

        let _ = cx.open_window(
            WindowOptions {
                window_bounds: Some(WindowBounds::Windowed(bounds)),
                titlebar: Some(TitlebarOptions {
                    title: Some("VU Course Hub".into()),
                    appears_transparent: false,
                    traffic_light_position: None,
                }),
                window_min_size: Some(size(px(800.0), px(550.0))),
                ..Default::default()
            },
            |_window, cx| cx.new(|cx| RootView::new(cx)),
        );
    });
}
