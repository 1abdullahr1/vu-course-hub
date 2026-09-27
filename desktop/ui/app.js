// VU Course Hub Desktop Application Logic

// Application State
const state = {
  courses: [],
  departments: ["All"],
  selectedDept: "All",
  searchQuery: "",
  selectedCourse: null,
  currentLectureIndex: 1,
  savedCodes: new Set(),
  theme: "dark",
  currentTab: "home"
};

// Official Resources List
const officialLinks = [
  {
    title: "Virtual University LMS (VULMS)",
    url: "https://vulms.vu.edu.pk",
    description: "Access course announcements, assignments, quizzes, and gradebooks."
  },
  {
    title: "VU Official Portal",
    url: "https://www.vu.edu.pk",
    description: "University announcements, academic calendar, and official programs."
  },
  {
    title: "VU Digital Library",
    url: "https://library.vu.edu.pk",
    description: "Research papers, e-books, journals, and comprehensive digital archives."
  },
  {
    title: "VU Date Sheet System",
    url: "https://datesheet.vu.edu.pk",
    description: "Midterm and final examination date sheet scheduling portal."
  },
  {
    title: "Official YouTube Channel",
    url: "https://www.youtube.com/@VirtualUniversityofPakistan",
    description: "Official channel broadcasts, live sessions, and ceremony coverage."
  },
  {
    title: "Student Notice Board",
    url: "https://www.vu.edu.pk/NewsNotice.aspx",
    description: "Urgent announcements, deadline extensions, and university notifications."
  },
  {
    title: "VU Admissions Portal",
    url: "https://www.vu.edu.pk/apply",
    description: "Program criteria, fee schedules, and new student admission portal."
  },
  {
    title: "VU Past Papers & Solutions",
    url: "https://www.google.com/search?q=Virtual+University+past+papers+filetype:pdf",
    description: "Past midterm and final exam papers, solved questions, and review files."
  }
];

// Open URL externally via Tauri shell / Rust command / browser
function openExternal(url) {
  if (window.__TAURI__ && window.__TAURI__.invoke) {
    window.__TAURI__.invoke("open_in_browser", { url }).catch(() => {
      window.open(url, "_blank");
    });
  } else {
    window.open(url, "_blank");
  }
}

// Format Course Code
function getCourseCode(course) {
  if (course.courseCode && course.courseCode.trim()) return course.courseCode.trim();
  const match = (course.title || "").match(/^([A-Za-z]{2,4}\d{3})/i);
  return match ? match[1].toUpperCase() : "VU";
}

// Clean Course Title
function getCleanTitle(course) {
  const code = getCourseCode(course);
  let title = (course.title || "").trim();
  if (title.toUpperCase().startsWith(code.toUpperCase())) {
    title = title.substring(code.length).trim();
    title = title.replace(/^[-:–—\s]+/, "");
  }
  return title || course.title || "Course Details";
}

// Format Department
function getDepartment(course) {
  return course.department && course.department.trim() ? course.department.trim() : "General";
}

// Generate Embed URL for YouTube
function getEmbedUrl(playlistId, lectureIndex, firstVideoId) {
  const cleanPid = (playlistId || "").trim();
  if (lectureIndex <= 1 && firstVideoId && firstVideoId.trim()) {
    const vid = firstVideoId.trim();
    if (cleanPid) {
      return `https://www.youtube-nocookie.com/embed/${vid}?autoplay=1&enablejsapi=1&rel=0&list=${cleanPid}`;
    }
    return `https://www.youtube-nocookie.com/embed/${vid}?autoplay=1&enablejsapi=1&rel=0`;
  }
  const zeroIndex = Math.max(0, lectureIndex - 1);
  return `https://www.youtube-nocookie.com/embed/videoseries?list=${cleanPid}&index=${zeroIndex}&autoplay=1&enablejsapi=1&rel=0`;
}

