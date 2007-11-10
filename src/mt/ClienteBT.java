package mt;

/*
 * Copyright © 2005-2007 Carlos Duarte do Nascimento (Chester)
 * cd@pobox.com
 * 
 * Este programa é um software livre; você pode redistribui-lo e/ou 
 * modifica-lo dentro dos termos da Licença Pública Geral GNU como 
 * publicada pela Fundação do Software Livre (FSF); na versão 3 da 
 * Licença, ou (na sua opnião) qualquer versão.
 *
 * Este programa é distribuido na esperança que possa ser util, 
 * mas SEM NENHUMA GARANTIA; sem uma garantia implicita de ADEQUAÇÂO
 * a qualquer MERCADO ou APLICAÇÃO EM PARTICULAR. Veja a Licença
 * Pública Geral GNU para maiores detalhes.
 *
 * Você deve ter recebido uma cópia da Licença Pública Geral GNU
 * junto com este programa, se não, escreva para a Fundação do Software
 * Livre(FSF) Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

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
	 * Indica que a busca (de serviço ou no celular) foi concluída
	 */
	boolean terminou = false;	

	/**
	 * Dispositivos encontrados
	 */
	Vector devs = new Vector();

	/**
	 * Serviço "servidor miniTruco" encontrado
	 */
	ServiceRecord srServidor = null;

	/**
	 * Conexão com o servidor
	 */
	public StreamConnection conn = null;

	/**
	 * Posição que o jogador local ocupa no jogo (do servidor)
	 * <p>
	 * A posição dele na mesa é diferente.
	 * 
	 * @see ClienteBT#getPosicaoMesa(int)
	 */
	private int posJogador;

	/**
	 * Recupera a posição "visual" correspondente a uma posição de jogo (i.e.,
	 * uma posição no servidor)
	 * <p>
	 * A idéia é que o jogador local fique sempre na parte inferior da tela,
	 * então o método retorna 1 para o jogador local, 2 para quem está à direita
	 * dele, etc.
	 * 
	 * @param i
	 *            posição (no servidor) do jogador que queremos consultar
	 */
	public int getPosicaoMesa(int i) {
		int retorno = i - posJogador + 1;
		if (retorno < 1)
			retorno += 4;
		return retorno;
	}

	boolean estaVivo = true;

	/**
	 * Agente que iniciou a busca por celular ou serviço
	 */
	private DiscoveryAgent agente;

	/**
	 * Responde aos eventos gerados pela busca por celular ou serviço
	 */
	private DiscoveryListener listener;

	/**
	 * ID da busca de serviço iniciada (necessário para cancelar se o usuário
	 * volta para o menu antes de terminar)
	 */
	private int idBuscaServico;

	public ClienteBT(MiniTruco midlet) {
		super(midlet);
	}

	public void run() {

		// Inicia a busca por celulares remotos
		setTelaMsg("Procurando celulares...");
		agente = localDevice.getDiscoveryAgent();
		listener = new ClienteBTListener();
		terminou = false;
		log("Iniciando busca de servidores");
		try {
			agente.startInquiry(DiscoveryAgent.GIAC, listener);
		} catch (BluetoothStateException re) {
			setTelaMsg("Erro:" + re.getMessage());
			return;
		}

		// Aguarda o final da busca
		while (!terminou) {
			Thread.yield();
			if (!estaVivo) {
				return; // Usuário cancelou
			}
		}
		log("finalizou busca de servidores");

		// Para cada celular encontrado, verifica se é um servidor e conecta
		for (int i = 0; i < devs.size(); i++) {
			RemoteDevice remdev = (RemoteDevice) devs.elementAt(i);
			try {
				// Recupera o nome do celular, se der
				String nome = null;
				try {
					nome = remdev.getFriendlyName(true);
				} catch (IOException e) {
					nome = null;
				}
				if (nome == null)
					nome = "celular";
				// Dá um feedback
				setTelaMsg("Consultando " + nome);
				log("Consultando " + nome);
				terminou = false;
				idBuscaServico = agente.searchServices(null,
						new UUID[] { UUID_BT }, remdev, listener);
				while (!terminou) {
					Thread.yield();
					if (!estaVivo) {
						return; // Usuário cancelou
					}
				}
			} catch (BluetoothStateException e) {
				setTelaMsg("Erro:" + e.getMessage());
				return;
			}
			// Se conectou num jogo, não precisa mais procurar
			if (conn != null)
				break;
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
			// Loop principal: decodifica as notificações recebidas e as
			// processa (ou encaminha ao JogoBT, se estivermos em jogo)
			int c;
			StringBuffer sbLinha = new StringBuffer();
			try {
				in = conn.openInputStream();
				out = conn.openOutputStream();
				while (estaVivo && (c = in.read()) != -1) {
					if (c == TelaBT.SEPARADOR_REC) {
						if (sbLinha.length() > 0) {
							log(sbLinha.toString());
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
								// Exibe as informações recebidas fora do jogo
								String[] tokens = split(parametros, ' ');
								apelidos = split(tokens[0], '|');
								regras = tokens[1];
								posJogador = Integer.parseInt(tokens[2]);
								setTelaMsg(null);
								break;
							case 'P':
								// Cria um o jogo remoto
								jogo = new JogoBT(this);
								// Adiciona o jogador na posição correta
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
							// i.e., quando o Jogador local já existe, logo,
							// vamos encaminhar para o objeto JogoRemoto
							default:
								jogo.processaNotificacao(tipoNotificacao,
										parametros);
							}
							sbLinha.setLength(0);
						}
					} else {
						sbLinha.append((char) c);
					}
				}
			} catch (IOException e) {
				if (estaVivo) {
					alerta("Erro de I/O", e.getMessage(), true);
				}
			} finally {
				log("saiu do loop");
				// Se a desconexão foi forçada, avisa e sai
				if (estaVivo) {
					alerta("Desconectado",
							"Você foi desconectado do servidor.", true);
					super.commandAction(voltarCommand, this);
				}
			}
		}

	}

	public void encerraSessaoBT() {

		// Sinaliza o encerramento
		estaVivo = false;

		// Interrompe procuras em andamento, se existirem
		agente.cancelInquiry(listener);
		agente.cancelServiceSearch(idBuscaServico);

		// Fecha a conexão, se existir
		if (conn != null) {
			try {
				// Obs.: fechar o conn invalida o in() e out() (cujos close()
				// não fazem nada, cf. Javadoc do MIDP), por isso não fiz try...
				// catch spearados
				in.close();
				out.close();
				conn.close();
				log("fechou conexao cliente");
			} catch (IOException e) {
				// Ja estava fechado
			}
		}
	}

	/**
	 * Responde ao eventos gerados pela busca por aparelhos (e, para cada
	 * aparelho, pela busca do serviço "servidor de miniTruco").
	 * 
	 * @author Chester
	 */
	class ClienteBTListener implements DiscoveryListener {

		/**
		 * Achou um celular (potencialmente servidor), adiciona à lista
		 */
		public void deviceDiscovered(RemoteDevice arg0, DeviceClass arg1) {
			if (estaVivo) {
				devs.addElement(arg0);
				log("encontrou celular ID "+arg0.getBluetoothAddress());
			}
		}

		/**
		 * Achou um serviço (potencialmente um jogo miniTruco aberto), tenta
		 * conectar
		 */
		public void servicesDiscovered(int idBusca, ServiceRecord[] servicos) {
			// Só vai haver um serviço de truco por celular mesmo
			if (estaVivo && servicos.length > 0) {
				log("Encontrou servico truco");
				String url = servicos[0].getConnectionURL(
						ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
				try {
					// Tenta conectar, e se der certo, encerra as buscas
					RemoteDevice dev = servicos[0].getHostDevice();
					String nome;
					if (dev != null) {
						nome = dev.getFriendlyName(false);
						setTelaMsg("Tentando " + nome + ", url="+ url);
						conn = (StreamConnection) Connector.open(url);
						log("conectou em "+nome+", cancelando busca");
						setTelaMsg("Conectado em " + nome + "!");
						agente.cancelServiceSearch(idBusca);
						log("cancelou busca");
					}
				} catch (IOException e) {
					// Deu errado, desencana e vai pro próximo
					log(e.getMessage());
					log(url);					
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

}
