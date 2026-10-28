// Same alphabet and length as the server's random seeds: no look-alikes (l/1, o/0), easy to read out.
const ALPHABET = 'abcdefghijkmnpqrstuvwxyz23456789';
const LENGTH = 10;

export function randomSeed(): string {
  const values = crypto.getRandomValues(new Uint32Array(LENGTH));
  return Array.from(values, (value) => ALPHABET[value % ALPHABET.length]).join('');
}
