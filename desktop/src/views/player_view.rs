use gpui::prelude::*;
use gpui::{Context, FontWeight, div, px};
use crate::RootView;
use crate::state::{AppState, Tab};
use crate::theme::Theme;

pub fn render_player(
    state: &AppState,
    theme: &Theme,
    cx: &Context<RootView>,
) -> impl IntoElement {
    let text_primary = theme.text_primary;
    let text_secondary = theme.text_secondary;
    let card_bg = theme.card_bg;
    let border_color = theme.border;
    let primary_color = theme.primary;

    if let (Some(course), Some(active_lecture)) = (&state.selected_course, &state.active_lecture) {
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
                            .bg(theme.black)
                            .border_1()
                            .border_color(border_color)
                            .flex()
                            .flex_col()
                            .items_center()
                            .justify_center()
                            .gap_4()
                            .cursor_pointer()
                            .hover(|s| s.border_color(primary_color))
                            .on_mouse_down(gpui::MouseButton::Left, cx.listener(|this: &mut RootView, _event, window, cx| {
                                this.play_current_lecture(window, cx);
                            }))
                            .child(
                                div()
                                    .w(px(72.0))
                                    .h(px(72.0))
                                    .rounded_full()
                                    .bg(primary_color)
                                    .flex()
                                    .items_center()
                                    .justify_center()
                                    .text_color(theme.white)
                                    .font_weight(FontWeight::BOLD)
                                    .text_xl()
                                    .hover(|s| s.opacity(0.85))
                                    .child("PLAY")
                            )
                            .child(
                                div()
                                    .flex()
                                    .flex_col()
                                    .items_center()
                                    .gap_1()
                                    .child(
                                        div()
                                            .text_color(theme.white)
                                            .font_weight(FontWeight::BOLD)
                                            .text_lg()
                                            .child(active_lecture.title.clone())
                                    )
                                    .child(
                                        div()
                                            .text_color(text_secondary)
                                            .text_xs()
                                            .child("In-App HD Video Player")
                                    )
                            )
                            .child(
                                div()
                                    .px_3()
                                    .py_1()
                                    .rounded_full()
                                    .bg(if state.is_playing { theme.chip_bg } else { theme.surface_hover })
                                    .text_color(if state.is_playing { primary_color } else { text_secondary })
                                    .text_xs()
                                    .font_weight(FontWeight::BOLD)
                                    .child(if state.is_playing {
                                        "In-App HD Player Active"
                                    } else {
                                        "Ready to Play • Virtual University Official HD"
                                    })
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
                                    .gap_2()
                                    .child(
                                        div()
                                            .px_4()
                                            .py_2()
                                            .rounded_lg()
                                            .bg(primary_color)
                                            .text_color(theme.white)
                                            .font_weight(FontWeight::BOLD)
                                            .text_xs()
                                            .cursor_pointer()
                                            .hover(|s| s.opacity(0.9))
                                            .on_mouse_down(gpui::MouseButton::Left, cx.listener(|this: &mut RootView, _event, window, cx| {
                                                this.play_current_lecture(window, cx);
                                            }))
                                            .child("Play Video")
                                    )
                                    .child(
                                        div()
                                            .px_3()
                                            .py_2()
                                            .rounded_lg()
                                            .border_1()
                                            .border_color(border_color)
                                            .text_color(text_primary)
                                            .font_weight(FontWeight::MEDIUM)
                                            .text_xs()
                                            .cursor_pointer()
                                            .hover(|s| s.bg(theme.surface_hover))
                                            .on_mouse_down(gpui::MouseButton::Left, cx.listener(|this: &mut RootView, _event, window, cx| {
                                                this.prev_lecture(window, cx);
                                            }))
                                            .child("Prev")
                                    )
                                    .child(
                                        div()
                                            .px_3()
                                            .py_2()
                                            .rounded_lg()
                                            .border_1()
                                            .border_color(border_color)
                                            .text_color(text_primary)
                                            .font_weight(FontWeight::MEDIUM)
                                            .text_xs()
                                            .cursor_pointer()
                                            .hover(|s| s.bg(theme.surface_hover))
                                            .on_mouse_down(gpui::MouseButton::Left, cx.listener(|this: &mut RootView, _event, window, cx| {
                                                this.next_lecture(window, cx);
                                            }))
                                            .child("Next")
                                    )
                            )
                            .child(
                                div()
                                    .px_3()
                                    .py_2()
                                    .rounded_lg()
                                    .border_1()
                                    .border_color(border_color)
                                    .text_color(text_secondary)
                                    .font_weight(FontWeight::BOLD)
                                    .text_xs()
                                    .cursor_pointer()
                                    .hover(|s| s.bg(theme.surface_hover))
                                    .on_mouse_down(gpui::MouseButton::Left, cx.listener(|this: &mut RootView, _event, window, cx| {
                                        this.open_current_in_browser(window, cx);
                                    }))
                                    .child("Open in Browser")
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
                                            .font_weight(FontWeight::BOLD)
                                            .text_xs()
                                            .child(course.display_code())
                                    )
                                    .child(
                                        div()
                                            .text_color(text_secondary)
                                            .text_xs()
                                            .child(course.dept().to_string())
                                    )
                            )
                            .child(
                                div()
                                    .text_color(text_primary)
                                    .font_weight(FontWeight::BOLD)
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
                                    .font_weight(FontWeight::BOLD)
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
                            .id("playlist_scroll")
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
                                    .on_mouse_down(gpui::MouseButton::Left, cx.listener(move |this: &mut RootView, _event, window, cx| {
                                        this.select_lecture(lec_clone.clone(), window, cx);
                                    }))
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
                                                    .text_color(if is_active { theme.white } else { text_secondary })
                                                    .flex()
                                                    .items_center()
                                                    .justify_center()
                                                    .text_xs()
                                                    .font_weight(FontWeight::BOLD)
                                                    .child(format!("{:02}", lec.lecture_number))
                                            )
                                            .child(
                                                div()
                                                    .text_color(if is_active { primary_color } else { text_primary })
                                                    .font_weight(FontWeight::MEDIUM)
                                                    .text_sm()
                                                    .child(lec.title.clone())
                                            )
                                    )
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
                                                    .bg(if is_active { primary_color } else { theme.surface_hover })
                                                    .text_color(if is_active { theme.white } else { text_secondary })
                                                    .text_xs()
                                                    .font_weight(FontWeight::BOLD)
                                                    .child(if is_active { "Playing" } else { "Play" })
                                            )
                                    )
                            }))
                    )
            )
            .into_any_element()
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
                    .font_weight(FontWeight::BOLD)
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
                    .text_color(theme.white)
                    .font_weight(FontWeight::BOLD)
                    .text_sm()
                    .cursor_pointer()
                    .hover(|s| s.opacity(0.9))
                    .on_mouse_down(gpui::MouseButton::Left, cx.listener(|this: &mut RootView, _event, window, cx| {
                        this.set_tab(Tab::Courses, window, cx);
                    }))
                    .child("Browse Courses")
            )
            .into_any_element()
    }
}
