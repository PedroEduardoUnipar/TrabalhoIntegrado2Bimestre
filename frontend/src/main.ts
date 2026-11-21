import { bootstrapApplication } from '@angular/platform-browser';
import { provideZonelessChangeDetection, LOCALE_ID } from '@angular/core';
import { registerLocaleData } from '@angular/common';
import localePt from '@angular/common/locales/pt';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { AppComponent } from './app/app.component';
import { routes } from './app/app.routes';
import { authInterceptor } from './app/core/auth.interceptor';

registerLocaleData(localePt);
bootstrapApplication(AppComponent, {
  providers: [provideZonelessChangeDetection(), provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])), { provide: LOCALE_ID, useValue: 'pt-BR' }]
}).catch(console.error);
