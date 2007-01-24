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
	 * OutputSterams dos jogadores conectados
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
	 * Envia um comando para o cliente no slot especificado.
	 * <p>
	 * Se o slot espedificado estiver vazio, n�o faz nada.
	 * <p>
	 * Se o cliente der erro, processa sua desconex�o.
	 * 
	 * @param slot
	 *            �ndice do cliente em connClientes/outClientes
	 * @param comando
	 *            texto do comando/notifica��o a enviar
	 */
	public void enviaComando(int slot, String comando) {
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
				// Libera slot e atualiza status (dos remanescentes e do
				// servidor)
				connClientes[slot] = null;
				outClientes[slot] = null;
				apelidos[slot+1] = APELIDOS_CPU[slot];
				atualizaClientes();
				atualizaServidor();
			}
		}
	}

	/**
	 * Sinaliza que o servidor ainda est� em atividade
	 */
	protected boolean estaVivo = true;

	/**
	 * Loop da thread principal (que recebe e processa as conex�es dos clientes)
	 */
	public void run() {

		this.addCommand(voltarCommand);
		this.setCommandListener(this);

		// Inicializa os apelidos (servidor na 1a. posi��o)
		apelidos[0] = apelido;
		for (int i = 1; i <= 3; i++)
			apelidos[i] = APELIDOS_CPU[i - 1];

		// Incializa a thread de monitora��o dos clientes conectados
		Thread tm = new ThreadMonitoraClientes();
		tm.start();

		// Loop principal
		Logger.debug("Server aguardando conexoes BT");
		while (estaVivo) {

			// Atualiza o display (local e remoto)
			int numClientes = getNumClientes();
			atualizaServidor();
			atualizaClientes();

			// Espera surgir pelo menos um slot livre
			if (numClientes == 3) {
				aguardarConexoes(false);
				while (getNumClientes() == 3) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// n�o precisa tratar
					}
				}
			}

			// Aguarda uma conex�o e encaixa ela no slot livre
			aguardarConexoes(true);
			StreamConnection c = null;
			try {
				c = scnServidor.acceptAndOpen();
				for (int i = 0; i <= 2; i++) {
					if (connClientes[i] == null) {
						connClientes[i] = c;
						outClientes[i] = c.openOutputStream();
						apelidos[i+1] = RemoteDevice.getRemoteDevice(c)
								.getFriendlyName(false);
						c = null;
						break;
					}
				}
			} catch (IOException e) {
				if (c != null)
					try {
						c.close();
					} catch (IOException e2) {
						// Nada a fazer
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
			setStatusDisplay("Aguardando jogadores...");
			this.removeCommand(iniciarJogoCommand);
		} else {
			setStatusDisplay(null);
			this.addCommand(iniciarJogoCommand);
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
			while (estaVivo) {
				// Envia um comando vazio (apenas para testar a conex�o, e
				// processar qualquer desconex�o que tenha ocorrido)
				for (int i = 0; i <= 2; i++) {
					enviaComando(i, "");
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
	 * Interrompe o loop principal e quaisquer conex�es existentes,
	 */
	public void encerraSessaoBT() {
		setStatusDisplay("Encerrando...");
		estaVivo = false;
		aguardarConexoes(false);
		for (int i = 0; i <= 2; i++) {
			if (connClientes[i] != null) {
				try {
					connClientes[i].close();
				} catch (IOException e) {
					// J� estava fechada, desencana
				}
			}
		}
	}

	/**
	 * Permite/bloqueia a entrada de novas conex�es.
	 * <p>
	 * Caso o servidor j� esteja aguardando/parado, n�o faz nada.
	 * 
	 * @param aguardar
	 *            true para permitir, false para bloquear
	 */
	public void aguardarConexoes(boolean aguardar) {
		if (aguardar) {
			if (scnServidor == null) {
				try {
					localDevice.setDiscoverable(DiscoveryAgent.GIAC);
					// TODO: adicionar o "name=minitruco", verificar se �
					// filtr�vel
					scnServidor = (StreamConnectionNotifier) Connector
							.open("btspp://localhost:" + UUID_BT.toString());
				} catch (IOException e) {
					Logger.debug("Erro server:");
					Logger.debug(e.toString());
					encerraSessaoBT();
					midlet.alerta("Erro Bluetooth", e.getMessage());
				}
			}
		} else {
			if (scnServidor != null) {
				try {
					scnServidor.close();
				} catch (IOException e) {
					// J� fechou, nada a fazer
				}
				scnServidor = null;
			}

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
			enviaComando(i, comando + (i + 2));
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
		if (cmd.equals(iniciarJogoCommand)) {
			// Cria um novo jogo e adiciona o jogador que est� no servidor
			Jogo jogo = new JogoLocal(regras.charAt(0) == 'T',
					regras.charAt(1) == 'T');
			jogo.adiciona(new JogadorHumano(display, midlet.mesa));
			// Adiciona jogadores para os outros slots
			for (int i = 0; i <= 2; i++) {
				if (connClientes[i] != null) {
					// Se h� algu�m neste slot, cria um JogadorBT para
					// represent�-lo
					jogo.adiciona(new JogadorBT(this,i));
				} else {
					// Se n�o h�, preenche com um JogadorCPU
					jogo.adiciona(new JogadorCPU("Sortear"));
				}
			}
			midlet.iniciaJogo(jogo);
		}
	}

}
