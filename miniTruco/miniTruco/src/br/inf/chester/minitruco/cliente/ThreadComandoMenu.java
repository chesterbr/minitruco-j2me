package br.inf.chester.minitruco.cliente;

/*
 * Copyright � 2006 Carlos Duarte do Nascimento (Chester)
 * cd@pobox.com
 * 
 * Este programa � um software livre; voc� pode redistribui-lo e/ou 
 * modifica-lo dentro dos termos da Licen�a P�blica Geral GNU como 
 * publicada pela Funda��o do Software Livre (FSF); na vers�o 2 da 
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

import javax.microedition.lcdui.Command;

/**
 * Thread que executa os comandos do menu (permitindo que o jogador responda
 * ass�ncronamente com os outros)
 * 
 * @author Chester
 * 
 */
public class ThreadComandoMenu extends Thread {

	private Command cmd;

	private Mesa mesa;

	public ThreadComandoMenu(Mesa mesa) {
		this.mesa = mesa;
	}

	public void executa(Command cmd) {
		this.cmd = cmd;
		this.start();
	}

	public void run() {
		mesa.getJogador().executaComando(cmd);
	}

}
