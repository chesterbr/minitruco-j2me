package br.inf.chester.minitruco.cliente;

import java.io.IOException;
import java.util.Vector;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.lcdui.Graphics;

/**
 * Conecta-se (via Bluetooth) num celular-servidor, mostra a configuração da
 * mesa e cria o proxy do jogo (JogoBT)
 * 
 * @author Chester
 * 
 */
public class ClienteBT extends TelaBT {

	public ClienteBT(MiniTruco midlet) {
		// Exibe o form de apelido, que iniciará a busca de servidores no ok
		super(midlet);
	}

	public void run() {

		// Inicia a busca por celulares remotos
		DiscoveryAgent agente = localDevice.getDiscoveryAgent();
		MiniTrucoDiscoveryListener lsnr = new MiniTrucoDiscoveryListener();
		try {
			agente.startInquiry(DiscoveryAgent.GIAC, lsnr);
		} catch (BluetoothStateException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Aguarda o final da busca
		while (!lsnr.terminou) {
			Thread.yield();
		}

		// Para cada celular encontrado, verifica se é um servidor miniTruco
		for (int i = 0; i < lsnr.devs.size(); i++) {
			RemoteDevice remdev = (RemoteDevice) lsnr.devs.elementAt(i);
			try {
				Logger.debug("Buscando servico de truco no aparelho "
						+ remdev.getBluetoothAddress());
				MiniTrucoDiscoveryListener servicoListener = new MiniTrucoDiscoveryListener();
				agente.searchServices(null, new UUID[] { ServidorBT.UUID_BT },
						remdev, servicoListener);
				while (!servicoListener.terminou) {
					Thread.yield();
				}
				// TODO verificar se encontrou e tomar a ação apropriada
			} catch (BluetoothStateException e) {
				// TODO tratar
				e.printStackTrace();

			}
		}
	}

	/**
	 * Responde à busca de aparelho, e, para cada aparelho, à busca do serviço
	 * "servidor de miniTruco".
	 * 
	 * @author Chester
	 */
	class MiniTrucoDiscoveryListener implements DiscoveryListener {

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
			for (int i = 0; i < servicos.length; i++) {
				// TODO: verificar se é minitruco. Se for, setar srServidor e
				// encerrar a busca
				Logger.debug("achou servico URL "
						+ servicos[i].getConnectionURL(
								ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false));

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

	protected void paint(Graphics arg0) {
		// TODO Auto-generated method stub

	}

}
