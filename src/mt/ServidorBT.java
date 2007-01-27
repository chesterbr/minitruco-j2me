package mt;

import java.io.IOException;
import java.io.OutputStream;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.RemoteDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;

/**
 * Recebe conex�es (via Bluetooth) de outros celulares-cliente, exibe suas
 * posi��es, configura e inicia a partida (criando os proxies JogadorBT para
 * cada jogador remoto).
 * 
 * @author Chester
 * 
 */
public class ServidorBT extends TelaBT {

	/**
	 * Notificador do servidor (atrav�s do qual vamos aceitar as conex�es)
	 */
	private StreamConnectionNotifier scnServidor;

	/**
	 * Conexoes dos jogadores conectados.
	 */
	StreamConnection[] connClientes = new StreamConnection[3];

	/**
	 * OutputStreams dos jogadores conectados
	 */
	private OutputStream[] outClientes = new OutputStream[3];

	/**
	 * Inicializa o servidor
	 * 
	 * @param midlet
	 *            refer�ncia ao programa principal
	 */
	public ServidorBT(MiniTruco midlet) {
		super(midlet);
		// Usa as regras escolhidas pelo usu�rio
		this.regras = (midlet.cgRegras.isSelected(0) ? "T" : "F")
				+ (midlet.cgRegras.isSelected(1) ? "T" : "F");
	}

	/**
	 * Envia uma linha de texto para o cliente no slot especificado.
	 * <p>
	 * Se o slot espedificado estiver vazio, n�o faz nada.
	 * <p>
	 * Se o cliente der erro, processa sua desconex�o (principal motivo do
	 * synchronized).
	 * 
	 * @param slot
	 *            �ndice do cliente em connClientes/outClientes
	 * @param comando
	 *            texto do comando/notifica��o a enviar
	 */
	public synchronized void enviaMensagem(int slot, String comando) {
		if (outClientes[slot] != null) {
			try {
				outClientes[slot].write(comando.getBytes());
				outClientes[slot].write(ENTER);
			} catch (IOException e) {
				// Processa desconex�o
				try {
					outClientes[slot].close();
				} catch (IOException ioe) {
					// No prob, j� deve ter morrido
				}
				try {
					connClientes[slot].close();
				} catch (IOException ioe) {
					// No prob, j� deve ter morrido
				}
				// Libera o slot e encerra o jogo em andamento
				desconecta(slot);
			}
		}
	}

	/**
	 * Desconecta um jogador (ou notifica desist�ncia do servidor) e exibe a
	 * tela de jogadores
	 * 
	 * @param slot
	 *            slot do jogador a desconectar (0 a 2). Se for -1, notifica
	 *            desist�ncia do servidor. Se for -2, n�o notifica nada (apenas
	 *            encerra e vai para a tela).
	 */
	void desconecta(int slot) {
		MiniTruco.log("desconecta() " + slot);
		if (slot >= 0) {
			connClientes[slot] = null;
			outClientes[slot] = null;
			apelidos[slot + 1] = APELIDOS_CPU[slot];
		}
		// -1 vai notificar que o servidor (posi��o -1+2=1) desistiu
		// -2 n�o notifica ningu�m (posi��o -2+2=0)
		midlet.encerraJogo(slot + 2, false);
		setModoSetup(true);
		atualizaServidor();
		atualizaClientes();
	}

	/**
	 * Determina a situa��o atual do servidor, a saber:
	 * <p>
	 * J = Jogo em andamento<br>
	 * L = Lotado, aguardando inicio de jogo<br>
	 * A = Aguardando conexao ou inicio de jogo <br>
	 * X = Servidor sendo encerrado
	 */
	private char status;

	private static final Command iniciarJogoCommand = new Command("Iniciar",
			Command.SCREEN, 1);

	private static final Command trocaParceiroCommand = new Command(
			"Troca Parceiro", Command.SCREEN, 2);

	private static final Command inverteAdversariosCommand = new Command(
			"Inverte Advers\u00E1rios", Command.SCREEN, 3);

	/**
	 * Loop da thread principal (que recebe e processa as conex�es dos clientes)
	 */
	public void run() {

		Thread.yield(); // O ME2SE d� uma zica planet�ria sem isso

		// Inicializa os apelidos (servidor na 1a. posi��o)
		apelidos[0] = localDevice.getFriendlyName();
		for (int i = 1; i <= 3; i++)
			apelidos[i] = APELIDOS_CPU[i - 1];

		// Coloca o servidor em modo "setup", e a thread secund�ria para
		// monitorar desconex�es
		setModoSetup(true);
		Thread tm = new ThreadMonitoraClientes();
		tm.start();

		// Loop principal (executa enquanto o servidor n�o for encerrado)
		while (status != 'X') {

			// Se estivermos em jogo, aguarda o encerramento
			while (status == 'J') {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// n�o precisa tratar
				}
				continue; // checa novamente X
			}

			// Sa�mos do modo jogo, atualiza o display (local e remoto)
			atualizaServidor();
			atualizaClientes();

			// Se estiver lotado, aguarda desconex�o ou in�cio de jogo
			if (status == 'L') {
				do {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// n�o precisa tratar
					}
				} while (status == 'L');
				continue; // checa novamente J/X
			}

