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

/**
 * Faz o jogador sair da sala onde está.
 * <p>
 * Se houver algum jogo em andamento, será encerrado.
 * <p>
 * Os participantes da sala são notificados.
 * 
 * @author Chester
 * 
 */
public class ComandoS extends Comando {

	@Override
	public void executa(String[] args, JogadorConectado j) {
		Sala s = j.getSala();
		if (s != null) {
			s.remove(j);
			j.println("S");
			if (s.getNumPessoas() == 0) {
				// Se esvaziou a sala, volta as regras para o default
				s.baralhoLimpo = false;
				s.manilhaVelha = false;
			}
			s.notificaJogadores(s.getInfo());
		} else {
			j.println("X FS");
		}

	}

}
