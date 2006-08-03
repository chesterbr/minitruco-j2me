package br.inf.chester.minitruco.cliente;

/*
 * Copyright © 2006 Carlos Duarte do Nascimento (Chester)
 * cd@pobox.com
 * 
 * Este programa é um software livre; você pode redistribui-lo e/ou 
 * modifica-lo dentro dos termos da Licença Pública Geral GNU como 
 * publicada pela Fundação do Software Livre (FSF); na versão 2 da 
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

import javax.microedition.lcdui.Command;

/**
 * Thread que executa os comandos do menu (permitindo que o jogador responda
 * assíncronamente com os outros)
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
