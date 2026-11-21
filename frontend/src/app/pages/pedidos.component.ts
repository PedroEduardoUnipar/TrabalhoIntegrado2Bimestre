import { Component, computed, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { Pedido } from '../core/models';
import { mensagemErro, ToastService } from '../core/toast.service';
import { ModalComponent } from '../shared/modal.component';

@Component({
  standalone: true, imports: [CurrencyPipe, DatePipe, RouterLink, ModalComponent],
  template: `
    <div class="page admin-page"><div class="page-heading"><div><span class="eyebrow">CADA ESCOLHA TEM UMA HISTÓRIA</span><h1>{{ auth.admin() ? 'Todos os pedidos' : 'Meus pedidos' }}</h1><p class="muted">Acompanhe os detalhes de cada pedido por aqui.</p></div><a class="button primary" routerLink="/catalogo">Explorar catálogo ↗</a></div>
      <div class="stats-grid"><div class="stat-card"><span>Pedidos realizados</span><strong>{{ pedidos().length }}</strong></div><div class="stat-card"><span>Pedidos ativos</span><strong>{{ ativos() }}</strong></div><div class="stat-card"><span>Total em pedidos ativos</span><strong>{{ total() | currency:'BRL' }}</strong></div></div>
      <div class="panel"><div class="panel-title"><h2>Histórico de pedidos</h2><button class="link-button" [disabled]="carregando()" (click)="carregar()">Atualizar</button></div>
        @if (carregando()) { <div class="empty-state" role="status">Carregando pedidos…</div> }
        @else if (erroLista()) { <div class="empty-state"><p>{{ erroLista() }}</p><button class="button secondary" (click)="carregar()">Tentar novamente</button></div> }
        @else if (!pedidos().length) { <div class="empty-state"><span class="empty-icon">↗</span><h3>O primeiro pedido vem aí.</h3><p>Explore o catálogo e encontre o que combina com você.</p><a class="button primary" routerLink="/catalogo">Ver catálogo</a></div> }
        @else { <div class="table-wrap"><table><thead><tr><th>Pedido</th><th>Data</th>@if (auth.admin()) { <th>Cliente</th> }<th>Itens</th><th>Total</th><th>Situação</th><th class="align-right">Detalhes</th></tr></thead><tbody>@for (p of pedidos(); track p.id) { <tr><td><strong>#{{ p.id }}</strong></td><td class="nowrap">{{ p.criadoEm | date:'dd/MM/yyyy, HH:mm' }}</td>@if (auth.admin()) { <td>{{ p.cliente.nome }}</td> }<td>{{ quantidade(p) }} un.</td><td class="nowrap">{{ p.total | currency:'BRL' }}</td><td><span class="badge" [class.badge-muted]="p.status === 'CANCELADO'">{{ p.status === 'CRIADO' ? 'Criado' : 'Cancelado' }}</span></td><td class="align-right"><button class="button small secondary" (click)="detalhes(p.id)">Ver pedido</button></td></tr> }</tbody></table></div> }
      </div>
      @if (selecionado(); as p) {
        <app-modal [title]="'Pedido #' + p.id" (closed)="fechar()"><div class="stack"><div class="row space-between"><span class="muted">{{ p.criadoEm | date:'dd/MM/yyyy, HH:mm' }}</span><span class="badge" [class.badge-muted]="p.status === 'CANCELADO'">{{ p.status === 'CRIADO' ? 'Criado' : 'Cancelado' }}</span></div><p class="muted">Cliente: <strong>{{ p.cliente.nome }}</strong></p><div class="order-items">@for (item of p.itens; track item.id) { <div class="order-item"><div><strong>{{ item.nomeProduto }}</strong><small>{{ item.quantidade }} × {{ item.precoUnitario | currency:'BRL' }}</small></div><strong>{{ item.subtotal | currency:'BRL' }}</strong></div> }</div><div class="cart-total"><span>Total do pedido</span><strong>{{ p.total | currency:'BRL' }}</strong></div>
          @if (erro()) { <p class="form-error" role="alert">{{ erro() }}</p> }
          @if (p.status === 'CRIADO') {
            @if (confirmando()) { <div class="notice">Cancelar este pedido? Os produtos serão devolvidos ao estoque.</div><div class="modal-actions"><button class="button secondary" [disabled]="cancelando()" (click)="confirmando.set(false)">Manter pedido</button><button class="button danger-button" [disabled]="cancelando()" (click)="cancelar(p.id)">{{ cancelando() ? 'Cancelando…' : 'Confirmar cancelamento' }}</button></div> }
            @else { <button class="link-button danger align-self-end" (click)="confirmando.set(true)">Cancelar pedido</button> }
          }
        </div></app-modal>
      }
    </div>`
})
export class PedidosComponent {
  private api = inject(ApiService); private toast = inject(ToastService);
  auth = inject(AuthService);
  pedidos = signal<Pedido[]>([]); selecionado = signal<Pedido | null>(null);
  carregando = signal(false); erroLista = signal(''); erro = signal('');
  confirmando = signal(false); cancelando = signal(false);
  ativos = computed(() => this.pedidos().filter(p => p.status === 'CRIADO').length);
  total = computed(() => this.pedidos().filter(p => p.status === 'CRIADO').reduce((soma, p) => soma + Math.round(p.total * 100), 0) / 100);
  constructor() { this.carregar(); }
  carregar() { this.carregando.set(true); this.erroLista.set(''); this.api.pedidos().subscribe({ next: p => { this.pedidos.set(p); this.carregando.set(false); }, error: e => { this.erroLista.set(mensagemErro(e)); this.carregando.set(false); } }); }
  detalhes(id: number) { this.api.pedido(id).subscribe({ next: p => { this.selecionado.set(p); this.erro.set(''); this.confirmando.set(false); }, error: e => this.toast.erro(e) }); }
  quantidade(p: Pedido) { return p.itens.reduce((soma, i) => soma + i.quantidade, 0); }
  fechar() { if (!this.cancelando()) { this.selecionado.set(null); this.confirmando.set(false); } }
  cancelar(id: number) { this.cancelando.set(true); this.erro.set(''); this.api.cancelarPedido(id).subscribe({ next: p => { this.cancelando.set(false); this.confirmando.set(false); this.selecionado.set(p); this.toast.mostrar('Pedido cancelado. Estoque atualizado.'); this.carregar(); }, error: e => { this.cancelando.set(false); this.erro.set(mensagemErro(e)); } }); }
}
