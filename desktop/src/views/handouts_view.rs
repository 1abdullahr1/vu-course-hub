use gpui::prelude::*;
use gpui::{FontWeight, div, px};
use crate::models::Handout;
use crate::theme::Theme;

pub fn render_handouts(
    handouts: &[Handout],
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
                        .child("VU Course Handouts")
                )
                .child(
                    div()
                        .text_color(text_secondary)
                        .text_sm()
                        .child("Download official PDF handouts directly in your web browser (Chrome/Edge).")
                )
        )
        .child(
            // Handouts Table / Cards
            div()
                .id("handouts_scroll")
                .flex()
                .flex_col()
                .gap_3()
                .overflow_y_scroll()
                .children(handouts.iter().take(60).map(|item| {
                    let query = format!("{} {} VU handouts filetype:pdf", item.course_code, item.title);
                    let encoded_url = format!("https://www.google.com/search?q={}", urlencoding::encode(&query));

                    div()
                        .flex()
                        .items_center()
                        .justify_between()
                        .p_4()
                        .rounded_xl()
                        .bg(card_bg)
                        .border_1()
                        .border_color(border_color)
                        .hover(|s| s.border_color(primary_color))
                        .child(
                            div()
                                .flex()
                                .items_center()
                                .gap_4()
                                .child(
                                    div()
                                        .px_3()
                                        .py_1()
                                        .rounded_md()
                                        .bg(theme.chip_bg)
                                        .text_color(primary_color)
                                        .font_weight(FontWeight::BOLD)
                                        .text_sm()
                                        .child(item.course_code.clone())
                                )
                                .child(
                                    div()
                                        .flex()
                                        .flex_col()
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
                                                .child(item.department.clone())
                                        )
                                )
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
                                    let _ = open::that(&encoded_url);
                                })
                                .child("Download PDF")
                        )
                }))
        )
}
