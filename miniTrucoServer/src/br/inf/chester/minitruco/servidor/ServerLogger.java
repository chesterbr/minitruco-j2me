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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import mt.Jogador;


/**
 * Efetua o log dos eventos do servidor.
 * <p>
 * A implementa��o atual coloca estes eventos em stdout num formato padronizado,
 * o que funciona bem para quem tem o grep � m�o. Implementa��es futuras podem
 * fazer logs por sala, por jogador, efetuar alertas baseados em log, whatever.
 * 
 * @author Chester
 * 
 */
public class ServerLogger {

	private static DateFormat dataLog = new SimpleDateFormat("yyyyMMdd.HHmmss");

	/**
	 * Guarda um evento no log.
	 * <p>
	 * Obs.: Est� como synchronized para evitar encavalamento de mensagens. Se
	 * isso for ruim em termos de performance, uma id�ia seria consolidar os
	 * println com um StringBuilder e cuspir de uma vez s�.
	 * 
	 * @param j
	 *            Jogador com que ocorreu (opcional)
	 * @param mensagem
	 *            Mensagem do evento
	 */
	public static synchronized void evento(Jogador j, String mensagem) {
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
			System.out.print(j.getNome() != null ? j.getNome() : "[sem_nome]");
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

	/**
	 * Guarda um erro no log
	 * 
	 * @param e
	 *            Exce��o associada ao erro (opcional)
	 * @param mensagem
	 *            Mensagem do erro
	 */
	public static void evento(Exception e, String mensagem) {
		evento((Jogador) null, mensagem + ". Detalhe do erro:");
		e.printStackTrace();
	}

	/**
	 * Gera um evento n�o ligado a um jogador (startup, shtudown, etc.)
	 * 
	 * @param mensagem
	 *            Mensagem do evento
	 */
	public static void evento(String mensagem) {
		evento((Jogador) null, mensagem);
	}
}
