import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { mensagemErro, ToastService } from '../core/toast.service';

@Component({
  standalone: true, imports: [FormsModule, RouterLink],
  template: `
    <div class="auth-layout">
      <section class="auth-story" aria-label="Conheça a Loja Integrada">
        <a class="brand brand-white" routerLink="/login"><span class="brand-mark">L<span>·</span></span><span>loja<span class="brand-light">integrada</span></span></a>
        <div class="auth-story-content"><span class="eyebrow light">SUA LOJA, MAIS CONECTADA</span><h1>Pequenos detalhes.<br>Grandes possibilidades.</h1><p>Do primeiro item ao próximo pedido.<br>Tudo o que você precisa, em um só lugar.</p>
          <div class="story-card"><div class="story-icon">✓</div><div><strong>Uma rotina mais simples</strong><span>Explore, escolha e acompanhe seus pedidos.</span></div><span class="story-spark">✦</span></div>
        </div>
        <span class="auth-caption">Organização que faz a diferença.</span>
        <div class="story-decoration" aria-hidden="true"></div>
      </section>
      <section class="auth-form-area">
        <div class="auth-form-wrap">
          <span class="eyebrow">BEM-VINDO À LOJA INTEGRADA</span>
          <h2>{{ cadastro ? 'Vamos começar?' : 'Bom ter você por aqui.' }}</h2>
          <p class="muted">{{ cadastro ? 'Crie sua conta para explorar o catálogo e fazer pedidos.' : 'Entre na sua conta e continue de onde parou.' }}</p>
          <form #form="ngForm" (ngSubmit)="enviar()" class="stack auth-form">
            @if (cadastro) { <label>Seu nome<input name="nome" [(ngModel)]="nome" required maxlength="100" autocomplete="name" placeholder="Como podemos chamar você?"></label> }
            <label>E-mail<input name="email" type="email" [(ngModel)]="email" required email maxlength="180" autocomplete="username" placeholder="voce@exemplo.com"></label>
            <label>Senha<input name="senha" type="password" [(ngModel)]="senha" required [minlength]="cadastro ? 8 : 1" maxlength="64" [autocomplete]="cadastro ? 'new-password' : 'current-password'" placeholder="Sua senha"></label>
            @if (cadastro) { <small class="muted">Use pelo menos 8 caracteres.</small> }
            @if (erro()) { <p class="form-error" role="alert">{{ erro() }}</p> }
            <button class="button primary full" type="submit" [disabled]="form.invalid || enviando()">{{ enviando() ? 'Aguarde…' : cadastro ? 'Criar minha conta' : 'Entrar na loja' }} <span aria-hidden="true">↗</span></button>
          </form>
          <p class="auth-switch">{{ cadastro ? 'Já tem uma conta?' : 'Primeira vez por aqui?' }} <a [routerLink]="cadastro ? '/login' : '/cadastro'">{{ cadastro ? 'Entrar' : 'Criar uma conta' }}</a></p>
          @if (!cadastro) {
            <div class="demo-box"><strong>Conheça a loja</strong><p>No modo demonstração, escolha uma conta para testar.</p><div class="row"><button class="button small secondary" type="button" (click)="preencher('admin')">Administrador</button><button class="button small secondary" type="button" (click)="preencher('cliente')">Cliente</button></div></div>
          }
        </div>
      </section>
    </div>`
})
export class LoginComponent {
  private auth = inject(AuthService);
  private router = inject(Router);
  private toast = inject(ToastService);
  cadastro = !!inject(ActivatedRoute).snapshot.data['cadastro'];
  nome = ''; email = ''; senha = '';
  enviando = signal(false);
  erro = signal('');
  preencher(tipo: 'admin' | 'cliente') {
    this.email = tipo === 'admin' ? 'admin@loja.com' : 'cliente@loja.com';
    this.senha = tipo === 'admin' ? 'Admin123!' : 'Cliente123!';
    this.erro.set('');
  }
  enviar() {
    if (this.enviando()) return;
    this.enviando.set(true); this.erro.set('');
    if (this.cadastro) {
      this.auth.cadastro(this.nome, this.email, this.senha).subscribe({
        next: () => { this.toast.mostrar('Conta criada! Entre com seu e-mail e senha.'); void this.router.navigateByUrl('/login'); },
        error: error => { this.erro.set(mensagemErro(error)); this.enviando.set(false); }
      });
    } else {
      this.auth.login(this.email, this.senha).subscribe({
        next: () => { void this.router.navigateByUrl('/catalogo'); },
        error: error => { this.erro.set(mensagemErro(error)); this.enviando.set(false); }
      });
    }
  }
}