// Tab Switching
function switchTab(tabId) {
  state.currentTab = tabId;

  document.querySelectorAll(".nav-item").forEach(item => {
    if (item.dataset.tab === tabId) {
      item.classList.add("active");
    } else {
      item.classList.remove("active");
    }
  });

  document.querySelectorAll(".view-panel").forEach(panel => {
    panel.classList.remove("active");
  });

  const targetPanel = document.getElementById(`view-${tabId}`);
  if (targetPanel) {
    targetPanel.classList.add("active");
  }

  if (tabId === "saved") {
    renderSavedView();
  }
}

// Bookmark Storage
function loadSaved() {
  try {
    const raw = localStorage.getItem("vu_saved_codes");
    if (raw) {
      const arr = JSON.parse(raw);
      state.savedCodes = new Set(arr);
    }
  } catch (e) {
    state.savedCodes = new Set();
  }
  updateSavedBadge();
}

function saveSaved() {
  try {
    localStorage.setItem("vu_saved_codes", JSON.stringify(Array.from(state.savedCodes)));
  } catch (e) {}
  updateSavedBadge();
}

function toggleBookmark(code) {
  if (state.savedCodes.has(code)) {
    state.savedCodes.delete(code);
  } else {
    state.savedCodes.add(code);
  }
  saveSaved();

  // Update card buttons if visible
  document.querySelectorAll(`[data-bookmark-code="${code}"]`).forEach(btn => {
    const isSaved = state.savedCodes.has(code);
    btn.textContent = isSaved ? "Saved" : "Save";
    if (isSaved) {
      btn.classList.add("saved");
    } else {
      btn.classList.remove("saved");
    }
  });

  if (state.currentTab === "saved") {
    renderSavedView();
  }
}

function updateSavedBadge() {
  const badge = document.getElementById("saved-badge");
  const count = state.savedCodes.size;
  if (count > 0) {
    badge.textContent = count;
    badge.classList.remove("hidden");
  } else {
    badge.classList.add("hidden");
  }
}

// Theme Storage
function initTheme() {
  const saved = localStorage.getItem("vu_theme") || "dark";
  setTheme(saved);
}

function setTheme(theme) {
  state.theme = theme;
  document.body.className = theme === "light" ? "theme-light" : "theme-dark";
  document.getElementById("theme-status").textContent = theme === "light" ? "Light" : "Dark";
  try {
    localStorage.setItem("vu_theme", theme);
  } catch (e) {}
}

function toggleTheme() {
  setTheme(state.theme === "light" ? "dark" : "light");
}

// Play Course / Lecture
function playCourse(course, lectureIndex = 1) {
  state.selectedCourse = course;
  state.currentLectureIndex = lectureIndex;

  // Save last watched
  try {
    localStorage.setItem("vu_last_watched", JSON.stringify({
      code: getCourseCode(course),
      lectureIndex: lectureIndex
    }));
  } catch (e) {}

  switchTab("player");
  renderPlayer();
}

