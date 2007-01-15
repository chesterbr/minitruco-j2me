package br.inf.chester.minitruco.cliente;

import java.io.IOException;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.RemoteDevice;
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnectionNotifier;
import javax.microedition.lcdui.Graphics;

/**
 * Recebe conexões (via Bluetooth) de outros celulares-cliente, exibe suas
 * posições, configura e inicia a partida (criando os proxies JogadorBT para
 * cada jogador remoto).
 * 
 * @author Chester
 * 
 */
public class ServidorBT extends TelaBT {

	/**
	 * Notificador do servidor (através do qual vamos aceitar as conexões)
	 */
	private StreamConnectionNotifier scnServidor;

	public ServidorBT(MiniTruco midlet) {
		// Exibe o form de apelido, que iniciará a busca de clientes no ok
		super(midlet);
	}

	protected void paint(Graphics g) {
		// TODO desenhar lista de clientes conectados, na posição
	}

	/**
	 * Recebe as conexões
	 */
	public void run() {

		// Registra o serviço e recupera o objeto que irá receber as conexões
		try {
			localDevice.setDiscoverable(DiscoveryAgent.GIAC);
			scnServidor = (StreamConnectionNotifier) Connector
					.open("btspp://localhost:" + UUID_BT.toString());
		} catch (IOException e) {
			midlet.alerta("Erro Bluetooth", e.getMessage());
		}
		// TODO: adicionar o "name=minitruco", verificar se é filtrável

		Logger.debug("Server aguardando conexoes BT");
		while (estaVivo) {
			Connection c = null;
			try {
				c = scnServidor.acceptAndOpen();
				RemoteDevice rdev = RemoteDevice.getRemoteDevice(c);
				Logger.debug("Cliente conectou. endereco: "
						+ rdev.getBluetoothAddress() + " nome="
						+ rdev.getFriendlyName(true));
				// TODO: adicionar o cliente à lista, criar uma thread para
				// processar seu I/O
				// ou criar logo o objeto Jogador para ele
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
