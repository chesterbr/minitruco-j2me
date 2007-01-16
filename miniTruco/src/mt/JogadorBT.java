package mt;

import java.io.IOException;
import java.io.OutputStream;

import javax.microedition.io.StreamConnection;

/**
 * Representa o jogador conectado em um servidor bluetooth.
 * <p>
 * Suas funções são, essencialmente:
 * <p>
 * 1) Encaminhar os eventos gerados pela classe Jogo local para o celular
 * remoto;
 * <p>
 * 2) Receber os comandos do celular remoto e encaminhar para a classe Jogo;
 * <p>
 * Os eventos são descritos em uma linguagem baseada no antigo protocolo do
 * miniTruco client-server.
 * <P>
 * O evento "I" (que informa os nomes dos jogadores e regras do jogo) é o único
 * que não é gerado por esta classe, pois ocorre antes do <code>Jogo</code>
 * ser efetivamente criado.
 * 
 * TODO documentar esta linguagem (na real, atualizar/traduzir a documentação do
 * client-server)
 * 
 * @author chester
 * 
 */
public class JogadorBT extends Jogador {

	private StreamConnection conn;

	private OutputStream out;

	/**
	 * Cria uma instância que representa um jogador conectado no servidor via
	 * Bluetooth
	 * 
	 * @param conn
	 *            conexão BT estabelecida pelo jogador
	 */
	public JogadorBT(StreamConnection conn) {
		this.conn = conn;
	}

	/**
	 * Manda uma linha de comando para o cliente
	 * 
	 * @param linha
	 */
	public synchronized void println(String linha) {
		try {
			if (this.out == null) {
				this.out = conn.openOutputStream();
			}
			out.write(linha.getBytes());
			out.write('\n');
		} catch (IOException e) {
			// TODO TRATAR!!!!!
			e.printStackTrace();
		}
	}

	public void cartaJogada(Jogador j, Carta c) {
		String param;
		if (c.isFechada()) {
			if (j.equals(this)) {
				param = " " + c + " T";
			} else {
				param = "";
			}
		} else {
			param = " " + c.toString();
		}
		println("J " + j.getPosicao() + param);
	}

	public void inicioMao() {
		StringBuilder comando = new StringBuilder("M");
		for (int i = 0; i <= 2; i++)
			comando.append(" " + getCartas()[i]);
		if (jogo.isManilhaVelha()) {
			comando.append(" " + jogo.getCartaDaMesa());
		}
		println(comando.toString());
	}

	public void inicioPartida() {
		println("P " + getPosicao());
	}

	public void vez(Jogador j, boolean podeFechada) {
		println("V " + j.getPosicao() + ' ' + (podeFechada ? 'T' : 'F'));
	}

	public void pediuAumentoAposta(Jogador j, int valor) {
		println("T " + j.getPosicao() + ' ' + valor);
	}

	public void aceitouAumentoAposta(Jogador j, int valor) {
		println("D " + j.getPosicao() + ' ' + valor);
	}

	public void recusouAumentoAposta(Jogador j) {
		println("C " + j.getPosicao());
	}

	public void rodadaFechada(int numRodada, int resultado,
			Jogador jogadorQueTorna) {
		println("R " + resultado + ' ' + jogadorQueTorna.getPosicao());
	}

	public void maoFechada(int[] pontosEquipe) {
		println("O " + pontosEquipe[0] + ' ' + pontosEquipe[1]);
	}

	public void jogoFechado(int numEquipeVencedora) {
		// TODO desvinculaJogo();
		println("G " + numEquipeVencedora);
	}

	public void decidiuMao11(Jogador j, boolean aceita) {
		println("H " + j.getPosicao() + (aceita ? " T" : " F"));
	}

	public void informaMao11(Carta[] cartasParceiro) {
		StringBuilder sbComando = new StringBuilder("F ");
		for (int i = 0; i <= 2; i++) {
			sbComando.append(cartasParceiro[i]);
			if (i != 2)
				sbComando.append(' ');
		}
		println(sbComando.toString());
	}

	public void jogoAbortado(Jogador j) {
		// TODO desvinculaJogo();
		println("A " + j.getPosicao());
	}

}
