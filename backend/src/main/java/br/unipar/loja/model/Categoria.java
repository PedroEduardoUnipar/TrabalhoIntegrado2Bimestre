package br.unipar.loja.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Entity
@Table(name = "categorias")
public class Categoria {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 80)
    private String nome;
    @Column(nullable = false, unique = true, length = 80)
    private String nomeNormalizado;
    @Column(length = 250)
    private String descricao;
    @OneToMany(mappedBy = "categoria")
    private List<Produto> produtos = new ArrayList<>();

    protected Categoria() {}
    public Categoria(String nome, String descricao) { atualizar(nome, descricao); }
    public void atualizar(String nome, String descricao) {
        this.nome = nome.strip();
        this.nomeNormalizado = this.nome.toLowerCase(Locale.ROOT);
        this.descricao = descricao == null ? "" : descricao.strip();
    }
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
}
