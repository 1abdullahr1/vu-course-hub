#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

mod data;
mod models;
mod state;
mod theme;
mod views;

use gpui::prelude::*;
use gpui::{
    App, Bounds, Context, Render, TitlebarOptions, Window, WindowBounds, WindowOptions,
    div, point, px, size,
};
use crate::data::CourseData;
use crate::models::{Course, Lecture};
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
        cx.notify();
    }

    pub fn select_course(&mut self, course: Course, _window: &mut Window, cx: &mut Context<Self>) {
        let lectures = CourseData::generate_lectures(&course);
        self.state.set_active_course(course, lectures);
        cx.notify();
    }

    pub fn select_lecture(&mut self, lecture: Lecture, _window: &mut Window, cx: &mut Context<Self>) {
        self.state.active_lecture = Some(lecture);
        cx.notify();
    }

    pub fn toggle_play(&mut self, _window: &mut Window, cx: &mut Context<Self>) {
        self.state.is_playing = !self.state.is_playing;
        cx.notify();
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
    let app = gpui_platform::application();
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
