package br.unipar.loja.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
public class Pedido {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    @Column(nullable = false, updatable = false)
    private Instant criadoEm;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 15)
    private StatusPedido status;
    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal total;
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ItemPedido> itens = new ArrayList<>();

    protected Pedido() {}
    public Pedido(Usuario usuario) {
        this.usuario = usuario;
        this.criadoEm = Instant.now();
        this.status = StatusPedido.CRIADO;
        this.total = BigDecimal.ZERO;
    }
    public void adicionarItem(Produto produto, int quantidade) {
        ItemPedido item = new ItemPedido(this, produto, quantidade);
        itens.add(item);
        total = total.add(item.getSubtotal());
    }
    public void cancelar() { status = StatusPedido.CANCELADO; }
    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public Instant getCriadoEm() { return criadoEm; }
    public StatusPedido getStatus() { return status; }
    public BigDecimal getTotal() { return total; }
    public List<ItemPedido> getItens() { return List.copyOf(itens); }
}
