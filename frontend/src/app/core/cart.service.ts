import { computed, effect, inject, Injectable, signal } from '@angular/core';
import { AuthService } from './auth.service';
import { Produto } from './models';
import { ToastService } from './toast.service';

export interface ItemCarrinho { produto: Produto; quantidade: number; }
@Injectable({ providedIn: 'root' })
export class CartService {
  private auth = inject(AuthService);
  private toast = inject(ToastService);
  readonly itens = signal<ItemCarrinho[]>([]);
  readonly quantidade = computed(() => this.itens().reduce((soma, item) => soma + item.quantidade, 0));
  readonly total = computed(() => this.itens().reduce((soma, item) => soma + Math.round(item.produto.preco * 100) * item.quantidade, 0) / 100);
  constructor() {
    let usuarioAnterior = this.auth.usuario()?.id;
    effect(() => {
      const usuarioAtual = this.auth.usuario()?.id;
      if (usuarioAtual !== usuarioAnterior) this.limpar();
      usuarioAnterior = usuarioAtual;
    });
  }
  adicionar(produto: Produto) {
    const existente = this.itens().find(item => item.produto.id === produto.id);
    if (!produto.ativo || (existente?.quantidade ?? 0) >= Math.min(produto.estoque, 9999)) {
      this.toast.mostrar('Quantidade disponível atingida.', 'erro'); return;
    }
    if (!existente && this.itens().length >= 50) { this.toast.mostrar('O pedido pode ter até 50 produtos diferentes.', 'erro'); return; }
    this.itens.update(itens => existente
      ? itens.map(item => item.produto.id === produto.id ? { ...item, quantidade: item.quantidade + 1 } : item)
      : [...itens, { produto, quantidade: 1 }]);
    this.toast.mostrar('Produto adicionado ao carrinho.');
  }
  alterar(id: number, quantidade: number) {
    if (quantidade < 1) { this.remover(id); return; }
    this.itens.update(itens => itens.map(item => item.produto.id === id
      ? { ...item, quantidade: Math.min(quantidade, item.produto.estoque, 9999) } : item));
  }
  remover(id: number) { this.itens.update(itens => itens.filter(item => item.produto.id !== id)); }
  limpar() { this.itens.set([]); }
}
