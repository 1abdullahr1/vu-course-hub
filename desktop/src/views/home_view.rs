use gpui::prelude::*;
use gpui::{Context, Window, div, px, rgb};
use crate::state::{AppState, Tab};
use crate::theme::Theme;

pub fn render_home<V: 'static>(
    state: &AppState,
    theme: &Theme,
    on_tab_change: impl Fn(Tab, &mut Window, &mut Context<V>) + 'static + Copy,
    on_select_dept: impl Fn(String, &mut Window, &mut Context<V>) + 'static + Copy,
) -> impl IntoElement {
    let text_primary = theme.text_primary;
    let text_secondary = theme.text_secondary;
    let card_bg = theme.card_bg;
    let border_color = theme.border;
    let primary_color = theme.primary;

    div()
        .flex()
        .flex_col()
        .w_full()
        .h_full()
        .p_8()
        .gap_8()
        .child(
            // Welcome Header
            div()
                .flex()
                .flex_col()
                .gap_1()
                .child(
                    div()
                        .text_color(text_primary)
                        .font_bold()
                        .text_2xl()
                        .child("Welcome to VU Course Hub")
                )
                .child(
                    div()
                        .text_color(text_secondary)
                        .text_sm()
                        .child("Virtual University of Pakistan Academic Lectures & Handouts")
                )
        )
        .child(
            if let Some(course) = &state.selected_course {
                // Active Course Resume Card
                div()
                    .flex()
                    .flex_col()
                    .p_6()
                    .rounded_xl()
                    .bg(card_bg)
                    .border_1()
                    .border_color(border_color)
                    .gap_4()
                    .child(
                        div()
                            .flex()
                            .items_center()
                            .justify_between()
                            .child(
                                div()
                                    .flex()
                                    .items_center()
                                    .gap_3()
                                    .child(
                                        div()
                                            .px_3()
                                            .py_1()
                                            .rounded_md()
                                            .bg(theme.chip_bg)
                                            .text_color(primary_color)
                                            .font_bold()
                                            .text_sm()
                                            .child(course.courseCode.clone())
                                    )
                                    .child(
                                        div()
                                            .text_color(text_primary)
                                            .font_bold()
                                            .text_lg()
                                            .child(course.clean_title())
                                    )
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
                            .text_color(text_secondary)
                            .text_sm()
                            .child(if let Some(active) = &state.active_lecture {
                                format!("Current Lecture: {}", active.title)
                            } else {
                                "Ready to watch lectures".to_string()
                            })
                    )
                    .child(
                        div()
                            .flex()
                            .items_center()
                            .gap_4()
                            .child(
                                div()
                                    .px_5()
                                    .py_2()
                                    .rounded_lg()
                                    .bg(primary_color)
                                    .text_color(rgb(0xFFFFFF))
                                    .font_bold()
                                    .text_sm()
                                    .cursor_pointer()
                                    .hover(|s| s.opacity(0.9))
                                    .on_mouse_down(gpui::MouseButton::Left, move |_event, window, cx| {
                                        on_tab_change(Tab::Player, window, cx);
                                    })
                                    .child("Resume Watching")
                            )
                            .child(
                                div()
                                    .px_4()
                                    .py_2()
                                    .rounded_lg()
                                    .border_1()
                                    .border_color(border_color)
                                    .text_color(text_primary)
                                    .text_sm()
                                    .cursor_pointer()
                                    .hover(|s| s.bg(theme.surface_hover))
                                    .on_mouse_down(gpui::MouseButton::Left, move |_event, window, cx| {
                                        on_tab_change(Tab::Courses, window, cx);
                                    })
                                    .child("Browse All Courses")
                            )
                    )
            } else {
                // Empty State with Call to Action
                div()
                    .flex()
                    .flex_col()
                    .items_center()
                    .justify_center()
                    .p_12()
                    .rounded_xl()
                    .bg(card_bg)
                    .border_1()
                    .border_color(border_color)
                    .gap_4()
                    .child(
                        div()
                            .text_color(text_primary)
                            .font_bold()
                            .text_xl()
                            .child("Start Your Learning Journey")
                    )
                    .child(
                        div()
                            .text_color(text_secondary)
                            .text_sm()
                            .text_center()
                            .max_w(px(460.0))
                            .child("You have not started any course yet. Browse through 190+ courses across Computer Science, Management, Math, and Sciences.")
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
                            .child("Explore 190+ Courses")
                    )
            }
        )
        .child(
            // Popular Faculties / Departments Shortcuts
            div()
                .flex()
                .flex_col()
                .gap_3()
                .child(
                    div()
                        .text_color(text_primary)
                        .font_bold()
                        .text_base()
                        .child("Browse by Faculty")
                )
                .child(
                    div()
                        .flex()
                        .flex_wrap()
                        .gap_3()
                        .child(dept_card("Computer Science", theme, on_tab_change, on_select_dept))
                        .child(dept_card("Management", theme, on_tab_change, on_select_dept))
                        .child(dept_card("Accounting", theme, on_tab_change, on_select_dept))
                        .child(dept_card("Mathematics", theme, on_tab_change, on_select_dept))
                        .child(dept_card("Economics", theme, on_tab_change, on_select_dept))
                        .child(dept_card("Bioinformatics", theme, on_tab_change, on_select_dept))
                )
        )
}

fn dept_card<V: 'static>(
    name: &'static str,
    theme: &Theme,
    on_tab_change: impl Fn(Tab, &mut Window, &mut Context<V>) + 'static + Copy,
    on_select_dept: impl Fn(String, &mut Window, &mut Context<V>) + 'static + Copy,
) -> impl IntoElement {
    div()
        .px_4()
        .py_3()
        .rounded_lg()
        .bg(theme.card_bg)
        .border_1()
        .border_color(theme.border)
        .cursor_pointer()
        .hover(|s| s.bg(theme.surface_hover))
        .on_mouse_down(gpui::MouseButton::Left, move |_event, window, cx| {
            on_select_dept(name.to_string(), window, cx);
            on_tab_change(Tab::Courses, window, cx);
        })
        .child(
            div()
                .text_color(theme.text_primary)
                .font_medium()
                .text_sm()
                .child(name)
        )
}