			// Se chegou aqui, estamos ativos, fora do jogo e com vaga
			// dispon�vel, logo, vamos aguardar uma conex�o (ou um
			// encerramento, que interrompe o acceptAndOpen())
			StreamConnection c = null;
			try {
				c = scnServidor.acceptAndOpen();
				// Encaixa na primeira vaga dispon�vel
				// (o synchronized � pra n�o fazer isso enquanto estiver
				// mexendo nas posi��es via menu)
				synchronized (this) {
					for (int i = 0; i <= 2; i++) {
						if (connClientes[i] == null) {
							connClientes[i] = c;
							outClientes[i] = c.openOutputStream();
							apelidos[i + 1] = RemoteDevice.getRemoteDevice(c)
									.getFriendlyName(false);
							c = null;
							break;
						}
					}
				}
				// Continua recebendo conex�es (a menos que lote)
				setModoSetup(true);
			} catch (IOException e) {
				if (c != null) {
					try {
						c.close();
					} catch (IOException e2) {
						// Nada a fazer
					}
				}
			}
		}
	}

	/**
	 * Atualiza o status do display (e adiciona/remove comandos), de acordo com
	 * os jogadores conectados.
	 */
	private void atualizaServidor() {
		if (getNumClientes() == 0) {
			setTelaMsg("Aguardando jogadores...");
			this.removeCommand(iniciarJogoCommand);
			this.removeCommand(trocaParceiroCommand);
			this.removeCommand(inverteAdversariosCommand);
		} else {
			setTelaMsg(null);
			this.addCommand(iniciarJogoCommand);
			this.addCommand(trocaParceiroCommand);
			this.addCommand(inverteAdversariosCommand);
		}
	}

	/**
	 * Thread secundd�ria (verifica se houve desconex�o de algum cliente).
	 * <p>
	 * Tem que ser feito em outra thread, porque a principal bloqueia enquanto
	 * agurada clientes
	 * 
	 * @author Chester
	 * 
	 */
	class ThreadMonitoraClientes extends Thread {

		public void run() {
			// Executa enquanto o servidor n�o for encerrado
			while (status != 'X') {
				// Envia um comando vazio (apenas para testar a conex�o, e
				// processar qualquer desconex�o que tenha ocorrido)
				for (int i = 0; i <= 2; i++) {
					enviaMensagem(i, "");
				}
				try {
					sleep(2000);
				} catch (InterruptedException e) {
					// n�o precisa tratar
				}
			}
		}
	}

	/**
	 * Conta quantos clientes est�o conectados
	 * 
	 * @return N�mero de clientes
	 */
	public int getNumClientes() {
		int numClientes = 0;
		for (int i = 0; i <= 2; i++) {
			if (connClientes[i] != null) {
				numClientes++;
			}
		}
		return numClientes;
	}

	/**
	 * Interrompe as threads e quaisquer conex�es existentes
	 */
	public void encerraSessaoBT() {
		setTelaMsg("Encerrando...");
		for (int i = 0; i <= 2; i++) {
			if (connClientes[i] != null) {
				try {
					if (midlet.jogoEmAndamento != null) {
						Jogador j = midlet.jogoEmAndamento.getJogador(i + 2);
						if (j instanceof JogadorBT) {
							((JogadorBT) j).finaliza();
						}
					}
					outClientes[i].close();
					connClientes[i].close();
				} catch (IOException e) {
					// J� estava fechada, desencana
				}
			}
		}
		status = 'X';
		setModoSetup(false);
	}

	/**
	 * Coloca o servidor no modo "jogo" (n�o aceita conex�es e exibe a mesa de
	 * jogo) ou "setup" (exibe os usu�rios conectados e aceita novas conex�es
	 * apenas se houver vaga).
	 * <p>
	 * Esta opera��o atualiza o indicador <code>status</code> para A ou L (no
	 * modo setup) ou J (no modo jogo). Caso ele j� esteja previamente em X
	 * (encerramento), o notifier � desligado, independente de
	 * <code>isSetup</code>
	 * <p>
	 * 
	 * @param isSetup
	 *            true para modo "setup", false para modo "jogo"
	 */
	public synchronized void setModoSetup(boolean isSetup) {

		if ((!isSetup) || (status == 'X')) {

			// Se n�o for encerramento, � novo jogo
			if (status != 'X') {
				status = 'J';
				display.setCurrent(midlet.mesa);
			}

			// Desativa o notifier, impedindo novas conex�es
			if (scnServidor != null) {
				try {
					localDevice
							.setDiscoverable(DiscoveryAgent.NOT_DISCOVERABLE);
					scnServidor.close();
				} catch (IOException e) {
					// J� fechou, nada a fazer
				}
				scnServidor = null;
			}

		} else {

			if (status == 'J') {
				if (midlet.jogoEmAndamento != null) {
					midlet.encerraJogo(1, false);
				}
			}

			int n = getNumClientes();
			int modo = localDevice.getDiscoverable();
			status = n < 3 ? 'A' : 'L';

			// Se h� vaga e n�o estamos em listen, ativa o listen
			if ((n < 3) && (scnServidor == null)) {
				try {
					localDevice.setDiscoverable(DiscoveryAgent.GIAC);
					scnServidor = (StreamConnectionNotifier) Connector
							.open("btspp://localhost:" + UUID_BT.toString()
									+ ";name=miniTruco");
				} catch (IOException e) {
					MiniTruco.log("Erro server:");
					MiniTruco.log(e.toString());
					alerta("Erro Bluetooth", e.getMessage(), true);
					encerraSessaoBT();
				}
			}

			// Se n�o h� vaga e estamos em listen, desativa
			if ((n == 3) && (scnServidor != null)
					&& (modo != DiscoveryAgent.NOT_DISCOVERABLE)) {
				try {
					localDevice
							.setDiscoverable(DiscoveryAgent.NOT_DISCOVERABLE);
					scnServidor.close();
				} catch (IOException e) {
					// J� fechou, nada a fazer
				}
				scnServidor = null;
			}

			display.setCurrent(this);

		}
	}

	/**
	 * Atualiza os dados nos clientes conectados
	 */
	private void atualizaClientes() {

		// Monta o comando de dados no formato:
		// I apelido1|apelido2|apelido3|apelido4 regras
		StringBuffer sbComando = new StringBuffer("I ");
		for (int i = 0; i <= 3; i++) {
			sbComando.append(apelidos[i]);
			sbComando.append(i < 3 ? '|' : ' ');
		}
		sbComando.append(regras);
		sbComando.append(' ');
		String comando = sbComando.toString();
		// Envia a notifica��o para cada jogador (com sua posi��o)
		for (int i = 0; i <= 2; i++) {
			enviaMensagem(i, comando + (i + 2));
		}
	}

	public int getPosicaoMesa(int i) {
		// O servidor � sempre o primeiro, ent�o t� f�cil:
		return i;
	}

	/**
	 * Processa comandos de menu exclusivos do servidor
	 */
	public void commandAction(Command cmd, Displayable arg1) {
		super.commandAction(cmd, arg1);
		if (cmd.equals(ServidorBT.iniciarJogoCommand)) {
			// Bloqueia novas conex�es
			setModoSetup(false);
			// Cria um novo jogo e adiciona o jogador que est� no servidor
			Jogo jogo = new JogoLocal(regras.charAt(0) == 'T',
					regras.charAt(1) == 'T');
			midlet.jogadorHumano = new JogadorHumano(display, midlet.mesa);
			jogo.adiciona(midlet.jogadorHumano);
			// Adiciona jogadores para os outros slots
			for (int i = 0; i <= 2; i++) {
				if (connClientes[i] != null) {
					// Se h� algu�m neste slot, cria um JogadorBT para
					// represent�-lo
					jogo.adiciona(new JogadorBT(this));
				} else {
					// Se n�o h�, preenche com um JogadorCPU
					jogo.adiciona(new JogadorCPU("Sortear"));
				}
			}
			midlet.iniciaJogo(jogo);
		} else if (cmd.equals(ServidorBT.trocaParceiroCommand)) {
			// Enquanto rola essa dan�a da cadeira, n�o queremos ningu�m
			// se conectando, da� o synchornized.
			synchronized (this) {
				Object temp;
				temp = connClientes[2];
				connClientes[2] = connClientes[1];
				connClientes[1] = connClientes[0];
				connClientes[0] = (StreamConnection) temp;
				temp = outClientes[2];
				outClientes[2] = outClientes[1];
				outClientes[1] = outClientes[0];
				outClientes[0] = (OutputStream) temp;
				temp = apelidos[3];
				apelidos[3] = apelidos[2];
				apelidos[2] = apelidos[1];
				apelidos[1] = (String) temp;
				atualizaClientes();
				atualizaServidor();
			}
		} else if (cmd.equals(ServidorBT.inverteAdversariosCommand)) {
			// Idem acima
			synchronized (this) {
				Object temp;
				temp = connClientes[0];
				connClientes[0] = connClientes[2];
				connClientes[2] = (StreamConnection) temp;
				temp = outClientes[0];
				outClientes[0] = outClientes[2];
				outClientes[2] = (OutputStream) temp;
				temp = apelidos[1];
				apelidos[1] = apelidos[3];
				apelidos[3] = (String) temp;
				atualizaClientes();
				atualizaServidor();
			}
		}

	}
}
