package dao;

import model.Produto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class ProdutoDAO {
	private Connection conexao;
    private static int maxId = 0; 
    public static int getMaxId() {
		return maxId;
	} 
    public ProdutoDAO() {
        conectar();
    }

    public boolean conectar() {
        // String driverName = "org.postgresql.Driver";
        // String serverName = "localhost";
        // String mydatabase = "postgres";
        // int porta = 5432;
        // String url = "jdbc:postgresql://" + serverName + ":" + porta + "/" + mydatabase;
        // String username = "postgres";
        // String password = "1234";
        // boolean status = false;
        String driverName = "org.postgresql.Driver";
        String serverName = "localhost";
        String mydatabase = "teste";
        int porta = 5432;
        String url = "jdbc:postgresql://" + serverName + ":" + porta + "/" + mydatabase;
        String username = "ti@2cc";
        String password = "ti@2cc";
        boolean status = false;

        try {
            Class.forName(driverName);
            conexao = DriverManager.getConnection(url, username, password);
            status = (conexao == null);
            System.out.println("Conexão efetuada com o postgres!");
        } catch (ClassNotFoundException e) {
            System.err.println("Conexão NÃO efetuada com o postgres -- Driver não encontrado -- " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Conexão NÃO efetuada com o postgres -- " + e.getMessage());
        }

        return status;
    }

    public boolean close() {
        boolean status = false;

        try {
            conexao.close();
            status = true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return status;
    }

    public boolean inserirProduto(Produto produto) {
        boolean status = false;
        try {
            Statement st = conexao.createStatement();
            st.executeUpdate("INSERT INTO produto (id, descricao, preco, quantidade, fabricacao, datavalidade) " + "VALUES (" + produto.getId()
                    + ", '" + produto.getDescricao() + "', '" + produto.getPreco() + "', '" + produto.getQuant() + "', '" + produto.getDataFabricacao() + "','" + produto.getDataValidade() + "');");
            st.close();
            status = true;
            ProdutoDAO.maxId++;
        } catch (SQLException u) {
            throw new RuntimeException(u);
        }
        return status;
    }

    public boolean atualizarProduto(Produto produto) {
        boolean status = false;
        try {
            Statement st = conexao.createStatement();
            String sql = "UPDATE produto SET descricao = '" + produto.getDescricao() + "', preco = '" + produto.getPreco()
                    + "', quantidade = '" + produto.getQuant() + "'" + "', fabricacao = '" + produto.getDataFabricacao() + "'" + "', datavalidade = '" + produto.getDataValidade() + "'" + " WHERE id = " + produto.getId();
            st.executeUpdate(sql);
            st.close();
            status = true;
        } catch (SQLException u) {
            throw new RuntimeException(u);
        }
        return status;
    }

    public boolean excluirProduto(int id) {
        boolean status = false;
        try {
            Statement st = conexao.createStatement();
            st.executeUpdate("DELETE FROM produto WHERE id = " + id);
            st.close();
            status = true;
        } catch (SQLException u) {
            throw new RuntimeException(u);
        }
        return status;
    }

    public Produto[] getProduto() {
        Produto[] produtos = new Produto[0];

        try {
            Statement st = conexao.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = st.executeQuery("SELECT * FROM produto");
            if (rs.next()) {
                rs.last();
                produtos = new Produto[rs.getRow()];
                rs.beforeFirst();

                for (int i = 0; rs.next(); i++) {
                    produtos[i] = new Produto(rs.getInt("id"), rs.getString("descricao"), rs.getFloat("preco"),
                    rs.getInt("quantidade"),rs.getString("fabricacao"),rs.getString("datavalidade"));
                }
            }
            st.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return produtos;
    }


    public Produto selecionarProduto(int id) {
        Produto[] produtos = null;

        try {
            Statement st = conexao.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = st.executeQuery("SELECT * FROM produto WHERE id = " + id);
            if (rs.next()) {
                rs.last();
                produtos = new Produto[rs.getRow()];
                rs.beforeFirst();

                for (int i = 0; rs.next(); i++) {
                    produtos[i] = new Produto(rs.getInt("id"), rs.getString("descricao"), rs.getFloat("preco"),
                    rs.getInt("quantidade"),rs.getString("fabricacao"),rs.getString("datavalidade"));
                }
            }
            st.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return produtos[0];
    }
	/*private List<Produto> produtos;
	private int maxId = 0;

	private File file;
	private FileOutputStream fos;
	private ObjectOutputStream outputFile;

	public int getMaxId() {
		return maxId;
	}

	public ProdutoDAO(String filename) throws IOException {
		file = new File(filename);
		produtos = new ArrayList<Produto>();
		if (file.exists()) {
			readFromFile();
		}

	}

	public void add(Produto produto) {
		try {
			produtos.add(produto);
			this.maxId = (produto.getId() > this.maxId) ? produto.getId() : this.maxId;
			this.saveToFile();
		} catch (Exception e) {
			System.out.println("ERRO ao gravar o produto '" + produto.getDescricao() + "' no disco!");
		}
	}

	public Produto get(int id) {
		for (Produto produto : produtos) {
			if (id == produto.getId()) {
				return produto;
			}
		}
		return null;
	}

	public void update(Produto p) {
		int index = produtos.indexOf(p);
		if (index != -1) {
			produtos.set(index, p);
			this.saveToFile();
		}
	}

	public void remove(Produto p) {
		int index = produtos.indexOf(p);
		if (index != -1) {
			produtos.remove(index);
			this.saveToFile();
		}
	}

	public List<Produto> getAll() {
		return produtos;
	}

	private List<Produto> readFromFile() {
		produtos.clear();
		Produto produto = null;
		try (FileInputStream fis = new FileInputStream(file);
				ObjectInputStream inputFile = new ObjectInputStream(fis)) {

			while (fis.available() > 0) {
				produto = (Produto) inputFile.readObject();
				produtos.add(produto);
				maxId = (produto.getId() > maxId) ? produto.getId() : maxId;
			}
		} catch (Exception e) {
			System.out.println("ERRO ao gravar produto no disco!");
			e.printStackTrace();
		}
		return produtos;
	}

	private void saveToFile() {
		try {
			fos = new FileOutputStream(file, false);
			outputFile = new ObjectOutputStream(fos);

			for (Produto produto : produtos) {
				outputFile.writeObject(produto);
			}
			outputFile.flush();
			this.close();
		} catch (Exception e) {
			System.out.println("ERRO ao gravar produto no disco!");
			e.printStackTrace();
		}
	}

	private void close() throws IOException {
		outputFile.close();
		fos.close();
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			this.saveToFile();
			this.close();
		} catch (Exception e) {
			System.out.println("ERRO ao salvar a base de dados no disco!");
			e.printStackTrace();
		}
	}*/
}