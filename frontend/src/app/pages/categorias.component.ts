import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { Categoria, CategoriaRequest } from '../core/models';
import { mensagemErro, ToastService } from '../core/toast.service';
import { ModalComponent } from '../shared/modal.component';

@Component({
  standalone: true, imports: [FormsModule, ModalComponent],
  template: `
    <div class="page admin-page"><div class="page-heading"><div><span class="eyebrow">ORGANIZE SUA LOJA</span><h1>Categorias</h1><p class="muted">Um lugar para cada ideia. Uma categoria para cada produto.</p></div><button class="button primary" (click)="nova()">+ Nova categoria</button></div>
      <div class="panel"><div class="panel-title"><h2>Todas as categorias <span class="count">{{ categorias().length }}</span></h2></div>
        @if (carregando()) { <div class="empty-state" role="status">Carregando categorias…</div> }
        @else if (erroLista()) { <div class="empty-state"><p>{{ erroLista() }}</p><button class="button secondary" (click)="carregar()">Tentar novamente</button></div> }
        @else if (!categorias().length) { <div class="empty-state"><h3>Seu catálogo começa aqui.</h3><p>Crie a primeira categoria para organizar seus produtos.</p><button class="button primary" (click)="nova()">Criar categoria</button></div> }
        @else { <div class="table-wrap"><table><thead><tr><th>Código</th><th>Nome</th><th>Descrição</th><th class="align-right">Ações</th></tr></thead><tbody>@for (c of categorias(); track c.id) { <tr><td class="muted">#{{ c.id }}</td><td><strong>{{ c.nome }}</strong></td><td>{{ c.descricao || '—' }}</td><td><div class="table-actions"><button class="button small secondary" (click)="editar(c.id)">Editar</button><button class="link-button danger" (click)="excluindo.set(c)">Excluir</button></div></td></tr> }</tbody></table></div> }
      </div>
      @if (aberto()) {
        <app-modal [title]="idEditando ? 'Editar categoria' : 'Nova categoria'" (closed)="fechar()"><form #form="ngForm" (ngSubmit)="salvar()" class="stack"><label>Nome<input name="nome" [(ngModel)]="dados.nome" required maxlength="80" placeholder="Ex.: Papelaria" autofocus></label><label>Descrição<textarea name="descricao" [(ngModel)]="dados.descricao" maxlength="250" rows="3" placeholder="Conte um pouco sobre esta categoria"></textarea></label>@if (erro()) { <p class="form-error" role="alert">{{ erro() }}</p> }<div class="modal-actions"><button type="button" class="button secondary" [disabled]="salvando()" (click)="fechar()">Cancelar</button><button class="button primary" type="submit" [disabled]="form.invalid || !dados.nome.trim() || salvando()">{{ salvando() ? 'Salvando…' : 'Salvar categoria' }}</button></div></form></app-modal>
      }
      @if (excluindo(); as categoria) {
        <app-modal title="Excluir categoria" (closed)="!salvando() && excluindo.set(null)"><p>Excluir a categoria <strong>{{ categoria.nome }}</strong>?</p><p class="muted">Categorias com produtos vinculados precisam ser esvaziadas antes da exclusão.</p>@if (erro()) { <p class="form-error" role="alert">{{ erro() }}</p> }<div class="modal-actions"><button class="button secondary" [disabled]="salvando()" (click)="excluindo.set(null); erro.set('')">Voltar</button><button class="button danger-button" [disabled]="salvando()" (click)="excluir(categoria.id)">{{ salvando() ? 'Excluindo…' : 'Excluir categoria' }}</button></div></app-modal>
      }
    </div>`
})
export class CategoriasComponent {
  private api = inject(ApiService); private toast = inject(ToastService);
  categorias = signal<Categoria[]>([]); carregando = signal(false); erroLista = signal('');
  aberto = signal(false); salvando = signal(false); erro = signal(''); excluindo = signal<Categoria | null>(null);
  idEditando?: number;
  dados: CategoriaRequest = { nome: '', descricao: '' };
  constructor() { this.carregar(); }
  carregar() {
    this.carregando.set(true); this.erroLista.set('');
    this.api.categorias().subscribe({ next: dados => { this.categorias.set(dados); this.carregando.set(false); }, error: e => { this.erroLista.set(mensagemErro(e)); this.carregando.set(false); } });
  }
  nova() { this.idEditando = undefined; this.dados = { nome: '', descricao: '' }; this.erro.set(''); this.aberto.set(true); }
  editar(id: number) { this.api.categoria(id).subscribe({ next: c => { this.idEditando = c.id; this.dados = { nome: c.nome, descricao: c.descricao }; this.erro.set(''); this.aberto.set(true); }, error: e => this.toast.erro(e) }); }
  fechar() { if (!this.salvando()) { this.aberto.set(false); this.erro.set(''); } }
  salvar() {
    if (this.salvando()) return;
    this.salvando.set(true); this.erro.set('');
    this.api.salvarCategoria(this.dados, this.idEditando).subscribe({ next: () => { this.salvando.set(false); this.aberto.set(false); this.toast.mostrar('Categoria salva com sucesso.'); this.carregar(); }, error: e => { this.salvando.set(false); this.erro.set(mensagemErro(e)); } });
  }
  excluir(id: number) {
    this.salvando.set(true); this.erro.set('');
    this.api.excluirCategoria(id).subscribe({ next: () => { this.salvando.set(false); this.excluindo.set(null); this.toast.mostrar('Categoria excluída.'); this.carregar(); }, error: e => { this.salvando.set(false); this.erro.set(mensagemErro(e)); } });
  }
}
