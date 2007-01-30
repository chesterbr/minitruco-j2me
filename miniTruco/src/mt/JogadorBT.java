package mt;

import java.io.IOException;
import java.io.InputStream;

/**
 * Representa o jogador conectado em um servidor bluetooth.
 * <p>
 * Ele converte as notificações do jogo local em mensagens de texto (enviando-as
 * para o cliente) e converte as mensagens de texto vindas do cliente em
 * notificações para o jogo local.
 * <p>
 * A ida é feita através do ServidorBT (que detém a conexão e os outputstreams),
 * mas a volta é feita por um InputStream local, criado só para isso.
 * <p>
 * Desta forma, objeto "vive" enquanto este InputStream local estiver aberto,
 * mas a conexão/saída ficam disponíveis para novas partidas com o mesmo
 * cliente. que não é gerado por esta classe, pois ocorre antes do
 * 
 * TODO documentar esta linguagem (na real, atualizar/traduzir a documentação do
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
	 * Inputstream do jogador conectado
	 */
	InputStream in;

	/**
	 * Cria uma instância que representa um jogador conectado no servidor via
	 * Bluetooth
	 * 
	 * @param servidor
	 *            Servidor que fará o meio-de-campo
	 */
	public JogadorBT(ServidorBT servidor) {
		this.servidor = servidor;
		Thread t = new Thread(this);
		t.start();

	}

	/**
	 * Processa as mensagens vindas do cliente (i.e., do JogoBT no celular
	 * remoto), transformando-as novamente em eventos no Jogo local
	 */
	public void run() {
		// Aguarda a definição da posição (importante, pois ela determina o slot
		// no servidor para o envio de mensagens)
		while (getPosicao() == 0) {
			Thread.yield();
		}
		// Caractere lido e buffer que acumula a linha lida
		int c = 0;
		StringBuffer sbLinha = new StringBuffer();
		try {
			in = servidor.connClientes[getPosicao() - 2].openInputStream();
			// O loop dura enquanto o InputStream não for null. Sim, eu poderia
			// usar uma leitura mais eficiente, com blocking, mas aí não consigo
			// detectar o fim da partida (sem perder o primeiro caractere do
			// primeiro comando da partida seguinte)
			do {
				while (in != null && in.available() == 0) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// Alguém já tratou isso um dia?
					}
				}
				if (in == null)
					break;
				// Lê o próximo caractre
				c = in.read();
				if (c != '\r' && c != '\n') {
					// Acumula caracteres até formar uma linha
					sbLinha.append((char) c);
				} else {
					// Processa linhas (não-vazias)
					if (sbLinha.length() > 0) {
						MiniTruco.log("Linha acumulada: " + sbLinha.toString());
						char tipoNotificacao = sbLinha.charAt(0);
						String[] args = ServidorBT.split(sbLinha.toString(),
								' ');
						switch (tipoNotificacao) {
						case 'J':
							// Procura a carta correspondente ao parâmetro
							Carta[] cartas = getCartas();
							for (int i = 0; i < cartas.length; i++) {
								if (cartas[i] != null
										&& cartas[i].toString().equals(args[1])) {
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
						sbLinha.setLength(0);
					}
				}
			} while (in != null);
		} catch (IOException e) {
			// Não precisa tratar - ou é fim de jogo, ou o servidor cuida
		}
		MiniTruco.log("encerrando loop JogadorBT");
	}

	/**
	 * Encerra a thread principal, efetivamente finalizando o JogadorBT
	 */
	void finaliza() {
		// O in.close "does nothing", segundo a especificação (
		// http://tinyurl.com/2r59cp#close() ), então eu anulo o objeto e
		// monitoro isso no loop (mas fecho anyway)
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				// nao tratar
			}
		}
		in = null;
	}

	/**
	 * Manda uma linha de texto para o celular do cliente.
	 * <p>
	 * Estas linhas representam eventos gerados pelo JogoLocal.
	 * 
	 * @param linha
	 */
	public synchronized void enviaMensagem(String linha) {
		servidor.enviaMensagem(getPosicao() - 2, linha);
	}

	// Os métodos restantes convertem as notificações do JogoLocal em mensagens
	// de texto, que serão reconvertidas em solicitações no cliente para o
	// JogadorHumano.
	//
	// As únicas exceções são os eventos de fim-de-jogo, que finalizam o
	// JogadorBT

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
		enviaMensagem("J " + j.getPosicao() + param);
	}

	public void inicioMao() {
		StringBuffer comando = new StringBuffer("M");
		for (int i = 0; i <= 2; i++)
			comando.append(" " + getCartas()[i]);
		// Se for manilha nova, também envia o "vira"
		if (!jogo.isManilhaVelha()) {
			comando.append(" " + jogo.cartaDaMesa);
		}
		enviaMensagem(comando.toString());
	}

	public void inicioPartida() {
		enviaMensagem("P");
	}

	public void vez(Jogador j, boolean podeFechada) {
		enviaMensagem("V " + j.getPosicao() + ' ' + (podeFechada ? 'T' : 'F'));
	}

	public void pediuAumentoAposta(Jogador j, int valor) {
		enviaMensagem("T " + j.getPosicao() + ' ' + valor);
	}

	public void aceitouAumentoAposta(Jogador j, int valor) {
		enviaMensagem("D " + j.getPosicao() + ' ' + valor);
	}

	public void recusouAumentoAposta(Jogador j) {
		enviaMensagem("C " + j.getPosicao());
	}

	public void rodadaFechada(int numRodada, int resultado,
			Jogador jogadorQueTorna) {
		enviaMensagem("R " + resultado + ' ' + jogadorQueTorna.getPosicao());
	}

	public void maoFechada(int[] pontosEquipe) {
		enviaMensagem("O " + pontosEquipe[0] + ' ' + pontosEquipe[1]);
	}

	public void decidiuMao11(Jogador j, boolean aceita) {
		enviaMensagem("H " + j.getPosicao() + (aceita ? " T" : " F"));
	}

	public void informaMao11(Carta[] cartasParceiro) {
		StringBuffer sbComando = new StringBuffer("F ");
		for (int i = 0; i <= 2; i++) {
			sbComando.append(cartasParceiro[i]);
			if (i != 2)
				sbComando.append(' ');
		}
		enviaMensagem(sbComando.toString());
	}

	// Eventos de fim-de-jogo

	public void jogoFechado(int numEquipeVencedora) {
		enviaMensagem("G " + numEquipeVencedora);
		finaliza();
	}

	public void jogoAbortado(int posicao) {
		enviaMensagem("A " + posicao);
		finaliza();
	}

}
