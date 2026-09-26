use gpui::prelude::*;
use gpui::{FontWeight, div, px};
use crate::models::ResourceLink;
use crate::theme::Theme;

pub fn render_links(
    links: &[ResourceLink],
    theme: &Theme,
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
                        .child("Virtual University Portals & Links")
                )
                .child(
                    div()
                        .text_color(text_secondary)
                        .text_sm()
                        .child("Quick official access to LMS, Date Sheet, Digital Library, and student notices.")
                )
        )
        .child(
            div()
                .id("links_scroll")
                .flex()
                .flex_wrap()
                .gap_4()
                .overflow_y_scroll()
                .children(links.iter().map(|item| {
                    let url = item.url.clone();

                    div()
                        .w(px(360.0))
                        .p_5()
                        .rounded_xl()
                        .bg(card_bg)
                        .border_1()
                        .border_color(border_color)
                        .flex()
                        .flex_col()
                        .justify_between()
                        .gap_4()
                        .hover(|s| s.border_color(primary_color))
                        .child(
                            div()
                                .flex()
                                .flex_col()
                                .gap_2()
                                .child(
                                    div()
                                        .text_color(text_primary)
                                        .font_weight(FontWeight::BOLD)
                                        .text_base()
                                        .child(item.title.clone())
                                )
                                .child(
                                    div()
                                        .text_color(text_secondary)
                                        .text_xs()
                                        .child(item.description.clone())
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
                                        .child(if item.url.len() > 30 {
                                            format!("{}...", &item.url[..30])
                                        } else {
                                            item.url.clone()
                                        })
                                )
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
                                        .on_click(move |_event, _window, _cx| {
                                            let _ = open::that(&url);
                                        })
                                        .child("Open Portal")
                                )
                        )
                }))
        )
}
