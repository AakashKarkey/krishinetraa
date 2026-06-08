if (typeof window === "undefined") {
  const hasFunctionalLocalStorage =
    typeof globalThis.localStorage !== "undefined" &&
    typeof globalThis.localStorage.getItem === "function";

  if (!hasFunctionalLocalStorage) {
    const storage = new Map();

    globalThis.localStorage = {
      getItem(key) {
        const value = storage.get(String(key));
        return value === undefined ? null : value;
      },
      setItem(key, value) {
        storage.set(String(key), String(value));
      },
      removeItem(key) {
        storage.delete(String(key));
      },
      clear() {
        storage.clear();
      },
      key(index) {
        return Array.from(storage.keys())[index] ?? null;
      },
      get length() {
        return storage.size;
      },
    };
  }
}
