package br.unipar.loja.service;

import br.unipar.loja.dto.*;
import br.unipar.loja.model.*;
import br.unipar.loja.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import static br.unipar.loja.exception.RegraNegocioException.*;

@Service
@Transactional(readOnly = true)
public class PedidoService {
    private final PedidoRepository pedidos;
    private final ProdutoRepository produtos;
    private final UsuarioRepository usuarios;
    public PedidoService(PedidoRepository pedidos, ProdutoRepository produtos, UsuarioRepository usuarios) {
        this.pedidos = pedidos;
        this.produtos = produtos;
        this.usuarios = usuarios;
    }
    public List<PedidoResponse> listar(Long usuarioId) {
        Usuario usuario = obterUsuario(usuarioId);
        List<Pedido> lista = usuario.getPerfil() == Perfil.ADMIN
            ? pedidos.findAllByOrderByIdDesc() : pedidos.findByUsuarioIdOrderByIdDesc(usuarioId);
        return lista.stream().map(DtoMapper::pedido).toList();
    }
    public PedidoResponse buscar(Long id, Long usuarioId) {
        Pedido pedido = pedidos.findById(id).orElseThrow(() -> naoEncontrado("Pedido"));
        validarAcesso(pedido, obterUsuario(usuarioId));
        return DtoMapper.pedido(pedido);
    }
    @Transactional
    public PedidoResponse criar(PedidoRequest request, Long usuarioId) {
        Pedido pedido = new Pedido(obterUsuario(usuarioId));
        // Ordem fixa de bloqueios evita deadlock em compras com vários produtos.
        Map<Long, Integer> quantidades = new TreeMap<>();
        for (ItemPedidoRequest item : request.itens()) {
            if (quantidades.putIfAbsent(item.produtoId(), item.quantidade()) != null) {
                throw invalido("Informe cada produto uma única vez no pedido.");
            }
        }
        for (var item : quantidades.entrySet()) {
            Produto produto = produtos.buscarParaAtualizacao(item.getKey()).orElseThrow(() -> naoEncontrado("Produto"));
            if (!produto.getAtivo()) throw conflito("O produto " + produto.getNome() + " está inativo.");
            if (produto.getEstoque() < item.getValue()) throw conflito("Estoque insuficiente para " + produto.getNome() + ".");
            pedido.adicionarItem(produto, item.getValue());
            produto.alterarEstoque(produto.getEstoque() - item.getValue());
        }
        // Uma única transação: falha em qualquer item desfaz todo o pedido e as baixas.
        return DtoMapper.pedido(pedidos.save(pedido));
    }
    @Transactional
    public PedidoResponse cancelar(Long id, Long usuarioId) {
        Pedido pedido = pedidos.buscarParaAtualizacao(id).orElseThrow(() -> naoEncontrado("Pedido"));
        validarAcesso(pedido, obterUsuario(usuarioId));
        if (pedido.getStatus() != StatusPedido.CRIADO) throw conflito("Este pedido já está cancelado.");
        List<ItemPedido> itens = pedido.getItens().stream()
            .sorted(Comparator.comparing(i -> i.getProduto().getId())).toList();
        for (ItemPedido item : itens) {
            Produto produto = produtos.buscarParaAtualizacao(item.getProduto().getId()).orElseThrow(() -> naoEncontrado("Produto"));
            produto.alterarEstoque(produto.getEstoque() + item.getQuantidade());
        }
        pedido.cancelar();
        return DtoMapper.pedido(pedido);
    }
    private Usuario obterUsuario(Long id) {
        return usuarios.findById(id).orElseThrow(() -> naoEncontrado("Usuário"));
    }
    private void validarAcesso(Pedido pedido, Usuario usuario) {
        if (usuario.getPerfil() != Perfil.ADMIN && !pedido.getUsuario().getId().equals(usuario.getId())) {
            throw new AccessDeniedException("Pedido de outro usuário.");
        }
    }
}
