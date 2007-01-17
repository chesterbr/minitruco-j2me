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
 * Recebe conex�es (via Bluetooth) de outros celulares-cliente, exibe suas
 * posi��es, configura e inicia a partida (criando os proxies JogadorBT para
 * cada jogador remoto).
 * 
 * @author Chester
 * 
 */
public class ServidorBT extends TelaBT {

	private static final char ENTER = '\n';

	/**
	 * Notificador do servidor (atrav�s do qual vamos aceitar as conex�es)
	 */
	private StreamConnectionNotifier scnServidor;

	/**
	 * Conexoes dos jogadores conectados nos slots 1 a 3.
	 * <p>
	 * Note que o "slot" 0 estar� sempre vazio, ele s� existe pra alinhar com o
	 * array apelidos[]
	 */
	StreamConnection[] connClientes = new StreamConnection[4];

	public ServidorBT(MiniTruco midlet) {
		// Exibe o form de apelido, que iniciar� a busca de clientes no ok
		super(midlet);
	}

	/**
	 * Recebe as conex�es
	 */
	public void run() {

		// Inicializa os apelidos (servidor na 1a. posi��o)
		apelidos[0] = apelido;
		for (int i = 1; i <= 3; i++)
			apelidos[i] = APELIDOS_CPU[i - 1];

		for (int i = 0; i <= 3; i++)
			System.out.println(apelidos[i]);

		// Registra o servi�o e recupera o objeto que ir� receber as conex�es
		try {
			localDevice.setDiscoverable(DiscoveryAgent.GIAC);
			scnServidor = (StreamConnectionNotifier) Connector
					.open("btspp://localhost:" + UUID_BT.toString());
		} catch (IOException e) {
			midlet.alerta("Erro Bluetooth", e.getMessage());
		}
		// TODO: adicionar o "name=minitruco", verificar se � filtr�vel
		// TODO: acrescentar um esquema para n�o aceitar mais conex�pes
		// quando estiver "lotado", mas voltar a aceitar se algum jogador
		// cair antes do in�cio

		Logger.debug("Server aguardando conexoes BT");
		mostraMsgAguarde = false;
		this.addCommand(iniciarJogoCommand);
		this.addCommand(voltarCommand);
		while (estaVivo) {
			repaint();
			serviceRepaints();
			StreamConnection c = null;
			try {
				// Aguarda uma conex�o e encaixa ela em alguma vaga
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
				// Se n�o tiver vaga, rejeita
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
	 * para todo mundo ficar com a mesma informa��)
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
		// Envia para cada jogador, adicionando, ao final, a posi��o do mesmo
		for (int i = 1; i <= 3; i++) {
			try {
				if (connClientes[i] != null) {
					// TODO: verificar se n�o � melhor abrir uma vez s� e
					// guardar
					OutputStream out = connClientes[i].openOutputStream();
					out.write(sbComando.toString().getBytes());
					out.write('2' + i);
					out.write(ENTER);
					out.close();
				}
			} catch (IOException e) {
				// Em caso de erro, desconecta o jogador (e termina, pois a
				// desconex�o vai re-atualizar os clientes)
				desconecta(i);
				return;
			}
		}
	}

	/**
	 * Fecha a conex�o com este cliente e libera o slot dele
	 * 
	 * @param posicao
	 *            posi��o da conex�o no array (0 a 2)
	 */
	private void desconecta(int posicao) {
		try {
			connClientes[posicao].close();
		} catch (IOException e) {
			// No prob, pode j� estar fechada mesmo
		}
		connClientes[posicao] = null;
		apelidos[posicao] = APELIDOS_CPU[posicao];
		atualizaClientes();
	}

	public int getPosicaoMesa(int i) {
		// O 1o. slot no servidor � sempre para o jogador-servidor, os outros
		// v�o na ordem, ent�o fica f�cil:
		return i + 1;
	}

	/**
	 * Processa comandos exclusivos do servidor
	 */
	public void commandAction(Command cmd, Displayable arg1) {
		super.commandAction(cmd, arg1);
		if (cmd.equals(iniciarJogoCommand)) {
			// Cria um novo jogo e adiciona o jogador que est� no servidor
			Jogo j = new JogoLocal(regras.charAt(0) == 'T',
					regras.charAt(1) == 'T');
			midlet.setJogo(j);
			j.adiciona(new JogadorHumano(display, midlet.mesa));
			Random r = new Random();
			// Adiciona jogadores para cada um dos slots remanescenets
			for (int i = 1; i <= 3; i++) {
				if (connClientes[i] != null) {
					// Se h� algu�m neste slot, cria um objeto JogadorBT para
					// represent�-lo dentro do jogo
					j.adiciona(new JogadorBT(connClientes[i]));
				} else {
					// Se n�o h� neste slot, preenche com um JogadorCPU

					// Sorteia uma estrat�gia (o while bizarro � pq eu n�o sei
					// onde est� o "Sortear" na lista)
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
