const API_BASE = process.env.NEXT_PUBLIC_API_BASE ?? '';

export function withBasePath(path: string): string {
  return `${API_BASE}${path}`;
}

interface FetchOptions extends RequestInit {
  skipAuth?: boolean;
}

export async function apiFetch<T>(path: string, options: FetchOptions = {}): Promise<T> {
  const { skipAuth, ...fetchOptions } = options;

  const response = await fetch(withBasePath(path), {
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...fetchOptions.headers,
    },
    ...fetchOptions,
  });

  if (!response.ok) {
    if (response.status === 401 && !skipAuth) {
      throw new Error('Unauthorized');
    }
    const errorBody = await response.json().catch(() => ({}));
    throw new ApiError(response.status, errorBody);
  }

  if (response.status === 204) return undefined as T;
  return response.json();
}

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    public readonly body: unknown
  ) {
    super(`API Error: ${status}`);
  }
}
