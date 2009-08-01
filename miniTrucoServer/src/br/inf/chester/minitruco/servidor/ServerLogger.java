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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import mt.Jogador;


/**
 * Efetua o log dos eventos do servidor.
 * <p>
 * A implementação atual coloca estes eventos em stdout num formato padronizado,
 * o que funciona bem para quem tem o grep à mão. Implementações futuras podem
 * fazer logs por sala, por jogador, efetuar alertas baseados em log, whatever.
 * 
 * @author Chester
 * 
 */
public class ServerLogger {

	private static DateFormat dataLog = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * Guarda um evento no log.
	 * <p>
	 * Obs.: Está como synchronized para evitar encavalamento de mensagens. Se
	 * isso for ruim em termos de performance, uma idéia seria consolidar os
	 * println com um StringBuilder e cuspir de uma vez só.
	 * 
	 * @param j
	 *            Jogador com que ocorreu (opcional)
	 * @param mensagem
	 *            Mensagem do evento
	 */
	public static synchronized void evento(Jogador j, String mensagem) {
		if (MiniTrucoServer.SERVER.get("EVENTS_SHOW").equals("TRUE")) {
			if (!(mensagem.equals(""))) { // se for somente um newline, why bother
				// Formato:
				// data thread [numsala|NA] jogador[@ip] mensagem
				System.out.print(dataLog.format(new Date()));
				System.out.print(' ');
				System.out.print(Thread.currentThread().getName());
				System.out.print(' ');
				Sala s = null;
				if (j instanceof JogadorConectado) {
					s = ((JogadorConectado) j).getSala();
				}
				if (s != null) {
					System.out.print(s.getNumSala());
					System.out.print(' ');
				} else {
					System.out.print("[sem_sala] ");
				}
				if (j != null) {
					System.out.print(!j.getNome().equals("unnamed") ? j.getNome() : "[sem_nome]");
					if (j instanceof JogadorConectado) {
						System.out.print('@');
						System.out.print(((JogadorConectado) j).getIp());
					}
					System.out.print(' ');
				} else {
					System.out.print("[sem_jogador] ");
				}
				System.out.println(mensagem);
			}
		}
	}

	/**
	 * Guarda um erro no log
	 * 
	 * @param e
	 *            Exceção associada ao erro (opcional)
	 * @param mensagem
	 *            Mensagem do erro
	 */
	public static void evento(Exception e, String mensagem) {
		evento((Jogador) null, mensagem + ". Detalhe do erro: " + 
		e.toString());
	}

	/**
	 * Gera um evento não ligado a um jogador (startup, shtudown, etc.)
	 * 
	 * @param mensagem
	 *            Mensagem do evento
	 */
	public static void evento(String mensagem) {
		evento((Jogador) null, mensagem);
	}
	
	// prints out relevant data for server statistics 
	public static synchronized void stats(String mensagem) {
		if (MiniTrucoServer.SERVER.get("STATS_SHOW").equals("TRUE")) {	
			System.out.print(dataLog.format(new Date()));
			System.out.print(' ');
			System.out.println(mensagem);
		}
	}
}
