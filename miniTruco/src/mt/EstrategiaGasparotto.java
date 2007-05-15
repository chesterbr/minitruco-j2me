package mt;

/*
 * Copyright � 2007 Sandro Gasparotto (sandro.gasparoto@gmail.com)
 * 
 * Nota 1: parte do c�digo (m�todos de suporte) adaptada 
 * da Estrat�gia Sellani (Copyright � 2006 Leonardo Sellani)
 * Nota 2: l�gica estrat�gica completamente nova
 *   
 * Este programa � um software livre; voc� pode redistribu�-lo e/ou 
 * modific�-lo dentro dos termos da Licen�a P�blica Geral GNU como 
 * publicada pela Funda��o do Software Livre (FSF); na vers�o 2 da 
 * Licen�a, ou (na sua opni�o) qualquer vers�o.
 *
 * Este programa � distribu�do na esperan�a que possa ser �til, 
 * mas SEM NENHUMA GARANTIA; sem uma garantia impl�cita de ADEQUA��O
 * a qualquer MERCADO ou APLICA��O EM PARTICULAR. Veja a Licen�a
 * P�blica Geral GNU para maiores detalhes.
 *
 * Voc� deve ter recebido uma c�pia da Licen�a P�blica Geral GNU
 * junto com este programa, se n�o, escreva para a Funda��o do Software
 * Livre(FSF) Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

import java.util.Random;

/**
 * Estrat�gia inteligente para jogadores CPU
 * 
 * @author Sandro Gasparotto
 * 
 */
public class EstrategiaGasparotto implements Estrategia 
{
	private static Random rand = new Random();
	
	int[] C = new int[3];
	
	private static int LIXO      = 0;
	private static int AS        = 1;
	private static int DOIS      = 2;
	private static int TRES      = 3;
	private static int PICAFUMO  = 4;
	private static int ESPADILHA = 5;
	private static int ESCOPETA  = 6;
	private static int ZAP       = 7;
	
	/**
	 * Retorna o nome "de tela" da Estrategia
	 */
	public String getNomeEstrategia() 
	{
		return "Gasparotto v1.0";
	}

	/**
	 * Retorna informa��es de copyright e afins
	 */
	public String getInfoEstrategia() 
	{
		return "Copyright � 2007 Sandro Gasparotto";
	}
	
	/**
	 * Retorna verdadeiro ou falso a partir de um n�mero rand�mico e um fator de d�vida/incerteza. 
	 * f = 100: sem d�vida vou em frente; 
	 * f = 50:  d�vida m�xima
	 * f = 0:   sem d�vida N�O vou em frente;
	 * Ou seja, qto maior esse fator, maior a chance de "mandarmos bala"
	 * A id�ia � gerar um pouco de aleatoriedade ao jogo
	 * Para n�o ficar evidente as estrat�gias...
	 * Confundir os advers�rios...
	 * E dar um toque humano � CPU...
	 */
		private boolean mandaBala(int fatorDeDuvida)
		{
			return (Math.abs(rand.nextInt())%100 + 1 <= fatorDeDuvida)?true:false;		
		}
	 
	/**
	 * Seta as vari�veis C[0],C[1] e C[2] com o �ndice da maior para menor carta da minha m�o
	 */
	private void classificaCartas(SituacaoJogo s)
	{
		int i,i2,cAux;
		
		C[0]=0;
		C[1]=1;
		C[2]=2;
		
		for(i=0;i<s.cartasJogador.length;i++)
		{
			for(i2=i;i2<3;i2++)
			{
				if(i2>0 && i2>=s.cartasJogador.length)
					C[i2]=C[i2-1];
				else
				if(s.cartasJogador[C[i2]].getValorTruco(s.manilha) >= s.cartasJogador[C[i]].getValorTruco(s.manilha))
				{
					cAux=C[i];
					C[i]=C[i2];
					C[i2]=cAux;
				}
			}
		}
	}
	

	/**
	 * Retorna minha posi��o na rodada (0..3) (m�o..p�)
	 */
	private int minhaPosicao(SituacaoJogo s)
	{
		int mPos;
		mPos = (eu(s) - (s.posJogadorQueAbriuRodada-1) + ((eu(s)>=(s.posJogadorQueAbriuRodada-1))?0:4));
		return mPos;
	}

	/**
	 * Retorna minha posi��o na mesa (0..3)
	 */
	private int eu(SituacaoJogo s)
	{
		return s.posJogador-1;
	}

	/**
	 * Retorna a posicao do meu parceiro na mesa (0..3)
	 */
	private int parceiro(SituacaoJogo s)
	{
		return ((s.posJogador+1)%4);
	}

	/**
	 * Retorna a posicao do advers�rio 1 na mesa (0..3)
	 */
	private int adversario1(SituacaoJogo s)
	{
		return ((s.posJogador+0)%4);
	}

	/**
	 * Retorna a posicao do advers�rio 2 na mesa (0..3)
	 */
	private int adversario2(SituacaoJogo s)
	{
		return ((s.posJogador+2)%4);
	}

	/**
	 * Retorna o n�mero da minha vez na rodada (0..3) (m�o..p�)
	 */
	private int minhaVez(SituacaoJogo s)
	{
		return (eu(s) - (s.posJogadorQueAbriuRodada-1) + ((eu(s)>=(s.posJogadorQueAbriuRodada-1))?0:4));
	}

	/**
	 * Retorna o n�mero da vez do trucador na rodada (0..3) (m�o..p�)
	 */
	private int vezTrucador(SituacaoJogo s)
	{
		return ((s.posJogadorPedindoAumento-1) - (s.posJogadorQueAbriuRodada-1) + ((((s.posJogadorPedindoAumento-1))>=(s.posJogadorQueAbriuRodada-1))?0:4));
	}
	
	/**
	 *  Retorna o �ndice da maior carta da mesa na rodada atual
	 */
	private int maiorCartaMesa(SituacaoJogo s)
	{
		int maiorCarta=0;

		for(int i=0;i<=3;i++)
		{
			if(s.cartasJogadas[s.numRodadaAtual-1][i]==null)
				continue;
			if(s.cartasJogadas[s.numRodadaAtual-1][maiorCarta]==null)
			{
				maiorCarta=i;
				continue;
			}
			if(s.cartasJogadas[s.numRodadaAtual-1][i].getValorTruco(s.manilha) > s.cartasJogadas[s.numRodadaAtual-1][maiorCarta].getValorTruco(s.manilha))
				maiorCarta=i;
		}
		return maiorCarta;
	}
	
	/**
	 * Retorna se a maior carta da mesa � a do meu parceiro ou n�o.
	 */
	private boolean maiorCartaEDoParceiro(SituacaoJogo s)
	{
		if(parceiro(s) == maiorCartaMesa(s) && !taMelado(s))
			return true;		
		return false;
	}
	
	/**
	 * Retorna se a maior carta da mesa � minha ou do meu parceiro ou n�o.
	 */
	private boolean maiorCartaENossa(SituacaoJogo s)
	{
		if((eu(s) == maiorCartaMesa(s) || parceiro(s) == maiorCartaMesa(s)) && !taMelado(s))
			return true;		
		return false;
	}
	
	/**
	 * Retorna se eu tenho na m�o alguma carta para matar a do advers�rio
	 */
	private boolean matoAdversario(SituacaoJogo s,boolean consideraEmpate)
	{
		if(minhaPosicao(s)==0)
			return true;
		for(int i=0;i<s.cartasJogador.length;i++)
		{
			if(s.cartasJogador[i].getValorTruco(s.manilha) > s.cartasJogadas[s.numRodadaAtual-1][maiorCartaMesa(s)].getValorTruco(s.manilha))
				return true;
			if((s.cartasJogador[i].getValorTruco(s.manilha) >= s.cartasJogadas[s.numRodadaAtual-1][maiorCartaMesa(s)].getValorTruco(s.manilha))&&
				consideraEmpate)
				return true;
		}
		return false;
	}
	
