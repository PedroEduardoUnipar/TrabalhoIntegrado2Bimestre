import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { LoginResponse, Usuario } from './models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private sessao = signal<LoginResponse | null>(this.restaurar());
  readonly usuario = computed(() => this.sessao()?.usuario ?? null);
  readonly admin = computed(() => this.usuario()?.perfil === 'ADMIN');
  get token() { return this.sessao()?.token ?? null; }

  login(email: string, senha: string) {
    return this.http.post<LoginResponse>('/api/auth/login', { email, senha }).pipe(tap(sessao => {
      this.sessao.set(sessao);
      sessionStorage.setItem('loja.sessao', JSON.stringify(sessao));
    }));
  }
  cadastro(nome: string, email: string, senha: string) {
    return this.http.post<Usuario>('/api/auth/cadastro', { nome, email, senha });
  }
  validarSessao() {
    if (!this.token) return;
    this.http.get<Usuario>('/api/auth/me').subscribe({
      next: usuario => this.sessao.update(sessao => sessao ? { ...sessao, usuario } : null),
      error: () => { /* O interceptor encerra sessões inválidas. */ }
    });
  }
  sair() {
    this.sessao.set(null);
    sessionStorage.removeItem('loja.sessao');
    void this.router.navigateByUrl('/login');
  }
  private restaurar(): LoginResponse | null {
    try {
      const sessao: LoginResponse = JSON.parse(sessionStorage.getItem('loja.sessao') || 'null');
      if (sessao?.token && sessao.usuario?.id && Date.parse(sessao.expiraEm) > Date.now()) return sessao;
    } catch { /* Uma sessão incompleta é descartada. */ }
    sessionStorage.removeItem('loja.sessao');
    return null;
  }
}