// Render Player View
function renderPlayer() {
  const course = state.selectedCourse;
  const activeState = document.getElementById("player-active-state");
  const emptyState = document.getElementById("player-empty-state");
  const indicator = document.getElementById("player-indicator");

  if (!course) {
    activeState.classList.add("hidden");
    emptyState.classList.remove("hidden");
    indicator.classList.add("hidden");
    return;
  }

  activeState.classList.remove("hidden");
  emptyState.classList.add("hidden");
  indicator.classList.remove("hidden");

  const code = getCourseCode(course);
  const title = getCleanTitle(course);
  const dept = getDepartment(course);
  const totalLectures = Math.max(1, Math.min(course.videoCount || 45, 100));

  document.getElementById("player-course-code").textContent = code;
  document.getElementById("player-course-dept").textContent = dept;
  document.getElementById("player-course-title").textContent = title;
  document.getElementById("player-active-lec-title").textContent = `${code} - Lecture ${String(state.currentLectureIndex).padStart(2, "0")}`;
  document.getElementById("playlist-count").textContent = `${totalLectures} lectures`;

  // Hide placeholder and load iframe
  const iframe = document.getElementById("player-iframe");
  const placeholder = document.getElementById("player-placeholder");
  const embedUrl = getEmbedUrl(course.playlistId, state.currentLectureIndex, course.firstVideoId);

  if (iframe.src !== embedUrl) {
    iframe.src = embedUrl;
  }
  placeholder.classList.add("hidden");

  // Render Playlist Items
  const listEl = document.getElementById("playlist-items-list");
  listEl.innerHTML = "";

  for (let num = 1; num <= totalLectures; num++) {
    const isCurrent = num === state.currentLectureIndex;
    const item = document.createElement("div");
    item.className = `playlist-item ${isCurrent ? "active" : ""}`;
    item.innerHTML = `
      <div class="playlist-item-left">
        <span class="lec-num">${String(num).padStart(2, "0")}</span>
        <span class="lec-name">${code} - Lecture ${String(num).padStart(2, "0")}</span>
      </div>
      <span class="lec-badge">${isCurrent ? "Playing" : "Play"}</span>
    `;
    item.onclick = () => {
      playCourse(course, num);
    };
    listEl.appendChild(item);

    if (isCurrent) {
      setTimeout(() => {
        item.scrollIntoView({ behavior: "smooth", block: "nearest" });
      }, 50);
    }
  }
}

// Next / Prev Lecture
function nextLecture() {
  if (!state.selectedCourse) return;
  const max = Math.max(1, Math.min(state.selectedCourse.videoCount || 45, 100));
  if (state.currentLectureIndex < max) {
    playCourse(state.selectedCourse, state.currentLectureIndex + 1);
  }
}

function prevLecture() {
  if (!state.selectedCourse) return;
  if (state.currentLectureIndex > 1) {
    playCourse(state.selectedCourse, state.currentLectureIndex - 1);
  }
}

// Open Current in Browser
function openCurrentInBrowser() {
  if (!state.selectedCourse) return;
  const pid = state.selectedCourse.playlistId || "";
  const idx = state.currentLectureIndex || 1;
  const vid = state.selectedCourse.firstVideoId || "";
  let url = `https://www.youtube.com/watch?list=${pid}&index=${idx}`;
  if (idx === 1 && vid) {
    url = `https://www.youtube.com/watch?v=${vid}&list=${pid}&index=1`;
  }
  openExternal(url);
}

// Create Course Card Component
function createCourseCard(course) {
  const code = getCourseCode(course);
  const title = getCleanTitle(course);
  const dept = getDepartment(course);
  const countText = course.videoCountText || `${course.videoCount || 45} lessons`;
  const isSaved = state.savedCodes.has(code);

  const card = document.createElement("div");
  card.className = "course-card";
  card.innerHTML = `
    <div>
      <div class="card-top">
        <span class="card-code">${code}</span>
        <button class="btn-bookmark ${isSaved ? "saved" : ""}" data-bookmark-code="${code}">
          ${isSaved ? "Saved" : "Save"}
        </button>
      </div>
      <h4 class="card-title">${title}</h4>
      <p class="card-dept">${dept}</p>
    </div>
    <div class="card-bottom">
      <span class="card-count">${countText}</span>
      <button class="btn btn-primary btn-sm watch-btn">Watch</button>
    </div>
  `;

  card.querySelector(".btn-bookmark").onclick = (e) => {
    e.stopPropagation();
    toggleBookmark(code);
  };

  card.querySelector(".watch-btn").onclick = (e) => {
    e.stopPropagation();
    playCourse(course, 1);
  };

  card.onclick = () => {
    playCourse(course, 1);
  };

  return card;
}

