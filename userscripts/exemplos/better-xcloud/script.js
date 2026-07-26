(function () {
  "use strict";

  const STYLE_ID = "better-xcloud-style";

  function injectStyles() {
    if (document.getElementById(STYLE_ID)) return;

    const style = document.createElement("style");
    style.id = STYLE_ID;
    style.textContent = `
      [class*="NavBar"],
      [class*="nav-bar"],
      header[class*="Header"],
      [data-testid="header"],
      [class*="Footer"],
      footer {
        display: none !important;
      }

      [class*="CookieBanner"],
      [class*="cookie-banner"],
      [id*="cookie"] {
        display: none !important;
      }

      [class*="SidePanel"],
      [class*="side-panel"],
      [class*="Rail"] {
        display: none !important;
      }

      [class*="GameCanvas"],
      [class*="game-canvas"],
      [class*="PlayerContainer"],
      [id*="game-stream"] {
        width: 100vw !important;
        height: 100vh !important;
        max-width: 100vw !important;
        max-height: 100vh !important;
        position: fixed !important;
        top: 0 !important;
        left: 0 !important;
        z-index: 9999 !important;
      }

      [class*="TouchControls"] button,
      [class*="touch-controls"] button,
      [class*="GamepadButton"] {
        opacity: 0.7 !important;
        transition: opacity 0.2s ease !important;
      }

      [class*="TouchControls"] button:active,
      [class*="touch-controls"] button:active,
      [class*="GamepadButton"]:active {
        opacity: 1 !important;
        transform: scale(1.1) !important;
      }

      body {
        overflow: hidden !important;
        background: #000 !important;
      }

      [class*="LoadingScreen"] {
        background: #0e0e10 !important;
      }
    `;
    document.head.appendChild(style);
  }

  function addQuickActions() {
    if (document.getElementById("bx-quick-actions")) return;

    const container = document.createElement("div");
    container.id = "bx-quick-actions";
    container.style.cssText = `
      position: fixed;
      bottom: 12px;
      right: 12px;
      z-index: 99999;
      display: flex;
      gap: 8px;
    `;

    const fullscreenBtn = document.createElement("button");
    fullscreenBtn.textContent = "⛶";
    fullscreenBtn.title = "Tela cheia";
    fullscreenBtn.style.cssText = `
      width: 40px;
      height: 40px;
      border-radius: 50%;
      border: none;
      background: rgba(16, 124, 16, 0.85);
      color: #fff;
      font-size: 18px;
      cursor: pointer;
      backdrop-filter: blur(4px);
    `;
    fullscreenBtn.addEventListener("click", function () {
      if (!document.fullscreenElement) {
        document.documentElement.requestFullscreen().catch(function () {});
      } else {
        document.exitFullscreen().catch(function () {});
      }
    });

    const reloadBtn = document.createElement("button");
    reloadBtn.textContent = "↻";
    reloadBtn.title = "Recarregar stream";
    reloadBtn.style.cssText = `
      width: 40px;
      height: 40px;
      border-radius: 50%;
      border: none;
      background: rgba(50, 50, 50, 0.85);
      color: #fff;
      font-size: 18px;
      cursor: pointer;
      backdrop-filter: blur(4px);
    `;
    reloadBtn.addEventListener("click", function () {
      location.reload();
    });

    container.appendChild(fullscreenBtn);
    container.appendChild(reloadBtn);
    document.body.appendChild(container);
  }

  function removeClutter() {
    const selectors = [
      '[class*="PromoBanner"]',
      '[class*="promo-banner"]',
      '[class*="Upsell"]',
      '[class*="Recommendation"]',
      '[class*="SocialShare"]',
      '[aria-label="Share"]',
      '[class*="FeedbackButton"]',
    ];

    selectors.forEach(function (sel) {
      document.querySelectorAll(sel).forEach(function (el) {
        el.remove();
      });
    });
  }

  function init() {
    injectStyles();
    removeClutter();

    if (document.body) {
      addQuickActions();
    } else {
      document.addEventListener("DOMContentLoaded", addQuickActions);
    }

    const observer = new MutationObserver(function () {
      removeClutter();
    });

    observer.observe(document.documentElement, {
      childList: true,
      subtree: true,
    });
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();
