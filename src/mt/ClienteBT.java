package mt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.lcdui.Display;

/**
 * Conecta-se (via Bluetooth) num celular-servidor, exibindo a configura��o da
 * mesa, al�m de cria o proxy do jogo (JogoBT) e o JogadorHumano conectado nele.
 * 
 * @author Chester
 * 
 */
public class ClienteBT extends TelaBT {

	StreamConnection conn = null;

	public InputStream in;

	public OutputStream out;

	private JogoBT jogo;

	/**
	 * Posi��o que o jogador local ocupa no jogo (do servidor)
	 * <p>
	 * A posi��o dele na mesa � diferente.
	 * 
	 * @see ClienteBT#getPosicaoMesa(int)
	 */
	private int posJogador;

	/**
	 * Recupera a posi��o "visual" correspondente a uma posi��o de jogo (i.e.,
	 * uma posi��o no servidor)
	 * <p>
	 * A id�ia � que o jogador local fique sempre na parte inferior da tela,
	 * ent�o o m�todo retorna 1 para o jogador local, 2 para quem est� � direita
	 * dele, etc.
	 * 
	 * @param i
	 *            posi��o (no servidor) do jogador que queremos consultar
	 */
	public int getPosicaoMesa(int i) {
		int retorno = i - posJogador + 1;
		if (retorno < 1)
			retorno += 4;
		return retorno;
	}

	private boolean estaVivo = true;

	public ClienteBT(MiniTruco midlet) {
		// Exibe o form de apelido, que iniciar� a busca de servidores no ok
		super(midlet);
	}

	public void run() {

		// Inicia a busca por celulares remotos
		setTelaMsg("Procurando celulares...");
		DiscoveryAgent agente = localDevice.getDiscoveryAgent();
		MiniTrucoDiscoveryListener lsnr = new MiniTrucoDiscoveryListener(agente);
		try {
			agente.startInquiry(DiscoveryAgent.GIAC, lsnr);
		} catch (BluetoothStateException re) {
			setTelaMsg("Erro:" + re.getMessage());
			return;
		}

		// Aguarda o final da busca
		while (!lsnr.terminou) {
			Thread.yield();
		}

		// Para cada celular encontrado, verifica se � um servidor e conecta
		for (int i = 0; i < lsnr.devs.size(); i++) {
			RemoteDevice remdev = (RemoteDevice) lsnr.devs.elementAt(i);
			try {
				setTelaMsg("Localizando jogo...");
				MiniTrucoDiscoveryListener servicoListener = new MiniTrucoDiscoveryListener(
						agente);
				agente.searchServices(null, new UUID[] { UUID_BT }, remdev,
						servicoListener);
				while (!servicoListener.terminou) {
					Thread.yield();
				}
				if (servicoListener.conn != null) {
					conn = servicoListener.conn;
					break;
				}
			} catch (BluetoothStateException e) {
				setTelaMsg("Erro:" + e.getMessage());
				return;
			}
		}

		if (conn == null) {
			if (estaVivo) {
				display.setCurrent(midlet.mesa);
				midlet
						.alerta(
								"Jogo n\u00E3o encontrado",
								"Nenhum celular com jogo Bluetooth criado foi encontrado. Crie um jogo ou tente novamente");
			}
		} else {
			// Loop principal: decodifica as notifica��es recebidas e as
			// processa (ou encaminha ao JogoBT, se estivermos em jogo)
			int c;
			StringBuffer sbLinha = new StringBuffer();
			try {
				in = conn.openInputStream();
				out = conn.openOutputStream();
				while (estaVivo && (c = in.read()) != -1) {
					if (c == ENTER) {
						if (sbLinha.length() > 0) {
							Logger.debug(sbLinha.toString());
							char tipoNotificacao = sbLinha.charAt(0);
							String parametros = sbLinha.delete(0, 2).toString();
							switch (tipoNotificacao) {
							case 'I':
								// Encerra qualquer jogo em andamento
								if (jogo != null) {
									midlet.encerraJogo(jogo.getJogadorHumano()
											.getPosicao(), false);
									display.setCurrent(this);
									jogo = null;
								}
								// Exibe as informa��es recebidas fora do jogo
								String[] tokens = split(parametros, ' ');
								apelidos = split(tokens[0], '|');
								regras = tokens[1];
								posJogador = Integer.parseInt(tokens[2]);
								setTelaMsg(null);
								break;
							case 'P':
								// Cria um o jogo remoto
								jogo = new JogoBT(this);
								// Adiciona o jogador na posi��o correta
								// (preenchendo as outras com dummies)
								for (int i = 1; i <= 4; i++) {
									if (i == posJogador) {
										midlet.jogadorHumano = new JogadorHumano(
												display, midlet.mesa);
										jogo.adiciona(midlet.jogadorHumano);

									} else {
										jogo.adiciona(new JogadorDummy());
									}
								}
								midlet.iniciaJogo(jogo);
								break;
							// Os outros eventos ocorrem durante o jogo,
							// i.e., quando o Jogador local j� existe, logo,
							// vamos encaminhar para o objeto JogoRemoto
							default:
								jogo.processaNotificacao(tipoNotificacao,
										parametros);
							}
						}
						sbLinha.setLength(0);
					} else {
						sbLinha.append((char) c);
					}
				}
			} catch (IOException e) {
				if (estaVivo) {
					alerta("Erro de I/O", e.getMessage(), true);
				}
			} finally {
				Logger.debug("saiu do loop");
				// Se a desconex�o foi for�ada, avisa e sai
				if (estaVivo) {
					alerta("Desconectado",
							"Voc� foi desconectado do servidor.", true);
					super.commandAction(voltarCommand, this);
				}
			}
		}

	}

