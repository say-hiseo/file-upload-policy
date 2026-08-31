import { ApiErrorResponse } from '../types';

export class CustomApiError extends Error {
  code?: string;
  timestamp?: string;

  constructor(message: string, code?: string, timestamp?: string) {
    super(message);
    this.name = 'CustomApiError';
    this.code = code;
    this.timestamp = timestamp;
  }
}

export async function apiRequest<T>(url: string, options?: RequestInit): Promise<T> {
  try {
    const res = await fetch(url, {
      ...options,
      credentials: 'include',
    });

    if (!res.ok) {
      let errorMessage = '서버 처리 중 오류가 발생했습니다.';
      let errorCode: string | undefined;
      let errorTimestamp: string | undefined;

      try {
        const errorData: ApiErrorResponse = await res.json();
        if (errorData && errorData.message) {
          errorMessage = errorData.message;
          errorCode = errorData.code;
          errorTimestamp = errorData.timestamp;
        }
      } catch {
        if (res.status === 401) {
          errorMessage = '아이디 또는 비밀번호가 올바르지 않습니다';
        } else if (res.status === 404) {
          errorMessage = '요청한 리소스를 찾을 수 없습니다.';
        } else if (res.status === 409) {
          errorMessage = '이미 등록되었거나 충돌하는 리소스입니다.';
        } else if (res.status === 500) {
          errorMessage = '서버 내부 오류가 발생했습니다.';
        }
      }

      throw new CustomApiError(errorMessage, errorCode, errorTimestamp);
    }

    if (res.status === 204) {
      return undefined as T;
    }

    const text = await res.text();
    return text ? JSON.parse(text) : (undefined as T);
  } catch (err: unknown) {
    if (err instanceof CustomApiError) {
      throw err;
    }
    // Network or fetch connection failure
    throw new CustomApiError('네트워크 연결을 확인해주세요.');
  }
}
