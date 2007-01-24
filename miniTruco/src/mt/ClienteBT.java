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

/**
 * Conecta-se (via Bluetooth) num celular-servidor, exibindo a configuração da
 * mesa, além de cria o proxy do jogo (JogoBT) e o JogadorHumano conectado nele.
 * 
 * @author Chester
 * 
 */
public class ClienteBT extends TelaBT {

	public InputStream in;

	public OutputStream out;

	private JogoBT jogo;

	/**
	 * Posição deste cliente na mesa (determinada pelo servidor)
	 */
	private int posJogador;

	private boolean estaVivo = true;

	public ClienteBT(MiniTruco midlet) {
		// Exibe o form de apelido, que iniciará a busca de servidores no ok
		super(midlet);
	}

	public void run() {

		// Inicia a busca por celulares remotos
		setStatusDisplay("Procurando celulares...");
		DiscoveryAgent agente = localDevice.getDiscoveryAgent();
		MiniTrucoDiscoveryListener lsnr = new MiniTrucoDiscoveryListener(agente);
		try {
			agente.startInquiry(DiscoveryAgent.GIAC, lsnr);
		} catch (BluetoothStateException re) {
			setStatusDisplay("Erro:" + re.getMessage());
			return;
		}

		// Aguarda o final da busca
		while (!lsnr.terminou) {
			Thread.yield();
		}

		// Para cada celular encontrado, verifica se é um servidor e conecta
		StreamConnection conn = null;
		for (int i = 0; i < lsnr.devs.size(); i++) {
			RemoteDevice remdev = (RemoteDevice) lsnr.devs.elementAt(i);
			try {
				setStatusDisplay("Localizando jogo...");
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
				setStatusDisplay("Erro:" + e.getMessage());
				return;
			}
		}

		if (conn == null) {
			display.setCurrent(midlet.mesa);
			midlet
					.alerta("Jogo n\u00E3o encontrado",
							"Nenhum celular com jogo Bluetooth criado foi encontrado. Crie um jogo ou tente novamente");
		} else {
			// Loop principal: decodifica as notificações recebidas e as
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
								// Recupera as informações do servidor
								String[] tokens = split(parametros, ' ');
								apelidos = split(tokens[0], '|');
								regras = tokens[1];
								posJogador = Integer.parseInt(tokens[2]);
								// Atualiza as posições dos jogadores
								setStatusDisplay(null);
								break;
							case 'P':
								// Cria um o jogo remoto
								jogo = new JogoBT(this);
								// Adiciona o jogador na posição correta
								// (preenchendo as outras com dummies)
								for (int i = 1; i <= 4; i++) {
									if (i == posJogador) {
										jogo.adiciona(new JogadorHumano(
												display, midlet.mesa));
									} else {
										jogo.adiciona(new JogadorDummy());
									}
								}
								midlet.iniciaJogo(jogo);
								break;
							// Os outros eventos ocorrem durante o jogo,
							// i.e., quando o Jogador local já existe, logo,
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
				// É normal dar um erro de I/O quando o usuário pede pra
				// desconectar (porque o loop vai tentar ler a última linha). Só
				// vamos alertar se a desconexão foi forçada, ou se não foi
				// possível abrir os streams de I/O
				if ((in == null) || (out == null) || estaVivo) {
					alerta("Erro de I/O", e.getMessage(), true);
				}
				// TODO finalizaServidor()?
				return;
			} finally {
				// Se saiu do loop e ainda estava "vivo", foi desconectado,
				// avisa
				if (estaVivo) {
					alerta("Desconectado",
							"Você foi desconectado do servidor.", true);
				}
				// TODO finalizaServidor();
			}
		}

	}

	public void encerraSessaoBT() {
		// TODO implementar
	}

	/**
	 * Responde à busca de aparelho, e, para cada aparelho, à busca do serviço
	 * "servidor de miniTruco".
	 * 
	 * @author Chester
	 */
	class MiniTrucoDiscoveryListener implements DiscoveryListener {

		/**
		 * Agente que iniciou esta busca (útil para interrompê-la quando
		 * encontramos um servidor rodando o serviço
		 */
		DiscoveryAgent agent;

		/**
		 * Dispositivos encontrados
		 */
		Vector devs = new Vector();

		/**
		 * Serviço "servidor miniTruco" encontrado
		 */
		ServiceRecord srServidor = null;

		/**
		 * Indica que a busca foi concluída
		 */
		boolean terminou = false;

		/**
		 * Conexão com o servidor, se algum for encontrado
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
		 * Adiciona um dispositivo encontrado à lista
		 */
		public void deviceDiscovered(RemoteDevice arg0, DeviceClass arg1) {
			devs.addElement(arg0);
			try {
				System.out.println("achou" + arg0.getFriendlyName(false));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void servicesDiscovered(int idBusca, ServiceRecord[] servicos) {
			// Só vai haver um serviço de truco por celular mesmo
			if (servicos.length > 0) {
				String url = servicos[0].getConnectionURL(
						ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
				try {
					// Tenta conectar, e se der certo, encerra as buscas
					RemoteDevice dev = servicos[0].getHostDevice();
					String nome;
					if (dev != null) {
						nome = dev.getFriendlyName(false);
						setStatusDisplay("Tentando " + nome);
						conn = (StreamConnection) Connector.open(url);
						setStatusDisplay("Conectado em " + nome + "!");
						agent.cancelServiceSearch(idBusca);
					}
				} catch (IOException e) {
					// Deu errado, desencana e vai pro próximo
					Logger.debug(e.getMessage());
					Logger.debug(url);
				}
			}
		}

		/**
		 * Notifica conclusão da busca de dispositivos
		 */
		public void inquiryCompleted(int arg0) {
			terminou = true;
		}

		/**
		 * Notifica conclusão da busca de serviço
		 */
		public void serviceSearchCompleted(int arg0, int arg1) {
			terminou = true;
		}

	}

	public int getPosicaoMesa(int i) {
		// O 1o. slot no cliente é ele mesmo, logo este valor depende da posição
		// do jogador no serivdor. A conta abaixo garante que o slot
		// correspondente à posição do jogador (i.e., o slot posJogador)
		// retorne sempre 1, o seguinte 2, o outro 3 e o último 4, "dando a
		// volta" se necessário
		int retorno = i - posJogador + 1;
		if (retorno < 1)
			retorno += 4;
		return retorno;
	}
}
