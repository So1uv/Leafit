(() => {
  const root = document.documentElement;
  const dark = matchMedia("(prefers-color-scheme: dark)");
  const calm = matchMedia("(prefers-reduced-motion: reduce)").matches;

  const isDark = () => root.dataset.theme ? root.dataset.theme === "dark" : dark.matches;
  const segButtons = [...document.querySelectorAll(".seg button")];
  const themeBtn = document.getElementById("theme");
  const paint = () => {
    document.querySelectorAll('meta[name="theme-color"]').forEach(m => m.content = isDark() ? "#111412" : "#F5FBF6");
    const mode = root.dataset.theme || "system";
    segButtons.forEach(b => b.setAttribute("aria-pressed", b.dataset.set === mode));
  };
  const setTheme = mode => {
    if (mode === "system") delete root.dataset.theme;
    else root.dataset.theme = mode;
    try {
      if (mode === "system") localStorage.removeItem("leafit-theme");
      else localStorage.setItem("leafit-theme", mode);
    } catch (e) {}
    themeBtn.classList.remove("spin");
    void themeBtn.offsetWidth;
    themeBtn.classList.add("spin");
    paint();
  };
  themeBtn.addEventListener("click", () => setTheme(isDark() ? "light" : "dark"));
  segButtons.forEach(b => b.addEventListener("click", () => setTheme(b.dataset.set)));
  dark.addEventListener("change", paint);
  paint();

  const fmtSize = bytes => Math.round(bytes / 1e6) + " MB";
  const fmtCount = n => n >= 1000 ? (n / 1000).toFixed(n >= 10000 ? 0 : 1).replace(/\.0$/, "") + "k" : String(n);
  const applyRelease = r => {
    const set = (key, fn) => document.querySelectorAll(`[data-gh="${key}"]`).forEach(fn);
    set("short", el => { el.textContent = r.short; });
    set("name", el => { el.textContent = "Leafit " + r.version; });
    set("date", el => { el.textContent = "Released " + r.date; });
    if (r.size) set("size", el => { el.textContent = " · " + fmtSize(r.size); });
    if (r.apk) set("apk", el => { el.href = r.apk; });
    if (r.downloads > 0) set("downloads", el => { el.textContent = fmtCount(r.downloads) + " downloads"; el.hidden = false; });
  };
  const loadRelease = async () => {
    const key = "leafit-release";
    try {
      const cached = JSON.parse(sessionStorage.getItem(key) || "null");
      if (cached && Date.now() - cached.at < 3600000) return applyRelease(cached);
    } catch (e) {}
    try {
      const res = await fetch("https://api.github.com/repos/So1uv/Leafit/releases?per_page=100", { headers: { Accept: "application/vnd.github+json" } });
      if (!res.ok) return;
      const list = await res.json();
      const latest = list.find(x => !x.draft && !x.prerelease);
      if (!latest) return;
      const version = (latest.tag_name || "").replace(/^[^0-9]*/, "") || latest.tag_name;
      const apk = (latest.assets || []).find(a => /\.apk$/i.test(a.name));
      const downloads = list.reduce((sum, rel) => sum + (rel.assets || []).reduce((s, a) => s + (a.download_count || 0), 0), 0);
      const r = {
        at: Date.now(),
        version,
        short: version.replace(/\.0$/, ""),
        date: new Date(latest.published_at).toLocaleDateString("en-GB", { day: "numeric", month: "short", year: "numeric" }),
        size: apk ? apk.size : 0,
        apk: apk ? apk.browser_download_url : "",
        downloads
      };
      try { sessionStorage.setItem(key, JSON.stringify(r)); } catch (e) {}
      applyRelease(r);
    } catch (e) {}
  };
  loadRelease();

  const bar = document.querySelector(".bar");
  const wavePath = document.querySelector(".scrollwave path");
  let ticking = false;
  const onScroll = () => {
    if (ticking) return;
    ticking = true;
    requestAnimationFrame(() => {
      bar.classList.toggle("scrolled", scrollY > 8);
      const max = document.documentElement.scrollHeight - innerHeight;
      if (wavePath) wavePath.style.strokeDashoffset = String(1500 * (1 - Math.min(1, scrollY / Math.max(1, max))));
      ticking = false;
    });
  };
  addEventListener("scroll", onScroll, { passive: true });
  onScroll();

  const ripple = e => {
    const el = e.currentTarget;
    const r = el.getBoundingClientRect();
    const d = Math.max(r.width, r.height) * 2.2;
    const s = document.createElement("span");
    s.className = "rip";
    s.style.width = s.style.height = d + "px";
    s.style.left = (e.clientX - r.left - d / 2) + "px";
    s.style.top = (e.clientY - r.top - d / 2) + "px";
    el.appendChild(s);
    s.addEventListener("animationend", () => s.remove());
  };
  if (!calm) document.querySelectorAll(".btn, .icon-btn, .seg button, .tabs button, .chip-new, summary, .nav a, .list-mock li").forEach(el => el.addEventListener("pointerdown", ripple));

  const strip = document.querySelector(".tabs");
  const ind = strip.querySelector(".ind");
  const tabs = [...strip.querySelectorAll('[role="tab"]')];
  const moveInd = tab => {
    ind.style.width = tab.offsetWidth + "px";
    ind.style.transform = `translateX(${tab.offsetLeft}px)`;
  };
  const select = (tab, focus) => {
    tabs.forEach(t => {
      const on = t === tab;
      t.setAttribute("aria-selected", on);
      t.tabIndex = on ? 0 : -1;
      const panel = document.getElementById(t.getAttribute("aria-controls"));
      panel.hidden = !on;
      if (on) {
        panel.classList.remove("enter");
        void panel.offsetWidth;
        panel.classList.add("enter");
      }
    });
    moveInd(tab);
    strip.scrollTo({ left: tab.offsetLeft - strip.clientWidth / 2 + tab.clientWidth / 2, behavior: calm ? "auto" : "smooth" });
    if (focus) tab.focus();
  };
  tabs.forEach((t, i) => {
    t.addEventListener("click", () => select(t));
    t.addEventListener("keydown", e => {
      const step = e.key === "ArrowRight" ? 1 : e.key === "ArrowLeft" ? -1 : 0;
      if (!step) return;
      e.preventDefault();
      select(tabs[(i + step + tabs.length) % tabs.length], true);
    });
  });
  strip.addEventListener("pointerdown", () => strip.classList.add("pressing"));
  addEventListener("pointerup", () => strip.classList.remove("pressing"));
  const placeInd = () => moveInd(tabs.find(t => t.getAttribute("aria-selected") === "true") || tabs[0]);
  addEventListener("resize", placeInd);
  if (document.fonts && document.fonts.ready) document.fonts.ready.then(placeInd);
  placeInd();

  const stage = document.querySelector(".stage");
  if (stage && !calm && matchMedia("(pointer: fine)").matches) {
    const hero = document.querySelector(".hero");
    hero.addEventListener("pointermove", e => {
      const r = hero.getBoundingClientRect();
      stage.style.setProperty("--px", ((e.clientX - r.left) / r.width - 0.5).toFixed(3));
      stage.style.setProperty("--py", ((e.clientY - r.top) / r.height - 0.5).toFixed(3));
    });
    hero.addEventListener("pointerleave", () => {
      stage.style.setProperty("--px", 0);
      stage.style.setProperty("--py", 0);
    });
  }

  document.querySelectorAll(".shape").forEach(shape => {
    const host = shape.closest(".num");
    const a = shape.querySelector(".m-in");
    const b = shape.querySelector(".m-out");
    if (!host || !a || !b || calm) return;
    host.addEventListener("pointerenter", () => { try { a.beginElement(); } catch (e) {} });
    host.addEventListener("pointerleave", () => { try { b.beginElement(); } catch (e) {} });
  });

  const countUp = el => {
    const to = Number(el.dataset.count);
    if (calm || !to) return;
    const start = performance.now();
    const dur = 1200;
    const step = now => {
      const t = Math.min(1, (now - start) / dur);
      const eased = 1 - Math.pow(1 - t, 4);
      el.textContent = Math.round(to * eased);
      if (t < 1) requestAnimationFrame(step);
    };
    el.textContent = "0";
    requestAnimationFrame(step);
  };

  const live = document.querySelector(".live");
  let liveTimer = null;
  const startLive = () => {
    if (liveTimer || calm) return;
    const km = live.querySelector('[data-live="km"]');
    const time = live.querySelector('[data-live="time"]');
    let sec = 28 * 60 + 41;
    let dist = 5.24;
    liveTimer = setInterval(() => {
      sec += 1;
      dist += 0.0031;
      time.textContent = Math.floor(sec / 60) + ":" + String(sec % 60).padStart(2, "0");
      km.textContent = dist.toFixed(2);
    }, 1000);
  };

  document.querySelectorAll(".list-mock li").forEach(li => {
    li.setAttribute("role", "checkbox");
    li.tabIndex = 0;
    const sync = () => li.setAttribute("aria-checked", li.classList.contains("done"));
    const flip = () => { li.classList.toggle("done"); sync(); if (navigator.vibrate) navigator.vibrate(8); };
    li.addEventListener("click", flip);
    li.addEventListener("keydown", e => { if (e.key === " " || e.key === "Enter") { e.preventDefault(); flip(); } });
    sync();
  });
  document.querySelectorAll(".alarm .switch").forEach(sw => {
    const flip = () => {
      sw.classList.toggle("on");
      sw.setAttribute("aria-checked", sw.classList.contains("on"));
      if (navigator.vibrate) navigator.vibrate(10);
    };
    sw.addEventListener("click", flip);
    sw.addEventListener("keydown", e => { if (e.key === " " || e.key === "Enter") { e.preventDefault(); flip(); } });
  });
  document.querySelectorAll(".days i").forEach(d => d.addEventListener("click", () => d.classList.toggle("on")));

  const buzz = document.getElementById("buzz");
  if (buzz) {
    buzz.addEventListener("click", () => {
      if (navigator.vibrate) navigator.vibrate([12, 60, 12, 60, 30]);
      buzz.animate([{ transform: "translateX(0)" }, { transform: "translateX(-3px)" }, { transform: "translateX(3px)" }, { transform: "translateX(-2px)" }, { transform: "translateX(0)" }], { duration: 260 });
    });
  }

  document.querySelectorAll("details").forEach(d => {
    const summary = d.querySelector("summary");
    const body = d.querySelector("p");
    summary.addEventListener("click", e => {
      if (calm || !body.animate) return;
      e.preventDefault();
      if (d.open) {
        const h = body.offsetHeight;
        body.animate([{ height: h + "px", opacity: 1 }, { height: "0px", opacity: 0, paddingBottom: "0px" }], { duration: 280, easing: "cubic-bezier(.2,.8,.2,1)" }).onfinish = () => { d.open = false; };
      } else {
        d.open = true;
        const h = body.offsetHeight;
        body.animate([{ height: "0px", opacity: 0, paddingBottom: "0px" }, { height: h + "px", opacity: 1 }], { duration: 380, easing: "cubic-bezier(.2,.8,.2,1)" });
      }
    });
  });

  const reel = document.querySelector(".reel");
  document.querySelectorAll(".scroll-btns button").forEach(b => {
    b.addEventListener("click", () => {
      reel.scrollBy({ left: Number(b.dataset.dir) * reel.clientWidth * 0.8, behavior: "smooth" });
    });
  });

  const box = document.getElementById("lightbox");
  const boxImg = box.querySelector("img");
  const boxCap = box.querySelector(".lb-cap");
  let set = [];
  let at = 0;
  const show = i => {
    at = (i + set.length) % set.length;
    boxImg.src = set[at].src;
    boxImg.alt = set[at].alt;
    boxCap.textContent = set[at].alt;
    if (!calm) boxImg.animate([{ opacity: 0, transform: "scale(.96)" }, { opacity: 1, transform: "none" }], { duration: 260, easing: "ease-out" });
  };
  const open = (list, i) => {
    set = list.map(img => ({ src: img.dataset.full || img.src, alt: img.alt }));
    show(i);
    box.showModal();
  };
  document.querySelectorAll(".trio").forEach(trio => {
    const imgs = [...trio.querySelectorAll("img")];
    imgs.forEach((img, i) => img.addEventListener("click", () => open(imgs, i)));
  });
  const reelImgs = [...reel.querySelectorAll("img")];
  reel.querySelectorAll("button").forEach((b, i) => b.addEventListener("click", () => open(reelImgs, i)));
  box.querySelector(".lb-close").addEventListener("click", () => box.close());
  box.querySelector(".lb-prev").addEventListener("click", () => show(at - 1));
  box.querySelector(".lb-next").addEventListener("click", () => show(at + 1));
  box.addEventListener("click", e => { if (e.target === box) box.close(); });
  box.addEventListener("keydown", e => {
    if (e.key === "ArrowRight") show(at + 1);
    if (e.key === "ArrowLeft") show(at - 1);
  });
  let touchX = null;
  box.addEventListener("touchstart", e => { touchX = e.touches[0].clientX; }, { passive: true });
  box.addEventListener("touchend", e => {
    if (touchX === null) return;
    const dx = e.changedTouches[0].clientX - touchX;
    if (Math.abs(dx) > 50) show(at + (dx < 0 ? 1 : -1));
    touchX = null;
  });

  const quiet = ".wave, .wavy";
  const targets = document.querySelectorAll(".num, .act, .card, .tile, .formula, .private, .design, .live, details, .get, " + quiet);
  const onSeen = el => {
    el.classList.add("in");
    if (el.style.transitionDelay) setTimeout(() => { el.style.transitionDelay = ""; }, 1300);
    el.querySelectorAll("[data-count]").forEach(countUp);
    if (el === live) startLive();
  };
  if ("IntersectionObserver" in window) {
    const io = new IntersectionObserver(entries => {
      entries.forEach(en => {
        if (en.isIntersecting) {
          onSeen(en.target);
          io.unobserve(en.target);
        }
      });
    }, { rootMargin: "0px 0px -6% 0px" });
    targets.forEach(el => {
      if (!el.matches(quiet)) {
        el.classList.add("reveal");
        const sibs = [...el.parentElement.children].filter(c => c.matches(el.tagName));
        const idx = sibs.indexOf(el);
        if (idx > 0) el.style.transitionDelay = Math.min(idx, 7) * 70 + "ms";
      }
      io.observe(el);
    });
  } else {
    targets.forEach(onSeen);
  }
})();
