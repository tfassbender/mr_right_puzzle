// Must produce the same strings as backend/.../game/SolutionHashes.kt.

async function sha256Hex(text: string): Promise<string> {
  const digest = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(text));
  return Array.from(new Uint8Array(digest), (byte) => byte.toString(16).padStart(2, '0')).join('');
}

export function finalistsHash(gameId: string, finalistIds: Iterable<number>): Promise<string> {
  const sorted = [...finalistIds].sort((a, b) => a - b);
  return sha256Hex(`${gameId}:${sorted.join(',')}`);
}

export function solutionHash(gameId: string, solutionId: number): Promise<string> {
  return sha256Hex(`${gameId}:${solutionId}`);
}
