export interface Usuario { id: number; nome: string; email: string; perfil: 'ADMIN' | 'COMUM'; }
export interface LoginResponse { token: string; tipo: string; expiraEm: string; usuario: Usuario; }
export interface Categoria { id: number; nome: string; descricao: string; }
export interface CategoriaRequest { nome: string; descricao: string; }
export interface Produto {
  id: number; nome: string; descricao: string; cor: string; preco: number;
  estoque: number; ativo: boolean; categoriaId: number; categoriaNome: string;
}
export interface ProdutoRequest {
  nome: string; descricao: string; cor: string; preco: number;
  estoque: number; ativo: boolean; categoriaId: number;
}
export interface Filtros {
  nome?: string; cor?: string; categoriaId?: string;
  precoMin?: string; precoMax?: string; ativo?: string;
}
export interface ItemPedido {
  id: number; produtoId: number; nomeProduto: string; quantidade: number;
  precoUnitario: number; subtotal: number;
}
export interface Pedido {
  id: number; criadoEm: string; status: 'CRIADO' | 'CANCELADO'; total: number;
  cliente: Usuario; itens: ItemPedido[];
}
