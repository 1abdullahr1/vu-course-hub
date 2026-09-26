use gpui::prelude::*;
use gpui::{ClickEvent, Context, Window, div, px, rgb};
use crate::state::Tab;
use crate::theme::Theme;

pub fn render_sidebar<V: 'static>(
    current_tab: Tab,
    theme: &Theme,
    on_tab_change: impl Fn(Tab, &mut Window, &mut Context<V>) + 'static + Copy,
    on_toggle_theme: impl Fn(&mut Window, &mut Context<V>) + 'static + Copy,
) -> impl IntoElement {
    let bg_color = theme.surface;
    let border_color = theme.border;
    let text_primary = theme.text_primary;
    let text_secondary = theme.text_secondary;
    let primary_color = theme.primary;

    div()
        .flex()
        .flex_col()
        .w(px(260.0))
        .h_full()
        .bg(bg_color)
        .border_r_1()
        .border_color(border_color)
        .p_4()
        .justify_between()
        .child(
            div()
                .flex()
                .flex_col()
                .gap_4()
                .child(
                    // Brand Header
                    div()
                        .flex()
                        .items_center()
                        .gap_3()
                        .pb_4()
                        .border_b_1()
                        .border_color(border_color)
                        .child(
                            div()
                                .w(px(40.0))
                                .h(px(40.0))
                                .rounded_lg()
                                .bg(primary_color)
                                .flex()
                                .items_center()
                                .justify_center()
                                .text_color(rgb(0xFFFFFF))
                                .font_bold()
                                .text_lg()
                                .child("VU")
                        )
                        .child(
                            div()
                                .flex()
                                .flex_col()
                                .child(
                                    div()
                                        .text_color(text_primary)
                                        .font_bold()
                                        .text_base()
                                        .child("VU Course Hub")
                                )
                                .child(
                                    div()
                                        .text_color(text_secondary)
                                        .text_xs()
                                        .child("Virtual University of Pakistan")
                                )
                        )
                )
                .child(
                    // Navigation List
                    div()
                        .flex()
                        .flex_col()
                        .gap_1()
                        .child(nav_item("Learn", Tab::Home, current_tab, theme, on_tab_change))
                        .child(nav_item("Explore Courses", Tab::Courses, current_tab, theme, on_tab_change))
                        .child(nav_item("Lecture Player", Tab::Player, current_tab, theme, on_tab_change))
                        .child(nav_item("My Learning", Tab::Saved, current_tab, theme, on_tab_change))
                        .child(nav_item("Handouts", Tab::Handouts, current_tab, theme, on_tab_change))
                        .child(nav_item("VU Portals & Links", Tab::Links, current_tab, theme, on_tab_change))
                )
        )
        .child(
            // Bottom Area: Theme & Version
            div()
                .flex()
                .flex_col()
                .gap_2()
                .pt_3()
                .border_t_1()
                .border_color(border_color)
                .child(
                    div()
                        .flex()
                        .items_center()
                        .justify_between()
                        .p_2()
                        .rounded_md()
                        .bg(theme.surface_hover)
                        .cursor_pointer()
                        .on_mouse_down(gpui::MouseButton::Left, move |_event, window, cx| {
                            on_toggle_theme(window, cx);
                        })
                        .child(
                            div()
                                .text_color(text_secondary)
                                .text_xs()
                                .font_bold()
                                .child("Theme Mode")
                        )
                        .child(
                            div()
                                .text_color(primary_color)
                                .text_xs()
                                .font_bold()
                                .child(match theme.mode {
                                    crate::theme::ThemeMode::Light => "Light",
                                    crate::theme::ThemeMode::Dark => "Dark",
                                })
                        )
                )
                .child(
                    div()
                        .text_center()
                        .text_color(theme.text_muted)
                        .text_xs()
                        .child("Native GPUI Desktop v1.0")
                )
        )
}

fn nav_item<V: 'static>(
    title: &'static str,
    tab: Tab,
    current_tab: Tab,
    theme: &Theme,
    on_tab_change: impl Fn(Tab, &mut Window, &mut Context<V>) + 'static + Copy,
) -> impl IntoElement {
    let is_selected = current_tab == tab;
    let bg = if is_selected {
        theme.chip_bg
    } else {
        theme.surface
    };
    let text_color = if is_selected {
        theme.primary
    } else {
        theme.text_secondary
    };

    div()
        .flex()
        .items_center()
        .px_3()
        .py_2()
        .rounded_md()
        .bg(bg)
        .cursor_pointer()
        .hover(|s| s.bg(theme.surface_hover))
        .on_mouse_down(gpui::MouseButton::Left, move |_event, window, cx| {
            on_tab_change(tab, window, cx);
        })
        .child(
            div()
                .text_color(text_color)
                .font_weight(if is_selected { gpui::FontWeight::BOLD } else { gpui::FontWeight::NORMAL })
                .text_sm()
                .child(title)
        )
}
