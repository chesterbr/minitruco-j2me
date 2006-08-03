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

import java.util.Random;

/**
 * Estratégia burra para jogadores CPU.
 * <p>
 * Ela joga aleatoriamente (daí o nome), e existe para permitr os diferentes
 * níveis de dificuldade (ter um parceiro bêbado é sempre mais difícil,
 * adversários bêbados são mais fáceis, e o jogo default é sem álcool).
 * 
 * @author Chester
 * 
 */
public class EstrategiaJohhnyWalker implements Estrategia {

	Random r = new Random();

	public String getNomeEstrategia() {
		return "Johnny Walker";
	}

	public String getInfoEstrategia() {
		return "feito por Chester, Dez/2005";
	}

	public int joga(SituacaoJogo s) {
		if (s.valorProximaAposta > 0 && r.nextInt() % 3 == 0) {
			return -1; // Se der na louca, pede truco
		}
		return 0; // Devolve sempre a primeira carta da lista
	}

	public boolean aceitaTruco(SituacaoJogo s) {
		return (r.nextInt() % 2 == 0);
	}

	public boolean aceitaMao11(Carta[] cartasParceiro, SituacaoJogo s) {
		return (r.nextInt() % 2 == 0);
	}

	public void inicioPartida() {

	}

	public void inicioMao() {

	}

	public void pediuAumentoAposta(int posJogador, int valor) {

	}

	public void aceitouAumentoAposta(int posJogador, int valor) {

	}

	public void recusouAumentoAposta(int posJogador) {

	}

}
