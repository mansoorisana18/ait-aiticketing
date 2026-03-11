type Listener = () => void;

const listeners = new Set<Listener>();
let sessionExpiredEmitted = false;

export function subscribeSessionExpired(listener: Listener): () => void {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

export function emitSessionExpired() {
  if (sessionExpiredEmitted) return;
  sessionExpiredEmitted = true;
  listeners.forEach((l) => l());
}

export function resetSessionExpiredEvent() {
  sessionExpiredEmitted = false;
}