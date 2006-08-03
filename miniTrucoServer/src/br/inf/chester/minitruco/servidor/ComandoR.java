package br.inf.chester.minitruco.servidor;

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

/**
 * Troca as regras da sala atual.
 * <p>
 * Recebe como parâmetro uma string de exatamente 2 caracteres T ou F,
 * indicando, na ordem, baralho limpo e manilha velha.
 * <p>
 * Apenas o gerente pode dar esse comando.
 * 
 * @author Chester
 * 
 */
public class ComandoR extends Comando {

	@Override
	public void executa(String[] args, JogadorConectado j) {
		Sala s = j.getSala();
		if (s.getGerente().equals(j) && args.length == 2
				&& args[1].length() == 2) {
			if (args[1].equals("TT")) {
				// Não dá pra jogar com baralho limpo e manilha velha
				j.println("X TT");
			} else {
				// Seta as novas regras e avisa a galera
				s.baralhoLimpo = args[1].charAt(0) == 'T';
				s.manilhaVelha = args[1].charAt(1) == 'T';
				s.notificaJogadores(s.getInfo());
			}
		}

	}

}
