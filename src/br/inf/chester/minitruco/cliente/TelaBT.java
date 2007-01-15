package br.inf.chester.minitruco.cliente;

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
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

/**
 * Decorator para o servidor e o cliente BlueTooth - adiciona form de apelido e
 * elementos comuns (ex.: UUID do serviço de truco)
 * <p>
 * Após a exibição do form de apelido, o processo apropriado (busca de clientes
 * ou de servidores) é iniciado através do método run()
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
	 * Identificador único Bluetooth do "serviço miniTruco"
	 */
	public static final UUID UUID_BT = new UUID(
			"102030405060708090A0B0C0D0E0F010", false);

	/**
	 * Referência ao jogo em execução
	 */
	protected MiniTruco midlet;

	/**
	 * Referência à tela do jogo em execução
	 */
	protected Display display;

	/**
	 * Apelido do jogador (obtido do "friendly name" do celular, mas editável
	 * quando ele pede pra jogar via bluetooth)
	 */
	String apelido;

	/**
	 * Campo texto do apleido do jogador
	 */
	TextField txtApelido;

	/**
	 * Permite acessar as capacidades bluetooth do celular
	 */
	LocalDevice localDevice;

	/**
	 * Sinaliza que ainda estamos em uma sessão bluetooth
	 */
	protected boolean estaVivo = true;

	public TelaBT(MiniTruco midlet) {

		// Guarda o display da MIDlet (vamos precisar dele pra mostrar forms e
		// alerts) e uma referência a ela (que vamos usar para devolver o
		// controle quando a sessão acabar)
		this.display = Display.getDisplay(midlet);
		this.midlet = midlet;

		// Recupera o dispositivo local (que é o ponto de entrada para as
		// comunicações bluetooth
		try {
			localDevice = LocalDevice.getLocalDevice();
		} catch (BluetoothStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mostraFormApelido();

	}

	public void mostraFormApelido() {
		String sugestao = apelido;
		if (sugestao == null) {
			sugestao = localDevice.getFriendlyName();
		}
		Form formApelido = new Form("Apelido");
		txtApelido = new TextField("Informe seu apelido", sugestao, 15,
				TextField.ANY);
		formApelido.append(txtApelido);
		formApelido.setCommandListener(this);
		formApelido.addCommand(okApelidoCommand);
		formApelido.addCommand(voltarCommand);
		display.setCurrent(formApelido);
	}

	public void commandAction(Command cmd, Displayable arg1) {
		if (cmd.equals(okApelidoCommand)) {
			// Confirma o apelido e começa a procurar servidores
			apelido = txtApelido.getString();
			display.setCurrent(this);
			(new Thread(this)).start();
		} else if (cmd.equals(voltarCommand)) {
			// Sinaliza a finalização para a thread e volta ao menu
			midlet.telaBT = null;
			estaVivo = false;
			midlet.novaMesa(false);
			midlet.startApp();
		}
	}

	/**
	 * Exibe um alerta e aguarda o "ok"
	 * 
	 * @param titulo Título da janela
	 * @param texto Texto do alerta
	 */
	public void alerta(String titulo, String texto) {
		alerta(titulo, texto, false);
	}

	/**
	 * Exibe um alerta
	 * 
	 * @param titulo Título da janela
	 * @param texto Texto do alerta
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

}
