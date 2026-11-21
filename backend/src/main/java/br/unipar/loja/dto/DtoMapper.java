package br.unipar.loja.dto;

import br.unipar.loja.model.*;

/** Conversão explícita: nenhuma entidade JPA atravessa a camada HTTP. */
public final class DtoMapper {
    private DtoMapper() {}
    public static CategoriaResponse categoria(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNome(), c.getDescricao());
    }
    public static ProdutoResponse produto(Produto p) {
        return new ProdutoResponse(p.getId(), p.getNome(), p.getDescricao(), p.getCor(),
            p.getPreco(), p.getEstoque(), p.getAtivo(), p.getCategoria().getId(), p.getCategoria().getNome());
    }
    public static UsuarioResponse usuario(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getPerfil());
    }
    public static PedidoResponse pedido(Pedido p) {
        return new PedidoResponse(p.getId(), p.getCriadoEm(), p.getStatus(), p.getTotal(),
            usuario(p.getUsuario()), p.getItens().stream().map(i -> new ItemPedidoResponse(
                i.getId(), i.getProduto().getId(), i.getNomeProduto(), i.getQuantidade(),
                i.getPrecoUnitario(), i.getSubtotal())).toList());
    }
}
