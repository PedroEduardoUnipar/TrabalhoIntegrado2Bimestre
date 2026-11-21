import { Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth.service';
import { CartService } from './core/cart.service';
import { ToastService } from './core/toast.service';
import { CarrinhoComponent } from './pages/carrinho.component';

@Component({
  selector: 'app-root', standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CarrinhoComponent],
  template: `
    @if (auth.usuario(); as usuario) {
      <header class="site-header">
        <a class="brand" routerLink="/catalogo"><span class="brand-mark">L<span>·</span></span><span>loja<span class="brand-light">integrada</span></span></a>
        <nav aria-label="Navegação principal">
          <a routerLink="/catalogo" routerLinkActive="active">Catálogo</a>
          <a routerLink="/pedidos" routerLinkActive="active">{{ auth.admin() ? 'Todos os pedidos' : 'Meus pedidos' }}</a>
          @if (auth.admin()) {
            <a routerLink="/admin/produtos" routerLinkActive="active">Produtos</a>
            <a routerLink="/admin/categorias" routerLinkActive="active">Categorias</a>
          }
        </nav>
        <div class="header-actions">
          <button class="button cart-button" (click)="carrinhoAberto.set(true)" aria-label="Abrir carrinho">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" aria-hidden="true"><path d="M3 3h2l3 13h10l3-9H6M9 21h.01M18 21h.01" stroke-linecap="round" stroke-linejoin="round"/></svg>
            <span>Carrinho</span><span class="cart-count">{{ cart.quantidade() }}</span>
          </button>
          <div class="user-info"><span class="avatar">{{ usuario.nome.charAt(0).toUpperCase() }}</span><div><strong>{{ usuario.nome }}</strong><small>{{ auth.admin() ? 'Administrador' : 'Cliente' }}</small></div></div>
          <button class="link-button muted" (click)="sair()">Sair</button>
        </div>
      </header>
    }
    <main id="conteudo"><router-outlet /></main>
    @if (auth.usuario()) { <footer class="site-footer"><span>Loja Integrada</span><span>Feito para simplificar o seu dia.</span></footer> }
    @if (carrinhoAberto() && auth.usuario()) { <app-carrinho (closed)="carrinhoAberto.set(false)" /> }
    <div class="toast-area" aria-live="polite" aria-atomic="true">
      @if (toast.mensagem(); as mensagem) {
        <div class="toast" [class.toast-error]="mensagem.tipo === 'erro'"><span>{{ mensagem.texto }}</span><button class="icon-button" aria-label="Fechar mensagem" (click)="toast.mensagem.set(null)">×</button></div>
      }
    </div>`
})
export class AppComponent {
  auth = inject(AuthService);
  cart = inject(CartService);
  toast = inject(ToastService);
  carrinhoAberto = signal(false);
  constructor() { this.auth.validarSessao(); }
  sair() { this.carrinhoAberto.set(false); this.cart.limpar(); this.auth.sair(); }
}
