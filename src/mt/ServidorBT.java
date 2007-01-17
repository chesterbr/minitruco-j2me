package mt;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.RemoteDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;

/**
 * Recebe conexões (via Bluetooth) de outros celulares-cliente, exibe suas
 * posições, configura e inicia a partida (criando os proxies JogadorBT para
 * cada jogador remoto).
 * 
 * @author Chester
 * 
 */
public class ServidorBT extends TelaBT {

	private static final char ENTER = '\n';

	/**
	 * Notificador do servidor (através do qual vamos aceitar as conexões)
	 */
	private StreamConnectionNotifier scnServidor;

	/**
	 * Conexoes dos jogadores conectados nos slots 1 a 3.
	 * <p>
	 * Note que o "slot" 0 estará sempre vazio, ele só existe pra alinhar com o
	 * array apelidos[]
	 */
	StreamConnection[] connClientes = new StreamConnection[4];

	public ServidorBT(MiniTruco midlet) {
		// Exibe o form de apelido, que iniciará a busca de clientes no ok
		super(midlet);
	}

	/**
	 * Recebe as conexões
	 */
	public void run() {

		// Inicializa os apelidos (servidor na 1a. posição)
		apelidos[0] = apelido;
		for (int i = 1; i <= 3; i++)
			apelidos[i] = APELIDOS_CPU[i - 1];

		for (int i = 0; i <= 3; i++)
			System.out.println(apelidos[i]);

		// Registra o serviço e recupera o objeto que irá receber as conexões
		try {
			localDevice.setDiscoverable(DiscoveryAgent.GIAC);
			scnServidor = (StreamConnectionNotifier) Connector
					.open("btspp://localhost:" + UUID_BT.toString());
		} catch (IOException e) {
			midlet.alerta("Erro Bluetooth", e.getMessage());
		}
		// TODO: adicionar o "name=minitruco", verificar se é filtrável
		// TODO: acrescentar um esquema para não aceitar mais conexõpes
		// quando estiver "lotado", mas voltar a aceitar se algum jogador
		// cair antes do início

		Logger.debug("Server aguardando conexoes BT");
		mostraMsgAguarde = false;
		this.addCommand(iniciarJogoCommand);
		this.addCommand(voltarCommand);
		while (estaVivo) {
			repaint();
			serviceRepaints();
			StreamConnection c = null;
			try {
				// Aguarda uma conexão e encaixa ela em alguma vaga
				c = scnServidor.acceptAndOpen();
				for (int i = 1; i <= 3; i++) {
					if (connClientes[i] == null) {
						connClientes[i] = c;
						apelidos[i] = RemoteDevice.getRemoteDevice(c)
								.getFriendlyName(false);
						c = null;
						atualizaClientes();
						break;
					}
				}
				// Se não tiver vaga, rejeita
				if (c != null) {
					c.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
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
	 * Atualiza os dados nos clientes conectados (e re-pinta a tela do servidor,
	 * para todo mundo ficar com a mesma informaçõ)
	 */
	private void atualizaClientes() {

		// Atualiza a tela do servidor
		repaint();

		// Monta o comando de dados no formato:
		// I apelido1|apelido2|apelido3|apelido4 regras
		StringBuffer sbComando = new StringBuffer("I ");
		for (int i = 0; i <= 3; i++) {
			sbComando.append(apelidos[i]);
			sbComando.append(i < 3 ? '|' : ' ');
		}
		sbComando.append(regras);
		sbComando.append(' ');
		// Envia para cada jogador, adicionando, ao final, a posição do mesmo
		for (int i = 1; i <= 3; i++) {
			try {
				if (connClientes[i] != null) {
					// TODO: verificar se não é melhor abrir uma vez só e
					// guardar
					OutputStream out = connClientes[i].openOutputStream();
					out.write(sbComando.toString().getBytes());
					out.write('2' + i);
					out.write(ENTER);
					out.close();
				}
			} catch (IOException e) {
				// Em caso de erro, desconecta o jogador (e termina, pois a
				// desconexão vai re-atualizar os clientes)
				desconecta(i);
				return;
			}
		}
	}

	/**
	 * Fecha a conexão com este cliente e libera o slot dele
	 * 
	 * @param posicao
	 *            posição da conexão no array (0 a 2)
	 */
	private void desconecta(int posicao) {
		try {
			connClientes[posicao].close();
		} catch (IOException e) {
			// No prob, pode já estar fechada mesmo
		}
		connClientes[posicao] = null;
		apelidos[posicao] = APELIDOS_CPU[posicao];
		atualizaClientes();
	}

	public int getPosicaoMesa(int i) {
		// O 1o. slot no servidor é sempre para o jogador-servidor, os outros
		// vão na ordem, então fica fácil:
		return i + 1;
	}

	/**
	 * Processa comandos exclusivos do servidor
	 */
	public void commandAction(Command cmd, Displayable arg1) {
		super.commandAction(cmd, arg1);
		if (cmd.equals(iniciarJogoCommand)) {
			// Cria um novo jogo e adiciona o jogador que está no servidor
			Jogo j = new JogoLocal(regras.charAt(0) == 'T',
					regras.charAt(1) == 'T');
			midlet.setJogo(j);
			j.adiciona(new JogadorHumano(display, midlet.mesa));
			Random r = new Random();
			// Adiciona jogadores para cada um dos slots remanescenets
			for (int i = 1; i <= 3; i++) {
				if (connClientes[i] != null) {
					// Se há alguém neste slot, cria um objeto JogadorBT para
					// representá-lo dentro do jogo
					j.adiciona(new JogadorBT(connClientes[i]));
				} else {
					// Se não há neste slot, preenche com um JogadorCPU

					// Sorteia uma estratégia (o while bizarro é pq eu não sei
					// onde está o "Sortear" na lista)
					// TODO melhorar essa tosqueira? integrar com o main? Mandar
					// pra classe Estrategia?
					String nome = "Sortear";
					while (nome.equals("Sortear")) {
						nome = MiniTruco.OPCOES_ESTRATEGIAS[(r.nextInt() >>> 1)
								% (MiniTruco.OPCOES_ESTRATEGIAS.length)];
					}
					j.adiciona(new JogadorCPU(midlet
							.criaEstrategiaPeloNome(nome)));
				}
			}
			// Daqui em diante o jogo local "assume"
			// TODO: ver como vai fazer aqui (provavelmente aguardar o fim do jogo)
			
		}
	}

}
