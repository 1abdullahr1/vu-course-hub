use std::process::Command;

pub struct PlayerLauncher;

impl PlayerLauncher {
    pub fn get_watch_url(playlist_id: &str, lecture_index: i32, first_video_id: Option<&str>) -> String {
        if lecture_index == 1 {
            if let Some(vid) = first_video_id.filter(|s| !s.trim().is_empty()) {
                return format!("https://www.youtube.com/watch?v={}&list={}&index=1", vid, playlist_id);
            }
        }
        format!("https://www.youtube.com/watch?list={}&index={}", playlist_id, lecture_index.max(1))
    }

    pub fn play(playlist_id: &str, lecture_index: i32, first_video_id: Option<&str>) {
        let watch_url = Self::get_watch_url(playlist_id, lecture_index, first_video_id);

        let edge_paths = [
            r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe",
            r"C:\Program Files\Microsoft\Edge\Application\msedge.exe",
        ];
        let chrome_paths = [
            r"C:\Program Files\Google\Chrome\Application\chrome.exe",
            r"C:\Program Files (x86)\Google\Chrome\Application\chrome.exe",
        ];

        // 1. Try Microsoft Edge in standalone app window
        for path in edge_paths {
            if std::path::Path::new(path).exists() {
                if Command::new(path)
                    .args([
                        &format!("--app={}", watch_url),
                        "--window-size=1120,700",
                    ])
                    .spawn()
                    .is_ok()
                {
                    return;
                }
            }
        }

        // 2. Try Google Chrome in standalone app window
        for path in chrome_paths {
            if std::path::Path::new(path).exists() {
                if Command::new(path)
                    .args([
                        &format!("--app={}", watch_url),
                        "--window-size=1120,700",
                    ])
                    .spawn()
                    .is_ok()
                {
                    return;
                }
            }
        }

        // 3. Fallback to default system web browser
        let _ = open::that(&watch_url);
    }

    pub fn open_in_browser(playlist_id: &str, lecture_index: i32, first_video_id: Option<&str>) {
        let watch_url = Self::get_watch_url(playlist_id, lecture_index, first_video_id);
        let _ = open::that(&watch_url);
    }
}
