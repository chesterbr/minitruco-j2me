package mt;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.microedition.io.StreamConnection;

/**
 * Representa o jogador conectado em um servidor bluetooth.
 * <p>
 * Suas fun��es s�o, essencialmente:
 * <p>
 * 1) Encaminhar os eventos gerados pela classe Jogo local para o celular
 * remoto;
 * <p>
 * 2) Receber os comandos do celular remoto e encaminhar para a classe Jogo;
 * <p>
 * Os eventos s�o descritos em uma linguagem baseada no antigo protocolo do
 * miniTruco client-server.
 * <P>
 * O evento "I" (que informa os nomes dos jogadores e regras do jogo) � o �nico
 * que n�o � gerado por esta classe, pois ocorre antes do <code>Jogo</code>
 * ser efetivamente criado.
 * <p>
 * 
 * TODO documentar esta linguagem (na real, atualizar/traduzir a documenta��o do
 * client-server)
 * 
 * @author chester
 * 
 */
public class JogadorBT extends Jogador {

	/**
	 * Conex�o com o celular remoto (onde est� um objeto JogoRemoto)
	 */
	StreamConnection conn;

	/**
	 * Sa�da de dados da conex�o (conn)
	 */
	private DataOutputStream out;

	/**
	 * Cria uma inst�ncia que representa um jogador conectado no servidor via
	 * Bluetooth
	 * 
	 * @param conn
	 *            conex�o BT estabelecida pelo jogador
	 */
	public JogadorBT(StreamConnection conn) {
		this.conn = conn;
		// TODO: Criar uma thread para processar as entradas (via inputstream) e
		// encaminhar para o jogo local
	}

	/**
	 * Manda uma notifica��o (linha de comando) para o celular do cliente.
	 * <p>
	 * Esta notifica��o veio do JogoLocal, e ser� processada l� pela classe
	 * JogoRemoto, para dar a id�ia de que veio de um jogo rodando l�.
	 * 
	 * @param linha
	 */
	public synchronized void println(String linha) {
		try {
			if (out == null) {
				out = conn.openDataOutputStream();
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
		StringBuffer comando = new StringBuffer("M");
		for (int i = 0; i <= 2; i++)
			comando.append(" " + getCartas()[i]);
		if (jogo.isManilhaVelha()) {
			comando.append(" " + jogo.cartaDaMesa);
		}
		println(comando.toString());
	}

	public void inicioPartida() {
		println("P");
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
		StringBuffer sbComando = new StringBuffer("F ");
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
