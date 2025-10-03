// core/interceptors/api-key.interceptor.ts
import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { HttpRequest, HttpHandlerFn, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environment';

export const apiKeyInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
): Observable<HttpEvent<unknown>> => {
  const apiKey = environment.apiKey;
  const modifiedReq = req.clone({
    setHeaders: {
      'X-API-KEY': apiKey
    }
  });
  return next(modifiedReq);
};
