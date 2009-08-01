package br.inf.chester.minitruco.servidor;

import mt.Jogador;

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


/**
 * Expulsa (Kick) um jogador da sala.
 * <p>
 * Parâmetro: posição
 * <p>
 * Só pode ser usado pelo gerente
 * 
 * @author Chester
 * 
 */
public class ComandoK extends Comando {

	@Override
	public void executa(String[] args, JogadorConectado j) {
		if (j.getNome().equals("unnamed")) {
			// can't execute this command until a nickname is set
			j.println("X NO");
			return;
		}
		Sala s = j.getSala();
		if (s != null && s.getGerente().equals(j) && args.length == 2) {
			try {
				// Recupera o jogador naquela posição e faz ele se retirar, via
				// comando "S" (que já avisa todo mundo)
				int posicao = Integer.parseInt(args[1]);
				Jogador jExpulso = s.getJogador(posicao);
				if (jExpulso instanceof JogadorConectado) {
					(new ComandoS()).executa(null, (JogadorConectado) jExpulso);
					return;
				} else {
					// Caso seja uma expulsao "em falso" (posição vazia ou com
					// robô), apenas atualiza a info da sala
					j.println(s.getInfo());
				}
			} catch (NumberFormatException e) {
				// Posição inválida
				j.println("X JI");
			}
		}

	}
}
