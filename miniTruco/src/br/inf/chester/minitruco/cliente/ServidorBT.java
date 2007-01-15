package br.inf.chester.minitruco.cliente;

import java.io.IOException;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnectionNotifier;
import javax.microedition.lcdui.Graphics;

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
	 * Identificador �nico Bluetooth do "servi�o miniTruco"
	 */
	// TODO: mudar, est� igual � do bluechat
	public static final UUID UUID_BT = new UUID(
			"102030405060708090A0B0C0D0E0F010", false);

	/**
	 * Notificador do servidor (atrav�s do qual vamos aceitar as conex�es)
	 */
	private StreamConnectionNotifier scnServidor;

	public ServidorBT(MiniTruco midlet) {
		// Exibe o form de apelido, que iniciar� a busca de clientes no ok
		super(midlet);
	}

	protected void paint(Graphics g) {
		// TODO desenhar lista de clientes conectados, na posi��o
	}

	/**
	 * Recebe as conex�es
	 */
	public void run() {

		// Registra o servi�o e recupera o objeto que ir� receber as conex�es
		try {
			localDevice.setDiscoverable(DiscoveryAgent.GIAC);
			scnServidor = (StreamConnectionNotifier) Connector
					.open("btspp://localhost:" + UUID_BT.toString());
		} catch (IOException e) {
			midlet.alerta("Erro Bluetooth", e.getMessage());
		}
		// TODO: adicionar o "name=minitruco", verificar se � filtr�vel

		Logger.debug("Server aguardando conexoes BT");
		while (estaVivo) {
			Connection c = null;
			try {
				c = scnServidor.acceptAndOpen();
				RemoteDevice rdev = RemoteDevice.getRemoteDevice(c);
				Logger.debug("Cliente conectou. endereco: "
						+ rdev.getBluetoothAddress() + " nome="
						+ rdev.getFriendlyName(true));
				// TODO: adicionar o cliente � lista
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
}
