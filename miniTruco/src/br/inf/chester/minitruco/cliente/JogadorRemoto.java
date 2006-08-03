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

/**
 * Jogador que participa de um jogo online (além do JogadorHumano).
 * <p>
 * Na outra ponta, pode ser um JogadorCPU ou um JogadorConectado. Na real, pouco
 * importa, pois a classe não faz muita coisa - é só pro JogadorHumano não se
 * sentir sozinho (i.e., ter a quem referenciar nos eventos).
 * 
 * @author Chester
 * 
 */
public class JogadorRemoto extends Jogador {

	public void jogadorAceito(Jogador j, Jogo jogo) {

	}

	public void cartaJogada(Jogador j, Carta c) {

	}

	public void inicioMao() {

	}

	public void inicioPartida() {

	}

	public void vez(Jogador j, boolean podeFechada) {

	}

	public void pediuAumentoAposta(Jogador j, int valor) {

	}

	public void aceitouAumentoAposta(Jogador j, int valor) {

	}

	public void recusouAumentoAposta(Jogador j) {

	}

	public void rodadaFechada(int numRodada, int resultado,
			Jogador jogadorQueTorna) {

	}

	public void maoFechada(int[] pontosEquipe) {

	}

	public void jogoFechado(int numEquipeVencedora) {

	}

	public void decidiuMao11(Jogador j, boolean aceita) {

	}

	public void informaMao11(Carta[] cartasParceiro) {

	}

	public void jogoAbortado(Jogador j) {

	}

}
