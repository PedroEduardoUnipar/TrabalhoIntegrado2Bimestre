import { Component, inject, OnDestroy, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { ApiService } from '../core/api.service';
import { CartService } from '../core/cart.service';
import { Categoria, Filtros, Produto } from '../core/models';
import { mensagemErro, ToastService } from '../core/toast.service';
import { ModalComponent } from '../shared/modal.component';

@Component({
  standalone: true, imports: [CurrencyPipe, FormsModule, ModalComponent],
  template: `
    <div class="page">
      <section class="catalog-hero"><div><span class="eyebrow">ESCOLHAS PARA O SEU DIA</span><h1>Encontre seu próximo favorito.</h1><p>Produtos para trabalhar, estudar e colocar suas ideias em prática.</p></div><div class="hero-art" aria-hidden="true"><span class="art-sheet"></span><span class="art-book"></span><span class="art-pencil"></span><span class="art-dot">✦</span></div></section>
      <div class="section-heading"><div><h2>Explore o catálogo <span class="count">{{ produtos().length }}</span></h2><p class="muted">Os detalhes certos fazem toda a diferença.</p></div><span class="subtle-label">SELECIONADOS PARA VOCÊ</span></div>
      <form class="filter-bar" (ngSubmit)="buscar()">
        <label class="search-field"><span class="sr-only">Buscar por nome</span><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" aria-hidden="true"><circle cx="10.5" cy="10.5" r="6.5"/><path d="m16 16 5 5"/></svg><input name="nome" [(ngModel)]="filtros.nome" maxlength="100" placeholder="O que você procura?"></label>
        <label><span class="sr-only">Categoria</span><select name="categoria" [(ngModel)]="filtros.categoriaId" aria-label="Categoria"><option value="">Todas as categorias</option>@for (c of categorias(); track c.id) { <option [value]="c.id">{{ c.nome }}</option> }</select></label>
        <label><span class="sr-only">Cor</span><input name="cor" [(ngModel)]="filtros.cor" maxlength="40" placeholder="Cor" aria-label="Cor"></label>
        <button class="button primary" type="submit" [disabled]="carregando()">Filtrar</button><button class="link-button" type="button" (click)="limpar()">Limpar</button>
      </form>
      @if (carregando()) { <div class="empty-state" role="status"><span class="spinner"></span><p>Preparando o catálogo…</p></div> }
      @else if (erro()) { <div class="empty-state"><h3>Não foi possível carregar o catálogo.</h3><p>{{ erro() }}</p><button class="button secondary" (click)="buscar()">Tentar novamente</button></div> }
      @else if (!produtos().length) { <div class="empty-state"><span class="empty-icon">⌕</span><h3>Nenhum produto por aqui.</h3><p>Tente outro nome, cor ou categoria.</p><button class="button secondary" (click)="limpar()">Ver todos os produtos</button></div> }
      @else {
        <div class="product-grid">@for (p of produtos(); track p.id) {
          <article class="product-card">
            <button class="product-visual" [class.visual-blue]="p.categoriaId % 3 === 1" [class.visual-lilac]="p.categoriaId % 3 === 2" (click)="detalhes(p.id)" [attr.aria-label]="'Ver detalhes de ' + p.nome"><span class="product-category">{{ p.categoriaNome }}</span><span class="product-object" aria-hidden="true">{{ p.nome.charAt(0) }}<span>·</span></span><span class="visual-caption">loja integrada</span></button>
            <div class="product-body"><div class="product-top"><h3><button class="text-button" (click)="detalhes(p.id)">{{ p.nome }}</button></h3><span class="color-label">{{ p.cor }}</span></div><p class="product-description">{{ p.descricao }}</p><div class="product-stock"><span class="status-dot" [class.out]="p.estoque === 0"></span>{{ p.estoque > 0 ? p.estoque + ' disponíveis' : 'Sem estoque' }}</div><div class="product-bottom"><strong>{{ p.preco | currency:'BRL' }}</strong><button class="button add-button" [disabled]="p.estoque === 0" (click)="cart.adicionar(p)" [attr.aria-label]="'Adicionar ' + p.nome + ' ao carrinho'">+ <span>Adicionar</span></button></div></div>
          </article>
        }</div>
      }
      @if (selecionado(); as p) {
        <app-modal [title]="p.nome" (closed)="selecionado.set(null)"><div class="stack"><span class="badge">{{ p.categoriaNome }}</span><p>{{ p.descricao }}</p><div class="detail-grid"><div><small>Cor</small><strong>{{ p.cor }}</strong></div><div><small>Estoque</small><strong>{{ p.estoque }} unidades</strong></div></div><div class="modal-actions"><strong class="large-price">{{ p.preco | currency:'BRL' }}</strong><button class="button primary" [disabled]="!p.ativo || p.estoque === 0" (click)="cart.adicionar(p); selecionado.set(null)">Adicionar ao carrinho</button></div></div></app-modal>
      }
    </div>`
})
export class CatalogoComponent implements OnDestroy {
  private api = inject(ApiService);
  private toast = inject(ToastService);
  cart = inject(CartService);
  produtos = signal<Produto[]>([]); categorias = signal<Categoria[]>([]);
  selecionado = signal<Produto | null>(null);
  carregando = signal(false); erro = signal('');
  filtros: Filtros = { nome: '', cor: '', categoriaId: '', ativo: 'true' };
  private busca?: Subscription;
  constructor() {
    this.api.categorias().subscribe({ next: categorias => this.categorias.set(categorias), error: error => this.toast.erro(error) });
    this.buscar();
  }
  buscar() {
    this.busca?.unsubscribe(); this.carregando.set(true); this.erro.set('');
    this.busca = this.api.produtos(this.filtros).subscribe({
      next: produtos => { this.produtos.set(produtos); this.carregando.set(false); },
      error: error => { this.erro.set(mensagemErro(error)); this.carregando.set(false); }
    });
  }
  limpar() { this.filtros = { nome: '', cor: '', categoriaId: '', ativo: 'true' }; this.buscar(); }
  detalhes(id: number) { this.api.produto(id).subscribe({ next: produto => this.selecionado.set(produto), error: error => this.toast.erro(error) }); }
  ngOnDestroy() { this.busca?.unsubscribe(); }
}
