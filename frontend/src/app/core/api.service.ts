import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Categoria, CategoriaRequest, Filtros, Pedido, Produto, ProdutoRequest } from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);
  categorias() { return this.http.get<Categoria[]>('/api/categorias'); }
  categoria(id: number) { return this.http.get<Categoria>(`/api/categorias/${id}`); }
  salvarCategoria(dados: CategoriaRequest, id?: number) {
    return id ? this.http.put<Categoria>(`/api/categorias/${id}`, dados) : this.http.post<Categoria>('/api/categorias', dados);
  }
  excluirCategoria(id: number) { return this.http.delete<void>(`/api/categorias/${id}`); }
  produtos(filtros: Filtros = {}) {
    let params = new HttpParams();
    for (const [chave, valor] of Object.entries(filtros)) {
      if (chave !== 'categoriaId' && valor !== undefined && valor !== '') params = params.set(chave, valor);
    }
    // Selecionar uma categoria exercita o filtro obrigatório recebido no path.
    const url = filtros.categoriaId ? `/api/categorias/${filtros.categoriaId}/produtos` : '/api/produtos';
    return this.http.get<Produto[]>(url, { params });
  }
  produto(id: number) { return this.http.get<Produto>(`/api/produtos/${id}`); }
  salvarProduto(dados: ProdutoRequest, id?: number) {
    return id ? this.http.put<Produto>(`/api/produtos/${id}`, dados) : this.http.post<Produto>('/api/produtos', dados);
  }
  excluirProduto(id: number) { return this.http.delete<void>(`/api/produtos/${id}`); }
  pedidos() { return this.http.get<Pedido[]>('/api/pedidos'); }
  pedido(id: number) { return this.http.get<Pedido>(`/api/pedidos/${id}`); }
  criarPedido(itens: { produtoId: number; quantidade: number }[]) { return this.http.post<Pedido>('/api/pedidos', { itens }); }
  cancelarPedido(id: number) { return this.http.patch<Pedido>(`/api/pedidos/${id}/cancelar`, {}); }
}
