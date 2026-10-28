import { session } from '../auth/session';

/**
 * A failed API call. [code] is the backend's stable error code, or `NETWORK` when the server was unreachable.
 * [body] is the parsed error JSON, e.g. the current server state of a 409 conflict.
 */
export class ApiError extends Error {
  constructor(
    readonly status: number,
    readonly code: string,
    message: string,
    readonly body?: unknown,
  ) {
    super(message);
  }
}

type Method = 'GET' | 'POST' | 'PUT' | 'DELETE';

/**
 * Sends a JSON request to the backend, with the session token if logged in.
 * [keepalive] lets the request outlive the page, e.g. the last save when the tab is closed.
 */
export async function apiRequest<T>(method: Method, path: string, body?: unknown, keepalive = false): Promise<T> {
  const headers: Record<string, string> = {};
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  const token = session.value?.token;
  if (token) headers['Authorization'] = `Bearer ${token}`;

  let response: Response;
  try {
    response = await fetch(`/api${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
      keepalive,
    });
  } catch (e) {
    throw new ApiError(0, 'NETWORK', String(e));
  }

  if (!response.ok) throw await toApiError(response);
  if (response.status === 204) return undefined as T;
  return (await response.json()) as T;
}

async function toApiError(response: Response): Promise<ApiError> {
  try {
    const error = (await response.json()) as { code?: string; message?: string };
    return new ApiError(response.status, error.code ?? 'UNKNOWN', error.message ?? response.statusText, error);
  } catch {
    // e.g. the plain 401 Quarkus returns for a missing/expired token
    return new ApiError(response.status, response.status === 401 ? 'UNAUTHORIZED' : 'UNKNOWN', response.statusText);
  }
}