	public void encerraSessaoBT() {
		// TODO: interromper, � muito chato isso
		// Obs.: poderia interromper o inquiry/discovery aqui, mas a flag j�
		// garante que vai dar timeout de qualquer jeito. Isso tem um efeito
		// colateral:se voce sair e entrar na busca muito rapido pode dar um
		// erro de BluetoothState, mas acho que � toler�vel.

		// Obs.: fechar o conn invalida o in() e out() (cujos close() n�o fazem
		// nada, cf. Javadoc do MIDP)
		
		Logger.debug("encerra sessao bt");
		estaVivo = false;
		if (conn != null) {
			try {
				in.close();
				out.close();
				conn.close();
				Logger.debug("fechou conexao cliente");
			} catch (IOException e) {
				// Ja estava fechado
			}
		}
	}

	/**
	 * Responde � busca de aparelho, e, para cada aparelho, � busca do servi�o
	 * "servidor de miniTruco".
	 * 
	 * @author Chester
	 */
	class MiniTrucoDiscoveryListener implements DiscoveryListener {

		/**
		 * Agente que iniciou esta busca (�til para interromp�-la quando
		 * encontramos um servidor rodando o servi�o
		 */
		DiscoveryAgent agent;

		/**
		 * Dispositivos encontrados
		 */
		Vector devs = new Vector();

		/**
		 * Servi�o "servidor miniTruco" encontrado
		 */
		ServiceRecord srServidor = null;

		/**
		 * Indica que a busca foi conclu�da
		 */
		boolean terminou = false;

		/**
		 * Conex�o com o servidor, se algum for encontrado
		 */
		public StreamConnection conn = null;

		/**
		 * Cria um novo listener, memorizando o agente originador
		 * 
		 * @param agent
		 */
		public MiniTrucoDiscoveryListener(DiscoveryAgent agent) {
			super();
			this.agent = agent;
		}

		/**
		 * Achou um celular (potencialmente servidor), adiciona � lista
		 */
		public void deviceDiscovered(RemoteDevice arg0, DeviceClass arg1) {
			if (estaVivo) {
				devs.addElement(arg0);
			}
		}

		/**
		 * Achou um servi�o (potencialmente um jogo miniTruco aberto), tenta
		 * conectar
		 */
		public void servicesDiscovered(int idBusca, ServiceRecord[] servicos) {
			// S� vai haver um servi�o de truco por celular mesmo
			if (estaVivo && servicos.length > 0) {
				String url = servicos[0].getConnectionURL(
						ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
				try {
					// Tenta conectar, e se der certo, encerra as buscas
					RemoteDevice dev = servicos[0].getHostDevice();
					String nome;
					if (dev != null) {
						nome = dev.getFriendlyName(false);
						setTelaMsg("Tentando " + nome);
						conn = (StreamConnection) Connector.open(url);
						setTelaMsg("Conectado em " + nome + "!");
						agent.cancelServiceSearch(idBusca);
					}
				} catch (IOException e) {
					// Deu errado, desencana e vai pro pr�ximo
					Logger.debug(e.getMessage());
					Logger.debug(url);
				}
			}
		}

		/**
		 * Notifica conclus�o da busca de dispositivos
		 */
		public void inquiryCompleted(int arg0) {
			terminou = true;
		}

		/**
		 * Notifica conclus�o da busca de servi�o
		 */
		public void serviceSearchCompleted(int arg0, int arg1) {
			terminou = true;
		}

	}

}