// Render Explore Courses
function renderCoursesView() {
  const container = document.getElementById("courses-grid");
  const countText = document.getElementById("courses-count-text");
  const query = state.searchQuery.trim().toLowerCase();

  const filtered = state.courses.filter(c => {
    const deptMatch = state.selectedDept === "All" || getDepartment(c).toLowerCase() === state.selectedDept.toLowerCase();
    const code = getCourseCode(c).toLowerCase();
    const title = (c.title || "").toLowerCase();
    const dept = getDepartment(c).toLowerCase();
    const searchMatch = !query || code.includes(query) || title.includes(query) || dept.includes(query);
    return deptMatch && searchMatch;
  });

  countText.textContent = `Showing ${filtered.length} courses`;
  container.innerHTML = "";

  filtered.forEach(c => {
    container.appendChild(createCourseCard(c));
  });
}

// Render Department Pills
function renderDeptPills() {
  const bar = document.getElementById("dept-pills-bar");
  bar.innerHTML = "";

  state.departments.forEach(dept => {
    const pill = document.createElement("button");
    pill.className = `dept-pill ${state.selectedDept === dept ? "active" : ""}`;
    pill.textContent = dept;
    pill.onclick = () => {
      state.selectedDept = dept;
      document.querySelectorAll(".dept-pill").forEach(p => p.classList.remove("active"));
      pill.classList.add("active");
      renderCoursesView();
    };
    bar.appendChild(pill);
  });
}

// Render Home View
function renderHomeView() {
  const featuredGrid = document.getElementById("home-featured-grid");
  featuredGrid.innerHTML = "";

  const popular = state.courses.slice(0, 6);
  popular.forEach(c => {
    featuredGrid.appendChild(createCourseCard(c));
  });

  document.getElementById("stat-courses").textContent = `${state.courses.length}+`;
  document.getElementById("stat-depts").textContent = `${state.departments.length - 1}`;
}

// Render Saved Courses View
function renderSavedView() {
  const grid = document.getElementById("saved-courses-grid");
  const empty = document.getElementById("saved-empty-state");
  grid.innerHTML = "";

  const savedList = state.courses.filter(c => state.savedCodes.has(getCourseCode(c)));

  if (savedList.length === 0) {
    grid.classList.add("hidden");
    empty.classList.remove("hidden");
  } else {
    grid.classList.remove("hidden");
    empty.classList.add("hidden");
    savedList.forEach(c => {
      grid.appendChild(createCourseCard(c));
    });
  }
}

// Render Handouts View
function renderHandoutsView() {
  const grid = document.getElementById("handouts-grid");
  grid.innerHTML = "";

  const searchInput = document.getElementById("handout-search-input");
  const query = (searchInput.value || "").trim().toLowerCase();

  const filtered = state.courses.filter(c => {
    const code = getCourseCode(c).toLowerCase();
    const title = getCleanTitle(c).toLowerCase();
    return !query || code.includes(query) || title.includes(query);
  });

  filtered.forEach(c => {
    const code = getCourseCode(c);
    const title = getCleanTitle(c);
    const dept = getDepartment(c);
    const card = document.createElement("div");
    card.className = "resource-card";
    card.innerHTML = `
      <div>
        <span class="chip chip-primary">${code}</span>
        <h4 class="resource-title" style="margin-top: 10px;">${title}</h4>
        <p class="resource-desc">${dept}</p>
      </div>
      <button class="btn btn-secondary btn-sm download-btn">Download Handout PDF</button>
    `;
    card.querySelector(".download-btn").onclick = () => {
      openExternal(`https://www.google.com/search?q=VU+${code}+handouts+filetype:pdf`);
    };
    grid.appendChild(card);
  });
}