	/**
	 * Retorna o �ndice da menor carta que tenho na m�o para matar a carta do advers�rio ou do parceiro na mesa,
	 * se n�o tiver a carta retorna a menor.
	 */
	private int menorCartaParaMatar(SituacaoJogo s)
	{
		//se eu n�o puder matar a carta do advers�rio retorno a menor
		if(!matoAdversario(s,false))
			return C[2];
		//procura pela primeira carta que mata a do advers�rio
		for(int i=2;i>=0;i--)
			if(s.cartasJogador[C[i]].getValorTruco(s.manilha) > s.cartasJogadas[s.numRodadaAtual-1][maiorCartaMesa(s)].getValorTruco(s.manilha))
				return C[i]; //� essa!
		//se n�o encontrou nenhuma, joga a menor
		return C[2];
	}
	
	/**
	 * Retorna o �ndice da menor carta que tenho na m�o para matar ou pelo menos amarrar a carta do advers�rio ou do parceiro na mesa,
	 * se n�o tiver a carta retorna a menor.
	 */
	private int menorCartaParaMatarOuAmarrar(SituacaoJogo s)
	{
		//se eu n�o puder matar ou amarrar a carta do advers�rio retorno a menor
		if(!matoAdversario(s,true))
			return C[2];
		//procura pela primeira carta que mata a do advers�rio
		for(int i=2;i>=0;i--)
			if(s.cartasJogador[C[i]].getValorTruco(s.manilha) > s.cartasJogadas[s.numRodadaAtual-1][maiorCartaMesa(s)].getValorTruco(s.manilha))
				return C[i]; //� essa!
		//procura pela primeira carta que amarra a do advers�rio
		for(int i=2;i>=0;i--)
			if(s.cartasJogador[C[i]].getValorTruco(s.manilha) == s.cartasJogadas[s.numRodadaAtual-1][maiorCartaMesa(s)].getValorTruco(s.manilha))
				return C[i]; //� essa!
		//se n�o encontrou nenhuma, joga a menor
		return C[2];
	}
	
	/**
	 * Verifica se eu tenho a maior carta do jogo na m�o, (por exemplo, o 7 Copas j� tendo saido o Zap), 
	 * considerando apenas as manilhas. 
	 */
	private boolean tenhoMaiorCarta(SituacaoJogo s)
	{
		boolean m12=false,m13=false,m14=false;
		
		if(s.cartasJogador.length==0)
			return false;
		if(s.cartasJogador[C[0]].getValorTruco(s.manilha)<=10)
			return false;

		//se eu estiver com o zap, ent�o n�o tem nem o que pensar
		if(s.cartasJogador[C[0]].getValorTruco(s.manilha)==14)
			return true; 

		//procura pelas manilhas que j� sairam nas rodadas anteriores
		for(int rodada=0;rodada<(s.numRodadaAtual-1);rodada++)
			for(int jogador=0;jogador<=3;jogador++)
			{
				if(s.cartasJogadas[rodada][jogador]!=null)
				{
					switch(s.cartasJogadas[rodada][jogador].getValorTruco(s.manilha))
					{
						case 12: m12=true; break; //espadilha j� saiu
						case 13: m13=true; break; //copas j� saiu
						case 14: m14=true; break; //zap j� saiu
					}
				}
			}
		//ser� que estou com a maior manilha do jogo na m�o
		if( (s.cartasJogador[C[0]].getValorTruco(s.manilha)==13 && m14) ||
			(s.cartasJogador[C[0]].getValorTruco(s.manilha)==12 && m14 && m13) ||
			(s.cartasJogador[C[0]].getValorTruco(s.manilha)==11 && m14 && m13 && m12))
			return true; //ha!!!
		return false;
	}
	
	/**
	 * Retorna se a partida j� esta garantida ou n�o (por exemplo, se eu to com o Zap e a 1� feita)
	 */
	private boolean partidaGanha(SituacaoJogo s)
	{
		if(s.numRodadaAtual==1 && s.cartasJogador.length>=2 && s.cartasJogador[C[0]].getValorTruco(s.manilha)==14 && s.cartasJogador[C[1]].getValorTruco(s.manilha)==13)
			return true;
		if(s.numRodadaAtual==2 && primeiraENossa(s) && tenhoMaiorCarta(s))
			return true;		
		if(s.numRodadaAtual==3 && tenhoMaiorCarta(s))
			return true;		
		return false;
	}

	/**
	 * Retorna se a rodada atual esta empatada. 
	 */
	private boolean taMelado(SituacaoJogo s)
	{
		if(s.cartasJogadas[s.numRodadaAtual-1][parceiro(s)]==null)
			return false;
		if(s.cartasJogadas[s.numRodadaAtual-1][adversario1(s)]!=null && s.cartasJogadas[s.numRodadaAtual-1][parceiro(s)].getValorTruco(s.manilha) == s.cartasJogadas[s.numRodadaAtual-1][adversario1(s)].getValorTruco(s.manilha))
			return true;
		else
		if(s.cartasJogadas[s.numRodadaAtual-1][adversario2(s)]!=null && s.cartasJogadas[s.numRodadaAtual-1][parceiro(s)].getValorTruco(s.manilha) == s.cartasJogadas[s.numRodadaAtual-1][adversario2(s)].getValorTruco(s.manilha))
			return true;
		return false;
	}
	
	/**
	 * Retorna a exata qualifica��o de uma carta
	 * Talvez seja necess�ria alguma adapta��o para o caso de "baralho limpo" 
	 */
	private int qualidadeCarta(Carta carta,SituacaoJogo s)
	{
			// Declara��es
			int qcarta; // return do m�todo
			qcarta = LIXO; // apenas para ter a vari�vel inicializada com algo

			// 4, 5, 6, 7, Q, J, K
			if(carta.getValorTruco(s.manilha)<=6)
				qcarta = LIXO;
			// A
			else if(carta.getValorTruco(s.manilha)==7)
				qcarta = AS;
			// 2
			else if(carta.getValorTruco(s.manilha)==8)
				qcarta = DOIS;
			// 3
			else if(carta.getValorTruco(s.manilha)==9)
				qcarta = TRES;
			// 10 n�o � utilizado?
			// Picafumo
			else if(carta.getValorTruco(s.manilha)==11)
				qcarta = PICAFUMO;
			// Espadilha
			else if(carta.getValorTruco(s.manilha)==12)
				qcarta = ESPADILHA;
			// Escopeta
			else if(carta.getValorTruco(s.manilha)==13)
				qcarta = ESCOPETA;
			// Zap
			else if(carta.getValorTruco(s.manilha)==14)
				qcarta = ZAP;

			//caso a manilha seja um 3, o 2 passa a ter qualidade de 3!
			//e o A passa a ter valor de 2!
			//Obs: logicamente isso n�o se aplica para o caso de manilha velha...

			// Workaround devido ao fato da propriedade 'manilha' n�o poder ser utilizada diretamente
			// Estas s�o cartas fict�cias somente para os testes condicionais logo abaixo
			Carta tres_testedemanilha = new Carta('3',3);
			Carta dois_testedemanilha = new Carta('2',3);
			if(qcarta==DOIS && tres_testedemanilha.getValorTruco(s.manilha)==14)
				qcarta = TRES;
			if(qcarta==AS && tres_testedemanilha.getValorTruco(s.manilha)==14)
				qcarta = DOIS;			
			//caso a manilha seja um 2, o A passa a ter valor de 2!
			if(qcarta==AS && dois_testedemanilha.getValorTruco(s.manilha)==14)
			qcarta = DOIS;
			
			return qcarta;
	}
	
