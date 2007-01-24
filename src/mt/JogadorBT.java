package mt;

import java.io.IOException;
import java.io.InputStream;

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
public class JogadorBT extends Jogador implements Runnable {

	/**
	 * Servidor que criou este jogador
	 */
	private ServidorBT servidor;

	/**
	 * Slot em que este jogador est� no jogo
	 */
	private int slot;

	/**
	 * Cria uma inst�ncia que representa um jogador conectado no servidor via
	 * Bluetooth
	 * 
	 * @param conn
	 *            conex�o BT estabelecida pelo jogador
	 * @param slot
	 *            slot que o jogador ocupa no servidor
	 */
	public JogadorBT(ServidorBT servidor, int slot) {
		this.servidor = servidor;
		this.slot = slot;
		Thread t = new Thread(this);
		t.start();

	}

	/**
	 * Processa as mensagens vindas do cliente (i.e., do JogoBT no celular
	 * remoto), transformando-as novamente em eventos no Jogo local
	 */
	public void run() {
		// TODO Auto-generated method stub
		int c;
		StringBuffer sbLinha = new StringBuffer();
		InputStream in = null;
		try {
			in = servidor.connClientes[slot].openInputStream();
			// TODO: melhorar estavivo
			boolean estaVivo = true;
			while (estaVivo && (c = in.read()) != -1) {
				if (c == '\n' || c == '\r') {
					if (sbLinha.length() > 0) {
						Logger.debug(sbLinha.toString());
						char tipoNotificacao = sbLinha.charAt(0);
						String[] args = ServidorBT.split(sbLinha.toString(),
								' ');
						switch (tipoNotificacao) {
						case 'J':
							Carta[] cartas = getCartas();
							for (int i = 0; i < cartas.length; i++) {
								if (cartas[i] != null
										&& cartas[i].toString().equals(args[1])) {
									// Joga a carta. Se der certo o evento vai
									// notificar a todos.
									cartas[i].setFechada(args.length > 2
											&& args[2].equals("T"));
									jogo.jogaCarta(this, cartas[i]);
								}
							}
							break;
						case 'H':
							jogo.decideMao11(this, args[1].equals("T"));
							break;
						case 'T':
							jogo.aumentaAposta(this);
							break;
						case 'D':
							jogo.respondeAumento(this, true);
							break;
						case 'C':
							jogo.respondeAumento(this, false);
							break;
						}
					}
					sbLinha.setLength(0);
				} else {
					sbLinha.append((char) c);
				}
			}
		} catch (IOException e) {
			// TODO o que fazer aqui?
			e.printStackTrace();
			// � normal dar um erro de I/O quando o usu�rio pede pra
			// desconectar (porque o loop vai tentar ler a �ltima linha). S�
			// vamos alertar se a desconex�o foi for�ada, ou se n�o foi
			// poss�vel abrir os streams de I/O
			// if ((in == null) || (out == null) || estaVivo) {
			// alerta("Erro de I/O", e.getMessage(), true);
			// }
			// TODO finalizaServidor()?
			return;
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Se saiu do loop e ainda estava "vivo", foi desconectado,
			// avisa
			// TODO o que fazer qqui?
			// if (estaVivo) {
			// alerta("Desconectado", "Voc� foi desconectado do servidor.",
			// true);
			// }
			// TODO finalizaServidor();
		}

	}

	/**
	 * Manda uma notifica��o (linha de comando) para o celular do cliente.
	 * <p>
	 * Esta notifica��o veio do JogoLocal, e ser� processada l� pela classe
	 * JogoRemoto, para dar a id�ia de que veio de um jogo rodando l�.
	 * 
	 * @param linha
	 */
	public synchronized void enviaLinha(String linha) {
		servidor.enviaComando(slot, linha);
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
		enviaLinha("J " + j.getPosicao() + param);
	}

	public void inicioMao() {
		StringBuffer comando = new StringBuffer("M");
		for (int i = 0; i <= 2; i++)
			comando.append(" " + getCartas()[i]);
		// Se for manilha nova, tamb�m envia o "vira"
		if (!jogo.isManilhaVelha()) {
			comando.append(" " + jogo.cartaDaMesa);
		}
		enviaLinha(comando.toString());
	}

	public void inicioPartida() {
		enviaLinha("P");
	}

	public void vez(Jogador j, boolean podeFechada) {
		enviaLinha("V " + j.getPosicao() + ' ' + (podeFechada ? 'T' : 'F'));
	}

	public void pediuAumentoAposta(Jogador j, int valor) {
		enviaLinha("T " + j.getPosicao() + ' ' + valor);
	}

	public void aceitouAumentoAposta(Jogador j, int valor) {
		enviaLinha("D " + j.getPosicao() + ' ' + valor);
	}

	public void recusouAumentoAposta(Jogador j) {
		enviaLinha("C " + j.getPosicao());
	}

	public void rodadaFechada(int numRodada, int resultado,
			Jogador jogadorQueTorna) {
		enviaLinha("R " + resultado + ' ' + jogadorQueTorna.getPosicao());
	}

	public void maoFechada(int[] pontosEquipe) {
		enviaLinha("O " + pontosEquipe[0] + ' ' + pontosEquipe[1]);
	}

	public void jogoFechado(int numEquipeVencedora) {
		// TODO desvinculaJogo();
		enviaLinha("G " + numEquipeVencedora);
	}

	public void decidiuMao11(Jogador j, boolean aceita) {
		enviaLinha("H " + j.getPosicao() + (aceita ? " T" : " F"));
	}

	public void informaMao11(Carta[] cartasParceiro) {
		StringBuffer sbComando = new StringBuffer("F ");
		for (int i = 0; i <= 2; i++) {
			sbComando.append(cartasParceiro[i]);
			if (i != 2)
				sbComando.append(' ');
		}
		enviaLinha(sbComando.toString());
	}

	public void jogoAbortado(Jogador j) {
		// TODO desvinculaJogo();
		enviaLinha("A " + j.getPosicao());
	}

}
