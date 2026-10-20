import { afterEach, describe, expect, it, vi } from 'vitest';
import { endSession, startSession } from '../auth/session';
import { ApiError, apiRequest } from './client';

const user = { userId: 'u1', email: 'emma@example.com', displayName: 'Emma', emailVerified: true, language: null };

function mockFetch(response: Response | Error) {
  const fetchMock = vi.fn(() => (response instanceof Error ? Promise.reject(response) : Promise.resolve(response)));
  vi.stubGlobal('fetch', fetchMock);
  return fetchMock;
}

describe('apiRequest', () => {
  afterEach(() => {
    vi.unstubAllGlobals();
    endSession();
  });

  it('sends the session token and parses JSON', async () => {
    startSession('secret', user);
    const fetchMock = mockFetch(new Response(JSON.stringify({ status: 'UP' }), { status: 200 }));

    const result = await apiRequest<{ status: string }>('GET', '/health');

    expect(result.status).toBe('UP');
    const init = fetchMock.mock.calls[0] as unknown as [string, RequestInit];
    expect(init[0]).toBe('/api/health');
    expect((init[1].headers as Record<string, string>)['Authorization']).toBe('Bearer secret');
  });

  it('turns an error response into an ApiError with the backend code', async () => {
    mockFetch(new Response(JSON.stringify({ code: 'INVALID_CREDENTIALS', message: 'nope' }), { status: 401 }));

    const error = await apiRequest('POST', '/auth/login', {}).catch((e: ApiError) => e) as ApiError;

    expect(error).toBeInstanceOf(ApiError);
    expect(error.status).toBe(401);
    expect(error.code).toBe('INVALID_CREDENTIALS');
  });

  it('reports an unreachable server as NETWORK', async () => {
    mockFetch(new TypeError('Failed to fetch'));

    const error = await apiRequest('GET', '/health').catch((e: ApiError) => e) as ApiError;

    expect(error.code).toBe('NETWORK');
  });
});