// Render Official Links View
function renderLinksView() {
  const grid = document.getElementById("links-grid");
  grid.innerHTML = "";

  officialLinks.forEach(link => {
    const card = document.createElement("div");
    card.className = "resource-card";
    card.innerHTML = `
      <div>
        <h4 class="resource-title">${link.title}</h4>
        <p class="resource-desc" style="margin-top: 6px;">${link.description}</p>
      </div>
      <button class="btn btn-outline btn-sm open-btn">Launch Portal</button>
    `;
    card.querySelector(".open-btn").onclick = () => {
      openExternal(link.url);
    };
    grid.appendChild(card);
  });
}

// Load Courses Data from Tauri Backend or Local Assets
async function loadCoursesData() {
  try {
    if (window.__TAURI__ && window.__TAURI__.invoke) {
      const raw = await window.__TAURI__.invoke("get_courses");
      state.courses = JSON.parse(raw);
    } else {
      const resp = await fetch("assets/courses.json");
      state.courses = await resp.json();
    }
  } catch (e) {
    console.error("Error loading courses:", e);
    try {
      const resp = await fetch("assets/courses.json");
      state.courses = await resp.json();
    } catch (err) {
      state.courses = [];
    }
  }

  // Extract unique departments
  const deptSet = new Set();
  state.courses.forEach(c => {
    const d = getDepartment(c);
    if (d && d !== "General") deptSet.add(d);
  });
  state.departments = ["All", ...Array.from(deptSet).sort()];

  // Render initial views
  renderDeptPills();
  renderCoursesView();
  renderHomeView();
  renderHandoutsView();
  renderLinksView();
  loadSaved();
  checkResumeLastWatched();
}

// Check and Setup Resume Last Watched
function checkResumeLastWatched() {
  try {
    const raw = localStorage.getItem("vu_last_watched");
    if (raw) {
      const data = JSON.parse(raw);
      const course = state.courses.find(c => getCourseCode(c) === data.code);
      if (course) {
        state.selectedCourse = course;
        state.currentLectureIndex = data.lectureIndex || 1;
        document.getElementById("hero-resume-btn").onclick = () => {
          playCourse(course, state.currentLectureIndex);
        };
        return;
      }
    }
  } catch (e) {}

  document.getElementById("hero-resume-btn").onclick = () => {
    if (state.courses.length > 0) {
      playCourse(state.courses[0], 1);
    } else {
      switchTab("courses");
    }
  };
}

// Initialize Application Events
function initApp() {
  initTheme();

  // Navigation Click Handlers
  document.querySelectorAll(".nav-item").forEach(item => {
    item.onclick = () => {
      switchTab(item.dataset.tab);
    };
  });

  // Hero Buttons
  document.getElementById("hero-explore-btn").onclick = () => switchTab("courses");

  // Search Input Handlers
  const searchInput = document.getElementById("course-search-input");
  const clearBtn = document.getElementById("search-clear-btn");
  searchInput.oninput = (e) => {
    state.searchQuery = e.target.value;
    if (state.searchQuery.length > 0) {
      clearBtn.classList.remove("hidden");
    } else {
      clearBtn.classList.add("hidden");
    }
    renderCoursesView();
  };
  clearBtn.onclick = () => {
    searchInput.value = "";
    state.searchQuery = "";
    clearBtn.classList.add("hidden");
    renderCoursesView();
  };

  // Handouts Search
  const handoutSearch = document.getElementById("handout-search-input");
  handoutSearch.oninput = () => renderHandoutsView();

  // Theme Toggle Button
  document.getElementById("theme-toggle").onclick = toggleTheme;

  // Player Toolbar Controls
  document.getElementById("btn-play-video").onclick = () => {
    if (state.selectedCourse) {
      playCourse(state.selectedCourse, state.currentLectureIndex);
    }
  };
  document.getElementById("btn-prev-lec").onclick = prevLecture;
  document.getElementById("btn-next-lec").onclick = nextLecture;
  document.getElementById("btn-open-browser").onclick = openCurrentInBrowser;

  // Load Data
  loadCoursesData();
}

// Start Application on DOM Ready
document.addEventListener("DOMContentLoaded", initApp);
