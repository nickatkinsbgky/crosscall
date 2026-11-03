// Permanent personal link support for HelloDarlin'
(function() {
  const PERMANENT_KEY = 'hellodarlin-permanent-id';
  const BASE_URL = 'https://nickatkinsbgky.github.io/crosscall/';

  function getPermanentId() {
    try { return localStorage.getItem(PERMANENT_KEY) || ''; } catch(e) { return ''; }
  }
  function setPermanentId(id) {
    const clean = (id || '').trim().toLowerCase().replace(/[^a-z0-9-]/g, '');
    if (!clean) return;
    try { localStorage.setItem(PERMANENT_KEY, clean); } catch(e) {}
    if (window.updatePermanentUI) window.updatePermanentUI();
    if (window.startPermanentListener) window.startPermanentListener();
  }
  function getPermanentLink(id) {
    return BASE_URL + '?call=' + encodeURIComponent(id || getPermanentId());
  }

  window.getPermanentId = getPermanentId;
  window.setPermanentId = setPermanentId;
  window.getPermanentLink = getPermanentLink;

  window.addEventListener('load', () => {
    if (getPermanentId() && window.startPermanentListener) {
      setTimeout(window.startPermanentListener, 500);
    }
  });
})();
