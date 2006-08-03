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
 * Lista as salas disponíveis e a quantidade de pessoas.
 * <p>
 * @author Chester
 *
 */
public class ComandoL extends Comando {

	@Override
	public void executa(String[] args, JogadorConectado j) {
		StringBuilder sbResposta = new StringBuilder("L ");
		for (int i=1;i<=Sala.getQtdeSalas();i++) {
			Sala s = Sala.getSala(i);
			if (i>1) {
				sbResposta.append('|');
			}
			sbResposta.append(s.getNumPessoas());
		}
		j.println(sbResposta.toString());
	}

}