	/**
	 * Retorna a qualifica��o da maior carta da mesa na rodada atual
	 */
	private int qualidadeMaiorMesa(SituacaoJogo s)
	{
		if(s.cartasJogadas[s.numRodadaAtual-1][maiorCartaMesa(s)]==null)
			return 0;
		return qualidadeCarta(s.cartasJogadas[s.numRodadaAtual-1][maiorCartaMesa(s)],s);
	}

	/**
	 * Retorna a qualifica��o da maior carta da minha m�o.
	 */
	private int qualidadeMinhaMaior(SituacaoJogo s)
	{
		if(s.cartasJogador[C[0]]==null)
			return 0;
		return qualidadeCarta(s.cartasJogador[C[0]],s);
	}
	
	/**
	 * Retorna se pode/compensa aumentar a aposta ou n�o, com input para o fator sorte. E considerando que se estiver com 9, n�o vou pedir 6 (e id�ias similares)... 
	 * Uma melhoria seria deixar este jogador mais agressivo caso estejamos perdendo...
	 */
	private boolean podeEValeAPenaAumentar(SituacaoJogo s, int fator3, int fator6, int fator9, int fator12)
	{
		//o valor da aposta vai ser maior do que eu preciso pra fechar o jogo?
		if(12-s.pontosEquipe[eu(s)%2] < s.valorProximaAposta)
			return false;
		if(s.valorProximaAposta==3 && mandaBala(fator3))
			return true;
		if(s.valorProximaAposta==6 && mandaBala(fator6))
			return true;
		if(s.valorProximaAposta==9 && mandaBala(fator9))
			return true;			
		if(s.valorProximaAposta==12 && mandaBala(fator12))
			return true;
		return false;
	}
	
	/**
	 * Retorna se a primeira rodada foi feita por mim ou meu parceiro.
	 */
	private boolean primeiraENossa(SituacaoJogo s)
	{
		if(s.numRodadaAtual==0)
			return false;
		return ((s.resultadoRodada[0]-1)==((s.posJogador+1)%4));
	}
	
