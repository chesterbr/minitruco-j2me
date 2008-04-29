package br.inf.chester.minitruco.servidor;

/*
 * Copyright © 2006-2007 Carlos Duarte do Nascimento (Chester)
 * cd@pobox.com
 * 
 * Este programa é um software livre; você pode redistribui-lo e/ou 
 * modifica-lo dentro dos termos da Licença Pública Geral GNU como 
 * publicada pela Fundação do Software Livre (FSF); na versão 3 da 
 * Licença, ou (na sua opnião) qualquer versão.
 *
 * Este programa é distribuido na esperança que possa ser util, 
 * mas SEM NENHUMA GARANTIA; sem uma garantia implicita de ADEQUAÇÂO
 * a qualquer MERCADO ou APLICAÇÃO EM PARTICULAR. Veja a Licença
 * Pública Geral GNU para maiores detalhes.
 *
 * Você deve ter recebido uma cópia da Licença Pública Geral GNU
 * junto com este programa, se não, escreva para a Fundação do Software
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
	 * Versão do servidor
	 */
	public static final String VERSAO_SERVER = "2.0";

	public static DateFormat dfStartup;

	public static Date dataStartup;

	public static String strDataStartup;

	public static void main(String[] args) {

		// Guarda a data de início do servidor num formato apropriado para HTTP
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
