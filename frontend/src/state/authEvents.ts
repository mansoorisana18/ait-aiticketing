type Listener = () => void;

const listeners = new Set<Listener>();

export function subscribeSessionExpired(listener: Listener): () => void {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

export function emitSessionExpired() {
  listeners.forEach((l) => l());
}