	// C�rebro da estrat�gia (sem �lcool no sangue!...)
	private int joga_primeira_rodada_mao(SituacaoJogo s) 
	{
		// Esta � a pior posi��o da mesa
		// E para piorar ainda mais, n�o temos como saber o que o parceiro tem...
		// Temos que inventar um sistema de sinal cibern�tico! rsrsrs
		// Ent�o, pelos ensinamentos de meu av�, vamos tentar garantir a primeira
		// Mas somente vale a pena se for o picafumo, espadilha ou escopeta
		// Um 3 morre f�cil, e pode ferrar o jogo do parceiro,
		// a n�o ser que temos o zap tamb�m
		// E o zap � sempre melhor guardar neste caso...

		if (mandaBala(95)) {
			// Checa se temos zap e tr�s
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP && 
				qualidadeCarta(s.cartasJogador[C[1]],s)==TRES)
					// Jogar o tr�s
					return C[1];
				// Vai que temos uma manilha tamb�m (C[1]) (ou mesmo outro tr�s),
				// mesmo assim vamos jogar o tr�s
				if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP && 
						qualidadeCarta(s.cartasJogador[C[2]],s)==TRES)
						// Jogar o tr�s
						return C[2];		
			// Checa se temos o picafumo, espadilha ou escopeta
			// Mas somente jogar caso n�o tenhamos o zap tamb�m
			// Isto quer dizer que uma dessas tem que ser a maior carta na m�o...
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==PICAFUMO ||
				qualidadeCarta(s.cartasJogador[C[0]],s)==ESPADILHA ||
				qualidadeCarta(s.cartasJogador[C[0]],s)==ESCOPETA)
					// Jogar ent�o
					return C[0];
		
		} else {
			// jogar a menor carta
			return C[2];
		}
		return C[0]; // para satisfazer o corretor do m�todo (Eclipse)...
	}

	private int joga_primeira_rodada_posmao(SituacaoJogo s) 
	{
		// Caso esteja vindo manilha:
		// Tentar matar a qualquer custo
		// Caso esteja vindo um tr�s:
		// Se temos zap e tr�s, jogar o tr�s
		// Sen�o tentar matar a qualquer custo
		// Caso esteja vindo um dois:
		// Caso tenhamos zap e tr�s, jogar o tr�s
		// Caso tenhamos zap e dois, jogar o dois
		// Sen�o tentar matar com a menor manilha (menos o zap) ou um tr�s pelo menos
		// Caso esteja vindo um �s:
		// Caso tenhamos zap e tr�s, jogar o tr�s
		// Caso tenhamos zap e dois, jogar o dois
		// Caso tenhamos zap e �s, jogar o �s
		// Sen�o tentar matar com a menor manilha (menos o zap) ou um tr�s ou dois pelo menos
		// Sen�o deixar para o parceiro, ele deve se virar
		// Caso esteja vindo lixo:
		// Jogar a menor manilha (menos o zap)
		// Ou um tr�s somente caso tenhamos o zap
		// Sen�o deixar para o parceiro, ele deve se virar
		
		if(qualidadeMaiorMesa(s)>=PICAFUMO)
			return menorCartaParaMatar(s);
		
		if(qualidadeMaiorMesa(s)==TRES) {
			// Checa se temos zap e tr�s
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP && 
				qualidadeCarta(s.cartasJogador[C[1]],s)==TRES)
					// Jogar o tr�s
					return C[1];
				// Vai que temos uma manilha tamb�m (C[1]) (ou mesmo outro tr�s),
				// mesmo assim vamos jogar o tr�s
				if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP && 
						qualidadeCarta(s.cartasJogador[C[2]],s)==TRES)
						// Jogar o tr�s
						return C[2];
			return menorCartaParaMatar(s);
		}
		
		if(qualidadeMaiorMesa(s)==DOIS) {
			// Checa se temos zap e tr�s
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP && 
				qualidadeCarta(s.cartasJogador[C[1]],s)==TRES)
					// Jogar o tr�s
					return C[1];
				// Vai que temos uma manilha tamb�m (C[1]) (ou mesmo outro tr�s),
				// mesmo assim vamos jogar o tr�s
				if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP && 
						qualidadeCarta(s.cartasJogador[C[2]],s)==TRES)
						// Jogar o tr�s
						return C[2];
			// Checa se temos zap e dois
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP && 
				qualidadeCarta(s.cartasJogador[C[1]],s)==DOIS)
					// Jogar o dois
					return C[1];
				// Vai que temos uma manilha tamb�m (C[1]) (ou mesmo outros tr�s/dois),
				// mesmo assim vamos jogar o dois
				if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP && 
						qualidadeCarta(s.cartasJogador[C[2]],s)==DOIS)
						// Jogar o dois
						return C[2];
			// Aqui vamos jogar a menor manilha (menos o zap)
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==PICAFUMO)
				return C[0];
			if(qualidadeCarta(s.cartasJogador[C[1]],s)==PICAFUMO)
				return C[1];
			if(qualidadeCarta(s.cartasJogador[C[2]],s)==PICAFUMO)
				return C[2];
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ESPADILHA)
				return C[0];
			if(qualidadeCarta(s.cartasJogador[C[1]],s)==ESPADILHA)
				return C[1];
			if(qualidadeCarta(s.cartasJogador[C[2]],s)==ESPADILHA)
				return C[0]; // hummmm... podemos jogar o zap para tentar enganar os advers�rios...
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ESCOPETA)
				return C[0];
			if(qualidadeCarta(s.cartasJogador[C[1]],s)==ESCOPETA)
				return C[0]; // hummmm.. podemos jogar o zap para tentar enganar os advers�rios...
			// Aqui vamos jogar o tr�s ou dois
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==TRES)
				return C[0];
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==DOIS)
				return C[0]; // ok se amarrar
			return C[2]; // retorna a menor ent�o
		}
		
		if(qualidadeMaiorMesa(s)==AS) {
			// Checa se temos zap e tr�s
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP && 
				qualidadeCarta(s.cartasJogador[C[1]],s)==TRES)
					// Jogar o tr�s
					return C[1];
				// Vai que temos uma manilha tamb�m (C[1]) (ou mesmo outro tr�s),
				// mesmo assim vamos jogar o tr�s
				if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP && 
						qualidadeCarta(s.cartasJogador[C[2]],s)==TRES)
						// Jogar o tr�s
						return C[2];
			// Checa se temos zap e dois
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP && 
				qualidadeCarta(s.cartasJogador[C[1]],s)==DOIS)
					// Jogar o dois
					return C[1];
				// Vai que temos uma manilha tamb�m (C[1]) (ou mesmo outros tr�s/dois),
				// mesmo assim vamos jogar o dois
				if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP && 
						qualidadeCarta(s.cartasJogador[C[2]],s)==DOIS)
						// Jogar o dois
						return C[2];
			// Checa se temos zap e �s
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP && 
				qualidadeCarta(s.cartasJogador[C[1]],s)==AS)
					// Jogar o �s
					return C[1];
				// Vai que temos uma manilha tamb�m (C[1]) (ou mesmo outos tr�s/dois/�s),
				// mesmo assim vamos jogar o �s
				if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP && 
						qualidadeCarta(s.cartasJogador[C[2]],s)==AS)
						// Jogar o �s
						return C[2];
			// Aqui vamos jogar a menor manilha (menos o zap)
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==PICAFUMO)
				return C[0];
			if(qualidadeCarta(s.cartasJogador[C[1]],s)==PICAFUMO)
				return C[1];
			if(qualidadeCarta(s.cartasJogador[C[2]],s)==PICAFUMO)
				return C[2];
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ESPADILHA)
				return C[0];
			if(qualidadeCarta(s.cartasJogador[C[1]],s)==ESPADILHA)
				return C[1];
			if(qualidadeCarta(s.cartasJogador[C[2]],s)==ESPADILHA)
				return C[0]; // hummmm... podemos jogar o zap para tentar enganar os advers�rios...
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ESCOPETA)
				return C[0];
			if(qualidadeCarta(s.cartasJogador[C[1]],s)==ESCOPETA)
				return C[0]; // hummmm... podemos jogar o zap para tentar enganar os advers�rios...
			// Aqui vamos jogar o tr�s ou dois
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==TRES)
				return C[0];
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==DOIS)
				return C[0];
			return C[2]; // retorna a menor ent�o
		}
		
		if (qualidadeMaiorMesa(s) == LIXO) {
			// Aqui vamos jogar a menor manilha (menos o zap)
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==PICAFUMO)
				return C[0];
			if(qualidadeCarta(s.cartasJogador[C[1]],s)==PICAFUMO)
				return C[1];
			if(qualidadeCarta(s.cartasJogador[C[2]],s)==PICAFUMO)
				return C[2];
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ESPADILHA)
				return C[0];
			if(qualidadeCarta(s.cartasJogador[C[1]],s)==ESPADILHA)
				return C[1];
			if(qualidadeCarta(s.cartasJogador[C[2]],s)==ESPADILHA)
				return C[0]; // hummmm... podemos jogar o zap para tentar enganar os advers�rios...
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ESCOPETA)
				return C[0];
			if(qualidadeCarta(s.cartasJogador[C[1]],s)==ESCOPETA)
				return C[0]; // hummmm... podemos jogar o zap para tentar enganar os advers�rios...
			// Checa se temos zap e tr�s
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP && 
				qualidadeCarta(s.cartasJogador[C[1]],s)==TRES)
					// Jogar o tr�s
					return C[1];
				// Vai que temos uma manilha tamb�m (C[1]) (ou mesmo outro tr�s),
				// mesmo assim vamos jogar o tr�s
				if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP && 
						qualidadeCarta(s.cartasJogador[C[2]],s)==TRES)
						// Jogar o tr�s
						return C[2];
			return C[2]; // retorna a menor ent�o
		}
		return C[0]; // para satisfazer o corretor do m�todo (Eclipse)...
	}
	
	private int joga_primeira_rodada_parape(SituacaoJogo s) 
	{
		// 1) Caso a maior carta da mesa seja do parceiro:
		// Se for maior ou igual a tr�s, ok, deixar passar
		// (apesar que aqui o correto seria "sinalizar" para o parceiro
		// caso tenhamos uma manilha (menos o zap) para tentar fazer
		// e n�o dar chance ao p� de amarrar...
		// assim como outros casos... mas tudo bem...
		// Se for um dois, vamos refor�ar s� se tivermos manilha (menos o zap) ou a menor, caso contr�rio
		// Se for um �s, vamos refor�ar, se temos zap e tr�s, jogar o tr�s, jogar manilha (menos o zap), tr�s/dois ou a menor, caso contr�rio
		// Se for lixo (menor que �s), vamos refor�ar, se temos zap e tr�s, jogar o tr�s, jogar manilha (menos o zap) ou um tr�s/dois/�s/zap (estamos secos?) ou a menor, caso contr�rio
		// 2) Caso a maior que esteja vindo n�o seja a do parceiro:
		// Caso esteja vindo manilha:
		// Tentar matar a qualquer custo
		// Caso esteja vindo um tr�s:
		// Se temos zap e tr�s, jogar o tr�s
		// Sen�o tentar matar a qualquer custo
		// Caso esteja vindo um dois:
		// Caso tenhamos zap e tr�s, jogar o tr�s
		// Caso tenhamos zap e dois, jogar o dois
		// Sen�o tentar matar com a menor manilha ou um tr�s pelo menos
		// Caso esteja vindo um �s/lixo:
		// Caso tenhamos zap e tr�s, jogar o tr�s
		// Caso tenhamos zap e dois, jogar o dois
		// Caso tenhamos zap e �s, jogar o �s
		// Sen�o tentar matar com a menor manilha ou um tr�s/dois/�s/...

		if(maiorCartaEDoParceiro(s) && qualidadeMaiorMesa(s) >= TRES)
			return C[2];
		if(maiorCartaEDoParceiro(s) && qualidadeMaiorMesa(s) == DOIS) {
			// Aqui vamos jogar a menor manilha (menos o zap)
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==PICAFUMO)
				return C[0];
			if(qualidadeCarta(s.cartasJogador[C[1]],s)==PICAFUMO)
				return C[1];
			if(qualidadeCarta(s.cartasJogador[C[2]],s)==PICAFUMO)
				return C[2];
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ESPADILHA)
				return C[0];
			if(qualidadeCarta(s.cartasJogador[C[1]],s)==ESPADILHA)
				return C[1];
			if(qualidadeCarta(s.cartasJogador[C[2]],s)==ESPADILHA)
				return C[0]; // hummmm... podemos jogar o zap para tentar enganar os advers�rios...
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ESCOPETA)
				return C[0];
			if(qualidadeCarta(s.cartasJogador[C[1]],s)==ESCOPETA)
				return C[0]; // hummmm... podemos jogar o zap para tentar enganar os advers�rios...
			return C[2];	
		}
		if(maiorCartaEDoParceiro(s) && qualidadeMaiorMesa(s) == AS) {
			// Checa se temos zap e tr�s
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP && 
				qualidadeCarta(s.cartasJogador[C[1]],s)==TRES)
					// Jogar o tr�s
					return C[1];
				// Vai que temos uma manilha tamb�m (C[1]) (ou mesmo outro tr�s),
				// mesmo assim vamos jogar o tr�s
				if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP && 
						qualidadeCarta(s.cartasJogador[C[2]],s)==TRES)
						// Jogar o tr�s
						return C[2];
			// Aqui vamos jogar a menor manilha (menos o zap)
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==PICAFUMO)
				return C[0];
			if(qualidadeCarta(s.cartasJogador[C[1]],s)==PICAFUMO)
				return C[1];
			if(qualidadeCarta(s.cartasJogador[C[2]],s)==PICAFUMO)
				return C[2];
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ESPADILHA)
				return C[0];
			if(qualidadeCarta(s.cartasJogador[C[1]],s)==ESPADILHA)
				return C[1];
			if(qualidadeCarta(s.cartasJogador[C[2]],s)==ESPADILHA)
				return C[0]; // hummmm... podemos jogar o zap para tentar enganar os advers�rios...
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ESCOPETA)
				return C[0];
			if(qualidadeCarta(s.cartasJogador[C[1]],s)==ESCOPETA)
				return C[0]; // hummmm... podemos jogar o zap para tentar enganar os advers�rios...
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==TRES)
				return C[0];
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==DOIS)
				return C[0];
			if(qualidadeCarta(s.cartasJogador[C[1]],s)==DOIS)
				return C[0];
			return C[2];
		}
		if(maiorCartaEDoParceiro(s) && qualidadeMaiorMesa(s) == LIXO) {
			// Checa se temos zap e tr�s
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP && 
				qualidadeCarta(s.cartasJogador[C[1]],s)==TRES)
					// Jogar o tr�s
					return C[1];
				// Vai que temos uma manilha tamb�m (C[1]) (ou mesmo outro tr�s),
				// mesmo assim vamos jogar o tr�s
				if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP && 
						qualidadeCarta(s.cartasJogador[C[2]],s)==TRES)
						// Jogar o tr�s
						return C[2];
			// Aqui vamos jogar a menor manilha (menos o zap)
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==PICAFUMO)
				return C[0];
			if(qualidadeCarta(s.cartasJogador[C[1]],s)==PICAFUMO)
				return C[1];
			if(qualidadeCarta(s.cartasJogador[C[2]],s)==PICAFUMO)
				return C[2];
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ESPADILHA)
				return C[0];
			if(qualidadeCarta(s.cartasJogador[C[1]],s)==ESPADILHA)
				return C[1];
			if(qualidadeCarta(s.cartasJogador[C[2]],s)==ESPADILHA)
				return C[0]; // hummmm... podemos jogar o zap para tentar enganar os advers�rios...
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ESCOPETA)
				return C[0];
			if(qualidadeCarta(s.cartasJogador[C[1]],s)==ESCOPETA)
				return C[0]; // hummmm... podemos jogar o zap para tentar enganar os advers�rios...
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==TRES)
				return C[0];
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==DOIS)
				return C[0];
			if(qualidadeCarta(s.cartasJogador[C[1]],s)==DOIS)
				return C[0];
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==AS)
				return C[0];
			if(qualidadeCarta(s.cartasJogador[C[1]],s)==AS)
				return C[0];
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP)
				return C[0];
			return C[2];
		}

		if (qualidadeMaiorMesa(s) >= PICAFUMO) {
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP)
				return C[0];
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ESCOPETA &&
				qualidadeMaiorMesa(s) != ZAP)
					return C[0]; // melhoria: talvez pudesse desenvolver essa an�lise aprimorada para os demais casos abaixo (caso tenhamos mais que uma manilha)...
			return menorCartaParaMatar(s);
		}
		if (qualidadeMaiorMesa(s) == TRES) {
			// Checa se temos zap e tr�s
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP && 
				qualidadeCarta(s.cartasJogador[C[1]],s)==TRES)
					// Jogar o tr�s
					return C[1];
				// Vai que temos uma manilha tamb�m (C[1]) (ou mesmo outro tr�s),
				// mesmo assim vamos jogar o tr�s
				if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP && 
						qualidadeCarta(s.cartasJogador[C[2]],s)==TRES)
						// Jogar o tr�s
						return C[2];
			return menorCartaParaMatar(s);
		}
		if (qualidadeMaiorMesa(s) == DOIS) {
			// Checa se temos zap e tr�s
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP && 
				qualidadeCarta(s.cartasJogador[C[1]],s)==TRES)
					// Jogar o tr�s
					return C[1];
				// Vai que temos uma manilha tamb�m (C[1]) (ou mesmo outro tr�s),
				// mesmo assim vamos jogar o tr�s
				if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP && 
						qualidadeCarta(s.cartasJogador[C[2]],s)==TRES)
						// Jogar o tr�s
						return C[2];
			// Checa se temos zap e dois
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP && 
				qualidadeCarta(s.cartasJogador[C[1]],s)==DOIS)
					// Jogar o dois
					return C[1];
				// Vai que temos uma manilha tamb�m (C[1]) (ou mesmo outros tr�s/dois),
				// mesmo assim vamos jogar o dois
				if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP && 
						qualidadeCarta(s.cartasJogador[C[2]],s)==DOIS)
						// Jogar o dois
						return C[2];
			// Aqui vamos jogar a menor manilha
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==PICAFUMO)
				return C[0];
			if(qualidadeCarta(s.cartasJogador[C[1]],s)==PICAFUMO)
				return C[1];
			if(qualidadeCarta(s.cartasJogador[C[2]],s)==PICAFUMO)
				return C[2];
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ESPADILHA)
				return C[0];
			if(qualidadeCarta(s.cartasJogador[C[1]],s)==ESPADILHA)
				return C[1];
			if(qualidadeCarta(s.cartasJogador[C[2]],s)==ESPADILHA)
				return C[0]; // hummmm... podemos jogar o zap para tentar enganar os advers�rios...
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ESCOPETA)
				return C[0];
			if(qualidadeCarta(s.cartasJogador[C[1]],s)==ESCOPETA)
				return C[0]; // hummmm... podemos jogar o zap para tentar enganar os advers�rios...
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP)
				return C[0];
			// Aqui jogamos um tr�s
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==TRES)
				return C[0];
			return C[2];
		}
		if (qualidadeMaiorMesa(s) <= AS) {
			// Checa se temos zap e tr�s
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP && 
				qualidadeCarta(s.cartasJogador[C[1]],s)==TRES)
					// Jogar o tr�s
					return C[1];
				// Vai que temos uma manilha tamb�m (C[1]) (ou mesmo outro tr�s),
				// mesmo assim vamos jogar o tr�s
				if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP && 
						qualidadeCarta(s.cartasJogador[C[2]],s)==TRES)
						// Jogar o tr�s
						return C[2];
			// Checa se temos zap e dois
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP && 
				qualidadeCarta(s.cartasJogador[C[1]],s)==DOIS)
					// Jogar o dois
					return C[1];
				// Vai que temos uma manilha tamb�m (C[1]) (ou mesmo outros tr�s/dois),
				// mesmo assim vamos jogar o dois
				if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP && 
						qualidadeCarta(s.cartasJogador[C[2]],s)==DOIS)
						// Jogar o dois
						return C[2];
			// Checa se temos zap e �s
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP && 
				qualidadeCarta(s.cartasJogador[C[1]],s)==AS)
					// Jogar o �s
					return C[1];
				// Vai que temos uma manilha tamb�m (C[1]) (ou mesmo outros tr�s/dois/�s),
				// mesmo assim vamos jogar o �s
				if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP && 
						qualidadeCarta(s.cartasJogador[C[2]],s)==AS)
						// Jogar o �s
						return C[2];				
			// Aqui vamos jogar a menor manilha
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==PICAFUMO)
				return C[0];
			if(qualidadeCarta(s.cartasJogador[C[1]],s)==PICAFUMO)
				return C[1];
			if(qualidadeCarta(s.cartasJogador[C[2]],s)==PICAFUMO)
				return C[2];
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ESPADILHA)
				return C[0];
			if(qualidadeCarta(s.cartasJogador[C[1]],s)==ESPADILHA)
				return C[1];
			if(qualidadeCarta(s.cartasJogador[C[2]],s)==ESPADILHA)
				return C[0]; // hummmm... podemos jogar o zap para tentar enganar os advers�rios...
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ESCOPETA)
				return C[0];
			if(qualidadeCarta(s.cartasJogador[C[1]],s)==ESCOPETA)
				return C[0]; // hummmm... podemos jogar o zap para tentar enganar os advers�rios...
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==ZAP)
				return C[0];
			// Aqui jogamos a melhor que temos ent�o desde que n�o seja menor que a que est� j� na mesa
			if(qualidadeCarta(s.cartasJogador[C[0]],s)>=qualidadeMaiorMesa(s))
				return C[0];
			return C[2];
		}
		return C[0]; // para satisfazer o corretor do m�todo (Eclipse)...
	}
	
	private int joga_primeira_rodada_pe(SituacaoJogo s) 
	{
		// Aqui � f�cil...
		// Tudo que o p� tem a fazer � garantir a primeira!

		if(maiorCartaEDoParceiro(s))
			return C[2]; //� isso a� parceiro!
		return menorCartaParaMatarOuAmarrar(s); //deixa comigo
	}
	
	private int joga_segunda_rodada_mao(SituacaoJogo s) 
	{
		// Fiz a primeira
		// Se estiver com a partida ganha, vou encobrir a menor!
		// Se estiver com duas cartas acima ou igual a um tr�s, jogar a menor aumentando a aposta (com alta probabilidade).
		// Se estiver com duas cartas acima ou igual a um �s, jogar a menor.
		// Se estiver com uma carta acima ou igual a um �s e um lixo, jogo o lixo encoberto (com alta probabilidade).
		// Se estiver com dois lixos, jogo o lixo mais lixo encoberto (com baixa probabilidade).

		if(partidaGanha(s))
			return C[1]+10;

		if(qualidadeCarta(s.cartasJogador[C[0]],s)>=TRES &&
			qualidadeCarta(s.cartasJogador[C[1]],s)>=TRES) {
			if(podeEValeAPenaAumentar(s,80,10,1,0))
				return -1;
			return C[1];
		}

		if(qualidadeCarta(s.cartasJogador[C[0]],s)>=AS &&
				qualidadeCarta(s.cartasJogador[C[1]],s)>=AS) {
				return C[1];
		}
	
		if(qualidadeCarta(s.cartasJogador[C[0]],s)>=AS &&
				qualidadeCarta(s.cartasJogador[C[1]],s)==LIXO) {
				if(mandaBala(75))
					return C[1]+10;
				return C[1];		
		}
	
		if(qualidadeCarta(s.cartasJogador[C[0]],s)==LIXO &&
				qualidadeCarta(s.cartasJogador[C[1]],s)==LIXO) {
				if(mandaBala(25))
					return C[1]+10;
				return C[1];		
		}
		return C[0]; // para satisfazer o corretor do m�todo (Eclipse)...
	}

	private int joga_segunda_rodada_posmao(SituacaoJogo s) 
	{
		// A primeira � deles
		// Assim fica dif�cil
		// Vou deixar para o parceiro

		return C[1];
	}
	
	private int joga_segunda_rodada_parape(SituacaoJogo s) 
	{
		// O parceiro fez a primeira
		// Se estiver com a partida ganha, vou jogar quieto a menor!
		// Caso contr�rio, vou sempre jogar a maior no p�.
		// A n�o ser que esteja vindo carta boa do parceiro...
		// Se estiver com uma carta acima ou igual a um �s, jogar aumentando a aposta (com alta probabilidade).
		// Se estiver com lixos, jogar aumentando a aposta (com baixa probabilidade - fac�o).

		if(partidaGanha(s))
			return C[1];
		
		if(maiorCartaEDoParceiro(s) && qualidadeMaiorMesa(s)>=TRES)
			return C[1]+10;
		if(qualidadeCarta(s.cartasJogador[C[0]],s)>=TRES) {
				if(podeEValeAPenaAumentar(s,95,40,1,0))
					return -1;
				return C[0];
		}
		if(maiorCartaEDoParceiro(s) && qualidadeMaiorMesa(s)==DOIS)
			return C[1]+10;
		if(qualidadeCarta(s.cartasJogador[C[0]],s)==DOIS) {
			if(podeEValeAPenaAumentar(s,90,30,1,0))
				return -1;
			return C[0];
		}
		if(maiorCartaEDoParceiro(s) && qualidadeMaiorMesa(s)==AS)
			return C[1]+10;
		if(qualidadeCarta(s.cartasJogador[C[0]],s)==AS) {
			if(podeEValeAPenaAumentar(s,85,20,1,0))
				return -1;
			return C[0];
		}

		if(qualidadeCarta(s.cartasJogador[C[0]],s)==LIXO) {
			if(podeEValeAPenaAumentar(s,10,2,1,0))
				return -1;
			return C[0];
		}
		return C[0]; // para satisfazer o corretor do m�todo (Eclipse)...
	}
	
	private int joga_segunda_rodada_pe(SituacaoJogo s) 
	{
		// A primeira � deles
		// Tenho que fazer de qualquer jeito

		if(maiorCartaEDoParceiro(s))
			return C[1]; //� isso a� parceiro!
		return menorCartaParaMatar(s); //deixa comigo
	}
	
	private int joga_terceira_rodada_mao(SituacaoJogo s) 
	{
		// Eles fizeram a primeira, eu fiz a segunda
		// Se estiver com a partida ganha, arrega�ar os caras!
		// Se estiver com manilha, jogar aumentando a aposta (com m�dia probabilidade).
		// Se estiver com tr�s ou menos, jogar quieto.

		if(partidaGanha(s) && podeEValeAPenaAumentar(s,100,100,100,100))
			return -1;
		
		if(qualidadeCarta(s.cartasJogador[C[0]],s)>TRES &&
			podeEValeAPenaAumentar(s,50,10,1,0))
				return -1;

		return C[0];
	}

	private int joga_terceira_rodada_posmao(SituacaoJogo s) 
	{
		// N�s fizemos a primeira, eles fizeram a segunda
		// Se estiver com a partida ganha, arrega�ar os caras!
		// Se n�o puder com a que estiver vindo, jogar encoberta.
		// Se estiver com manilha, jogar aumentando a aposta (com m�dia probabilidade).
		// Se estiver com tr�s ou menos, jogar quieto.

		if(partidaGanha(s) && podeEValeAPenaAumentar(s,100,100,100,100))
			return -1;
		
		if(qualidadeCarta(s.cartasJogador[C[0]],s)<qualidadeMaiorMesa(s))
			return C[0]+10;
		
		if(qualidadeCarta(s.cartasJogador[C[0]],s)>TRES &&
			podeEValeAPenaAumentar(s,50,10,1,0))
				return -1;
		
		return C[0];
	}
	
	private int joga_terceira_rodada_parape(SituacaoJogo s) 
	{
		// Eles fizeram a primeira, meu parceiro fez a segunda
		// Agora � a hora da verdade...
		// Se estiver com a partida ganha, arrega�ar os caras!
		// Se n�o puder com a que estiver vindo, jogar aumentando a aposta no fac�o (com baixa probabilidade).
		// Se estiver com manilha, jogar aumentando a aposta (com m�dia-alta probabilidade).
		// Se estiver com tr�s, jogar aumentando a aposta (com m�dia-baixa probabilidade)
		// Se estiver com dois ou menos, jogar quieto.

		if(partidaGanha(s) && podeEValeAPenaAumentar(s,100,100,100,100))
			return -1;
		
		if(qualidadeCarta(s.cartasJogador[C[0]],s)<=qualidadeMaiorMesa(s) &&
			podeEValeAPenaAumentar(s,20,5,1,0))
				return -1;
		
		if(qualidadeCarta(s.cartasJogador[C[0]],s)>TRES &&
			podeEValeAPenaAumentar(s,70,15,1,0))
				return -1;
	
		if(qualidadeCarta(s.cartasJogador[C[0]],s)==TRES &&
			podeEValeAPenaAumentar(s,30,8,1,0))
				return -1;
		
		return C[0];	
	}
	
	private int joga_terceira_rodada_pe(SituacaoJogo s) 
	{
		// N�s fizemos a primeira, eles fizeram a segunda	
		// Se estiver com a partida ganha, arrega�ar os caras!
		// Se n�o puder com a que estiver vindo, jogar aumentando a aposta no fac�o (com m�dia probabilidade).
		// Se puder com a que estiver vindo (pelo menos amarrar), arrega�ar os caras!

		if(partidaGanha(s) && podeEValeAPenaAumentar(s,100,100,100,100))
			return -1;
		
		if(qualidadeCarta(s.cartasJogador[C[0]],s)<qualidadeMaiorMesa(s) &&
			podeEValeAPenaAumentar(s,50,10,1,0))
				return -1;
		
		if(podeEValeAPenaAumentar(s,100,100,100,100))
				return -1;
		
		return C[0];	
	}
	
	// Somente para facilitar organiza��o do c�digo
	private int joga_primeira_rodada(SituacaoJogo s) 
	{
		switch (minhaPosicao(s)) {
		// M�o
		case 0:
			return this.joga_primeira_rodada_mao(s);
		// P�s m�o
		case 1:
			return this.joga_primeira_rodada_posmao(s);
		// Para p�
		case 2:
			return this.joga_primeira_rodada_parape(s);
		// P�
		case 3:
			return this.joga_primeira_rodada_pe(s);		
		}
		return C[0]; // para satisfazer o corretor do m�todo (Eclipse)...
	}
	
	private int joga_segunda_rodada(SituacaoJogo s) 
	{
		switch (minhaPosicao(s)) {
		// M�o
		case 0:
			return this.joga_segunda_rodada_mao(s);
		// P�s m�o
		case 1:
			return this.joga_segunda_rodada_posmao(s);
		// Para p�
		case 2:
			return this.joga_segunda_rodada_parape(s);
		// P�
		case 3:
			return this.joga_segunda_rodada_pe(s);		
		}
		return C[0]; // para satisfazer o corretor do m�todo (Eclipse)...
	}
	
	private int joga_terceira_rodada(SituacaoJogo s) 
	{
		switch (minhaPosicao(s)) {
		// M�o
		case 0:
			return this.joga_terceira_rodada_mao(s);
		// P�s m�o
		case 1:
			return this.joga_terceira_rodada_posmao(s);
		// Para p�
		case 2:
			return this.joga_terceira_rodada_parape(s);
		// P�
		case 3:
			return this.joga_terceira_rodada_pe(s);		
		}
		return C[0]; // para satisfazer o corretor do m�todo (Eclipse)...
	}
	
	/**
	 * Executa uma jogada.
	 * <p>
	 * Observe que, ao pedir aumento, o sistema ir� interagir com a outra dupla.
	 * Se a partida seguir, o m�todo ser� chamado novamente para efetivar a real
	 * jogada.
	 * <p>
	 * A estrat�gia � respons�vel por checar se o valor da pr�xima aposta �
	 * diferente de 0 e s� pedir aumento nesta situa��o.
	 * <p>
	 * 
	 * @param s
	 *            Situa��o do jogo no momento
	 * @return posi��o da carta na m�o a jogar (em letrasCartasJogador), ou -1
	 *         para pedir truco
	 */
	public int joga(SituacaoJogo s) 
	{
		// Classifica as cartas que tenho na m�o, da maior para a menor,
		// de maneira que vamos ter:
		// C[0] = maior carta (1a, 2a e 3a m�o); 
		// C[1] = carta intermedi�ria (1a m�o) OU
		// C[1] = menor carta (2a m�o);
		// C[2] = menor carta (1a m�o)
		classificaCartas(s);

		switch (s.numRodadaAtual) {
		// Primeira rodada
		case 1:

			//Debug
			//System.out.println("Cartas: " + s.cartasJogador[C[0]].toString() + ">" + s.cartasJogador[C[0]].getValorTruco(s.manilha) + ">" + qualidadeCarta(s.cartasJogador[C[0]],s) + " " + s.cartasJogador[C[1]].toString() + ">" + s.cartasJogador[C[1]].getValorTruco(s.manilha) + ">" + qualidadeCarta(s.cartasJogador[C[1]],s) + " " + s.cartasJogador[C[2]].toString() + ">" + s.cartasJogador[C[2]].getValorTruco(s.manilha) + ">" + qualidadeCarta(s.cartasJogador[C[2]],s) + "\n");

			return this.joga_primeira_rodada(s);
		// Segunda rodada
		case 2:
			//Checar se est� tudo amarrado
			if(s.resultadoRodada[0]==3)
			{
				if((partidaGanha(s) && podeEValeAPenaAumentar(s,100,100,100,100)) ||
					(matoAdversario(s,false) && (qualidadeMinhaMaior(s)==PICAFUMO || qualidadeMinhaMaior(s)==ESPADILHA || qualidadeMinhaMaior(s)==ESCOPETA) && podeEValeAPenaAumentar(s,50,10,1,0)) )
					return -1; //Aumentar aposta					
				return C[0]; // temos que jogar a maior
			}
			return this.joga_segunda_rodada(s);
		// Terceira rodada
		case 3:
			//Checar se est� tudo amarrado
			if(s.resultadoRodada[1]==3)
			{
				if((partidaGanha(s) && podeEValeAPenaAumentar(s,100,100,100,100)) ||
					(matoAdversario(s,false) && (qualidadeMinhaMaior(s)==PICAFUMO || qualidadeMinhaMaior(s)==ESPADILHA || qualidadeMinhaMaior(s)==ESCOPETA) && podeEValeAPenaAumentar(s,50,10,1,0)) )
					return -1; //Aumentar aposta					
				return C[0]; // temos que jogar a maior
			}
			return this.joga_terceira_rodada(s);
		}
		return 0; // para satisfazer o corretor do m�todo (Eclipse)...
	}
	
	/**
	 * Retorna se eu aceito o aumento da aposta dos advers�rios ou n�o. 
	 * Uma melhoria seria checar se est�o pedindo truco, seis, ... e tomar as decis�es de acordo
	 */
	public boolean aceitaTruco(SituacaoJogo s) 
	{
		classificaCartas(s);

		//n�o vai nem ter gra�a...
		if(partidaGanha(s))
			return true;
		
		switch (s.numRodadaAtual) 
		{

		//primeira rodada
		case 1:

			//se eu tiver uma manilha e na mesa j� estiver pelo menos um 3 nosso, aceito
			if(qualidadeCarta(s.cartasJogador[C[0]],s)>=TRES &&
				maiorCartaENossa(s) && 
				qualidadeMaiorMesa(s)>=TRES)
					return true;
			//se eu tiver uma tr�s e na mesa j� estiver pelo menos um 3 nosso, aceito
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==TRES &&
				maiorCartaENossa(s) && 
				qualidadeMaiorMesa(s)>=TRES)
					return true;
			//se eu tiver manilha e tr�s pelo menos, aceito sem pensar
			if(qualidadeCarta(s.cartasJogador[C[0]],s)>TRES && 
				qualidadeCarta(s.cartasJogador[C[1]],s)>=TRES)
					return true;
			//se eu tiver uma manilha seca, aceito (alta prob.)
			if(qualidadeCarta(s.cartasJogador[C[0]],s)>TRES &&
				qualidadeCarta(s.cartasJogador[C[1]],s)<TRES &&
				mandaBala(80))
					return true;			
			//se eu tiver dois tr�s, aceito (alta prob.)
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==TRES && 
				qualidadeCarta(s.cartasJogador[C[1]],s)==TRES &&
				mandaBala(80))
					return true;
			//se eu tiver um tr�s, aceito (m�dia prob.)
			if(qualidadeCarta(s.cartasJogador[C[0]],s)==TRES &&
				qualidadeCarta(s.cartasJogador[C[1]],s)<TRES &&
				mandaBala(50))
					return true;				
			return false;
			
		//segunda rodada
		case 2:

			//se a primeira for nossa:
			if(primeiraENossa(s)) {
				//se na mesa j� estiver pelo menos um 3 nosso, aceito
				if(maiorCartaENossa(s) && 
						qualidadeMaiorMesa(s)>=TRES)
						return true;
				//se eu tiver pelo menos um tr�s, aceito
				if(qualidadeCarta(s.cartasJogador[C[0]],s)>=TRES)
					return true;
			} else {
				//se a primeira N�O for nossa (pode tbm estar amarrado):
				//se eu tiver uma manilha e na mesa j� estiver pelo menos um 3 nosso, aceito
				if(qualidadeCarta(s.cartasJogador[C[0]],s)>=TRES &&
						maiorCartaENossa(s) && 
						qualidadeMaiorMesa(s)>=TRES)
						return true;
				//se eu tiver manilha e tr�s pelo menos, aceito
				if(qualidadeCarta(s.cartasJogador[C[0]],s)>TRES && 
						qualidadeCarta(s.cartasJogador[C[1]],s)>=TRES)
						return true;
				//se eu tiver uma manilha seca, aceito (baixa prob.)
				if(qualidadeCarta(s.cartasJogador[C[0]],s)>TRES &&
						qualidadeCarta(s.cartasJogador[C[1]],s)<TRES &&
						mandaBala(30))
						return true;			
			}
			return false;
			
		//terceira rodada
		case 3:

			//se a primeira for nossa:
			if(primeiraENossa(s)) {
				//se na mesa j� estiver uma manilha nossa, aceito com alta prob.
				if(maiorCartaENossa(s) && 
					qualidadeMaiorMesa(s)>TRES &&
					mandaBala(80))
						return true;
				//se na mesa j� estiver um 3 nosso, aceito com m�dia-alta prob.
				if(maiorCartaENossa(s) && 
					qualidadeMaiorMesa(s)==TRES &&
					mandaBala(60))
						return true;
				//se na mesa j� estiver um 2 nosso, aceito com m�dia-baixa prob.
				if(maiorCartaENossa(s) && 
					qualidadeMaiorMesa(s)==DOIS &&
					mandaBala(40))
						return true;
				//checar se ainda tenho cartas na m�o...
				if(minhaVez(s)>vezTrucador(s)) {
					//se eu tiver uma manilha, aceito com alta prob.
					if(qualidadeCarta(s.cartasJogador[C[0]],s)>TRES &&
						mandaBala(80))
							return true;
					//se eu tiver um tr�s, aceito com m�dia-alta prob.
					if(qualidadeCarta(s.cartasJogador[C[0]],s)==TRES &&
						mandaBala(60))
							return true;
					//se eu tiver um dois, aceito com m�dia-baixa prob.
					if(qualidadeCarta(s.cartasJogador[C[0]],s)==DOIS &&
						mandaBala(40))
							return true;
				}
			} else {
				//se a primeira N�O for nossa (pode tbm estar amarrado):
				//se na mesa j� estiver uma manilha nossa, aceito com alta prob.
				if(maiorCartaENossa(s) && 
					qualidadeMaiorMesa(s)>TRES &&
					mandaBala(80))
						return true;
				//se na mesa j� estiver um 3 nosso, aceito com m�dia-alta prob.
				if(maiorCartaENossa(s) && 
					qualidadeMaiorMesa(s)==TRES &&
					mandaBala(60))
						return true;
				//se na mesa j� estiver um 2 nosso, aceito com m�dia-baixa prob.
				if(maiorCartaENossa(s) && 
					qualidadeMaiorMesa(s)==DOIS &&
					mandaBala(40))
						return true;
				//se na mesa estiver um lixo nosso, aceito com baixa prob. - devem estar blefando...
				if(maiorCartaENossa(s) && 
					qualidadeMaiorMesa(s)<=AS &&
					mandaBala(20))
						return true;
				//checar se ainda tenho cartas na m�o...
				if(minhaVez(s)>vezTrucador(s)) {
					//se eu tiver uma manilha, aceito com alta prob.
					if(qualidadeCarta(s.cartasJogador[C[0]],s)>TRES &&
						mandaBala(80))
							return true;
					//se eu tiver um tr�s, aceito com m�dia-alta prob.
					if(qualidadeCarta(s.cartasJogador[C[0]],s)==TRES &&
						mandaBala(60))
							return true;
					//se eu tiver um dois, aceito com m�dia-baixa prob.
					if(qualidadeCarta(s.cartasJogador[C[0]],s)==DOIS &&
						mandaBala(40))
							return true;
				}
			}
			return false;
			
		}
		return false;
	}

	/**
	 * Retorna se eu aceito jogar ou n�o esta m�o de 11.
	 */
	public boolean aceitaMao11(Carta[] cartasParceiro, SituacaoJogo s) 
	{
		// N�o vamos pensar muito...
		
		int q3=0,q2=0,qManilhas=0;

		for(int i=0;i<=2;i++)
		{
			//quantidade de manilhas
			if(qualidadeCarta(s.cartasJogador[i],s) > TRES)
				qManilhas++;
			//quantidade de 3
			if(qualidadeCarta(s.cartasJogador[i],s) == TRES)
				q3++;
			//quantidade de 2
			if(qualidadeCarta(s.cartasJogador[i],s) == DOIS)
				q2++;
			//quantidade de manilhas do parceiro
			if(qualidadeCarta(cartasParceiro[i],s) > TRES)
				qManilhas++;
			//quantidade de 3 do parceiro
			if(qualidadeCarta(cartasParceiro[i],s) == TRES)
				q3++;
			//quantidade de 2 do parceiro
			if(qualidadeCarta(cartasParceiro[i],s) == DOIS)
				q2++;
		}
	
		//vamos analisar!
		if(qManilhas>=2 || q3>=3 || (q3>=2 && q2>=1) || (qManilhas>=1 && q3>=1))
			return true;		
		return false;
	}

	public void inicioPartida() {}
	public void inicioMao() {}
	public void pediuAumentoAposta(int posJogador, int valor) {}
	public void aceitouAumentoAposta(int posJogador, int valor) {}
	public void recusouAumentoAposta(int posJogador) {}
}
