import { Component, inject, output, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { Router } from '@angular/router';
import { CartService } from '../core/cart.service';
import { ApiService } from '../core/api.service';
import { mensagemErro, ToastService } from '../core/toast.service';
import { ModalComponent } from '../shared/modal.component';

@Component({
  selector: 'app-carrinho', standalone: true, imports: [CurrencyPipe, ModalComponent],
  template: `
    <app-modal title="Seu carrinho" (closed)="fechar()">
      @if (!cart.itens().length) { <div class="empty-state compact"><span class="empty-icon">+</span><h3>Espaço para suas escolhas.</h3><p>Adicione produtos do catálogo para começar um pedido.</p><button class="button primary" (click)="closed.emit(); irCatalogo()">Explorar catálogo</button></div> }
      @else {
        <div class="cart-items">@for (item of cart.itens(); track item.produto.id) {
          <div class="cart-item"><div class="cart-item-mark">{{ item.produto.nome.charAt(0) }}</div><div class="grow"><strong>{{ item.produto.nome }}</strong><small>{{ item.produto.cor }} · {{ item.produto.preco | currency:'BRL' }}</small><div class="quantity"><button aria-label="Diminuir quantidade" [disabled]="enviando()" (click)="cart.alterar(item.produto.id, item.quantidade - 1)">−</button><span>{{ item.quantidade }}</span><button aria-label="Aumentar quantidade" [disabled]="enviando() || item.quantidade >= item.produto.estoque || item.quantidade >= 9999" (click)="cart.alterar(item.produto.id, item.quantidade + 1)">+</button></div></div><div class="align-right"><strong>{{ item.produto.preco * item.quantidade | currency:'BRL' }}</strong><button class="link-button muted" [disabled]="enviando()" (click)="cart.remover(item.produto.id)">Remover</button></div></div>
        }</div>
        <div class="cart-total"><span>Total estimado</span><strong>{{ cart.total() | currency:'BRL' }}</strong></div>
        <p class="muted small-text">A disponibilidade e os preços serão confirmados ao finalizar o pedido.</p>
        @if (erro()) { <p class="form-error" role="alert">{{ erro() }}</p> }
        <button class="button primary full" [disabled]="enviando()" (click)="finalizar()">{{ enviando() ? 'Finalizando…' : 'Finalizar pedido' }} <span aria-hidden="true">↗</span></button>
      }
    </app-modal>`
})
export class CarrinhoComponent {
  cart = inject(CartService);
  private api = inject(ApiService);
  private router = inject(Router);
  private toast = inject(ToastService);
  closed = output<void>();
  enviando = signal(false); erro = signal('');
  fechar() { if (!this.enviando()) this.closed.emit(); }
  irCatalogo() { void this.router.navigateByUrl('/catalogo'); }
  finalizar() {
    if (this.enviando() || !this.cart.itens().length) return;
    this.enviando.set(true); this.erro.set('');
    this.api.criarPedido(this.cart.itens().map(item => ({ produtoId: item.produto.id, quantidade: item.quantidade }))).subscribe({
      next: pedido => { this.cart.limpar(); this.closed.emit(); this.toast.mostrar('Pedido #' + pedido.id + ' realizado com sucesso!'); void this.router.navigateByUrl('/pedidos'); },
      error: error => { this.erro.set(mensagemErro(error)); this.enviando.set(false); }
    });
  }
}
