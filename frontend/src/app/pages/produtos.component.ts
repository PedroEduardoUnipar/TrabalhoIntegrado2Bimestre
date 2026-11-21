import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { Categoria, Filtros, Produto, ProdutoRequest } from '../core/models';
import { mensagemErro, ToastService } from '../core/toast.service';
import { ModalComponent } from '../shared/modal.component';

@Component({
  standalone: true, imports: [FormsModule, CurrencyPipe, ModalComponent, RouterLink],
  template: `
    <div class="page admin-page"><div class="page-heading"><div><span class="eyebrow">CUIDE DO SEU CATÁLOGO</span><h1>Produtos</h1><p class="muted">Cadastre, organize e mantenha suas informações em dia.</p></div><button class="button primary" (click)="novo()">+ Novo produto</button></div>
      <form class="filter-bar" (ngSubmit)="carregar()"><label class="grow"><span class="sr-only">Nome do produto</span><input name="nome" [(ngModel)]="filtros.nome" maxlength="100" placeholder="Buscar por nome" aria-label="Nome do produto"></label><label><span class="sr-only">Cor</span><input name="cor" [(ngModel)]="filtros.cor" maxlength="40" placeholder="Cor" aria-label="Cor"></label><label><span class="sr-only">Categoria</span><select name="categoria" [(ngModel)]="filtros.categoriaId" aria-label="Categoria"><option value="">Todas as categorias</option>@for (c of categorias(); track c.id) { <option [value]="c.id">{{ c.nome }}</option> }</select></label><label><span class="sr-only">Situação</span><select name="ativo" [(ngModel)]="filtros.ativo" aria-label="Situação"><option value="">Todas as situações</option><option value="true">Ativos</option><option value="false">Inativos</option></select></label><button class="button primary" type="submit" [disabled]="carregando()">Filtrar</button><button class="link-button" type="button" (click)="limpar()">Limpar</button></form>
      <div class="panel"><div class="panel-title"><h2>Seu catálogo <span class="count">{{ produtos().length }}</span></h2><span class="muted small-text">Preços em reais</span></div>
        @if (carregando()) { <div class="empty-state" role="status">Carregando produtos…</div> }
        @else if (erroLista()) { <div class="empty-state"><p>{{ erroLista() }}</p><button class="button secondary" (click)="carregar()">Tentar novamente</button></div> }
        @else if (!produtos().length) { <div class="empty-state"><h3>Nenhum produto encontrado.</h3><p>Cadastre um produto ou ajuste os filtros.</p></div> }
        @else { <div class="table-wrap"><table><thead><tr><th>Produto</th><th>Categoria</th><th>Cor</th><th>Preço</th><th>Estoque</th><th>Situação</th><th class="align-right">Ações</th></tr></thead><tbody>@for (p of produtos(); track p.id) { <tr><td><strong>{{ p.nome }}</strong><small class="table-subtitle">#{{ p.id }}</small></td><td>{{ p.categoriaNome }}</td><td>{{ p.cor }}</td><td class="nowrap">{{ p.preco | currency:'BRL' }}</td><td><span [class.low-stock]="p.estoque < 5">{{ p.estoque }} un.</span></td><td><span class="badge" [class.badge-muted]="!p.ativo">{{ p.ativo ? 'Ativo' : 'Inativo' }}</span></td><td><div class="table-actions"><button class="button small secondary" (click)="editar(p.id)">Editar</button><button class="link-button danger" (click)="confirmarExclusao(p)">Excluir</button></div></td></tr> }</tbody></table></div> }
      </div>
      @if (aberto()) {
        <app-modal [title]="idEditando ? 'Editar produto' : 'Novo produto'" (closed)="fechar()"><form #form="ngForm" (ngSubmit)="salvar()" class="stack">
          @if (!categorias().length) { <div class="notice">Crie uma categoria antes de cadastrar produtos. <a routerLink="/admin/categorias">Ir para categorias</a></div> }
          <label>Nome<input name="nome" [(ngModel)]="dados.nome" required maxlength="100" placeholder="Ex.: Caneta gel" autofocus></label><label>Descrição<textarea name="descricao" [(ngModel)]="dados.descricao" maxlength="500" rows="2" placeholder="Características do produto"></textarea></label>
          <div class="form-grid"><label>Categoria<select name="categoriaId" [(ngModel)]="dados.categoriaId" required><option [ngValue]="0" disabled>Selecione</option>@for (c of categorias(); track c.id) { <option [ngValue]="c.id">{{ c.nome }}</option> }</select></label><label>Cor<input name="cor" [(ngModel)]="dados.cor" required maxlength="40" placeholder="Ex.: Azul"></label><label>Preço (R$)<input name="preco" type="number" [(ngModel)]="dados.preco" required min="0.01" max="9999999.99" step="0.01"></label><label>Estoque<input name="estoque" type="number" [(ngModel)]="dados.estoque" required min="0" max="1000000" step="1"></label></div>
          <label class="checkbox-label"><input type="checkbox" name="ativo" [(ngModel)]="dados.ativo"> Produto ativo no catálogo</label>
          @if (erro()) { <p class="form-error" role="alert">{{ erro() }}</p> }
          <div class="modal-actions"><button class="button secondary" type="button" [disabled]="salvando()" (click)="fechar()">Cancelar</button><button class="button primary" type="submit" [disabled]="form.invalid || !dados.categoriaId || !dados.nome.trim() || !dados.cor.trim() || salvando()">{{ salvando() ? 'Salvando…' : 'Salvar produto' }}</button></div>
        </form></app-modal>
      }
      @if (excluindo(); as p) { <app-modal title="Excluir produto" (closed)="!salvando() && excluindo.set(null)"><p>Excluir o produto <strong>{{ p.nome }}</strong>?</p><p class="muted">Produtos com histórico de pedidos podem ser desativados pela opção Editar.</p>@if (erro()) { <p class="form-error" role="alert">{{ erro() }}</p> }<div class="modal-actions"><button class="button secondary" [disabled]="salvando()" (click)="excluindo.set(null)">Voltar</button><button class="button danger-button" [disabled]="salvando()" (click)="excluir(p.id)">{{ salvando() ? 'Excluindo…' : 'Excluir produto' }}</button></div></app-modal> }
    </div>`
})
export class ProdutosComponent {
  private api = inject(ApiService); private toast = inject(ToastService);
  produtos = signal<Produto[]>([]); categorias = signal<Categoria[]>([]); carregando = signal(false); erroLista = signal('');
  aberto = signal(false); salvando = signal(false); erro = signal(''); excluindo = signal<Produto | null>(null);
  idEditando?: number;
  filtros: Filtros = { nome: '', cor: '', categoriaId: '', ativo: '' };
  dados: ProdutoRequest = this.vazio();
  constructor() { this.api.categorias().subscribe({ next: categorias => this.categorias.set(categorias), error: e => this.toast.erro(e) }); this.carregar(); }
  carregar() { this.carregando.set(true); this.erroLista.set(''); this.api.produtos(this.filtros).subscribe({ next: dados => { this.produtos.set(dados); this.carregando.set(false); }, error: e => { this.erroLista.set(mensagemErro(e)); this.carregando.set(false); } }); }
  limpar() { this.filtros = { nome: '', cor: '', categoriaId: '', ativo: '' }; this.carregar(); }
  novo() { this.idEditando = undefined; this.dados = this.vazio(); this.erro.set(''); this.aberto.set(true); }
  editar(id: number) { this.api.produto(id).subscribe({ next: p => { this.idEditando = p.id; this.dados = { nome: p.nome, descricao: p.descricao, cor: p.cor, preco: p.preco, estoque: p.estoque, ativo: p.ativo, categoriaId: p.categoriaId }; this.erro.set(''); this.aberto.set(true); }, error: e => this.toast.erro(e) }); }
  fechar() { if (!this.salvando()) this.aberto.set(false); }
  salvar() {
    if (this.salvando()) return;
    this.salvando.set(true); this.erro.set('');
    this.api.salvarProduto(this.dados, this.idEditando).subscribe({ next: () => { this.salvando.set(false); this.aberto.set(false); this.toast.mostrar('Produto salvo com sucesso.'); this.carregar(); }, error: e => { this.salvando.set(false); this.erro.set(mensagemErro(e)); } });
  }
  confirmarExclusao(produto: Produto) { this.erro.set(''); this.excluindo.set(produto); }
  excluir(id: number) { this.salvando.set(true); this.erro.set(''); this.api.excluirProduto(id).subscribe({ next: () => { this.salvando.set(false); this.excluindo.set(null); this.toast.mostrar('Produto excluído.'); this.carregar(); }, error: e => { this.salvando.set(false); this.erro.set(mensagemErro(e)); } }); }
  private vazio(): ProdutoRequest { return { nome: '', descricao: '', cor: '', preco: 0, estoque: 0, ativo: true, categoriaId: 0 }; }
}
