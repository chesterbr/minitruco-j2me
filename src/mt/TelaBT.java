package mt;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.TextField;

/**
 * Decorator para o servidor e o cliente BlueTooth código para exibição dos
 * jogadores conectados, constantes e outros elementos comnus aos dois lados.
 * <p>
 * A procura/oferta do serviço de jogo é feita pelas classes descendentes.
 * 
 * @author Chester
 * 
 */
public abstract class TelaBT extends Canvas implements CommandListener,
		Runnable {

	protected static final Command okApelidoCommand = new Command("Ok",
			Command.SCREEN, 1);

	protected static final Command voltarCommand = new Command("Voltar",
			Command.STOP, 999);

	/**
	 * Quebra de linha enviada tanto no sentido client->server quanto no
	 * server->client.
	 * <p>
	 * Poderia usar só \r ou só \n, mas enviar dois caracteres ajuda com
	 * celulares que enroscam no último caractere (ex.: a combinação Nokia 6600 -
	 * Motorla L6) sem prejudicar os demais (já que as rotinas de leitura
	 * ignoram linhas em branco)
	 */
	protected static final byte[] ENTER = "\r\n".getBytes();

	/**
	 * Fonte para a mensagem de "Aguarde"
	 */
	private static final Font fonteAguarde = Font.getFont(
			Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL);

	/**
	 * Fonte para os nomes dos jogadores "normais"
	 */
	private static final Font fonteNomes = Font.getFont(Font.FACE_PROPORTIONAL,
			Font.STYLE_PLAIN, Font.SIZE_SMALL);

	/**
	 * Fonte para o quadro de informações da sala
	 */
	private static final Font fonteInfo = Font.getFont(Font.FACE_PROPORTIONAL,
			Font.STYLE_ITALIC, Font.SIZE_SMALL);

	/**
	 * Identificador único Bluetooth do "serviço miniTruco"
	 */
	public static final UUID UUID_BT = new UUID(
			"3B175368ABB411DBA508C2B155D89593", false);

	protected static final String[] APELIDOS_CPU = { "CPU1", "CPU2", "CPU3" };

	/**
	 * Referência ao jogo em execução
	 */
	protected MiniTruco midlet;

	/**
	 * Referência à tela do jogo em execução
	 */
	protected Display display;

	/**
	 * Campo texto do apleido do jogador
	 */
	TextField txtApelido;

	/**
	 * Permite acessar as capacidades bluetooth do celular
	 */
	LocalDevice localDevice;

	/**
	 * Apelidos dos jogadores nas quatro posições da mesa.
	 */
	protected String[] apelidos = new String[4];

	/**
	 * Regras (string de 2 caracteres T/F, indicando baralho limpo e manilha
	 * velha, nesta ordem) para o jogo a iniciar
	 */
	public String regras;

	/**
	 * Mensagem exibida quando ainda não temos mesa (ou null para exibir a mesa)
	 */
	private String msgDisplay = "";

	/**
	 * Define o que será mostrado na tela (pode ser uma mensagem ou os jogadores
	 * conectados) e a atualiza.
	 * <p>
	 * Caso haja outro Displayable em exibição, solicita a mudança para este.
	 * 
	 * @param msg
	 *            Mensagem a ser exibida. Se for <code>null</code>, exibe a
	 *            mesa com os jogadores posicionados.
	 */
	protected void setTelaMsg(String msg) {
		display.setCurrent(this);
		Thread.yield();
		msgDisplay = msg;
		repaint();
		serviceRepaints();
	}

	public TelaBT(MiniTruco midlet) {

		// Guarda o display da MIDlet (vamos precisar dele pra mostrar forms e
		// alerts) e uma referência a ela.
		this.display = Display.getDisplay(midlet);
		this.midlet = midlet;

		// Recupera o dispositivo local (que é o ponto de entrada para as
		// comunicações bluetooth
		try {
			localDevice = LocalDevice.getLocalDevice();
		} catch (BluetoothStateException e) {
			MiniTruco.log(e.toString());
			alerta("Erro Bluetooth", e.getMessage());
			return;
		} catch (RuntimeException re) {
			// Esse catch é um pouco abrangente, mas é a primeira chamada a
			// classes bt, assim, se for dar algum erro bizarro, é aqui
			MiniTruco.log("erro runtime bt");
			MiniTruco.log(re.toString());
			alerta("Erro Runtime", re.getMessage());
			return;
		}

		// Inicia e mostra a tela básica (comando voltar)
		addCommand(voltarCommand);
		setCommandListener(this);
		display.setCurrent(this);

		// Inicia o oferecimento/procura do serviço (conforme o caso)
		(new Thread(this)).start();

	}

	public void commandAction(Command cmd, Displayable arg1) {
		if (cmd.equals(voltarCommand)) {
			// Sinaliza a finalização para a thread, encerra qualquer partida
			// pendente e volta ao menu
			encerraSessaoBT();
			midlet.telaBT = null;
			midlet.encerraJogo(0, true);
		}
	}

	/**
	 * Encerra a sessão (cliente ou servidor), liberando quaisquer recursos que
	 * estejam em uso.
	 */
	public abstract void encerraSessaoBT();

	/**
	 * Exibe um alerta e aguarda o "ok"
	 * 
	 * @param titulo
	 *            Título da janela
	 * @param texto
	 *            Texto do alerta
	 */
	public void alerta(String titulo, String texto) {
		alerta(titulo, texto, false);
	}

	/**
	 * Exibe um alerta
	 * 
	 * @param titulo
	 *            Título da janela
	 * @param texto
	 *            Texto do alerta
	 * @param bloqueia
	 *            true para bloquear até o usuário dar o "ok", false para exibir
	 *            e continuar rodando
	 */
	public void alerta(String titulo, String texto, boolean bloqueia) {
		Alert a = new Alert(titulo);
		a.setString(texto);
		a.setType(AlertType.INFO);
		a.setTimeout(Alert.FOREVER);
		display.setCurrent(a);
		if (bloqueia) {
			do {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// Nevermind, apenas aguardando...
				}
			} while (display.getCurrent().equals(a));
		}
	}

	/**
	 * Mostra os jogadores conectados
	 */
	protected void paint(Graphics g) {

		g.setColor(0x0000FF00);
		g.fillRect(0, 0, getWidth(), getHeight());

		if (msgDisplay == null) {

			// Nomes dos jogadores

			int topoNomes = fonteInfo.getHeight();
			int alturaNomes = getHeight() - topoNomes;

			for (int i = 0; i <= 3; i++) {

				String nome = apelidos[i];
				if (nome != null && !"".equals(nome)) {

					// Decide onde escrever
					int pos = getPosicaoMesa(i + 1);

					// Escreve
					g.setColor(0x00000000);
					g.setFont(fonteNomes);
					switch (pos) {
					case 1:
						g.drawString(nome, getWidth() / 2, getHeight(),
								Graphics.HCENTER | Graphics.BOTTOM);
						break;
					case 2:
						g.drawString(nome, getWidth() - 1, topoNomes
								+ alturaNomes / 2, Graphics.RIGHT
								| Graphics.TOP);
						break;
					case 3:
						g.drawString(nome, getWidth() / 2, topoNomes,
								Graphics.HCENTER | Graphics.TOP);
						break;
					case 4:
						g.drawString(nome, 0, topoNomes + alturaNomes / 2,
								Graphics.LEFT | Graphics.BOTTOM);
						break;
					}
				}
			}

			// Info da sala (acima dos nomes)

			g.setColor(0x00C0C0C0);
			g.fillRect(0, 0, getWidth(), topoNomes);

			// String linha1 = "SALA " + numSala;
			String linha2 = (regras.charAt(0) == 'T' ? "b.limpo / "
					: "b.sujo /")
					+ (regras.charAt(1) == 'T' ? "m.velha " : "m.nova");

			g.setColor(0x00000000);
			g.setFont(fonteInfo);
			// g.drawString(linha1, 1, 0, Graphics.TOP | Graphics.LEFT);
			g.drawString(linha2, getWidth() - 2, 0, Graphics.TOP
					| Graphics.RIGHT);

		} else {
			// Se não tiver nada pra mostrar, manda a mensagem de aguarde
			g.setColor(0x0000FF00);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(0x00FF0000);
			g.setFont(fonteAguarde);
			g.drawString(msgDisplay, getWidth() / 2, getHeight() / 2,
					Graphics.HCENTER | Graphics.BASELINE);
		}

	}

	/**
	 * Recupera a posição na mesa para o jogador conectado na posição i.
	 * <p>
	 * Este método permite que cliente e servidor compartilhem o código de
	 * desenho da tela (pois o que muda de um para outro é o "ponto de vista"
	 * mesmo - ambos têm que ter o jogador humano na posição 1)
	 * 
	 * @param i
	 *            posição (1 a 4) na conexão (no cliente é a posição na lista
	 *            recebida; no servidor é a posição de jogo mesmo)
	 * @return posição em que este jogador deve ser desenhado na mesa (e
	 *         adicionado no jogo), na mesma convenção da classe Mesa
	 *         (1=inferior, 2=direita, 3=superior, 4=esquerda)
	 */
	public abstract int getPosicaoMesa(int i);

	/**
	 * Divide uma string com base em um separador (como o <code>split()</code>)
	 * da classe <code>String</code> do J2SE.
	 * <p>
	 * Ele efetua a operação em dois passos, mas esta abordagem tem a vantagem
	 * de não alocar nenhum objeto além das strings não-nulas do array.
	 */
	public static String[] split(String original, char separador) {
		// Fase 1: Contagem dos tokens (para dimensionar o array)
		int tamanho = original.length();
		int qtdeTokens = 1;
		for (int i = 0; i < tamanho; i++) {
			if (original.charAt(i) == separador) {
				qtdeTokens++;
			}
		}
		// Fase 2: Montagem do array
		String[] result = new String[qtdeTokens];
		int numTokenAtual = 0, inicioTokenAtual = 0;
		for (int i = 0; i <= tamanho; i++) {
			if ((i == tamanho) || (original.charAt(i) == separador)) {
				result[numTokenAtual] = original.substring(inicioTokenAtual, i);
				inicioTokenAtual = i + 1;
				numTokenAtual++;
			}
		}
		return result;

	}

}
