package br.inf.chester.minitruco.cliente;

/*
 * Copyright � 2006 Leonardo Sellani
 * 
 * Este programa � um software livre; voc� pode redistribui-lo e/ou 
 * modifica-lo dentro dos termos da Licen�a P�blica Geral GNU como 
 * publicada pela Funda��o do Software Livre (FSF); na vers�o 2 da 
 * Licen�a, ou (na sua opni�o) qualquer vers�o.
 *
 * Este programa � distribuido na esperan�a que possa ser util, 
 * mas SEM NENHUMA GARANTIA; sem uma garantia implicita de ADEQUA��O
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
 * @author Leonardo Sellani
 * 
 */
public class EstrategiaSellani implements Estrategia 
{
	private static Random rand = new Random();
	private static int CARTA_RUIM = 0;
	private static int CARTA_MEDIA = 1;
	private static int CARTA_BOA = 2;
	private static int CARTA_EXCELENTE = 3;
	int[] C = new int[3];
	
	public String getNomeEstrategia() 
	{
		return "Leonardo Sellani";
	}

	public String getInfoEstrategia() 
	{
		return "vers�o 1.0";
	}
	
	/**
	 * Retorna verdadeiro ou falso a partir de um n�mero rand�mico e uma porcentaem. 
	 * Esta fun��o � utilizada quando o jogador fica na d�vida, assim o jogador fica mais din�mico.
	 */
	private boolean vouOuNaoVou(int Porcentagem)
	{
		return (Math.abs(rand.nextInt())%100<Porcentagem)?true:false;		
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
/*		if(s.cartasJogador.length==0) System.out.println(" Jogador sem cartas");
		if(s.cartasJogador.length>=1) System.out.println(" C[0]" + C[0] + ": " + s.cartasJogador[C[0]].getLetra() + (s.cartasJogador[C[0]].getNaipe()==0?" COPAS":s.cartasJogador[C[0]].getNaipe()==1?" OUROS":s.cartasJogador[C[0]].getNaipe()==2?" ESPADAS":s.cartasJogador[C[0]].getNaipe()==3?" PAUS":"-"));
		if(s.cartasJogador.length>=2) System.out.println(" C[1]" + C[1] + ": " + s.cartasJogador[C[1]].getLetra() + (s.cartasJogador[C[1]].getNaipe()==0?" COPAS":s.cartasJogador[C[1]].getNaipe()==1?" OUROS":s.cartasJogador[C[1]].getNaipe()==2?" ESPADAS":s.cartasJogador[C[1]].getNaipe()==3?" PAUS":"-"));
		if(s.cartasJogador.length>=3) System.out.println(" C[2]" + C[2] + ": " + s.cartasJogador[C[2]].getLetra() + (s.cartasJogador[C[2]].getNaipe()==0?" COPAS":s.cartasJogador[C[2]].getNaipe()==1?" OUROS":s.cartasJogador[C[2]].getNaipe()==2?" ESPADAS":s.cartasJogador[C[2]].getNaipe()==3?" PAUS":"-"));
*/		
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
	 *  Retorna o �ndice da maior carta da mesa na rodada atual.
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
	 * Retorna se eu tenho na m�o alguma carta para matar a do advers�rio.
	 */
	private boolean matoAdversario(SituacaoJogo s,boolean consideraEmpate)
	{
		if(minhaVez(s)==0)
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
	 * Retorna o �ndice da melhor carta para tentar fazer a m�o, se n�o tiver uma carta boa retorna a menor.
	 */
	private int melhorCartaParaTentarFazer(SituacaoJogo s)
	{
		//se eu n�o puder matar a carta do advers�rio retorno a menor
		if(!matoAdversario(s,false))
			return C[2];
		//procura pela primeira carta que mata a do advers�rio
		for(int i=2;i>=0;i--)
			if(s.cartasJogador[C[i]].getValorTruco(s.manilha) > s.cartasJogadas[s.numRodadaAtual-1][maiorCartaMesa(s)].getValorTruco(s.manilha))
			{
				//ser� que esta � uma boa carta? se n�o for vou tentar outra. S� vou jogar uma manilha em cima de uma carta boa
				if( (qualidadeCarta(s.cartasJogador[C[i]],s)==CARTA_BOA) ||
					(qualidadeCarta(s.cartasJogador[C[i]],s)==CARTA_EXCELENTE && qualidadeMaiorMesa(s) >= CARTA_BOA))
					return C[i];
			}
		//se n�o encontrou nenhuma, n�o vou gastar uma manilha na primeira
		if(qualidadeMinhaMaior(s)==CARTA_EXCELENTE)
			return C[1]; //vou jogar minha carta do meio
		return C[0]; //vou jogar minha maior
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
	 * Retorna a qualifica��o de uma carta.
	 * Mudando as faixas das cartas para cada qualidade, podemos variar a agrecividade, ou maluquice do jogador 
	 */
	private int qualidadeCarta(Carta carta,SituacaoJogo s)
	{
		if(s.baralhoSujo)
		{
			//cartas 4,5,6,7
			if(carta.getValorTruco(s.manilha)<=4)
				return CARTA_RUIM;
			//cartas Q,J,K
			if(carta.getValorTruco(s.manilha)<=7)
				return CARTA_MEDIA;
			//cartas A,2,3 
			if(carta.getValorTruco(s.manilha)<=10)
				return CARTA_BOA;
			//manilhas
			return CARTA_EXCELENTE;
		}
		else
		{
			//cartas Q,J
			if(carta.getValorTruco(s.manilha)<=6)
				return CARTA_RUIM;
			//cartas K,A
			if(carta.getValorTruco(s.manilha)<=8)
				return CARTA_MEDIA;
			//cartas 2,3 
			if(carta.getValorTruco(s.manilha)<=10)
				return CARTA_BOA;
			//manilhas
			return CARTA_EXCELENTE;
		}
	}
	
	/**
	 * Retorna a qualifica��o da maior carta da mesa na rodada atual.
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
	 * Retorna se compensa aumentar a aposta ou n�o. Se eu estiver com 9, n�o vou pedir 6 n� cabe��o! 
	 */
	private boolean valeAPenaAumentar(SituacaoJogo s,boolean consideraSorte)
	{
		//o valor da aposta vai ser maior do que eu preciso pra fechar o jogo?
		if(12-s.pontosEquipe[eu(s)%2] < s.valorProximaAposta)
			return false;
		if(s.valorProximaAposta==6 && vouOuNaoVou(30) && consideraSorte)
			return false;
		if(s.valorProximaAposta==9 && vouOuNaoVou(50) && consideraSorte)
			return false;			
		return true;		
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

	/**
	 * Efetua uma jogada. S�rio?!? Se t� brincando!
	 */
	public int joga(SituacaoJogo s) 
	{
//		System.out.println("\njoga() posi��o:" + eu(s) + " vez:" + minhaVez(s) + (minhaVez(s)==0?" m�o":minhaVez(s)==3?" p�":"") + (s.posJogador==3?" Parceiro":" Advers�rio"));
		classificaCartas(s);

		switch (s.numRodadaAtual) 
		{
		//primeira rodada
		case 1:
			switch(minhaVez(s))
			{
			case 0:
				return C[2]; //vuo jogar a menor carta, ainda n�o tem muito o que fazer, n�?
			case 1:
				if(qualidadeMaiorMesa(s)>=CARTA_BOA)
					return menorCartaParaMatar(s); //o cara j� saiu for�ando, vou tentar fazer
				else
					return C[2];//vou jogar uma carta baixa tamb�m
			case 2:
				//ser� que meu parceiro j� come�ou bem?
				if(maiorCartaEDoParceiro(s) && qualidadeMaiorMesa(s) >= CARTA_BOA)
					return C[2]; //jogo minha pior carta, deixa a do parceiro
				return melhorCartaParaTentarFazer(s); //vou fazer minha parte, quem sabe passa!
			case 3:
				//ser� que meu parceiro j� fez?
				if(maiorCartaEDoParceiro(s))
					return C[2]; //meu parceiro � duca, vou jogar minha menor carta
				return menorCartaParaMatar(s); //tudo bem parceiro, deixa comigo, vou tentar fazer
			}
		break;

		//segunda rodada
		case 2:
			//se melo a primeira mando a maior
			if(s.resultadoRodada[0]==3)
			{
				//eh beleza!!!
				if((partidaGanha(s) && valeAPenaAumentar(s,false)) ||
					(matoAdversario(s,false) && qualidadeMinhaMaior(s)==CARTA_EXCELENTE  && valeAPenaAumentar(s,false)) )
					return -1; //TRUCO!!!					
				return C[0];
			}			
			switch(minhaVez(s))
			{
			//j� fizemos a primeira
			case 0:
			case 2:
				//se estou com o jogo ganho, n�o tenho nem o que pensar, vou deixar passar, hehehe!!!
				if(partidaGanha(s))
					return C[2]+10; //vou esconder a outra, faz a segunda ai pat�o!!!
				//vou dar uma olhada na que meu parceiro jogou
				if(minhaVez(s)==2 && maiorCartaEDoParceiro(s) && qualidadeCarta(s.cartasJogador[C[2]],s)<=qualidadeMaiorMesa(s))
					return C[2]+10; //vou esconder e deixar passar a do parceiro
				//ser� que eu tenho uma manilha
				if(qualidadeMinhaMaior(s)==CARTA_EXCELENTE)
				{
					//vou dar uma olhada na minha outra carta, se for boa vou trucar, num quero nem saber...
					if(qualidadeCarta(s.cartasJogador[C[1]],s)>=CARTA_BOA)
					{
						if(s.valorProximaAposta>0 && valeAPenaAumentar(s,true) && vouOuNaoVou(80))
							return -1; //TRUCO!!!
						return C[2]; //o cara aceitou, vou passar a menorzinha s� pra arrancar
					}
					return C[2]+10; //minha outra carta n�o � das melhores, vou esconder a pior e ir com a manilha pra �ltima
				}
				else
				//t� sem manilha, mas vamos ver o que da pra fazer...
				if(qualidadeMinhaMaior(s)==CARTA_BOA)
				{
					//vou dar uma olhada na minha outra carta, se tamb�m for boa vou trucar 
					if(qualidadeCarta(s.cartasJogador[C[1]],s)>=CARTA_BOA)
					{
						if(s.valorProximaAposta>0 && valeAPenaAumentar(s,true) && vouOuNaoVou(50))
							return -1; //TRUCO!!!
						return C[2]; //o cara aceito, e agora?!?!
					}
					//vou jogar a menor
					if(minhaVez(s)==0)
						return C[2];
					else
						return C[2]+(maiorCartaEDoParceiro(s)?10:!matoAdversario(s,true)?10:0);
				}
				//� m�o ruim s�, n�o tem muito o que fazer, pelo menos j� garantimos a primeira
				if(minhaVez(s)==0)
					return C[2]+(qualidadeCarta(s.cartasJogador[C[2]],s)==CARTA_RUIM?10:0);
				else
					return C[2]+(maiorCartaEDoParceiro(s)?10:!matoAdversario(s,true)?10:0);
			case 1:
				//n�o vou gastar uma manilha em cima de porcaria, vou deixar pro parceiro e jogar minha pior carta
				if(qualidadeCarta(s.cartasJogador[menorCartaParaMatar(s)],s)==CARTA_EXCELENTE && qualidadeMaiorMesa(s)<=CARTA_MEDIA)
					return C[2]+(matoAdversario(s,true)?0:10);
				//vou tentar fazer essa
				return menorCartaParaMatar(s)+(matoAdversario(s,true)?0:10);
			case 3:
				//vamos ver se meu parceiro j� garantiu essa ou se eu estou muito ruim
				if(maiorCartaEDoParceiro(s) || !matoAdversario(s,false))
					return C[2]+10; //vou at� esconder
				return menorCartaParaMatar(s)+(matoAdversario(s,true)?0:10); //t� no p�, vou tentar fazer essa
			}
		break;

		//terceira rodada
		case 3:
			//se melo a segunda mando a maior
			if(s.resultadoRodada[1]==3)
			{
				//eh beleza!!!
				if((partidaGanha(s) && valeAPenaAumentar(s,false)) ||
					(matoAdversario(s,false) && qualidadeMinhaMaior(s)==CARTA_EXCELENTE  && valeAPenaAumentar(s,false)) )
					return -1; //TRUCO!!!
				return C[0];
			}
			//se j� estou garantido vou trucar, n�o vai nem ter gra�a, hehehe!!!
			if(partidaGanha(s))
			{					
				if(s.valorProximaAposta>0 && valeAPenaAumentar(s,false))
					return -1; //TRUCO, aceita ai marreco!!!
				return C[0]; //leva na testa!!!
			}
			switch(minhaVez(s))
			{
			case 0:
				//se eu estiver com uma carta ruim, vou jogar fechada pra fazer uma m�dia e deixar para o parceiro 
				if(qualidadeMinhaMaior(s)==CARTA_RUIM)
					return C[0]+10;
				//minha carta n�o � l� estas coisas, vou jogar e ver o que da
				if(qualidadeMinhaMaior(s)==CARTA_MEDIA)
					return C[0];
				//opa, minha carta at� que da pra alguma coisa 
				if( s.valorProximaAposta>0 && valeAPenaAumentar(s,true) && 
					((vouOuNaoVou(40) && qualidadeMinhaMaior(s) == CARTA_BOA) ||
					 (vouOuNaoVou(90) && qualidadeMinhaMaior(s) == CARTA_EXCELENTE)))
					return -1; //TRUCO!!!
				return C[0];
			case 1:
				//eu t� mal, espero que o parceiro fa�a alguma coisa
				if(!matoAdversario(s,false))
					return C[0]+10;
				//minha carta at� que � boa, vou arriscar
				if( s.valorProximaAposta>0 && valeAPenaAumentar(s,true) && matoAdversario(s,true) &&
					((vouOuNaoVou(40) && qualidadeMinhaMaior(s) == CARTA_BOA) ||
					 (vouOuNaoVou(90) && qualidadeMinhaMaior(s) == CARTA_EXCELENTE)))
					return -1; //TRUCO!!!
				return C[0];
			case 2:
				//eu n�o melhoro a do parceiro, mas acho que vou trucar com a carta dele
				if(maiorCartaEDoParceiro(s) && !matoAdversario(s,false) && qualidadeMaiorMesa(s) >= CARTA_BOA && vouOuNaoVou(40) && s.valorProximaAposta>0 && valeAPenaAumentar(s,true))
					return -1; //TRUCO!!!
				if(matoAdversario(s,false) && s.valorProximaAposta>0 && valeAPenaAumentar(s,true) &&
				   ((vouOuNaoVou(30) && qualidadeMinhaMaior(s) == CARTA_BOA) ||
					(vouOuNaoVou(90) && qualidadeMinhaMaior(s) == CARTA_EXCELENTE)))
					return -1; //TRUCO!!!
				if(matoAdversario(s,true) && primeiraENossa(s) && s.valorProximaAposta>0 && vouOuNaoVou(30) && qualidadeMinhaMaior(s)>=CARTA_BOA)
					return -1; //TRUCO!!!
				if(matoAdversario(s,false) && s.valorProximaAposta>0 && vouOuNaoVou(30) && qualidadeMinhaMaior(s)>=CARTA_BOA)
					return -1; //TRUCO!!!
				return C[0]+(matoAdversario(s,true)?0:10);
			case 3:
				//t� no p�, vamos ver se eu ganho
				if(matoAdversario(s,false) || maiorCartaEDoParceiro(s) || (primeiraENossa(s) && matoAdversario(s,true)))
				{
					if(s.valorProximaAposta>0 && valeAPenaAumentar(s,true))
						return -1; //TRUCO!!! s� troxa pra aceitar
					return C[0]+(matoAdversario(s,true)?0:10); //aeheh, o man� aceitou!!!
				}				
				//deixar eles ganharem com uma carta lixo? nem pensar, vou trucar essa merda
				if( s.valorProximaAposta>0 && valeAPenaAumentar(s,true) && 
					((vouOuNaoVou(90) && qualidadeMaiorMesa(s) == CARTA_RUIM) ||
					 (vouOuNaoVou(20) && qualidadeMaiorMesa(s) == CARTA_MEDIA) ||
					 (vouOuNaoVou(5)  && qualidadeMaiorMesa(s) == CARTA_BOA)))
					return -1; //TRUCO!!! foge ai.
				return C[0]; //n�o acredito, tomei uma piaba!!!
			}
		break;
		}
		return C[2]; //se alguma combina��o ficou de fora, joga a menor
	}

	/**
	 * Retorna se eu aceito o aumento da aposta dos advers�rios ou n�o. 
	 */
	public boolean aceitaTruco(SituacaoJogo s) 
	{
//		System.out.println("\naceitaTruco() vez:" + minhaVez(s) + " posi��o:" + eu(s) + (eu(s)==0?" m�o":eu(s)==3?" p�":"") + (s.posJogador==3?" Parceiro":" Advers�rio"));
		classificaCartas(s);

		//se estou com o casal maior n�o vai nem ter gra�a...
		if(partidaGanha(s))
			return true;
		
		switch (s.numRodadaAtual) 
		{
		//primeira rodada
		case 1:
			//se eu tiver pelo menos uma manilha e uma carta boa eu aceito
			if(qualidadeMinhaMaior(s)==CARTA_EXCELENTE && qualidadeCarta(s.cartasJogador[C[1]],s)>=CARTA_BOA)
				return true;
			//eu:SIM  parceiro:SIM - considerando quem j� jogou
			if((minhaVez(s)==0 && vezTrucador(s)==3) ||	(minhaVez(s)==2 && vezTrucador(s)==3))
			{
				//o p� trucando na primeira, esse cara t� de sacanagem
				if(maiorCartaENossa(s) && qualidadeMaiorMesa(s) == CARTA_EXCELENTE)
					return true; //esse cara t� blefando
				if(maiorCartaENossa(s) && qualidadeMaiorMesa(s) >= CARTA_BOA)
				{
					//vou dar uma olhada na minha m�o
					if(qualidadeMinhaMaior(s)>=CARTA_BOA)
						return true;
					//temos uma carta boa na mesa, ent�o vamos nessa!
					if(vouOuNaoVou(20))
						return true;
				}
			}
			else
			//eu:N�O  parceiro:N�O - considerando quem j� jogou
			if((minhaVez(s)==1 && vezTrucador(s)==0) || (minhaVez(s)==3 && vezTrucador(s)==0))
			{
				//nem come�ou e o m�o j� trucou?!? vamos pensar
				if(qualidadeMinhaMaior(s)>=CARTA_BOA && qualidadeCarta(s.cartasJogador[C[1]],s)>=CARTA_BOA)
					return true;
			}
			else
			//eu:SIM  parceiro:N�O - considerando quem j� jogou
			if((minhaVez(s)==0 && vezTrucador(s)==1) || (minhaVez(s)==1 && vezTrucador(s)==2))
			{
				if(minhaVez(s)==1 && maiorCartaENossa(s) && qualidadeMaiorMesa(s) == CARTA_EXCELENTE && vouOuNaoVou(50))
					return true;
				if(qualidadeMinhaMaior(s)>=CARTA_BOA && qualidadeCarta(s.cartasJogador[C[1]],s)>=CARTA_BOA && vouOuNaoVou(50))
					return true;
			}
			else
			//eu:N�O  parceiro:SIM - considerando quem j� jogou
			if((minhaVez(s)==2 && vezTrucador(s)==1) || (minhaVez(s)==3 && vezTrucador(s)==2))
			{
				if(maiorCartaENossa(s) && qualidadeMaiorMesa(s) >= CARTA_BOA && qualidadeMinhaMaior(s)>=CARTA_BOA && vouOuNaoVou(40))
					return true;
			}
		//segunda rodada
		case 2:
			//se eu tiver pelo menos uma manilha e uma carta boa eu aceito
			if(primeiraENossa(s) && 
			   (qualidadeMinhaMaior(s)==CARTA_EXCELENTE ||
			   (qualidadeMinhaMaior(s)==CARTA_BOA && vouOuNaoVou(50))) )
				return true;
			//eu:SIM  parceiro:SIM - considerando quem j� jogou
			if((minhaVez(s)==0 && vezTrucador(s)==3) ||	(minhaVez(s)==2 && vezTrucador(s)==3))
			{
				//o p� ainda tem que fazer duas, e eu ainda tenho uma boa carta na mesa?!? Ser� que t� blefando?
				if(maiorCartaENossa(s) && qualidadeMaiorMesa(s) >= CARTA_BOA && vouOuNaoVou(70))
					return true;
				//o p� trucando e tem que fazer duas, ele pode estar blefando
				if(primeiraENossa(s) &&  vouOuNaoVou(40))
					return true;
			}
			else
			//eu:N�O  parceiro:N�O - considerando quem j� jogou
			if((minhaVez(s)==1 && vezTrucador(s)==0) || (minhaVez(s)==3 && vezTrucador(s)==0))
			{
				if(qualidadeMinhaMaior(s)>=CARTA_BOA && qualidadeCarta(s.cartasJogador[C[1]],s)>=CARTA_BOA && vouOuNaoVou(50))
					return true;
			}
			else
			//eu:SIM  parceiro:N�O - considerando quem j� jogou
			if((minhaVez(s)==0 && vezTrucador(s)==1) || (minhaVez(s)==1 && vezTrucador(s)==2))
			{
				//eu j� fiz a primeira, vou dar uma olhada na minha m�o
				if(minhaVez(s)==0 && (qualidadeMinhaMaior(s)>=CARTA_EXCELENTE || (qualidadeMinhaMaior(s)>=CARTA_BOA && vouOuNaoVou(40))))
					return true;
			}
			else
			//eu:N�O  parceiro:SIM - considerando quem j� jogou
			if((minhaVez(s)==2 && vezTrucador(s)==1) || (minhaVez(s)==3 && vezTrucador(s)==2))
			{
				//j� fiz a primeira e t� com uma boa na m�o, vamo embora!
				if(minhaVez(s)==2 && (qualidadeMinhaMaior(s)>=CARTA_EXCELENTE || (qualidadeMinhaMaior(s)>=CARTA_BOA && vouOuNaoVou(50))))
					return true;
			}
		//terceira rodada
		case 3:
			//eu:SIM  parceiro:SIM - considerando quem j� jogou
			if((minhaVez(s)==0 && vezTrucador(s)==3) ||	(minhaVez(s)==2 && vezTrucador(s)==3))
			{
				//o p� t� trucando, vamos ver!
				if(maiorCartaENossa(s) &&
					((qualidadeMaiorMesa(s)==CARTA_EXCELENTE) || 
					 (qualidadeMaiorMesa(s)==CARTA_BOA && vouOuNaoVou(70))) )
					return true;
			}
			else
			//eu:N�O  parceiro:N�O - considerando quem j� jogou
			if((minhaVez(s)==1 && vezTrucador(s)==0) || (minhaVez(s)==3 && vezTrucador(s)==0))
			{
				//se eu tiver pelo menos uma manilha e uma carta boa eu aceito
				if(qualidadeMinhaMaior(s)==CARTA_EXCELENTE ||
					(qualidadeMinhaMaior(s)==CARTA_BOA && vouOuNaoVou(40)) )
					return true;
			}
			else
			//eu:SIM  parceiro:N�O - considerando quem j� jogou
			if((minhaVez(s)==0 && vezTrucador(s)==1) || (minhaVez(s)==1 && vezTrucador(s)==2))
			{
				if(maiorCartaENossa(s) &&
					((qualidadeMaiorMesa(s)==CARTA_EXCELENTE) || 
					 (qualidadeMaiorMesa(s)==CARTA_BOA && vouOuNaoVou(50))) )
					return true;
			}
			else
			//eu:N�O  parceiro:SIM - considerando quem j� jogou
			if((minhaVez(s)==2 && vezTrucador(s)==1) || (minhaVez(s)==3 && vezTrucador(s)==2))
			{
				//se eu tiver pelo menos uma manilha e uma carta boa eu aceito
				if(qualidadeMinhaMaior(s)==CARTA_EXCELENTE ||
				   (qualidadeMinhaMaior(s)==CARTA_BOA && vouOuNaoVou(70)) )
					return true;
				if(maiorCartaENossa(s) &&
				   ((qualidadeMaiorMesa(s)==CARTA_EXCELENTE) || 
					(qualidadeMaiorMesa(s)==CARTA_BOA && vouOuNaoVou(60))) )
					return true;
			}
		}
		return false; //deixa quieto
	}

	/**
	 * Retorna se eu aceito jogar ou n�o esta m�o de 11.
	 */
	public boolean aceitaMao11(Carta[] cartasParceiro, SituacaoJogo s) 
	{
		int qBoa=0,qExcelente=0;

		for(int i=0;i<=2;i++)
		{
			//quantidade de manilhas
			if(s.cartasJogador[i].getValorTruco(s.manilha) >= 11)
				qExcelente++;
			else
			//quantidade de 2 e 3
			if(s.cartasJogador[i].getValorTruco(s.manilha) >= 9)
				qBoa++;

			//quantidade de manilhas do parceiro
			if(cartasParceiro[i].getValorTruco(s.manilha) >= 11)
				qExcelente++;
			else
			//quantidade de 2 e 3 do parceiro
			if(cartasParceiro[i].getValorTruco(s.manilha) >= 9)
				qBoa++;
		}
		//System.out.println("aceitaMao11()\n  Qtd Boa:" + qBoa + "  Qtd Excelente:" + qExcelente);
		//vamos analisar!
		if(qExcelente>=2 || qBoa>=3 || (qExcelente>=1 && qBoa>=1))
			return true;		
		return false;
	}

	public void inicioPartida() {}
	public void inicioMao() {}
	public void pediuAumentoAposta(int posJogador, int valor) {}
	public void aceitouAumentoAposta(int posJogador, int valor) {}
	public void recusouAumentoAposta(int posJogador) {}
}
