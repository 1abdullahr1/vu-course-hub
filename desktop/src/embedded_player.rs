use std::cell::RefCell;
use std::sync::mpsc;
use webview2_com::Microsoft::Web::WebView2::Win32::*;
use webview2_com::{
    CreateCoreWebView2ControllerCompletedHandler,
    CreateCoreWebView2EnvironmentCompletedHandler,
};
use windows::core::PCWSTR;
use windows::Win32::Foundation::{E_POINTER, HWND, RECT};

thread_local! {
    static EMBEDDED_PLAYER: RefCell<Option<EmbeddedPlayer>> = const { RefCell::new(None) };
}

pub struct EmbeddedPlayer {
    controller: ICoreWebView2Controller,
    webview: ICoreWebView2,
    hwnd: HWND,
    current_url: String,
}

impl Drop for EmbeddedPlayer {
    fn drop(&mut self) {
        unsafe {
            let _ = self.controller.Close();
        }
    }
}

pub fn get_embed_url(playlist_id: &str, lecture_index: i32, first_video_id: Option<&str>) -> String {
    let clean_pid = playlist_id.trim();
    if lecture_index <= 1 {
        if let Some(vid) = first_video_id.filter(|s| !s.trim().is_empty()) {
            if clean_pid.is_empty() {
                return format!(
                    "https://www.youtube-nocookie.com/embed/{}?autoplay=1&enablejsapi=1&rel=0",
                    vid.trim()
                );
            } else {
                return format!(
                    "https://www.youtube-nocookie.com/embed/{}?autoplay=1&enablejsapi=1&rel=0&list={}",
                    vid.trim(),
                    clean_pid
                );
            }
        }
    }

    let zero_based_index = (lecture_index - 1).max(0);
    format!(
        "https://www.youtube-nocookie.com/embed/videoseries?list={}&index={}&autoplay=1&enablejsapi=1&rel=0",
        clean_pid,
        zero_based_index
    )
}

unsafe extern "system" fn enum_windows_callback(
    hwnd: windows_sys::Win32::Foundation::HWND,
    lparam: windows_sys::Win32::Foundation::LPARAM,
) -> windows_sys::Win32::Foundation::BOOL {
    let mut pid: u32 = 0;
    windows_sys::Win32::UI::WindowsAndMessaging::GetWindowThreadProcessId(hwnd, &mut pid);
    if pid == std::process::id()
        && windows_sys::Win32::UI::WindowsAndMessaging::IsWindowVisible(hwnd) != 0
    {
        let result_ptr = lparam as *mut windows_sys::Win32::Foundation::HWND;
        *result_ptr = hwnd;
        0
    } else {
        1
    }
}

pub fn find_main_window_hwnd() -> Option<HWND> {
    let mut found_hwnd: windows_sys::Win32::Foundation::HWND = std::ptr::null_mut();
    unsafe {
        windows_sys::Win32::UI::WindowsAndMessaging::EnumWindows(
            Some(enum_windows_callback),
            &mut found_hwnd as *mut _ as isize,
        );
    }
    if !found_hwnd.is_null() {
        Some(HWND(found_hwnd as *mut _))
    } else {
        let fg = unsafe { windows_sys::Win32::UI::WindowsAndMessaging::GetForegroundWindow() };
        if !fg.is_null() {
            Some(HWND(fg as *mut _))
        } else {
            None
        }
    }
}

