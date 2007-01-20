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
		super(midlet);
		// Usa as regras escolhidas pelo usuário
		this.regras = (midlet.cgRegras.isSelected(0) ? "T" : "F")
				+ (midlet.cgRegras.isSelected(1) ? "T" : "F");
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
		this.setCommandListener(this);
		while (estaVivo) {
			repaint();
			serviceRepaints();
			Thread.yield();
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
					out.write('1' + i);
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
		// O servidor é sempre o primeiro, e os outros vão na ordem, então tá
		// fácil:
		return i;
	}

	/**
	 * Processa comandos exclusivos do servidor
	 */
	public void commandAction(Command cmd, Displayable arg1) {
		super.commandAction(cmd, arg1);
		if (cmd.equals(iniciarJogoCommand)) {
			// Cria um novo jogo e adiciona o jogador que está no servidor
			Logger.debug("TEMP: regras="+regras);
			Jogo jogo = new JogoLocal(regras.charAt(0) == 'T',
					regras.charAt(1) == 'T');
			jogo.adiciona(new JogadorHumano(display, midlet.mesa));
			// Adiciona jogadores para os outros slots
			for (int i = 1; i <= 3; i++) {
				System.out.println(connClientes[i]);
				if (connClientes[i] != null) {
					// Se há alguém neste slot, cria um JogadorBT para
					// representá-lo
					jogo.adiciona(new JogadorBT(connClientes[i]));
				} else {
					// Se não há, preenche com um JogadorCPU
					jogo.adiciona(new JogadorCPU("Sortear"));
				}
			}
			midlet.iniciaJogo(jogo);
		}
	}

}
