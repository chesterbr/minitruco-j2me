package br.inf.chester.minitruco.cliente;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.LocalDevice;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

/**
 * Decorator para o servidor e o cliente BlueTooth - adiciona form de apelido e
 * elementos comuns (ex.: UUID do servi�o de truco)
 * <p>
 * Ap�s a exibi��o do form de apelido, o processo apropriado (busca de clientes
 * ou de servidores) � iniciado atrav�s do m�todo run()
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
	 * Refer�ncia ao jogo em execu��o
	 */
	protected MiniTruco midlet;

	/**
	 * Refer�ncia � tela do jogo em execu��o
	 */
	protected Display display;

	/**
	 * Apelido do jogador (obtido do "friendly name" do celular, mas edit�vel
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
	 * Sinaliza que ainda estamos em uma sess�o bluetooth
	 */
	protected boolean estaVivo = true;

	public TelaBT(MiniTruco midlet) {

		// Guarda o display da MIDlet (vamos precisar dele pra mostrar forms e
		// alerts) e uma refer�ncia a ela (que vamos usar para devolver o
		// controle quando a sess�o acabar)
		this.display = Display.getDisplay(midlet);
		this.midlet = midlet;

		// Recupera o dispositivo local (que � o ponto de entrada para as
		// comunica��es bluetooth
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
			// Confirma o apelido e come�a a procurar servidores
			apelido = txtApelido.getString();
			display.setCurrent(this);
			(new Thread(this)).start();
		} else if (cmd.equals(voltarCommand)) {
			// Sinaliza a finaliza��o para a thread e volta ao menu
			midlet.telaBT = null;
			estaVivo = false;
			midlet.novaMesa(false);
			midlet.startApp();
		}
	}

}