impl EmbeddedPlayer {
    pub fn init(hwnd: HWND) -> std::result::Result<Self, String> {
        unsafe {
            let _ = windows::Win32::System::Com::CoInitializeEx(
                None,
                windows::Win32::System::Com::COINIT_APARTMENTTHREADED,
            );
        }

        let user_data = std::env::temp_dir().join("vu_course_hub_wv2");
        let _ = std::fs::create_dir_all(&user_data);
        unsafe {
            std::env::set_var("WEBVIEW2_USER_DATA_FOLDER", &user_data);
            std::env::set_var(
                "WEBVIEW2_ADDITIONAL_BROWSER_ARGUMENTS",
                "--autoplay-policy=no-user-gesture-required",
            );
        }

        let (tx_env, rx_env) = mpsc::channel();
        let env_res = CreateCoreWebView2EnvironmentCompletedHandler::wait_for_async_operation(
            Box::new(|handler| unsafe {
                CreateCoreWebView2Environment(&handler).map_err(webview2_com::Error::WindowsError)
            }),
            Box::new(move |error_code, env| {
                error_code?;
                let _ = tx_env.send(env.ok_or_else(|| windows::core::Error::from(E_POINTER)));
                Ok(())
            }),
        );

        if let Err(e) = env_res {
            return Err(format!("Environment init error: {:?}", e));
        }

        let environment = match rx_env.recv() {
            Ok(Ok(env)) => env,
            Ok(Err(e)) => return Err(format!("Environment creation error: {:?}", e)),
            Err(e) => return Err(format!("Channel receive error: {:?}", e)),
        };

        let (tx_ctrl, rx_ctrl) = mpsc::channel();
        let ctrl_res = CreateCoreWebView2ControllerCompletedHandler::wait_for_async_operation(
            Box::new(move |handler| unsafe {
                environment
                    .CreateCoreWebView2Controller(hwnd, &handler)
                    .map_err(webview2_com::Error::WindowsError)
            }),
            Box::new(move |error_code, ctrl| {
                error_code?;
                let _ = tx_ctrl.send(ctrl.ok_or_else(|| windows::core::Error::from(E_POINTER)));
                Ok(())
            }),
        );

        if let Err(e) = ctrl_res {
            return Err(format!("Controller init error: {:?}", e));
        }

        let controller = match rx_ctrl.recv() {
            Ok(Ok(ctrl)) => ctrl,
            Ok(Err(e)) => return Err(format!("Controller creation error: {:?}", e)),
            Err(e) => return Err(format!("Channel receive error: {:?}", e)),
        };

        let webview = match unsafe { controller.CoreWebView2() } {
            Ok(wv) => wv,
            Err(e) => return Err(format!("Failed to retrieve CoreWebView2: {:?}", e)),
        };

        let player = Self {
            controller,
            webview,
            hwnd,
            current_url: String::new(),
        };

        player.update_bounds();

        Ok(player)
    }

    pub fn update_bounds(&self) {
        let dpi = unsafe {
            windows_sys::Win32::UI::HiDpi::GetDpiForWindow(self.hwnd.0 as _)
        };
        let scale = if dpi > 0 { dpi as f32 / 96.0 } else { 1.0 };

        let left = (284.0 * scale).round() as i32;
        let top = (24.0 * scale).round() as i32;
        let width = (692.0 * scale).round() as i32;
        let height = (416.0 * scale).round() as i32;

        let rect = RECT {
            left,
            top,
            right: left + width,
            bottom: top + height,
        };

        unsafe {
            let _ = self.controller.SetBounds(rect);
        }
    }

    pub fn show(&self) {
        self.update_bounds();
        unsafe {
            let _ = self.controller.SetIsVisible(true);
        }
    }

    pub fn hide(&self) {
        unsafe {
            let _ = self.controller.SetIsVisible(false);
        }
    }

    pub fn navigate(&mut self, url: &str) {
        if self.current_url == url {
            self.show();
            return;
        }
        self.current_url = url.to_string();
        self.show();
        let url_wide: Vec<u16> = url.encode_utf16().chain(Some(0)).collect();
        unsafe {
            let _ = self.webview.Navigate(PCWSTR(url_wide.as_ptr()));
        }
    }
}

pub struct EmbeddedPlayerManager;

impl EmbeddedPlayerManager {
    pub fn navigate_to_lecture(playlist_id: &str, lecture_index: i32, first_video_id: Option<&str>) {
        let url = get_embed_url(playlist_id, lecture_index, first_video_id);
        EMBEDDED_PLAYER.with(|cell| {
            let mut opt = cell.borrow_mut();
            if opt.is_none() {
                if let Some(hwnd) = find_main_window_hwnd() {
                    match EmbeddedPlayer::init(hwnd) {
                        Ok(player) => {
                            *opt = Some(player);
                        }
                        Err(e) => {
                            eprintln!("Failed to initialize embedded player: {}", e);
                            crate::player::PlayerLauncher::open_in_browser(
                                playlist_id,
                                lecture_index,
                                first_video_id,
                            );
                            return;
                        }
                    }
                } else {
                    eprintln!("Main window HWND not found");
                    crate::player::PlayerLauncher::open_in_browser(
                        playlist_id,
                        lecture_index,
                        first_video_id,
                    );
                    return;
                }
            }

            if let Some(player) = opt.as_mut() {
                player.navigate(&url);
            }
        });
    }

    pub fn show() {
        EMBEDDED_PLAYER.with(|cell| {
            if let Some(player) = cell.borrow().as_ref() {
                player.show();
            }
        });
    }

    pub fn hide() {
        EMBEDDED_PLAYER.with(|cell| {
            if let Some(player) = cell.borrow().as_ref() {
                player.hide();
            }
        });
    }

    pub fn update_bounds() {
        EMBEDDED_PLAYER.with(|cell| {
            if let Some(player) = cell.borrow().as_ref() {
                player.update_bounds();
            }
        });
    }
}
