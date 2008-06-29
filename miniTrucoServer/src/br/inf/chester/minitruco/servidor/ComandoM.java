package br.inf.chester.minitruco.servidor;

import mt.Jogador;

/*
 * Copyright © 2008 Carlos Duarte do Nascimento (Chester)
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

/**
 * Manda uma mensagem particular para um jogador específico
 */
public class ComandoM extends Comando {

	@Override
	public void executa(String[] args, JogadorConectado j) {
		// O comando só roda dentro de uma sala
		Sala s = j.getSala();
		if (s == null) {
			j.println("X FS");
			return;
		}
		// Localiza o jogador na sala;
		JogadorConectado jDestino = null;
		if (args.length >= 2 && !args[0].equals("unnamed")) {
			for (int i = 1; i <= 4; i++) {
				Jogador ji = s.getJogador(i);
				if ((ji instanceof JogadorConectado)
						&& ji.getNome().equals(args[1])) {
					jDestino = (JogadorConectado) ji;
					break;
				}
			}
		}
		if (jDestino == null) {
			j.println("X JI");
			return;
		}
		// Envia a mensagem
		jDestino.println("M " + j.getNome() + getMensagem(args, 2));
	}
}
