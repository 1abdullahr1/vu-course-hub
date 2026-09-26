use gpui::prelude::*;
use gpui::{Context, FontWeight, Window, div, px};
use crate::RootView;
use crate::models::Course;
use crate::state::{AppState, Tab};
use crate::theme::Theme;

pub fn render_saved(
    state: &AppState,
    all_courses: &[Course],
    theme: &Theme,
    cx: &Context<RootView>,
) -> impl IntoElement {
    let text_primary = theme.text_primary;
    let text_secondary = theme.text_secondary;
    let card_bg = theme.card_bg;
    let border_color = theme.border;
    let primary_color = theme.primary;

    let bookmarked_list: Vec<&Course> = all_courses
        .iter()
        .filter(|c| state.is_bookmarked(&c.courseCode))
        .collect();

    div()
        .flex()
        .flex_col()
        .w_full()
        .h_full()
        .p_8()
        .gap_6()
        .child(
            div()
                .flex()
                .flex_col()
                .gap_1()
                .child(
                    div()
                        .text_color(text_primary)
                        .font_weight(FontWeight::BOLD)
                        .text_2xl()
                        .child("My Learning")
                )
                .child(
                    div()
                        .text_color(text_secondary)
                        .text_sm()
                        .child(format!("{} bookmarked courses saved for quick revision", bookmarked_list.len()))
                )
        )
        .child(
            if bookmarked_list.is_empty() {
                // Empty State
                div()
                    .flex()
                    .flex_col()
                    .items_center()
                    .justify_center()
                    .p_16()
                    .rounded_xl()
                    .bg(card_bg)
                    .border_1()
                    .border_color(border_color)
                    .gap_4()
                    .child(
                        div()
                            .text_color(text_primary)
                            .font_weight(FontWeight::BOLD)
                            .text_xl()
                            .child("Start Your Learning Journey")
                    )
                    .child(
                        div()
                            .text_color(text_secondary)
                            .text_sm()
                            .text_center()
                            .max_w(px(460.0))
                            .child("Courses you study, bookmarks, and recently watched lectures will appear here.")
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
                            .child("Explore Courses")
                    )
                    .into_any_element()
            } else {
                // Grid of Saved Courses
                div()
                    .id("saved_scroll")
                    .flex()
                    .flex_wrap()
                    .gap_4()
                    .overflow_y_scroll()
                    .children(bookmarked_list.into_iter().map(|course| {
                        let course_clone = course.clone();
                        let code_for_bookmark = course.courseCode.clone();

                        div()
                            .w(px(320.0))
                            .p_5()
                            .rounded_xl()
                            .bg(card_bg)
                            .border_1()
                            .border_color(border_color)
                            .flex()
                            .flex_col()
                            .justify_between()
                            .gap_3()
                            .hover(|s| s.border_color(primary_color))
                            .child(
                                div()
                                    .flex()
                                    .flex_col()
                                    .gap_2()
                                    .child(
                                        div()
                                            .flex()
                                            .items_center()
                                            .justify_between()
                                            .child(
                                                div()
                                                    .px_3()
                                                    .py_1()
                                                    .rounded_md()
                                                    .bg(theme.chip_bg)
                                                    .text_color(primary_color)
                                                    .font_weight(FontWeight::BOLD)
                                                    .text_xs()
                                                    .child(course.courseCode.clone())
                                            )
                                            .child(
                                                div()
                                                    .px_2()
                                                    .py_1()
                                                    .rounded_md()
                                                    .cursor_pointer()
                                                    .text_color(theme.accent)
                                                    .text_xs()
                                                    .font_weight(FontWeight::BOLD)
                                                    .on_mouse_down(gpui::MouseButton::Left, cx.listener(move |this: &mut RootView, _event, window, cx| {
                                                        this.toggle_bookmark(code_for_bookmark.clone(), window, cx);
                                                    }))
                                                    .child("Remove")
                                            )
                                    )
                                    .child(
                                        div()
                                            .text_color(text_primary)
                                            .font_weight(FontWeight::BOLD)
                                            .text_base()
                                            .child(course.clean_title())
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
                                    .flex()
                                    .items_center()
                                    .justify_between()
                                    .pt_3()
                                    .border_t_1()
                                    .border_color(border_color)
                                    .child(
                                        div()
                                            .text_color(theme.text_muted)
                                            .text_xs()
                                            .child(course.videoCountText.clone().unwrap_or_else(|| "45 lectures".to_string()))
                                    )
                                    .child(
                                        div()
                                            .px_3()
                                            .py_1()
                                            .rounded_md()
                                            .bg(primary_color)
                                            .text_color(theme.white)
                                            .font_weight(FontWeight::BOLD)
                                            .text_xs()
                                            .cursor_pointer()
                                            .hover(|s| s.opacity(0.9))
                                            .on_mouse_down(gpui::MouseButton::Left, cx.listener(move |this: &mut RootView, _event, window, cx| {
                                                this.select_course(course_clone.clone(), window, cx);
                                            }))
                                            .child("Watch")
                                    )
                            )
                    }))
                    .into_any_element()
            }
        )
}
