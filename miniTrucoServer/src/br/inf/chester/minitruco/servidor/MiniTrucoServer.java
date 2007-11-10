package br.inf.chester.minitruco.servidor;

/*
 * Copyright � 2006-2007 Carlos Duarte do Nascimento (Chester)
 * cd@pobox.com
 * 
 * Este programa � um software livre; voc� pode redistribui-lo e/ou 
 * modifica-lo dentro dos termos da Licen�a P�blica Geral GNU como 
 * publicada pela Funda��o do Software Livre (FSF); na vers�o 3 da 
 * Licen�a, ou (na sua opni�o) qualquer vers�o.
 *
 * Este programa � distribuido na esperan�a que possa ser util, 
 * mas SEM NENHUMA GARANTIA; sem uma garantia implicita de ADEQUA��O
 * a qualquer MERCADO ou APLICA��O EM PARTICULAR. Veja a Licen�a
 * P�blica Geral GNU para maiores detalhes.
 *
 * Voc� deve ter recebido uma c�pia da Licen�a P�blica Geral GNU
 * junto com este programa, se n�o, escreva para a Funda��o do Software
 * Livre(FSF) Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Ponto de entrada do servidor do miniTruco.
 * 
 * @author Chester
 * 
 */
public class MiniTrucoServer {

	public static final int PORTA_SERVIDOR = 6912;

	/**
	 * Vers�o do servidor
	 */
	public static final String VERSAO_SERVER = "2.0";

	public static DateFormat dfStartup;

	public static Date dataStartup;

	public static String strDataStartup;

	public static void main(String[] args) {

		// Guarda a data de in�cio do servidor num formato apropriado para HTTP
		// vide JogadorContectado.serveArquivosApplet
		
		dfStartup = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z",
				Locale.US);
		dataStartup = new Date();
		strDataStartup = dfStartup.format(dataStartup);
		
		ServerLogger
				.evento("Servidor Inicializado, pronto para escutar na porta "
						+ PORTA_SERVIDOR);

		try {

			// Inicializa as salas
			int numSalas = 10;
			Sala.inicializaSalas(numSalas);

			try {
				ServerSocket s = new ServerSocket(PORTA_SERVIDOR);
				while (true) {
					Socket sCliente = s.accept();
					JogadorConectado j = new JogadorConectado(sCliente);
					Thread t = new Thread(j);
					t.start();
				}
			} catch (IOException e) {
				ServerLogger.evento(e,
						"Erro de I/O no ServerSocket, saindo do programa");
			}

		} finally {
			ServerLogger.evento("Servidor Finalizado");
		}

	}

}
