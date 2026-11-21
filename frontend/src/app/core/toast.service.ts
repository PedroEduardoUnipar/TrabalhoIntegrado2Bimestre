import { Injectable, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class ToastService {
  readonly mensagem = signal<{ texto: string; tipo: 'sucesso' | 'erro' } | null>(null);
  private timer?: ReturnType<typeof setTimeout>;
  mostrar(texto: string, tipo: 'sucesso' | 'erro' = 'sucesso') {
    clearTimeout(this.timer);
    this.mensagem.set({ texto, tipo });
    this.timer = setTimeout(() => this.mensagem.set(null), 6000);
  }
  erro(error: unknown) { this.mostrar(mensagemErro(error), 'erro'); }
}
export function mensagemErro(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0 || error.status === 502 || error.status === 504) return 'Não foi possível acessar a loja. Confira se o servidor está iniciado.';
    return error.error?.detail || 'Não foi possível concluir a operação. Tente novamente.';
  }
  return 'Não foi possível concluir a operação. Tente novamente.';
}
