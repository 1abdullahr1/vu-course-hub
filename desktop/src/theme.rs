use gpui::{Hsla, Rgba, rgb, rgba};

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum ThemeMode {
    Light,
    Dark,
}

pub struct Theme {
    pub mode: ThemeMode,
    pub background: Hsla,
    pub surface: Hsla,
    pub surface_hover: Hsla,
    pub surface_variant: Hsla,
    pub border: Hsla,
    pub text_primary: Hsla,
    pub text_secondary: Hsla,
    pub text_muted: Hsla,
    pub primary: Hsla,
    pub primary_hover: Hsla,
    pub accent: Hsla,
    pub card_bg: Hsla,
    pub chip_bg: Hsla,
}

impl Theme {
    pub fn light() -> Self {
        Self {
            mode: ThemeMode::Light,
            background: rgb(0xF8F9FA).into(),
            surface: rgb(0xFFFFFF).into(),
            surface_hover: rgb(0xF1F3F5).into(),
            surface_variant: rgb(0xEEF2F6).into(),
            border: rgb(0xE2E8F0).into(),
            text_primary: rgb(0x0F172A).into(),
            text_secondary: rgb(0x475569).into(),
            text_muted: rgb(0x94A3B8).into(),
            primary: rgb(0x0059CC).into(),
            primary_hover: rgb(0x0047A5).into(),
            accent: rgb(0xFAA826).into(),
            card_bg: rgb(0xFFFFFF).into(),
            chip_bg: rgb(0xE8EEF8).into(),
        }
    }

    pub fn dark() -> Self {
        Self {
            mode: ThemeMode::Dark,
            background: rgb(0x0F1115).into(),
            surface: rgb(0x181A20).into(),
            surface_hover: rgb(0x22252D).into(),
            surface_variant: rgb(0x2A2E39).into(),
            border: rgb(0x2C303B).into(),
            text_primary: rgb(0xF8FAFC).into(),
            text_secondary: rgb(0x94A3B8).into(),
            text_muted: rgb(0x64748B).into(),
            primary: rgb(0x2563EB).into(),
            primary_hover: rgb(0x3B82F6).into(),
            accent: rgb(0xFAA826).into(),
            card_bg: rgb(0x181A20).into(),
            chip_bg: rgb(0x1E2433).into(),
        }
    }
}
