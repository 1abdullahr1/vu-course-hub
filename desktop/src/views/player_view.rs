use gpui::prelude::*;
use gpui::{Context, Window, div, px, rgb};
use crate::models::{Course, Lecture};
use crate::state::{AppState, Tab};
use crate::theme::Theme;

pub fn render_player<V: 'static>(
    state: &AppState,
    theme: &Theme,
    on_tab_change: impl Fn(Tab, &mut Window, &mut Context<V>) + 'static + Copy,
    on_select_lecture: impl Fn(Lecture, &mut Window, &mut Context<V>) + 'static + Copy,
    on_toggle_play: impl Fn(&mut Window, &mut Context<V>) + 'static + Copy,
    on_set_speed: impl Fn(f32, &mut Window, &mut Context<V>) + 'static + Copy,
) -> impl IntoElement {
    let text_primary = theme.text_primary;
    let text_secondary = theme.text_secondary;
    let card_bg = theme.card_bg;
    let border_color = theme.border;
    let primary_color = theme.primary;

    if let (Some(course), Some(active_lecture)) = (&state.selected_course, &state.active_lecture) {
        let yt_url = if let Some(first_id) = &course.firstVideoId {
            format!("https://www.youtube.com/watch?v={}", first_id)
        } else {
            course.playlistUrl.clone().unwrap_or_else(|| "https://www.youtube.com".to_string())
        };

        div()
            .flex()
            .w_full()
            .h_full()
            .child(
                // Left Area: Video Player & Controls
                div()
                    .flex()
                    .flex_col()
                    .w(px(740.0))
                    .h_full()
                    .p_6()
                    .gap_4()
                    .border_r_1()
                    .border_color(border_color)
                    .child(
                        // Video Screen Container
                        div()
                            .w_full()
                            .h(px(416.0))
                            .rounded_xl()
                            .bg(rgb(0x000000))
                            .flex()
                            .flex_col()
                            .items_center()
                            .justify_center()
                            .gap_4()
                            .child(
                                div()
                                    .w(px(64.0))
                                    .h(px(64.0))
                                    .rounded_full()
                                    .bg(primary_color)
                                    .flex()
                                    .items_center()
                                    .justify_center()
                                    .text_color(rgb(0xFFFFFF))
                                    .font_bold()
                                    .text_xl()
                                    .cursor_pointer()
                                    .hover(|s| s.opacity(0.85))
                                    .on_mouse_down(gpui::MouseButton::Left, move |_event, window, cx| {
                                        on_toggle_play(window, cx);
                                    })
                                    .child(if state.is_playing { "Pause" } else { "Play" })
                            )
                            .child(
                                div()
                                    .text_color(rgb(0xFFFFFF))
                                    .font_bold()
                                    .text_base()
                                    .child(active_lecture.title.clone())
                            )
                    )
                    .child(
                        // Playback Control Bar
                        div()
                            .flex()
                            .items_center()
                            .justify_between()
                            .p_4()
                            .rounded_xl()
                            .bg(card_bg)
                            .border_1()
                            .border_color(border_color)
                            .child(
                                div()
                                    .flex()
                                    .items_center()
                                    .gap_3()
                                    .child(
                                        div()
                                            .px_4()
                                            .py_2()
                                            .rounded_lg()
                                            .bg(primary_color)
                                            .text_color(rgb(0xFFFFFF))
                                            .font_bold()
                                            .text_xs()
                                            .cursor_pointer()
                                            .hover(|s| s.opacity(0.9))
                                            .on_mouse_down(gpui::MouseButton::Left, move |_event, window, cx| {
                                                on_toggle_play(window, cx);
                                            })
                                            .child(if state.is_playing { "Pause" } else { "Play" })
                                    )
                                    .child(
                                        div()
                                            .text_color(text_secondary)
                                            .text_xs()
                                            .child("00:00 / 45:00")
                                    )
                            )
                            .child(
                                // Speed Controls
                                div()
                                    .flex()
                                    .items_center()
                                    .gap_2()
                                    .child(speed_button(1.0, state.playback_speed, theme, on_set_speed))
                                    .child(speed_button(1.25, state.playback_speed, theme, on_set_speed))
                                    .child(speed_button(1.5, state.playback_speed, theme, on_set_speed))
                                    .child(speed_button(2.0, state.playback_speed, theme, on_set_speed))
                            )
                            .child(
                                // Open External YouTube Button
                                div()
                                    .px_3()
                                    .py_2()
                                    .rounded_lg()
                                    .border_1()
                                    .border_color(border_color)
                                    .text_color(text_primary)
                                    .font_bold()
                                    .text_xs()
                                    .cursor_pointer()
                                    .hover(|s| s.bg(theme.surface_hover))
                                    .on_mouse_down(gpui::MouseButton::Left, move |_event, _window, _cx| {
                                        let _ = open::that(&yt_url);
                                    })
                                    .child("Open in YouTube")
                            )
                    )
                    .child(
                        // Course & Lecture Details
                        div()
                            .flex()
                            .flex_col()
                            .p_4()
                            .rounded_xl()
                            .bg(card_bg)
                            .border_1()
                            .border_color(border_color)
                            .gap_1()
                            .child(
                                div()
                                    .flex()
                                    .items_center()
                                    .gap_2()
                                    .child(
                                        div()
                                            .px_2()
                                            .py_1()
                                            .rounded_md()
                                            .bg(theme.chip_bg)
                                            .text_color(primary_color)
                                            .font_bold()
                                            .text_xs()
                                            .child(course.courseCode.clone())
                                    )
                                    .child(
                                        div()
                                            .text_color(text_secondary)
                                            .text_xs()
                                            .child(course.department.clone())
                                    )
                            )
                            .child(
                                div()
                                    .text_color(text_primary)
                                    .font_bold()
                                    .text_lg()
                                    .child(course.clean_title())
                            )
                    )
            )
            .child(
                // Right Area: Lecture Playlist
                div()
                    .flex()
                    .flex_col()
                    .flex_1()
                    .h_full()
                    .p_6()
                    .gap_4()
                    .child(
                        div()
                            .flex()
                            .items_center()
                            .justify_between()
                            .child(
                                div()
                                    .text_color(text_primary)
                                    .font_bold()
                                    .text_base()
                                    .child("Course Playlist")
                            )
                            .child(
                                div()
                                    .text_color(text_secondary)
                                    .text_xs()
                                    .child(format!("{} lectures", state.current_course_lectures.len()))
                            )
                    )
                    .child(
                        div()
                            .flex()
                            .flex_col()
                            .gap_2()
                            .overflow_y_scroll()
                            .children(state.current_course_lectures.iter().map(|lec| {
                                let is_active = lec.id == active_lecture.id;
                                let lec_clone = lec.clone();

                                div()
                                    .flex()
                                    .items_center()
                                    .justify_between()
                                    .p_3()
                                    .rounded_lg()
                                    .bg(if is_active { theme.chip_bg } else { card_bg })
                                    .border_1()
                                    .border_color(if is_active { primary_color } else { border_color })
                                    .cursor_pointer()
                                    .hover(|s| s.bg(theme.surface_hover))
                                    .on_mouse_down(gpui::MouseButton::Left, move |_event, window, cx| {
                                        on_select_lecture(lec_clone.clone(), window, cx);
                                    })
                                    .child(
                                        div()
                                            .flex()
                                            .items_center()
                                            .gap_3()
                                            .child(
                                                div()
                                                    .w(px(28.0))
                                                    .h(px(28.0))
                                                    .rounded_md()
                                                    .bg(if is_active { primary_color } else { theme.surface_hover })
                                                    .text_color(if is_active { rgb(0xFFFFFF) } else { text_secondary })
                                                    .flex()
                                                    .items_center()
                                                    .justify_center()
                                                    .text_xs()
                                                    .font_bold()
                                                    .child(format!("{:02}", lec.lecture_number))
                                            )
                                            .child(
                                                div()
                                                    .text_color(if is_active { primary_color } else { text_primary })
                                                    .font_medium()
                                                    .text_sm()
                                                    .child(lec.title.clone())
                                            )
                                    )
                                    .child(
                                        div()
                                            .text_color(theme.text_muted)
                                            .text_xs()
                                            .child(lec.duration.clone())
                                    )
                            }))
                    )
            )
    } else {
        // No Course Selected Empty State
        div()
            .flex()
            .flex_col()
            .items_center()
            .justify_center()
            .w_full()
            .h_full()
            .p_12()
            .gap_4()
            .child(
                div()
                    .text_color(text_primary)
                    .font_bold()
                    .text_xl()
                    .child("No Course Selected")
            )
            .child(
                div()
                    .text_color(text_secondary)
                    .text_sm()
                    .child("Select a course from Explore Courses or My Learning to watch lectures.")
            )
            .child(
                div()
                    .mt_2()
                    .px_6()
                    .py_3()
                    .rounded_full()
                    .bg(primary_color)
                    .text_color(rgb(0xFFFFFF))
                    .font_bold()
                    .text_sm()
                    .cursor_pointer()
                    .hover(|s| s.opacity(0.9))
                    .on_mouse_down(gpui::MouseButton::Left, move |_event, window, cx| {
                        on_tab_change(Tab::Courses, window, cx);
                    })
                    .child("Browse Courses")
            )
    }
}

fn speed_button<V: 'static>(
    speed: f32,
    current_speed: f32,
    theme: &Theme,
    on_set_speed: impl Fn(f32, &mut Window, &mut Context<V>) + 'static + Copy,
) -> impl IntoElement {
    let is_selected = (current_speed - speed).abs() < 0.01;
    div()
        .px_2()
        .py_1()
        .rounded_md()
        .bg(if is_selected { theme.primary } else { theme.surface_hover })
        .text_color(if is_selected { rgb(0xFFFFFF) } else { theme.text_secondary })
        .text_xs()
        .font_bold()
        .cursor_pointer()
        .hover(|s| s.opacity(0.85))
        .on_mouse_down(gpui::MouseButton::Left, move |_event, window, cx| {
            on_set_speed(speed, window, cx);
        })
        .child(format!("{}x", speed))
}
