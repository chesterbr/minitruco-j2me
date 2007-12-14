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
 * Superclasse de todos os comandos que podem ser emitidos por um cliente.
 * <p>
 * Para criar um novo comando, basta criar uma sublcasse de <code>Comando</code>
 * neste mesmo package.
 * <p>
 * Os comandos do cliente são representados por uma letra. O nome da classe
 * deverá ter o formato <code>Comando_</code>, onde _ é a letra que irá
 * acionar o comando.
 * <p>
 * Por exemplo, o comando <code>E</code>, que entra numa sala é representado
 * pela classe <code>ComandoE</code>.
 * 
 * @author Chester
 * 
 */
public abstract class Comando {

	/**
	 * Executa o comando
	 * 
	 * @param args
	 *            argumentos recebidos pelo comando (o 1o. elemento é o próprio
	 *            comando)
	 * @param j
	 *            Jogador que solicitou o comando
	 */
	public abstract void executa(String[] args, JogadorConectado j);

}
