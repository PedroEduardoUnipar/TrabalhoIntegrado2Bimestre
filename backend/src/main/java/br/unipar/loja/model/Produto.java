package br.unipar.loja.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "produtos")
public class Produto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String nome;
    @Column(nullable = false, length = 500)
    private String descricao;
    @Column(nullable = false, length = 40)
    private String cor;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;
    @Column(nullable = false)
    private Integer estoque;
    @Column(nullable = false)
    private Boolean ativo;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    protected Produto() {}
    public Produto(String nome, String descricao, String cor, BigDecimal preco,
                   Integer estoque, Boolean ativo, Categoria categoria) {
        atualizar(nome, descricao, cor, preco, estoque, ativo, categoria);
    }
    public void atualizar(String nome, String descricao, String cor, BigDecimal preco,
                          Integer estoque, Boolean ativo, Categoria categoria) {
        this.nome = nome.strip();
        this.descricao = descricao == null ? "" : descricao.strip();
        this.cor = cor.strip();
        this.preco = preco;
        this.estoque = estoque;
        this.ativo = ativo;
        this.categoria = categoria;
    }
    public void alterarEstoque(int quantidade) { this.estoque = quantidade; }
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public String getCor() { return cor; }
    public BigDecimal getPreco() { return preco; }
    public Integer getEstoque() { return estoque; }
    public Boolean getAtivo() { return ativo; }
    public Categoria getCategoria() { return categoria; }
}
