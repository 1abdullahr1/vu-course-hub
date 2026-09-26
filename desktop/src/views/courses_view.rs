use gpui::prelude::*;
use gpui::{Context, Window, div, px, rgb};
use crate::models::Course;
use crate::state::AppState;
use crate::theme::Theme;

pub fn render_courses<V: 'static>(
    state: &AppState,
    courses: &[Course],
    departments: &[String],
    theme: &Theme,
    on_select_course: impl Fn(Course, &mut Window, &mut Context<V>) + 'static + Copy,
    on_toggle_bookmark: impl Fn(String, &mut Window, &mut Context<V>) + 'static + Copy,
    on_select_dept: impl Fn(String, &mut Window, &mut Context<V>) + 'static + Copy,
) -> impl IntoElement {
    let text_primary = theme.text_primary;
    let text_secondary = theme.text_secondary;
    let border_color = theme.border;
    let primary_color = theme.primary;
    let card_bg = theme.card_bg;

    // Filter courses based on department and search
    let dept_filter = state.selected_department.as_str();
    let search_lower = state.course_search_query.trim().to_lowercase();

    let filtered_courses: Vec<&Course> = courses
        .iter()
        .filter(|c| {
            let dept_match = dept_filter == "All" || c.department.eq_ignore_ascii_case(dept_filter);
            let search_match = search_lower.is_empty()
                || c.courseCode.to_lowercase().contains(&search_lower)
                || c.title.to_lowercase().contains(&search_lower)
                || c.department.to_lowercase().contains(&search_lower);
            dept_match && search_match
        })
        .collect();

    div()
        .flex()
        .flex_col()
        .w_full()
        .h_full()
        .p_8()
        .gap_6()
        .child(
            // Title & Count
            div()
                .flex()
                .items_center()
                .justify_between()
                .child(
                    div()
                        .flex()
                        .flex_col()
                        .gap_1()
                        .child(
                            div()
                                .text_color(text_primary)
                                .font_bold()
                                .text_2xl()
                                .child("Explore All Courses")
                        )
                        .child(
                            div()
                                .text_color(text_secondary)
                                .text_sm()
                                .child(format!("Showing {} courses available in catalog", filtered_courses.len()))
                        )
                )
        )
        .child(
            // Department Filter Chips
            div()
                .flex()
                .flex_wrap()
                .gap_2()
                .children(departments.iter().map(|dept| {
                    let is_active = dept == &state.selected_department;
                    let dept_name = dept.clone();
                    div()
                        .px_3()
                        .py_1()
                        .rounded_full()
                        .bg(if is_active { primary_color } else { theme.surface_hover })
                        .text_color(if is_active { rgb(0xFFFFFF) } else { text_secondary })
                        .text_xs()
                        .font_weight(if is_active { gpui::FontWeight::BOLD } else { gpui::FontWeight::NORMAL })
                        .cursor_pointer()
                        .hover(|s| s.opacity(0.85))
                        .on_mouse_down(gpui::MouseButton::Left, move |_event, window, cx| {
                            on_select_dept(dept_name.clone(), window, cx);
                        })
                        .child(dept.clone())
                }))
        )
        .child(
            // Course Cards Grid
            div()
                .flex()
                .flex_wrap()
                .gap_4()
                .overflow_y_scroll()
                .children(filtered_courses.into_iter().take(60).map(|course| {
                    let course_clone = course.clone();
                    let course_code = course.courseCode.clone();
                    let is_saved = state.is_bookmarked(&course_code);
                    let code_for_bookmark = course_code.clone();

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
                                                .font_bold()
                                                .text_xs()
                                                .child(course.courseCode.clone())
                                        )
                                        .child(
                                            div()
                                                .px_2()
                                                .py_1()
                                                .rounded_md()
                                                .cursor_pointer()
                                                .text_color(if is_saved { theme.accent } else { theme.text_muted })
                                                .text_xs()
                                                .font_bold()
                                                .on_mouse_down(gpui::MouseButton::Left, move |_event, window, cx| {
                                                    on_toggle_bookmark(code_for_bookmark.clone(), window, cx);
                                                })
                                                .child(if is_saved { "Saved" } else { "Save" })
                                        )
                                )
                                .child(
                                    div()
                                        .text_color(text_primary)
                                        .font_bold()
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
                                        .text_color(rgb(0xFFFFFF))
                                        .font_bold()
                                        .text_xs()
                                        .cursor_pointer()
                                        .hover(|s| s.opacity(0.9))
                                        .on_mouse_down(gpui::MouseButton::Left, move |_event, window, cx| {
                                            on_select_course(course_clone.clone(), window, cx);
                                        })
                                        .child("Watch")
                                )
                        )
                }))
        )
}
